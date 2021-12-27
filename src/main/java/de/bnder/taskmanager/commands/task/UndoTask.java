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

public class UndoTask {

    public static void undoTask(Member member, TextChannel textChannel, String taskID, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode) + " - " + taskID;
        if (taskID != null) {
            final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, member.getGuild());
            if (task.exists()) {
                task.undo();
                final TaskStatus taskStatus = task.getStatus();
                if (taskStatus == TaskStatus.TODO) {
                    MessageSender.send(embedTitle, Localizations.getString("task_status_to_do", langCode), textChannel, Color.green, langCode, slashCommandEvent);
                } else if (taskStatus == TaskStatus.IN_PROGRESS) {
                    MessageSender.send(embedTitle, Localizations.getString("task_status_in_progress", langCode), textChannel, Color.green, langCode, slashCommandEvent);
                } else if (taskStatus == TaskStatus.DONE) {
                    MessageSender.send(embedTitle, Localizations.getString("task_status_done", langCode), textChannel, Color.green, langCode, slashCommandEvent);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("task_request_unknown_error", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("no_task_by_id", langCode, new ArrayList<String>() {
                    {
                        add(taskID);
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("context_awareness_no_task_id_found", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
