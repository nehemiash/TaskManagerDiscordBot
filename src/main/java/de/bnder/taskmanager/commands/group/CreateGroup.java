package de.bnder.taskmanager.commands.group;

import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.slashcommands.UpdateGuildSlashCommands;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.Locale;

public class CreateGroup {

    public static void createGroup(Member member, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);
        if (!PermissionSystem.hasPermission(member, GroupPermission.CREATE_GROUP)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }
        final String groupName = args[1];

        if (groupExists(groupName, textChannel.getGuild().getId())) {
            MessageSender.send(embedTitle, Localizations.getString("group_not_created_name_already_exists", langCode, new ArrayList<String>() {
                {
                    add(groupName);
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        Main.firestore.collection("server").document(textChannel.getGuild().getId()).collection("groups").add(new HashMap<>(){{
            put("name", groupName);
        }});
        MessageSender.send(embedTitle, Localizations.getString("group_created_successfully", langCode, new ArrayList<String>() {
            {
                add(groupName);
            }
        }), textChannel, Color.green, langCode, slashCommandEvent);
        UpdateGuildSlashCommands.update(member.getGuild());
    }

    public static boolean groupExists(String groupName, String guildID) {
        try {
            if (Main.firestore.collection("server").document(guildID).collection("groups").whereEqualTo("name", groupName).get().get().size() > 0) {
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }

}
