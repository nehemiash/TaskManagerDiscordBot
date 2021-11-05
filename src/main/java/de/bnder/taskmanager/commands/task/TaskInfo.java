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

    public static void taskInfo(Member member, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        final String taskID = args[1];
        final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, textChannel.getGuild());
        if (task.exists()) {
            final EmbedBuilder builder = new EmbedBuilder().setColor(Color.cyan).setTimestamp(Calendar.getInstance().toInstant());
            if (task.getStatus() == TaskStatus.TODO) {
                builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode), true);
            } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_wird_bearbeitet", langCode), true);
            } else if (task.getStatus() == TaskStatus.DONE) {
                builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_erledigt", langCode), true);
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
            MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                {
                    add(taskID);
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
