package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class DeleteTask {

    public static void deleteTask(Member member, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) throws IOException {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        if (PermissionSystem.hasPermission(member, TaskPermission.DELETE_TASK)) {
            final String taskID = Connection.encodeString(args[1]);
            if (!taskID.equalsIgnoreCase("done")) {
                final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, textChannel.getGuild()).delete();
                final int statusCode = task.getStatusCode();
                if (statusCode == 200) {
                    MessageSender.send(embedTitle, Localizations.getString("task_deleted", langCode, new ArrayList<>() {
                        {
                            add(taskID);
                        }
                    }), textChannel, Color.green, langCode, slashCommandEvent);
                } else if (statusCode == 404) {
                    MessageSender.send(embedTitle, Localizations.getString("no_task_by_id", langCode, new ArrayList<>() {
                        {
                            add(taskID);
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("request_unknown_error", langCode, new ArrayList<String>() {
                        {
                            add(statusCode + " " + task.getResponseMessage());
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } else {
                final org.jsoup.Connection.Response res = Main.tmbAPI("server/done-tasks/" + member.getGuild().getId(), member.getId(), org.jsoup.Connection.Method.DELETE).execute();
                if (res.statusCode() == 200) {
                    MessageSender.send(embedTitle, Localizations.getString("deleted_done_tasks", langCode), textChannel, Color.green, langCode, slashCommandEvent);
                }
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
