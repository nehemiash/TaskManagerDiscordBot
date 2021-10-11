package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;

public class EditTask {

    public static void editTask(String commandMessage, Member member, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        if (PermissionSystem.hasPermission(member, TaskPermission.EDIT_TASK)) {
            final String taskID = args[1];
            final String newTask = AddTask.getTaskFromArgs(3, commandMessage, false);
            final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, textChannel.getGuild());
            if (task.exists()) {
            task.setText(newTask);
                MessageSender.send(embedTitle, Localizations.getString("aufgabe_editiert", langCode, new ArrayList<String>() {
                    {
                        add(taskID);
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
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }
}
