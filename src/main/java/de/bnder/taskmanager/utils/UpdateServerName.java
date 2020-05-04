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

import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import org.jsoup.Jsoup;

import java.io.IOException;

public class UpdateServerName {

    public static void update(Guild guild) throws IOException {
        String id = guild.getId();
        String name = guild.getName();
        Jsoup.connect(Main.requestURL + "setServername.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(id) + "&serverName=" + Connection.encodeString(name)).userAgent(Main.userAgent).execute();
    }

}
