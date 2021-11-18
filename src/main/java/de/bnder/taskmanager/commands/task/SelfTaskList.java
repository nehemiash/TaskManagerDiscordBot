package de.bnder.taskmanager.commands.task;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.Settings;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class SelfTaskList {

    public static void selfTaskList(Member member, TextChannel textChannel, SlashCommandEvent slashCommandEvent) throws IOException {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        final org.jsoup.Connection.Response res = Main.tmbAPI("task/user/tasks/" + textChannel.getGuild().getId() + "/" + member.getId(), member.getId(), org.jsoup.Connection.Method.GET).execute();
        final Document document = res.parse();
        final String jsonResponse = document.body().text();
        final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
        if (res.statusCode() == 200) {
            final String boardName = jsonObject.getString("board", null);
            JsonArray array = jsonObject.get("todo").asArray();
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < array.size(); i++) {
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
                StringBuilder finalBuilder2 = builder;
                MessageSender.send(embedTitle, Localizations.getString("all_tasks_by_user", langCode, new ArrayList<String>() {
                    {
                        add(member.getAsMention());
                        add(boardName);
                        add(finalBuilder2.toString());
                    }
                }), textChannel, Color.orange, langCode, slashCommandEvent);
            }
            builder = new StringBuilder();
            array = jsonObject.get("doing").asArray();
            for (int i = 0; i < array.size(); i++) {
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
                StringBuilder finalBuilder = builder;
                MessageSender.send(embedTitle, Localizations.getString("all_tasks_by_user", langCode, new ArrayList<String>() {
                    {
                        add(member.getAsMention());
                        add(boardName);
                        add(finalBuilder.toString());
                    }
                }), textChannel, Color.yellow, langCode, slashCommandEvent);
            }
            builder = new StringBuilder();

            //TASKS DONE
            array = jsonObject.get("done").asArray();
            String showDoneTasks = Settings.getUserSettings(member).getString("show_done_tasks", "1");
            for (int i = 0; i < array.size(); i++) {
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
                StringBuilder finalBuilder1 = builder;
                MessageSender.send(embedTitle, Localizations.getString("all_tasks_by_user", langCode, new ArrayList<String>() {
                    {
                        add(member.getAsMention());
                        add(boardName);
                        add(finalBuilder1.toString());
                    }
                }), textChannel, Color.green, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("no_tasks", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
