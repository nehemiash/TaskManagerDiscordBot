package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.main.Main;
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

public class DeleteTask {

    public static void deleteTask(Member member, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) throws IOException {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        if (PermissionSystem.hasPermission(member, TaskPermission.DELETE_TASK)) {
            final String taskID = args[1];
            if (!taskID.equalsIgnoreCase("done")) {
                final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, textChannel.getGuild());
                if (task.exists()) {
                    task.delete();
                    MessageSender.send(embedTitle, Localizations.getString("aufgabe_geloescht", langCode, new ArrayList<>() {
                        {
                            add(taskID);
                        }
                    }), textChannel, Color.green, langCode, slashCommandEvent);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<>() {
                        {
                            add(taskID);
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
