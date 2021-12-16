package de.bnder.taskmanager.commands.task;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.UserSettings;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.Locale;

public class ListTasksFromOthers {

    private static final Logger logger = LogManager.getLogger(ListTasksFromOthers.class);

    public static void listTasks(Member member, List<Member> mentionedMembers, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);

        if (mentionedMembers != null && mentionedMembers.size() > 0) {
            final Member mentionedMember = mentionedMembers.get(0);
            SelfTaskList.selfTaskList(mentionedMember, textChannel, slashCommandEvent);
        } else {
            final String groupName = args[1];
            try {

                final QuerySnapshot getGroupDoc = Main.firestore.collection("server").document(member.getGuild().getId()).collection("groups").whereEqualTo("name", groupName).get().get();
                if (getGroupDoc.size() > 0) {
                    ArrayList<Map<String, Object>> todoTasks = new ArrayList<>();
                    ArrayList<Map<String, Object>> inProgressTasks = new ArrayList<>();
                    ArrayList<Map<String, Object>> doneTasks = new ArrayList<>();

                    String boardID = "default";
                    String boardName = "default";
                    //Get active board id
                    final DocumentSnapshot getServerMemberDoc = Main.firestore.collection("server").document(member.getGuild().getId()).collection("server-member").document(member.getId()).get().get();
                    if (getServerMemberDoc.exists()) {
                        if (getServerMemberDoc.getData().containsKey("active_board_id")) {
                            boardID = getServerMemberDoc.getString("active_board_id");
                        }
                    }
                    final DocumentSnapshot groupDoc = getGroupDoc.getDocuments().get(0);
                    for (DocumentSnapshot groupTaskDoc : groupDoc.getReference().collection("group-tasks").whereEqualTo("board_id", boardID).orderBy("position", Query.Direction.ASCENDING).get().get().getDocuments()) {
                        String text = groupTaskDoc.getString("text");
                        long status = (long) groupTaskDoc.get("status");
                        String deadline = groupTaskDoc.getString("deadline");
                        String id = groupTaskDoc.getId();
                        HashMap<String, Object> data = new HashMap<>() {{
                            put("text", text);
                            put("deadline", deadline);
                            put("status", status);
                            put("task_id", id);
                            put("type", "group");
                            put("group_id", groupDoc.getId());
                            put("group_name", groupDoc.getString("name"));
                        }};
                        if (status == 0) {
                            todoTasks.add(data);
                        } else if (status == 1) {
                            inProgressTasks.add(data);
                        } else if (status == 2) {
                            doneTasks.add(data);
                        }
                    }

                    if (todoTasks.size() > 0 || inProgressTasks.size() > 0 || doneTasks.size() > 0) {
                        final StringBuilder todoStringBuilder = new StringBuilder();

                        for (Map<String, Object> taskData : todoTasks) {
                            final String taskID = taskData.get("task_id").toString();
                            final String task = taskData.get("text").toString();
                            String deadline = "";
                            if (taskData.containsKey("deadline") && taskData.get("deadline") != null) {
                                deadline = taskData.get("deadline").toString();
                            }
                            String dLine = "";
                            if (deadline.length() > 0) {
                                dLine = deadline + " |";
                            }
                            todoStringBuilder.append("- ").append(task).append(" (").append(Localizations.getString("task_status_to_do", langCode)).append(" | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                        }
                        //TASKS NOT STARTED
                        if (todoStringBuilder.length() > 0) {
                            StringBuilder finalBuilder2 = todoStringBuilder;
                            MessageSender.send(embedTitle, Localizations.getString("all_tasks_by_group", langCode, new ArrayList<String>() {
                                {
                                    add(groupName);
                                    add(boardName);
                                    add(finalBuilder2.toString());
                                }
                            }), textChannel, Color.orange, langCode, slashCommandEvent);
                        }
                        final StringBuilder inProgressStringBuilder = new StringBuilder();
                        for (Map<String, Object> taskData : inProgressTasks) {
                            final String taskID = taskData.get("task_id").toString();
                            final String task = taskData.get("text").toString();
                            String deadline = "";
                            if (taskData.containsKey("deadline") && taskData.get("deadline") != null) {
                                deadline = taskData.get("deadline").toString();
                            }
                            String dLine = "";
                            if (deadline.length() > 0) {
                                dLine = deadline + " |";
                            }
                            inProgressStringBuilder.append("- ").append(task).append(" (").append(Localizations.getString("task_status_in_progress", langCode)).append(" | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                        }
                        //TASKS IN PROGRESS
                        if (inProgressStringBuilder.length() > 0) {
                            StringBuilder finalBuilder = inProgressStringBuilder;
                            MessageSender.send(embedTitle, Localizations.getString("all_tasks_by_group", langCode, new ArrayList<String>() {
                                {
                                    add(groupName);
                                    add(boardName);
                                    add(finalBuilder.toString());
                                }
                            }), textChannel, Color.yellow, langCode, slashCommandEvent);
                        }
                        final StringBuilder doneStringBuilder = new StringBuilder();

                        //TASKS DONE
                        if (new UserSettings(member).getShowDoneTasks()) {
                            for (Map<String, Object> taskData : doneTasks) {
                                final String taskID = taskData.get("task_id").toString();
                                final String task = taskData.get("text").toString();
                                String deadline = "";
                                if (taskData.containsKey("deadline") && taskData.get("deadline") != null) {
                                    deadline = taskData.get("deadline").toString();
                                }
                                String dLine = "";
                                if (deadline.length() > 0) {
                                    dLine = deadline + " |";
                                }

                                doneStringBuilder.append("- ").append(task).append(" (").append(Localizations.getString("task_status_done", langCode)).append(" | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                            }
                            if (doneStringBuilder.length() > 0) {
                                StringBuilder finalBuilder1 = doneStringBuilder;
                                MessageSender.send(embedTitle, Localizations.getString("all_tasks_by_group", langCode, new ArrayList<String>() {
                                    {
                                        add(groupName);
                                        add(boardName);
                                        add(finalBuilder1.toString());
                                    }
                                }), textChannel, Color.green, langCode, slashCommandEvent);
                            }
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("no_tasks", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("group_with_name_doesnt_exist", langCode, new ArrayList<>() {{
                        add(groupName);
                    }}), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e);
            }

        }
    }

}
