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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class AddGroupMember {

    private static final Logger logger = LogManager.getLogger(AddGroupMember.class);

    public static void addGroupMember(Member member, TextChannel textChannel, String[] args, List<Member> mentionedMembers, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);

        //User doesn't have permission ADD_MEMBERS
        if (!PermissionSystem.hasPermission(member, GroupPermission.ADD_MEMBERS)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        //args are not complete
        if (args.length < 3) {
            MessageSender.send(embedTitle, Localizations.getString("group_name_needs_to_be_given", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        //No Member was mentioned in message
        if (mentionedMembers == null || mentionedMembers.size() == 0) {
            MessageSender.send(embedTitle, Localizations.getString("user_needs_to_be_mentioned", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        final String groupName = args[1 + mentionedMembers.size()];
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
                final DocumentReference groupRef = groupDoc.getReference();
                final QuerySnapshot getGroupMembers = groupRef.collection("group-member").whereEqualTo("user_id", mentionedMember.getId()).get().get();
                if (getGroupMembers.size() == 0) {
                    groupRef.collection("group-member").add(new HashMap<>() {{
                        put("user_id", mentionedMember.getId());
                    }});
                    MessageSender.send(embedTitle, Localizations.getString("user_added_to_group", langCode, new ArrayList<String>() {
                        {
                            add(mentionedMember.getUser().getName());
                            add(groupName);
                        }
                    }), textChannel, Color.green, langCode, slashCommandEvent);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("user_already_in_group", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e);
                MessageSender.send(embedTitle, Localizations.getString("user_added_to_group_unknown_error", langCode, new ArrayList<String>() {
                    {
                        add("901");
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            }
        }
    }
}
