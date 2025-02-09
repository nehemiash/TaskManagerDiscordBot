package de.bnder.taskmanager.commands;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.*;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import de.bnder.taskmanager.utils.permissions.PermissionPermission;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Data implements Command {
    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, List<Member> mentionedMembers, List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException {
        final Locale langCode = Localizations.getGuildLanguage(guild);
        MessageSender.send(Localizations.getString("data_title", langCode), Localizations.getString("data_will_be_sent", langCode), textChannel, Color.cyan, langCode, slashCommandEvent);

        //Get every user data
        final File file = new File("userData-" + guild.getId() + "-" + commandExecutor.getId() + ".yml");
        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
        }
        final YamlConfiguration yamlConfiguration = new YamlConfiguration();

        yamlConfiguration.set("language", langCode);
        final String guildId = guild.getId();
            try {
                //Get Tasks
                final org.jsoup.Connection.Response res = Main.tmbAPI("task/user/tasks/" + guildId + "/" + commandExecutor.getId(), commandExecutor.getId(), org.jsoup.Connection.Method.GET).execute();
                final String jsonResponse = res.parse().body().text();
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                if (res.statusCode() == 200) {
                    JsonArray array = jsonObject.get("todo").asArray();
                    for (int i = 0; i < array.size(); i++) {
                        final String taskID = array.get(i).asString();
                        final String task = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("task").asString();
                        final String deadline = !jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").isNull() ? jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").asString() : "---";
                        final String type = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("type").asString();
                        final String groupID = !jsonObject.get("allTasks").asObject().get(taskID).asObject().get("group_id").isNull() ? jsonObject.get("allTasks").asObject().get(taskID).asObject().get("group_id").asString() : "---";

                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "text", task);
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "deadline", deadline);
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "status", "not edited");
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "type", type);
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "group id", groupID);
                    }
                    array = jsonObject.get("doing").asArray();
                    for (int i = 0; i < array.size(); i++) {
                        final String taskID = array.get(i).asString();
                        final String task = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("task").asString();
                        final String deadline = !jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").isNull() ? jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").asString() : "---";
                        final String type = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("taskType").asString();
                        final String groupID = !jsonObject.get("allTasks").asObject().get(taskID).asObject().get("group_id").isNull() ? jsonObject.get("allTasks").asObject().get(taskID).asObject().get("group_id").asString() : "---";

                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "text", task);
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "deadline", deadline);
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "status", "in progress");
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "type", type);
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "group id", groupID);
                    }
                    array = jsonObject.get("done").asArray();
                    for (int i = 0; i < array.size(); i++) {
                        final String taskID = array.get(i).asString();
                        final String task = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("task").asString();
                        final String deadline = !jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").isNull() ? jsonObject.get("allTasks").asObject().get(taskID).asObject().get("deadline").asString() : "---";
                        final String type = jsonObject.get("allTasks").asObject().get(taskID).asObject().get("type").asString();
                        final String groupID = !jsonObject.get("allTasks").asObject().get(taskID).asObject().get("group_id").isNull() ? jsonObject.get("allTasks").asObject().get(taskID).asObject().get("group_id").asString() : "---";

                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "text", task);
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "deadline", deadline);
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "status", "done");
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "type", type);
                        yamlConfiguration.set("guild" + "." + guildId + "." + "tasks" + "." + taskID + "." + "group id", groupID);
                    }
                }

                //Get User Settings
                final JsonObject settingsObject = Settings.getUserSettings(commandExecutor);
                yamlConfiguration.set("guild" + "." + guildId + "." + "settings" + ".direct message", Boolean.valueOf(settingsObject.get("direct_message") != null ? settingsObject.get("direct_message").toString() : "1"));
                yamlConfiguration.set("guild" + "." + guildId + "." + "settings" + ".show done tasks", Boolean.valueOf(settingsObject.get("show_done_tasks") != null ? settingsObject.get("show_done_tasks").toString() : "1"));
                yamlConfiguration.set("guild" + "." + guildId + "." + "settings" + ".notify channel", settingsObject.get("notify_channel") != null && !settingsObject.get("notify_channel").isNull() ? settingsObject.getString("notify_channel", null) : null);

                //Get Users Groups
                final org.jsoup.Connection.Response getGroupsRes = Main.tmbAPI("group/list/" + guildId, commandExecutor.getId(), org.jsoup.Connection.Method.GET).execute();
                final JsonObject getGroupsObject = Json.parse(getGroupsRes.parse().body().text()).asObject();
                if (getGroupsRes.statusCode() == 200) {
                    JsonArray servers = getGroupsObject.get("groups").asArray();
                    if (servers.size() > 0) {
                        for (int i = 0; i < servers.size(); i++) {
                            final String groupName = servers.get(i).asString();
                            final org.jsoup.Connection.Response getMembersRes = Main.tmbAPI("group/commandExecutors/" + guildId + "/" + Connection.encodeString(groupName), commandExecutor.getId(), org.jsoup.Connection.Method.GET).execute();
                            final JsonObject getMembersObject = Json.parse(getMembersRes.parse().body().text()).asObject();
                            if (getMembersObject.getInt("status_code", 900) == 200) {
                                final String groupID = getMembersObject.getString("group_id", null);
                                for (JsonValue value : getMembersObject.get("commandExecutors").asArray()) {
                                    final String id = value.asObject().getString("user_id", null);
                                    if (id != null) {
                                        if (id.equalsIgnoreCase(commandExecutor.getId())) {
                                            yamlConfiguration.set("guild" + "." + guildId + "." + "group" + "." + groupID + ".name", groupName);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //Get User Permissions
                for (TaskPermission permission : TaskPermission.values()) {
                    final boolean hasPermission = PermissionSystem.hasPermission(commandExecutor, permission);
                    yamlConfiguration.set("guild" + "." + guildId + "." + "permission" + "." + permission.name(), hasPermission);
                }
                for (GroupPermission permission : GroupPermission.values()) {
                    final boolean hasPermission = PermissionSystem.hasPermission(commandExecutor, permission);
                    yamlConfiguration.set("guild" + "." + guildId + "." + "permission" + "." + permission.name(), hasPermission);
                }
                for (PermissionPermission permission : PermissionPermission.values()) {
                    final boolean hasPermission = PermissionSystem.hasPermission(commandExecutor, permission);
                    yamlConfiguration.set("guild" + "." + guildId + "." + "permission" + "." + permission.name(), hasPermission);
                }

                yamlConfiguration.save(file);
                commandExecutor.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendFile(file).queue(), (error) -> {});
                file.delete();
            } catch (Exception ignored) {
            }

    }
}
