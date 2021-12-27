package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.commands.Stats;
import de.bnder.taskmanager.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TaskInfo {

    public static void taskInfo(Member member, TextChannel textChannel, String taskID, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        if (taskID != null) {
            final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, textChannel.getGuild());
            if (task.exists()) {
                final EmbedBuilder builder = new EmbedBuilder().setColor(Color.cyan).setTimestamp(Calendar.getInstance().toInstant()).setTitle(embedTitle + " - " + task.getId());
                if (task.getStatus() == TaskStatus.TODO) {
                    builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("task_status_to_do_keyword", langCode), true);
                } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                    builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("task_status_in_progress_keyword", langCode), true);
                } else if (task.getStatus() == TaskStatus.DONE) {
                    builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("task_status_done_keyword", langCode), true);
                }
                if (task.getDeadline() != null) {
                    builder.addField(Localizations.getString("task_info_field_deadline", langCode), task.getDeadline(), true);
                }
                if (task.getType() == TaskType.USER) {
                    builder.addField(Localizations.getString("task_info_field_type", langCode), Localizations.getString("task_info_field_type_user", langCode), true);
                    final String userID = task.getHolder();
                    if (userID != null && textChannel.getGuild().retrieveMemberById(userID).complete() != null) {
                        builder.addField(Localizations.getString("task_info_field_assigned", langCode), textChannel.getGuild().retrieveMemberById(userID).complete().getUser().getAsTag(), true);
                    }
                } else if (task.getType() == TaskType.GROUP) {
                    builder.addField(Localizations.getString("task_info_field_type", langCode), Localizations.getString("task_info_field_type_group", langCode), true);
                    builder.addField(Localizations.getString("task_info_field_assigned", langCode), task.getHolder(), true);
                }
                builder.addField(Localizations.getString("task_info_field_task", langCode), task.getText(), false);
                Stats.handleEmbedsOnSlashCommand(textChannel, slashCommandEvent, builder);
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
