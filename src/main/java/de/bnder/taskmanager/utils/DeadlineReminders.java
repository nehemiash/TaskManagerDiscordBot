package de.bnder.taskmanager.utils;
/*
 * Copyright (C) 2021 Jan Brinkmann
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

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class DeadlineReminders {

    private static final Logger logger = LogManager.getLogger(DeadlineReminders.class);

    public static void start(final ShardManager shardManager) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final LocalDateTime ldt = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()).plusHours(24);
                final Date out = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                try {
                    logger.info("Querying user-tasks for deadlines...");
                    for (QueryDocumentSnapshot taskDoc : Main.firestore.collectionGroup("user-tasks").whereLessThanOrEqualTo("deadline", out).whereEqualTo("deadline_reminded", false).get().get()) {
                        final String guildID = taskDoc.getString("server_id");
                        if (guildID == null || guildID.equals("dc_server_id"))
                            continue;
                        logger.info("Found a task!");
                        final String taskID = taskDoc.getId();
                        final String task = taskDoc.getString("text");
                        final String userID = taskDoc.getString("user_id");
                        if (shardManager.getGuildById(guildID) != null) {
                            taskDoc.getReference().update("deadline_reminded", true);

                            final Guild guild = shardManager.getGuildById(guildID);
                            final Locale langCode = Localizations.getGuildLanguage(guild);
                            final String deadline = taskDoc.get("deadline") != null ? new SimpleDateFormat(Localizations.getString("datetime_format", langCode)).format(taskDoc.getDate("deadline")) : "";
                            guild.retrieveMemberById(userID).queue(member -> member.getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(Localizations.getString("deadline_remind_user", langCode, new ArrayList<>() {{
                                        add(task);
                                        add(taskID);
                                        add(deadline);
                                    }})).queue(), (error) -> {
                            }), throwable -> {
                            });
                        }
                    }

                    logger.info("Querying group-tasks for deadlines...");
                    for (QueryDocumentSnapshot taskDoc : Main.firestore.collectionGroup("group-tasks").whereLessThanOrEqualTo("deadline", out).whereEqualTo("deadline_reminded", false).get().get()) {
                        final String guildID = taskDoc.getString("server_id");
                        if (guildID == null || guildID.equals("dc_server_id"))
                            continue;
                        final String taskID = taskDoc.getId();
                        final String task = taskDoc.getString("text");
                        final String groupID = taskDoc.getString("group_id");
                        if (shardManager.getGuildById(guildID) != null) {
                            logger.info("Found a task!");
                            taskDoc.getReference().update("deadline_reminded", true);
                            final Guild guild = shardManager.getGuildById(guildID);
                            final Locale langCode = Localizations.getGuildLanguage(guild);
                            final String deadline = taskDoc.get("deadline") != null ? new SimpleDateFormat(Localizations.getString("datetime_format", langCode)).format(taskDoc.getDate("deadline")) : "";
                            final DocumentSnapshot group = Main.firestore.collection("server").document(guildID).collection("groups").document(groupID).get().get();
                            final String groupName = group.getString("name");
                            final QuerySnapshot groupMembers = group.getReference().collection("group-member").get().get();
                            for (final DocumentSnapshot groupMemberDoc : groupMembers) {
                                final String userID = groupMemberDoc.getString("user_id");
                                guild.retrieveMemberById(userID).queue(member -> member.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(Localizations.getString("deadline_remind_group", langCode, new ArrayList<>() {{
                                    add(task);
                                    add(taskID);
                                    add(groupName);
                                    add(deadline);
                                }})).queue(), (error) -> {
                                }), (error) -> {
                                });
                            }
                        }

                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    logger.error(exception);
                }
            }
        }, 30 * 1000, 60 * 60 * 1000);
    }
}
