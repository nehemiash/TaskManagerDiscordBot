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

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class DeadlineReminders {

    private static final Logger logger = LogManager.getLogger(DeadlineReminders.class);

    public static void start(ShardManager shardManager) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    final LocalDateTime ldt = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()).plusMinutes(30);
                    final Date out = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                    for (DocumentSnapshot serverDoc : Main.firestore.collection("server").get().get()) {
                        try {
                            final Guild guild = shardManager.getGuildById(serverDoc.getId());
                            if (guild != null) {
                                final Locale langCode = Localizations.getGuildLanguage(guild);
                                for (DocumentSnapshot boardDoc : serverDoc.getReference().collection("boards").get().get()) {
                                    final QuerySnapshot taskDocs = boardDoc.getReference().collection("user-tasks").whereLessThanOrEqualTo("deadline", out).whereEqualTo("deadline_reminded", false).limit(1).get().get();
                                    for (DocumentSnapshot taskDoc : taskDocs) {
                                        taskDoc.getReference().update("deadline_reminded", true);
                                        final String taskID = taskDoc.getId();
                                        final String task = taskDoc.getString("text");
                                        final String deadline = taskDoc.getString("deadline");
                                        final String userID = taskDoc.getString("user_id");
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

                                for (DocumentSnapshot group : serverDoc.getReference().collection("groups").get().get()) {
                                    final QuerySnapshot groupMembers = group.getReference().collection("group-member").get().get();
                                    for (DocumentSnapshot groupTask : group.getReference().collection("group-tasks").whereLessThanOrEqualTo("deadline", out).whereEqualTo("deadline_reminded", false).get().get()) {
                                        groupTask.getReference().update("deadline_reminded", true);
                                        final String groupName = group.getString("group");
                                        for (final DocumentSnapshot groupMemberDoc : groupMembers) {
                                            final String userID = groupMemberDoc.getString("user_id");
                                            guild.retrieveMemberById(userID).queue(member -> member.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(Localizations.getString("deadline_remind_group", langCode, new ArrayList<>() {{
                                                add(groupTask.getString("text"));
                                                add(groupTask.getId());
                                                add(groupName);
                                                add(groupTask.getString("deadline"));
                                            }})).queue(), (error) -> {
                                            }), (error) -> {
                                            });
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                } catch (ExecutionException | InterruptedException executionException) {
                    logger.error(executionException);
                }
            }
        }, 15 * 1000, 30 * 60 * 1000);
    }
}
