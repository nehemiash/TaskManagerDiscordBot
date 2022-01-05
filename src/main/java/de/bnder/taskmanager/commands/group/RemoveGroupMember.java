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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class RemoveGroupMember {

    private static final Logger logger = LogManager.getLogger(RemoveGroupMember.class);

    public static void removeGroupMember(Member member, TextChannel textChannel, String groupName, List<Member> mentionedMembers, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);

        if (!PermissionSystem.hasPermission(member, GroupPermission.REMOVE_MEMBERS)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_server_owner_have_admin_or_custom_permission", langCode, new ArrayList<>() {{
                add(GroupPermission.REMOVE_MEMBERS.name());
                add(member.getAsMention());
            }}), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }
        if (groupName == null) {
            MessageSender.send(embedTitle, Localizations.getString("context_awareness_no_group_name_found", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        if (mentionedMembers == null || mentionedMembers.size() == 0) {
            MessageSender.send(embedTitle, Localizations.getString("user_needs_to_be_mentioned", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        for (Member mentionedMember : mentionedMembers) {
            try {
                final QuerySnapshot getGroups = Main.firestore.collection("server").document(member.getGuild().getId()).collection("groups").whereEqualTo("name", groupName).get().get();

                if (getGroups.size() == 0) {
                    MessageSender.send(embedTitle, Localizations.getString("group_with_name_doesnt_exist", langCode, new ArrayList<String>() {
                        {
                            add(groupName);
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                    return;
                }

                final QueryDocumentSnapshot groupDoc = getGroups.getDocuments().get(0);
                QuerySnapshot groupMembers = groupDoc.getReference().collection("group-member").whereEqualTo("user_id", mentionedMember.getId()).get().get();
                if (groupMembers.size() == 0) {
                    MessageSender.send(embedTitle, Localizations.getString("user_is_in_no_group", langCode, new ArrayList<String>() {
                        {
                            add(mentionedMember.getUser().getAsTag());
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                } else {
                    for (QueryDocumentSnapshot groupMemberDoc : groupMembers) {
                        groupMemberDoc.getReference().delete();
                    }
                    MessageSender.send(embedTitle, Localizations.getString("user_removed_from_group", langCode, new ArrayList<String>() {
                        {
                            add(mentionedMember.getUser().getName());
                            add(groupName);
                        }
                    }), textChannel, Color.green, langCode, slashCommandEvent);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e);
                MessageSender.send(embedTitle, Localizations.getString("user_remove_from_group_unknown_error", langCode, new ArrayList<String>() {
                    {
                        add("901");
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            }
        }
    }

}
