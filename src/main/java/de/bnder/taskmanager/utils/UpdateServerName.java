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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class UpdateServerName {

    public static void update(Guild guild) throws IOException, ExecutionException, InterruptedException {
        DocumentSnapshot a = Main.firestore.collection("server")
                .document(guild.getId()).get().get();
        if (a.exists()) {
            a.getReference().update("name", guild.getName());
        } else {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", guild.getName());
            map.put("language", "en");
            map.put("owner", guild.retrieveOwner().complete().getId());
            a.getReference().set(map);
        }

    }

}
