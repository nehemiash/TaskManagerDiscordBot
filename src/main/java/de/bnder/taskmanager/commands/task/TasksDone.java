package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.TaskStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

public class TasksDone {

    public static void tasksDone(Member member, TextChannel textChannel, String taskID, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        if (taskID != null) {
            final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, member.getGuild());
            final int statusCode = task.setStatus(TaskStatus.DONE, member).getStatusCode();
            if (statusCode == 200) {
                MessageSender.send(embedTitle, Localizations.getString("aufgabe_erledigt", langCode), textChannel, Color.green, langCode, slashCommandEvent);
            } else if (statusCode == 404) {
                MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                    {
                        add(taskID);
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            } else {
                MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                    {
                        add(statusCode + " " + task.getResponseMessage());
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("context_awareness_no_task_id_found", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
