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

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.Locale;

public class SelfTaskList {

    public static void selfTaskList(Member member, TextChannel textChannel, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);

        ArrayList<Map<String, Object>> todoTasks = new ArrayList<>();
        ArrayList<Map<String, Object>> inProgressTasks = new ArrayList<>();
        ArrayList<Map<String, Object>> doneTasks = new ArrayList<>();
        try {
            String boardID = "default";
            String boardName = "default";
            //Get active board id
            final DocumentSnapshot getServerMemberDoc = Main.firestore.collection("server").document(member.getGuild().getId()).collection("server-member").document(member.getId()).get().get();
            if (getServerMemberDoc.exists()) {
                if (getServerMemberDoc.getData().containsKey("active_board_id")) {
                    boardID = getServerMemberDoc.getString("active_board_id");
                }
            }


            //Get user assigned tasks
            final DocumentSnapshot activeBoardDoc = Main.firestore.collection("server").document(member.getGuild().getId()).collection("boards").document(boardID).get().get();
            final QuerySnapshot getUserTasks = activeBoardDoc.getReference().collection("user-tasks").whereEqualTo("user_id", member.getId()).orderBy("position", Query.Direction.ASCENDING).get().get();
            for (final DocumentSnapshot userTaskDoc : getUserTasks) {
                String text = userTaskDoc.getString("text");
                long status = (long) userTaskDoc.get("status");
                String deadline = userTaskDoc.getString("deadline");
                String id = userTaskDoc.getId();
                HashMap<String, Object> data = new HashMap<>() {{
                    put("text", text);
                    put("deadline", deadline);
                    put("status", status);
                    put("task_id", id);
                    put("type", "user");
                    put("group_id", "---");
                    put("group_name", "---");
                }};
                if (status == 0) {
                    todoTasks.add(data);
                } else if (status == 1) {
                    inProgressTasks.add(data);
                } else if (status == 2) {
                    doneTasks.add(data);
                }
            }

            //Get groups
            for (DocumentSnapshot groupDoc : Main.firestore.collection("server").document(member.getGuild().getId()).collection("groups").get().get()) {
                //Member in group
                if (groupDoc.getReference().collection("group-member").document(member.getId()).get().get().exists()) {
                    for (DocumentSnapshot groupTaskDoc : groupDoc.getReference().collection("group-tasks").whereEqualTo("board_id", boardID).orderBy("position", Query.Direction.ASCENDING).get().get()) {
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
                    todoStringBuilder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                }
                //TASKS NOT STARTED
                if (todoStringBuilder.length() > 0) {
                    StringBuilder finalBuilder2 = todoStringBuilder;
                    MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_nutzer", langCode, new ArrayList<String>() {
                        {
                            add(member.getAsMention());
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
                    inProgressStringBuilder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_wird_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                }
                //TASKS IN PROGRESS
                if (inProgressStringBuilder.length() > 0) {
                    StringBuilder finalBuilder = inProgressStringBuilder;
                    MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_nutzer", langCode, new ArrayList<String>() {
                        {
                            add(member.getAsMention());
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

                        doneStringBuilder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_erledigt", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                    }
                    if (doneStringBuilder.length() > 0) {
                        StringBuilder finalBuilder1 = doneStringBuilder;
                        MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_nutzer", langCode, new ArrayList<String>() {
                            {
                                add(member.getAsMention());
                                add(boardName);
                                add(finalBuilder1.toString());
                            }
                        }), textChannel, Color.green, langCode, slashCommandEvent);
                    }
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("keine_aufgaben", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
