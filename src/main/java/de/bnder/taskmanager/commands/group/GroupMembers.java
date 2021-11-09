package de.bnder.taskmanager.commands.group;

import com.google.cloud.firestore.DocumentReference;
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
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class GroupMembers {

    public static void getGroupMembers(Member member, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);
        if (!PermissionSystem.hasPermission(member, GroupPermission.SHOW_MEMBERS)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        final String groupName = args[1];

        try {
            final QuerySnapshot getGroup = Main.firestore.collection("server").document(member.getGuild().getId()).collection("groups").whereEqualTo("name", groupName).get().get();
            if (getGroup.size() == 0) {
                MessageSender.send(embedTitle, Localizations.getString("group_with_name_doesnt_exist", langCode, new ArrayList<String>() {
                    {
                        add(groupName);
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
                return;
            }
            final QueryDocumentSnapshot groupDoc = getGroup.getDocuments().get(0);
            final DocumentReference groupDocRef = groupDoc.getReference();
            final QuerySnapshot getGroupMembers = groupDocRef.collection("group-member").get().get();
            if (getGroupMembers.size() == 0) {
                MessageSender.send(embedTitle, Localizations.getString("group_no_members", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                return;
            }
            final StringBuilder builder = new StringBuilder();
            for (QueryDocumentSnapshot doc : getGroupMembers.getDocuments()) {
                final String id = doc.getString("user_id");
                try {
                    final Member groupMember = member.getGuild().retrieveMemberById(id).complete();
                        builder.append("- ").append(groupMember.getUser().getAsTag()).append("\n");
                } catch (ErrorResponseException e) {
                    doc.getReference().delete();
                }
            }

            MessageSender.send(embedTitle, Localizations.getString("group_members", langCode, new ArrayList<String>() {
                {
                    add(builder.substring(0, builder.length() - 1));
                }
            }), textChannel, Color.green, langCode, slashCommandEvent);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                {
                    add("901");
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
