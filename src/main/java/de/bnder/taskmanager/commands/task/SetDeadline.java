package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.utils.*;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SetDeadline {

    public static void setDeadline(Member member, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        if (PermissionSystem.hasPermission(member, TaskPermission.EDIT_TASK)) {
            final String taskID = Connection.encodeString(args[1]);
            final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, textChannel.getGuild());
            String date = args[2];
            if (args.length == 4) {
                date += " " + args[3];
            }
            if (DateUtil.convertToDate(date) != null) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                final String newDate = dateFormat.format(DateUtil.convertToDate(date));
                task.setDeadline(newDate);
                if (task.getStatusCode() == 200) {
                    MessageSender.send(embedTitle, Localizations.getString("deadline_set", langCode, new ArrayList<String>() {
                        {
                            add(taskID);
                            add(newDate);
                        }
                    }), textChannel, Color.green, langCode, slashCommandEvent);
                } else if (task.getStatusCode() == 404) {
                    MessageSender.send(embedTitle, Localizations.getString("no_task_by_id", langCode, new ArrayList<String>() {
                        {
                            add(taskID);
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("invalid_date_format", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
