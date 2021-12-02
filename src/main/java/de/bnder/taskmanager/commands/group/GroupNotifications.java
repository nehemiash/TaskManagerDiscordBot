package de.bnder.taskmanager.commands.group;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.Locale;

public class GroupNotifications {

    public static void setGroupNotifications(Member member, TextChannel textChannel, String[] args, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);
        if (!PermissionSystem.hasPermission(member, GroupPermission.DEFINE_NOTIFY_CHANNEL)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        if (mentionedChannels == null || mentionedChannels.size() == 0) {
            MessageSender.send(embedTitle, Localizations.getString("notify_mention_one_channel", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        final String groupName = args[1];

        try {
            QuerySnapshot getGroups = Main.firestore.collection("server").document(member.getGuild().getId()).collection("groups").whereEqualTo("name", groupName).get().get();
            if (getGroups.size() == 0) {
                MessageSender.send(embedTitle, Localizations.getString("group_with_name_doesnt_exist", langCode, new ArrayList<String>() {
                    {
                        add(groupName);
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
                return;
            }

            final QueryDocumentSnapshot groupDoc = getGroups.getDocuments().get(0);
            groupDoc.getReference().update(new HashMap<>() {{
                put("notify_channel", mentionedChannels.get(0).getId());
            }});
            MessageSender.send(embedTitle, Localizations.getString("group_connect_channel_connected", langCode, new ArrayList<String>() {{
                add(groupName);
                add(mentionedChannels.get(0).getAsMention());
            }}), textChannel, Color.green, langCode, slashCommandEvent);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            MessageSender.send(embedTitle, Localizations.getString("request_unknown_error", langCode, new ArrayList<String>() {
                {
                    add("901");
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }
}
