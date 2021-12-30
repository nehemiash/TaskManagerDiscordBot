package de.bnder.taskmanager.utils;
/*
 * Copyright (C) 2019 Jan Brinkmann
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
import net.dv8tion.jda.api.entities.Guild;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class UpdateServer {

    public static void update(Guild guild) throws ExecutionException, InterruptedException {
        final DocumentSnapshot serverDoc = Main.firestore.collection("server")
                .document(guild.getId()).get().get();
        if (serverDoc.exists()) {
            if (!serverDoc.contains("name") || serverDoc.get("name") == null || !serverDoc.getString("name").equals(guild.getName())) {
                serverDoc.getReference().update("name", guild.getName());
            }
            if (!serverDoc.contains("icon_url") || (serverDoc.get("icon_url") == null && guild.getIconUrl() != null) || (serverDoc.get("icon_url") != null && !serverDoc.getString("icon_url").equals(guild.getIconUrl()))) {
                serverDoc.getReference().update("icon_url", guild.getIconUrl());
            }
        } else {
            guild.retrieveOwner().queue(owner -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("name", guild.getName());
                map.put("language", "en");
                map.put("owner", owner.getId());
                map.put("guild_id", guild.getId());
                map.put("icon_url", guild.getIconUrl());
                serverDoc.getReference().set(map);
            });
        }
    }
}
