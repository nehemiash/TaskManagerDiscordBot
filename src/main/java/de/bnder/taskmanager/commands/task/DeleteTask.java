package de.bnder.taskmanager.commands.task;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class DeleteTask {

    private static final Logger logger = LogManager.getLogger(DeleteTask.class);

    public static void deleteTask(Member member, TextChannel textChannel, String taskID, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        if (PermissionSystem.hasPermission(member, TaskPermission.DELETE_TASK)) {
            if (!taskID.equalsIgnoreCase("done")) {
                final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, textChannel.getGuild());
                if (task.exists()) {
                    task.delete();
                    MessageSender.send(embedTitle, Localizations.getString("task_deleted", langCode, new ArrayList<>() {
                        {
                            add(taskID);
                        }
                    }), textChannel, Color.green, langCode, slashCommandEvent);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("no_task_by_id", langCode, new ArrayList<>() {
                        {
                            add(taskID);
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } else {
                try {
                    for (QueryDocumentSnapshot boardDoc : Main.firestore.collection("server").document(member.getGuild().getId()).collection("boards").get().get()) {
                        for (QueryDocumentSnapshot taskDoc : boardDoc.getReference().collection("user-tasks").whereEqualTo("status", 2).get().get()) {
                            taskDoc.getReference().delete();
                        }
                    }
                    for (QueryDocumentSnapshot groupDoc : Main.firestore.collection("server").document(member.getGuild().getId()).collection("groups").get().get()) {
                        for (QueryDocumentSnapshot taskDoc : groupDoc.getReference().collection("group-tasks").whereEqualTo("status", 2).get().get()) {
                            taskDoc.getReference().delete();
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(e);
                }
                MessageSender.send(embedTitle, Localizations.getString("deleted_done_tasks", langCode), textChannel, Color.green, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
