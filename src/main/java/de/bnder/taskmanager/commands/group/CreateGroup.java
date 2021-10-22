package de.bnder.taskmanager.commands.group;

import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.slashcommands.UpdateGuildSlashCommands;
import de.bnder.taskmanager.utils.Connection;
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
import java.util.Locale;

public class CreateGroup {

    public static void createGroup(Member member, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) throws IOException {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);
        if (PermissionSystem.hasPermission(member, GroupPermission.CREATE_GROUP)) {
            final String groupName = Connection.encodeString(args[1]);
            final org.jsoup.Connection.Response res = Main.tmbAPI("group/create/" + member.getGuild().getId(), member.getId(), org.jsoup.Connection.Method.POST).data("group_name", groupName).execute();
            final int statusCode = res.statusCode();
            if (statusCode == 200) {
                MessageSender.send(embedTitle, Localizations.getString("group_created_successfully", langCode, new ArrayList<String>() {
                    {
                        add(groupName);
                    }
                }), textChannel, Color.green, langCode, slashCommandEvent);
                UpdateGuildSlashCommands.update(member.getGuild());
            } else if (statusCode == 400) {
                MessageSender.send(embedTitle, Localizations.getString("group_not_created_name_already_exists", langCode, new ArrayList<String>() {
                    {
                        add(groupName);
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            } else {
                MessageSender.send(embedTitle, Localizations.getString("group_not_created_unknown_error", langCode, new ArrayList<String>() {
                    {
                        add(groupName);
                        add(String.valueOf(statusCode));
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
