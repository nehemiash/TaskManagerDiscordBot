package de.bnder.taskmanager.commands.group;

import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.slashcommands.UpdateGuildSlashCommands;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.Locale;

public class DeleteGroup {

    private static final Logger logger = LogManager.getLogger(DeleteGroup.class);

    public static void deleteGroup(Member member, TextChannel textChannel, String groupName, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);
        if (!PermissionSystem.hasPermission(member, GroupPermission.DELETE_GROUP)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        //args are not complete
        if (groupName == null) {
            MessageSender.send(embedTitle, Localizations.getString("context_awareness_no_group_name_found", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        try {
            final QuerySnapshot getGroupsWithName = Main.firestore.collection("server").document(textChannel.getGuild().getId()).collection("groups").whereEqualTo("name", groupName).get().get();
            if (getGroupsWithName.getDocuments().size() == 0) {
                MessageSender.send(embedTitle, Localizations.getString("group_with_name_doesnt_exist", langCode, new ArrayList<String>() {
                    {
                        add(groupName);
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
                return;
            }
            getGroupsWithName.getDocuments().get(0).getReference().delete();
            MessageSender.send(embedTitle, Localizations.getString("group_was_deleted", langCode, new ArrayList<String>() {
                {
                    add(groupName);
                }
            }), textChannel, Color.green, langCode, slashCommandEvent);
            UpdateGuildSlashCommands.update(member.getGuild());
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
            MessageSender.send(embedTitle, Localizations.getString("group_delete_unknown_error", langCode, new ArrayList<String>() {
                {
                    add(groupName);
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
