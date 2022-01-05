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
import java.util.Locale;

public class EditTask {

    public static void editTask(String commandMessage, Member member, TextChannel textChannel, String taskID, int newTextStartIndex, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);

        if (taskID == null) {
            MessageSender.send(embedTitle, Localizations.getString("context_awareness_no_task_id_found", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        if (!PermissionSystem.hasPermission(member, TaskPermission.EDIT_TASK)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_server_owner_have_admin_or_custom_permission", langCode, new ArrayList<>() {{
                add(TaskPermission.EDIT_TASK.name());
                add(member.getAsMention());
            }}), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        final String newTask = CreateTask.getTaskFromArgs(3, commandMessage, false);
        final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, textChannel.getGuild());
        if (task.exists()) {
            task.setText(newTask);
            MessageSender.send(embedTitle + " - " + taskID, Localizations.getString("task_edited", langCode, new ArrayList<String>() {
                {
                    add(taskID);
                }
            }), textChannel, Color.green, langCode, slashCommandEvent);
        } else {
            MessageSender.send(embedTitle, Localizations.getString("no_task_by_id", langCode, new ArrayList<String>() {
                {
                    add(taskID);
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }
}
