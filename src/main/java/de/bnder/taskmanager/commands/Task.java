package de.bnder.taskmanager.commands;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.*;
import de.bnder.taskmanager.utils.Settings;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Task implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        final Guild guild = event.getGuild();
        final String langCode = Localizations.getGuildLanguage(guild);
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("add")) {
                if (PermissionSystem.hasPermission(event.getMember(), TaskPermission.CREATE_TASK)) {
                    if (event.getMessage().getMentionedMembers().size() > 0) {
                        final String task = getTaskFromArgs(1, event.getMessage(), true);
                        for (Member member : event.getMessage().getMentionedMembers()) {
                            final de.bnder.taskmanager.utils.Task taskObject = new de.bnder.taskmanager.utils.Task(guild, task, null, member);
                            if (taskObject.getStatusCode() == 200) {
                                String newLanguageSuggestionAppend = taskObject.newLanguageSuggestion() != null ? Localizations.getString("task_new_language_suggestion_text", taskObject.newLanguageSuggestion(), new ArrayList<String>(){{
                                    add(String.valueOf(event.getMessage().getContentRaw().charAt(0)));
                                    add(taskObject.newLanguageSuggestion());
                                }}) : "";
                                sendTaskMessage(member, event, taskObject.getId(), langCode, task);
                                MessageSender.send(embedTitle + " - " + taskObject.getId(), Localizations.getString("aufgabe_erstellt", langCode, new ArrayList<String>() {
                                    {
                                        add(member.getUser().getName());
                                    }
                                }) + newLanguageSuggestionAppend, event.getMessage(), Color.green);
                            } else {
                                MessageSender.send(embedTitle, Localizations.getString("aufgabe_erstellt_unbekannter_fehler", langCode, new ArrayList<String>() {
                                    {
                                        add(String.valueOf(taskObject.getStatusCode()));
                                    }
                                }), event.getMessage(), Color.red);
                            }
                        }
                    } else if (Group.serverHasGroup(args[1], event.getGuild())) {
                        final String groupName = Connection.encodeString(args[1]);
                        final String task = getTaskFromArgs(3, event.getMessage(), false);
                        final de.bnder.taskmanager.utils.Task taskObject = new de.bnder.taskmanager.utils.Task(guild, task, null, groupName);
                        final int statusCode = taskObject.getStatusCode();
                        if (statusCode == 200) {
                            final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/group/members/" + event.getGuild().getId() + "/" + groupName).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.getMember().getId()).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                            final int getGroupMembersStatusCode = res.statusCode();
                            if (getGroupMembersStatusCode == 200) {
                                final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
                                int usersWhoReceivedTheTaskAmount = 0;
                                for (JsonValue value : jsonObject.get("members").asArray()) {
                                    final String id = value.asObject().getString("user_id", null);
                                    if (id != null) {
                                        final Member member = event.getGuild().retrieveMemberById(id).complete();
                                        if (member != null) {
                                            usersWhoReceivedTheTaskAmount++;
                                            sendTaskMessage(member, event, taskObject.getId(), langCode, task);
                                        }
                                    }
                                }
                                int finalUsersWhoReceivedTheTaskAmount = usersWhoReceivedTheTaskAmount;
                                MessageSender.send(embedTitle + " - " + taskObject.getId(), Localizations.getString("aufgabe_an_x_mitglieder_gesendet", langCode, new ArrayList<String>() {
                                    {
                                        add(String.valueOf(finalUsersWhoReceivedTheTaskAmount));
                                    }
                                }), event.getMessage(), Color.green);
                            } else if (getGroupMembersStatusCode == 404) {
                                MessageSender.send(embedTitle + " - " + taskObject.getId(), Localizations.getString("aufgabe_an_x_mitglieder_gesendet", langCode, new ArrayList<String>() {
                                    {
                                        add("0");
                                    }
                                }), event.getMessage(), Color.green);
                            }
                        }
                        if (statusCode == 902) {
                            MessageSender.send(embedTitle, Localizations.getString("keine_gruppen_auf_server", langCode, new ArrayList<String>() {
                                {
                                    add(groupName);
                                }
                            }), event.getMessage(), Color.red);
                        } else {
                            MessageSender.send(embedTitle, Localizations.getString("aufgabe_erstellt_unbekannter_fehler", langCode, new ArrayList<String>() {
                                {
                                    add(groupName);
                                }
                            }), event.getMessage(), Color.red);
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("aufgabe_erstellen_fehlende_argumente", langCode), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("edit")) {
                if (PermissionSystem.hasPermission(event.getMember(), TaskPermission.EDIT_TASK)) {
                    final String taskID = Connection.encodeString(args[1]);
                    final String newTask = getTaskFromArgs(3, event.getMessage(), false);
                    final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, guild);
                    task.setText(newTask);
                    final int statusCode = task.getStatusCode();
                    if (statusCode == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("aufgabe_editiert", langCode, new ArrayList<String>() {
                            {
                                add(taskID);
                            }
                        }), event.getMessage(), Color.green);
                    } else if (statusCode == 902) {
                        MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                            {
                                add(taskID);
                            }
                        }), event.getMessage(), Color.red);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                            {
                                add(String.valueOf(statusCode));
                            }
                        }), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("deadline")) {
                if (PermissionSystem.hasPermission(event.getMember(), TaskPermission.EDIT_TASK)) {
                    final String taskID = Connection.encodeString(args[1]);
                    final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, guild);
                    String date = args[2];
                    if (args.length == 4) {
                        date += " " + args[3];
                    }
                    if (DateUtil.convertToDate(date) != null) {
                        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        final String newDate = dateFormat.format(DateUtil.convertToDate(date));
                        task.setDeadline(newDate);
                        if (task.getStatusCode() == 200) {
                            MessageSender.send(embedTitle, Localizations.getString("deadline_gesetzt", langCode, new ArrayList<String>() {
                                {
                                    add(taskID);
                                    add(newDate);
                                }
                            }), event.getMessage(), Color.green);
                        } else if (task.getStatusCode() == 902) {
                            MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                                {
                                    add(taskID);
                                }
                            }), event.getMessage(), Color.red);
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("ungueltiges_datum_format", langCode), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode), event.getMessage(), Color.red);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.getMessage(), Color.red);
            }
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("list")) {
                String jsonResponse = "";
                int statusCode = 0;
                String text = "";
                if (event.getMessage().getMentionedMembers().size() > 0) {
                    final Member member = event.getMessage().getMentionedMembers().get(0);
                    final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/task/user/tasks/" + guild.getId() + "/" + member.getId()).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member.getId()).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                    statusCode = res.statusCode();
                    jsonResponse = res.parse().body().text();
                } else {
                    final String groupName = args[1];
                    final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/task/group/tasks/" + guild.getId() + "/" + Connection.encodeString(groupName)).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                    statusCode = res.statusCode();
                    jsonResponse = res.parse().body().text();
                }
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                if (statusCode == 200) {
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
                            dLine = "$deadline |";
                        }
                        builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                    }
                    //TASKS NOT STARTED
                    if (builder.length() > 0) {
                        if (event.getMessage().getMentionedMembers().size() > 0) {
                            final Member member = event.getMessage().getMentionedMembers().get(0);
                            text = Localizations.getString("alle_aufgaben_von_nutzer", langCode, new ArrayList<String>() {
                                {
                                    add(member.getAsMention());
                                    add(builder.toString());
                                }
                            });
                        } else {
                            final String groupName = args[1];
                            text = Localizations.getString("alle_aufgaben_von_gruppe", langCode, new ArrayList<String>() {
                                {
                                    add(groupName);
                                    add(builder.toString());
                                }
                            });
                        }
                        MessageSender.send(embedTitle, text, event.getMessage(), Color.orange);
                    }
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
                            dLine = "$deadline |";
                        }
                        builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_wird_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                    }
                    //TASKS IN PROGRESS
                    if (builder.length() > 0) {
                        if (event.getMessage().getMentionedMembers().size() > 0) {
                            final Member member = event.getMessage().getMentionedMembers().get(0);
                            text = Localizations.getString("alle_aufgaben_von_nutzer", langCode, new ArrayList<String>() {
                                {
                                    add(member.getAsMention());
                                    add(builder.toString());
                                }
                            });
                        } else {
                            final String groupName = args[1];
                            text = Localizations.getString("alle_aufgaben_von_gruppe", langCode, new ArrayList<String>() {
                                {
                                    add(groupName);
                                    add(builder.toString());
                                }
                            });
                        }
                        MessageSender.send(embedTitle, text, event.getMessage(), Color.yellow);
                    }
                    array = jsonObject.get("done").asArray();
                    String showDoneTasks = Settings.getUserSettings(event.getMember()).getString("show_done_tasks", "1");
                    for (int i = 0; i < array.size(); i++){
                        final String taskID = array.get(i).asString();
                        final String task = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("task").asString();
                        String deadline = "";
                        if (!jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").isNull()) {
                            deadline = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").asString();
                        }
                        String dLine = "";
                        if (deadline.length() > 0) {
                            dLine = "$deadline |";
                        }

                        if (showDoneTasks == "1") {
                            builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_erledigt", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                        }
                    }
                    if (showDoneTasks == "1" && builder.length() > 0) {
                        if (event.getMessage().getMentionedMembers().size() > 0) {
                            final Member member = event.getMessage().getMentionedMembers().get(0);
                            text = Localizations.getString("alle_aufgaben_von_nutzer", langCode, new ArrayList<String>() {
                                {
                                    add(member.getAsMention());
                                    add(builder.toString());
                                }
                            });
                        } else {
                            final String groupName = args[1];
                            text = Localizations.getString("alle_aufgaben_von_gruppe", langCode, new ArrayList<String>() {
                                {
                                    add(groupName);
                                    add(builder.toString());
                                }
                            });
                        }
                        MessageSender.send(embedTitle, text, event.getMessage(), Color.green);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgaben", langCode), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (PermissionSystem.hasPermission(event.getMember(), TaskPermission.DELETE_TASK)) {
                    final String taskID = Connection.encodeString(args[1]);
                    final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, guild);
                    task.delete();
                    final int statusCode = task.getStatusCode();
                    if (statusCode == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("aufgabe_geloescht", langCode, new ArrayList<String>() {
                            {
                                add(taskID);
                            }
                        }), event.getMessage(), Color.green);
                    } else if (statusCode == 902) {
                        MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                            {
                                add(taskID);
                            }
                        }), event.getMessage(), Color.red);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                            {
                                add(String.valueOf(statusCode));
                            }
                        }), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("done")) {
                final String taskID = Connection.encodeString(args[1]);
                final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, guild);
                final int statusCode = task.setStatus(TaskStatus.DONE, event.getMember()).getStatusCode();
                if (statusCode == 200) {
                    MessageSender.send(embedTitle, Localizations.getString("aufgabe_erledigt", langCode), event.getMessage(), Color.green);
                } else if (statusCode == 902) {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                        {
                            add(taskID);
                        }
                    }), event.getMessage(), Color.red);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                        {
                            add(String.valueOf(statusCode));
                        }
                    }), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("proceed")) {
                final String taskID = Connection.encodeString(args[1]);
                final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, guild);
                final int statusCode = task.proceed(event.getMember()).getStatusCode();
                if (statusCode == 200) {
                    final TaskStatus taskStatus = task.getStatus();
                    if (taskStatus == TaskStatus.IN_PROGRESS) {
                        MessageSender.send(embedTitle, Localizations.getString("aufgabe_wird_nun_bearbeitet", langCode), event.getMessage(), Color.green);
                    } else if (taskStatus == TaskStatus.DONE) {
                        MessageSender.send(embedTitle, Localizations.getString("aufgabe_erledigt", langCode), event.getMessage(), Color.green);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("task_abfrage_unbekannter_fehler", langCode), event.getMessage(), Color.red);
                    }
                } else if (statusCode == 902) {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                                {
                                    add(taskID);
                                }
                            }), event.getMessage(), Color.red);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                        {
                            add(String.valueOf(statusCode));
                        }
                    }),event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("undo")) {
                final String taskID = Connection.encodeString(args[1]);
                final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, guild);
                final int statusCode = task.undo(event.getMember()).getStatusCode();
                    if (statusCode == 200) {
                        final TaskStatus taskStatus = task.getStatus();
                            if (taskStatus == TaskStatus.TODO) {
                                MessageSender.send(embedTitle, Localizations.getString("aufgabe_wird_nicht_bearbeitet", langCode), event.getMessage(), Color.green);
                            }
                            else if (taskStatus == TaskStatus.IN_PROGRESS) {
                                MessageSender.send(embedTitle, Localizations.getString("aufgabe_wird_nun_bearbeitet", langCode), event.getMessage(), Color.green);
                            }
                            else if (taskStatus == TaskStatus.DONE) {
                                MessageSender.send(embedTitle, Localizations.getString("aufgabe_erledigt", langCode), event.getMessage(), Color.green);
                            }
                            else {
                                MessageSender.send(embedTitle, Localizations.getString("task_abfrage_unbekannter_fehler", langCode), event.getMessage(), Color.red);
                            }
                    } else if (statusCode == 902) {
                        MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                            {
                                add(taskID);
                            }
                        }),event.getMessage(), Color.red);
                    }
                    else {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                            {
                                add(String.valueOf(statusCode));
                            }
                        }),event.getMessage(), Color.red);
                    }
            } else if (args[0].equalsIgnoreCase("info")) {
                final String taskID = Connection.encodeString(args[1]);
                final de.bnder.taskmanager.utils.Task task = new de.bnder.taskmanager.utils.Task(taskID, guild);
                if (task.exists()) {
                    final EmbedBuilder builder = new EmbedBuilder().setColor(Color.cyan).setTimestamp(Calendar.getInstance().toInstant());
                        if (task.getStatus() == TaskStatus.TODO) {
                            builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode), true);
                        }
                        else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                            builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_wird_bearbeitet", langCode), true);
                        }
                        else if (task.getStatus() == TaskStatus.DONE) {
                            builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_erledigt", langCode), true);
                        }
                    if (task.getDeadline() != null) {
                        builder.addField(Localizations.getString("task_info_field_deadline", langCode), task.getDeadline(), true);
                    }
                    if (task.getType() == TaskType.USER) {
                        builder.addField(Localizations.getString("task_info_field_type", langCode), Localizations.getString("task_info_field_type_user", langCode), true);
                        final String userID = task.getHolder();
                        if (userID != null && event.getGuild().retrieveMemberById(userID).complete() != null) {
                            builder.addField(Localizations.getString("task_info_field_assigned", langCode), event.getGuild().retrieveMemberById(userID).complete().getUser().getAsTag(), true);
                        }
                    } else if (task.getType() == TaskType.GROUP) {
                        builder.addField(Localizations.getString("task_info_field_type", langCode), Localizations.getString("task_info_field_type_group", langCode), true);
                        builder.addField(Localizations.getString("task_info_field_assigned", langCode), task.getHolder(), true);
                    }
                    builder.addField(Localizations.getString("task_info_field_task", langCode), task.getText(), false);
                    event.getChannel().sendMessage(builder.build()).queue();
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, new ArrayList<String>() {
                        {
                            add(taskID);
                        }
                    }),event.getMessage(), Color.red);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode), event.getMessage(), Color.red);
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                final Member member = event.getMember();
                final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/task/user/tasks/" + guild.getId()).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member.getId()).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                final Document document = res.parse();
                final String jsonResponse = document.body().text();
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                if (res.statusCode() == 200) {
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
                            dLine = "$deadline |";
                        }
                        builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                    }
                    //TASKS NOT STARTED
                    if (builder.length() > 0) {
                        MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_nutzer", langCode, new ArrayList<String>() {
                            {
                                add(member.getAsMention());
                                add(builder.toString());
                            }
                        }), event.getMessage(), Color.orange);
                        builder.delete(0, builder.length());
                    }
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
                            dLine = "$deadline |";
                        }
                        builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_wird_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                    }
                    //TASKS IN PROGRESS
                    if (builder.length() > 0) {
                        MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_nutzer", langCode, new ArrayList<String>() {
                            {
                                add(member.getAsMention());
                                add(builder.toString());
                            }
                        }), event.getMessage(), Color.yellow);
                        builder.delete(0, builder.length());
                    }
                    array = jsonObject.get("done").asArray();
                    String showDoneTasks = Settings.getUserSettings(event.getMember()).getString("show_done_tasks", "1");
                    for (int i = 0; i < array.size(); i++){
                        final String taskID = array.get(i).asString();
                        final String task = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("task").asString();
                        String deadline = "";
                        if (!jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").isNull()) {
                            deadline = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").asString();
                        }
                        String dLine = "";
                        if (deadline.length() > 0) {
                            dLine = "$deadline |";
                        }

                        if (showDoneTasks == "1") {
                            builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_erledigt", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n");
                        }
                    }
                    if (showDoneTasks == "1" && builder.length() > 0) {
                        MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_nutzer", langCode, new ArrayList<String>() {
                            {
                                add(member.getAsMention());
                                add(builder.toString());
                            }
                        }),event.getMessage(), Color.green);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgaben", langCode), event.getMessage(), Color.red);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode), event.getMessage(), Color.red);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode), event.getMessage(), Color.red);
        }
    }

    private void sendTaskMessage(Member member, GuildMessageReceivedEvent event, String task_id, String langCode, String task) {
        final JsonObject settings = de.bnder.taskmanager.utils.Settings.getUserSettings(member);
        if (settings.getString("direct_message", "1") == "1") {
            final PrivateChannel channel = member.getUser().openPrivateChannel().complete();
            channel.sendMessage(Localizations.getString("aufgabe_erhalten", langCode, new ArrayList<String>() {
                {
                    add(event.getAuthor().getAsTag());
                    add(task_id);
                }
            })).queue();
            try {
                channel.sendMessage(URLDecoder.decode(task, StandardCharsets.UTF_8.toString())).queue();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (settings.get("notify_channel") != null) {
            final TextChannel channel = event.getGuild().getTextChannelById(settings.getString("notify_channel", ""));
            if (channel != null) {
                channel.sendMessage(member.getAsMention() + Localizations.getString("aufgabe_erhalten", langCode, new ArrayList<String>() {
                    {
                        add(event.getAuthor().getAsTag());
                        add(task_id);
                    }
                })).queue();
                try {
                    channel.sendMessage(URLDecoder.decode(task, StandardCharsets.UTF_8.toString())).queue();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getTaskFromArgs(int startIndex, Message message, boolean mentionedUsers) {
        int beginIndex = startIndex;
        final StringBuilder taskBuilder = new StringBuilder();
        final String[] args = message.getContentDisplay().split(" ");
        if (mentionedUsers) {
            for (User user : message.getMentionedUsers()) {
                beginIndex += user.getAsTag().split(" ").length;
            }
            beginIndex += 1;
        }
        for (int i = beginIndex; i < args.length; i++) {
            taskBuilder.append(args[i]).append(" ");
        }
        if (taskBuilder.length() > 0) {
            taskBuilder.subSequence(0, taskBuilder.length() - 1);
        }
        return taskBuilder.toString();
    }
}
