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
        final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, member.getGuild());
        if (task.exists()) {
            task.setStatus(TaskStatus.DONE);
            MessageSender.send(embedTitle + " - " + taskID, Localizations.getString("task_status_done", langCode), textChannel, Color.green, langCode, slashCommandEvent);
        } else {
            MessageSender.send(embedTitle, Localizations.getString("no_task_by_id", langCode, new ArrayList<String>() {
                {
                    add(taskID);
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
