package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.utils.*;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SetDeadline {

    public static void setDeadline(Member member, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
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
                if (task.exists()) {
                    task.setDeadline(newDate);
                    MessageSender.send(embedTitle, Localizations.getString("deadline_gesetzt", langCode, new ArrayList<String>() {
                        {
                            add(taskID);
                            add(newDate);
                        }
                    }), textChannel, Color.green, langCode, slashCommandEvent);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                        {
                            add(taskID);
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("ungueltiges_datum_format", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
