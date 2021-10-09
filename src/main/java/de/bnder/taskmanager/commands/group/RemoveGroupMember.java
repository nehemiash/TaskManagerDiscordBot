package de.bnder.taskmanager.commands.group;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Main;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RemoveGroupMember {

    public static void removeGroupMember(Member member, TextChannel textChannel, String[] args, List<Member> mentionedMembers, SlashCommandEvent slashCommandEvent) throws IOException {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);
        if (!PermissionSystem.hasPermission(member, GroupPermission.REMOVE_MEMBERS)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }
        if (args.length < 3) {
            MessageSender.send(embedTitle, Localizations.getString("group_name_needs_to_be_given", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        if (mentionedMembers == null || mentionedMembers.size() == 0) {
            MessageSender.send(embedTitle, Localizations.getString("user_needs_to_be_mentioned", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }
        final String groupName = Connection.encodeString(args[1 + mentionedMembers.size()]);
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
                    MessageSender.send(embedTitle, Localizations.getString("nutzer_ist_in_keiner_gruppe", langCode, new ArrayList<String>() {
                        {
                            add(mentionedMember.getUser().getAsTag());
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                } else {
                    for (QueryDocumentSnapshot groupMemberDoc : groupMembers) {
                        groupMemberDoc.getReference().delete();
                    }
                    MessageSender.send(embedTitle, Localizations.getString("nutzer_aus_gruppe_entfernt", langCode, new ArrayList<String>() {
                        {
                            add(mentionedMember.getUser().getName());
                            add(groupName);
                        }
                    }), textChannel, Color.green, langCode, slashCommandEvent);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                MessageSender.send(embedTitle, Localizations.getString("nutzer_aus_gruppe_entfernen_unbekannter_fehler", langCode, new ArrayList<String>() {
                    {
                        add("901");
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            }
        }
    }

}
