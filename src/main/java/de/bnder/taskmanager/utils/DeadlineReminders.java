package de.bnder.taskmanager.utils;
/*
 * Copyright (C) 2020 Jan Brinkmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DeadlineReminders {

    public static void start(ShardManager shardManager) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    //TODO
                    final String jsonResponse = Jsoup.connect(Main.requestURL + "getDeadlineReminders.php?requestToken=" + Main.requestToken).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
                    final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                    final int statusCode = jsonObject.getInt("status_code", 900);
                    if (statusCode == 200) {
                        for (final JsonValue value : jsonObject.get("list").asArray()) {
                            final JsonObject taskObject = value.asObject();
                            final String taskID = taskObject.getString("task_id", null);
                            final String task = taskObject.getString("task", null);
                            final String serverID = taskObject.getString("server_id", null);
                            final String taskType = taskObject.getString("task_type", null);
                            final String deadline = taskObject.getString("deadline", null);
                            final Guild guild = shardManager.getGuildById(serverID);
                            if (guild != null) {
                                final String langCode = Localizations.Companion.getGuildLanguage(guild);
                                if (taskType.equalsIgnoreCase("group")) {
                                    final String groupName = taskObject.getString("group_name", null);
                                    for (final JsonValue groupMemberValue : taskObject.get("group_members").asArray()) {
                                        final String userID = groupMemberValue.asString();
                                        final PrivateChannel channel = guild.retrieveMemberById(userID).complete().getUser().openPrivateChannel().complete();
                                        channel.sendMessage(Localizations.Companion.getString("deadline_remind_group", langCode, new ArrayList<String>(){{
                                            add(task);
                                            add(taskID);
                                            add(groupName);
                                            add(deadline);
                                        }})).queue();
                                    }
                                } else if (taskType.equalsIgnoreCase("user")) {
                                    final String userID = taskObject.getString("user_id", null);
                                    final PrivateChannel channel = guild.retrieveMemberById(userID).complete().getUser().openPrivateChannel().complete();
                                    channel.sendMessage(Localizations.Companion.getString("deadline_remind_user", langCode, new ArrayList<String>(){{
                                        add(task);
                                        add(taskID);
                                        add(deadline);
                                    }})).queue();
                                }
                                setReminded(serverID, taskID);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 15 * 1000, 10 * 60 * 1000);
    }

    private static void setReminded(final String serverID, final String taskID) throws IOException {
        //TODO
        Jsoup.connect(Main.requestURL + "setDeadlineReminded.php?requestToken=" + Main.requestToken + "&server_id=" + serverID + "&task_id=" + taskID).timeout(Connection.timeout).userAgent(Main.userAgent).execute();
    }
}
