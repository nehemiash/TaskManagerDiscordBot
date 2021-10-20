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
import de.bnder.taskmanager.main.Main;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Stats {

    public static void updateMessagesSent() {
        long messagesSent = 1;
        try {
            final DocumentSnapshot alltimeStats = Main.firestore.collection("stats").document("alltime").get().get();
            if (alltimeStats.exists()) {
                if (alltimeStats.getData().get("messages_sent") != null) {
                    messagesSent = (long) alltimeStats.get("messages_sent");
                }
                long finalMessagesSent = messagesSent;
                alltimeStats.getReference().update(new HashMap<>() {{
                    put("messages_sent", finalMessagesSent);
                }});
            } else {
                long finalMessagesSent1 = messagesSent;
                alltimeStats.getReference().set(new HashMap<>() {{
                    put("messages_sent", finalMessagesSent1);
                }});
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void updateServers(int servers, int shard) {
        try {
            final DocumentSnapshot alltimeStats = Main.firestore.collection("stats").document("alltime").get().get();
            if (alltimeStats.exists()) {
                alltimeStats.getReference().update(new HashMap<>() {{
                    put("servers_shard_" + shard, servers);
                }});
            } else {
                alltimeStats.getReference().set(new HashMap<>() {{
                    put("servers_shard_" + shard, servers);
                }});
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void updateTasksCreated() {
        long tasksCreated = 1;
        try {
            final DocumentSnapshot alltimeStats = Main.firestore.collection("stats").document("alltime").get().get();
            if (alltimeStats.exists()) {
                if (alltimeStats.getData().containsKey("tasks_created")) {
                    tasksCreated = (long) alltimeStats.get("tasks_created");
                }
                long finalTasksCreated = tasksCreated;
                alltimeStats.getReference().update(new HashMap<>() {{
                    put("tasks_created", finalTasksCreated);
                }});
            } else {
                long finalTasksCreated1 = tasksCreated;
                alltimeStats.getReference().set(new HashMap<>() {{
                    put("tasks_created", finalTasksCreated1);
                }});
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void updateTasksDone() {
        long tasksDone = 1;
        try {
            final DocumentSnapshot alltimeStats = Main.firestore.collection("stats").document("alltime").get().get();
            if (alltimeStats.exists()) {
                if (alltimeStats.getData().containsKey("tasks_done")) {
                    tasksDone = (long) alltimeStats.get("tasks_done");
                }
                long finalTasksCreated = tasksDone;
                alltimeStats.getReference().update(new HashMap<>() {{
                    put("tasks_done", finalTasksCreated);
                }});
            } else {
                long finalTasksCreated1 = tasksDone;
                alltimeStats.getReference().set(new HashMap<>() {{
                    put("tasks_done", finalTasksCreated1);
                }});
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
