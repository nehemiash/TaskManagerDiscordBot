package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.TaskStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

public class ProceedTask {

    public static void proceedTask(Member member, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        final String taskID = Connection.encodeString(args[1]);
        final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, member.getGuild());
        final int statusCode = task.proceed(member).getStatusCode();
        if (statusCode == 200) {
            final TaskStatus taskStatus = task.getStatus();
            if (taskStatus == TaskStatus.IN_PROGRESS) {
                MessageSender.send(embedTitle, Localizations.getString("task_status_in_progress", langCode), textChannel, Color.green, langCode, slashCommandEvent);
            } else if (taskStatus == TaskStatus.DONE) {
                MessageSender.send(embedTitle, Localizations.getString("task_status_done", langCode), textChannel, Color.green, langCode, slashCommandEvent);
            } else {
                MessageSender.send(embedTitle, Localizations.getString("task_request_unknown_error", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else if (statusCode == 404) {
            MessageSender.send(embedTitle, Localizations.getString("no_task_by_id", langCode, new ArrayList<String>() {
                {
                    add(taskID);
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
        } else {
            MessageSender.send(embedTitle, Localizations.getString("request_unknown_error", langCode, new ArrayList<String>() {
                {
                    add(statusCode + " " + task.getResponseMessage());
                }
            }),textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
