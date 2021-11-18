package de.bnder.taskmanager.commands.task;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.Settings;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListTasksFromOthers {

    public static void listTasks(Member member, List<Member> mentionedMembers, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) throws IOException {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        String jsonResponse;
        int statusCode;
        String text;
        if (mentionedMembers != null && mentionedMembers.size() > 0) {
            final Member mentionedMember = mentionedMembers.get(0);
            final org.jsoup.Connection.Response res = Main.tmbAPI("task/user/tasks/" + member.getGuild().getId() + "/" + mentionedMember.getId(), member.getId(), org.jsoup.Connection.Method.GET).execute();
            statusCode = res.statusCode();
            jsonResponse = res.parse().body().text();
        } else {
            final String groupName = args[1];
            final org.jsoup.Connection.Response res = Main.tmbAPI("task/group/tasks/" + member.getGuild().getId() + "/" + Connection.encodeString(groupName), member.getId(), org.jsoup.Connection.Method.GET).execute();
            statusCode = res.statusCode();
            jsonResponse = res.parse().body().text();
        }
        final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
        if (statusCode == 200) {
            final String boardName = jsonObject.getString("board", null);
            JsonArray array = jsonObject.get("todo").asArray();
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < array.size(); i++){
                final String taskID = array.get(i).asString();
                final String task = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("task").asString();
                String deadline = "";
                if (!jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").isNull()) {
                    deadline = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").asString();
                }
                String dLine = "";
                if (deadline.length() > 0) {
                    dLine = deadline + " |";
                }
                builder.append("- ").append(task).append(" (" + Localizations.getString("task_status_to_do_keyword", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
            }
            //TASKS NOT STARTED
            if (builder.length() > 0) {
                if (mentionedMembers.size() > 0) {
                    final Member mentionedMember = mentionedMembers.get(0);
                    text = Localizations.getString("all_tasks_by_user", langCode, new ArrayList<>() {
                        {
                            add(mentionedMember.getAsMention());
                            add(boardName);
                            add(builder.toString());
                        }
                    });
                } else {
                    final String groupName = args[1];
                    text = Localizations.getString("all_tasks_by_group", langCode, new ArrayList<>() {
                        {
                            add(groupName);
                            add(boardName);
                            add(builder.toString());
                        }
                    });
                }
                MessageSender.send(embedTitle, text, textChannel, Color.orange, langCode, slashCommandEvent);
            }
            builder.delete(0, builder.length());
            array = jsonObject.get("doing").asArray();
            for (int i = 0; i < array.size(); i++){
                final String taskID = array.get(i).asString();
                final String task = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("task").asString();
                String deadline = "";
                if (!jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").isNull()) {
                    deadline = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").asString();
                }
                String dLine = "";
                if (deadline.length() > 0) {
                    dLine = deadline + " |";
                }
                builder.append("- ").append(task).append(" (" + Localizations.getString("task_status_in_progress_keyword", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
            }
            //TASKS IN PROGRESS
            if (builder.length() > 0) {
                if (mentionedMembers.size() > 0) {
                    final Member mentionedMember = mentionedMembers.get(0);
                    text = Localizations.getString("all_tasks_by_user", langCode, new ArrayList<>() {
                        {
                            add(mentionedMember.getAsMention());
                            add(boardName);
                            add(builder.toString());
                        }
                    });
                } else {
                    final String groupName = args[1];
                    text = Localizations.getString("all_tasks_by_group", langCode, new ArrayList<>() {
                        {
                            add(groupName);
                            add(boardName);
                            add(builder.toString());
                        }
                    });
                }
                MessageSender.send(embedTitle, text, textChannel, Color.yellow, langCode, slashCommandEvent);
            }
            builder.delete(0, builder.length());
            array = jsonObject.get("done").asArray();
            final String showDoneTasks = Settings.getUserSettings(member).getString("show_done_tasks", "1");
            for (int i = 0; i < array.size(); i++){
                final String taskID = array.get(i).asString();
                final String task = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("task").asString();
                String deadline = "";
                if (!jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").isNull()) {
                    deadline = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").asString();
                }
                String dLine = "";
                if (deadline.length() > 0) {
                    dLine = deadline + " |";
                }

                if (showDoneTasks.equals("1")) {
                    builder.append("- ").append(task).append(" (" + Localizations.getString("task_status_done_keyword", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                }
            }
            if (showDoneTasks.equals("1") && builder.length() > 0) {
                if (mentionedMembers.size() > 0) {
                    final Member mentionedMember = mentionedMembers.get(0);
                    text = Localizations.getString("all_tasks_by_user", langCode, new ArrayList<String>() {
                        {
                            add(mentionedMember.getAsMention());
                            add(boardName);
                            add(builder.toString());
                        }
                    });
                } else {
                    final String groupName = args[1];
                    text = Localizations.getString("all_tasks_by_group", langCode, new ArrayList<String>() {
                        {
                            add(groupName);
                            add(boardName);
                            add(builder.toString());
                        }
                    });
                }
                MessageSender.send(embedTitle, text, textChannel, Color.green, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("no_tasks", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
