package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;

public class EditTask {

    public static void editTask(String commandMessage, Member member, TextChannel textChannel, String[] args) {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        if (PermissionSystem.hasPermission(member, TaskPermission.EDIT_TASK)) {
            final String taskID = Connection.encodeString(args[1]);
            final String newTask = AddTask.getTaskFromArgs(3, commandMessage, false, null);
            final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, textChannel.getGuild());
            task.setText(newTask);
            final int statusCode = task.getStatusCode();
            if (statusCode == 200) {
                MessageSender.send(embedTitle, Localizations.getString("aufgabe_editiert", langCode, new ArrayList<String>() {
                    {
                        add(taskID);
                    }
                }), textChannel, Color.green);
            } else if (statusCode == 404) {
                MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                    {
                        add(taskID);
                    }
                }), textChannel, Color.red);
            } else {
                MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                    {
                        add(statusCode + " " + task.getResponseMessage());
                    }
                }), textChannel, Color.red);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red);
        }
    }
}
