package de.bnder.taskmanager.commands.group;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class GroupList {

    public static void getGroupList(Member member, TextChannel textChannel, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);

        try {
            final StringBuilder builder = new StringBuilder();
            final QuerySnapshot getGroupDocs = Main.firestore.collection("server").document(textChannel.getGuild().getId()).collection("groups").get().get();

            if (getGroupDocs.size() == 0) {
                MessageSender.send(embedTitle, Localizations.getString("keine_gruppen_auf_server", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                return;
            }

            for (QueryDocumentSnapshot groupDoc : getGroupDocs.getDocuments()) {
                final String groupName = groupDoc.getString("name");
                builder.append("- ").append(groupName).append("\n");
            }
            MessageSender.send(embedTitle, builder.toString(), textChannel, Color.green, langCode, slashCommandEvent);
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
