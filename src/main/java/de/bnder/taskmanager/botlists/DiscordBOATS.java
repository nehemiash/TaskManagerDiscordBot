package de.bnder.taskmanager.botlists;
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

import de.bnder.taskmanager.main.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class DiscordBOATS {

    private static final String baseURL = "https://discord.boats/api";
    private static final String apiKey = Main.dotenv.get("DISCORD.BOATS_API_KEY") != null ? Main.dotenv.get("DISCORD.BOATS_API_KEY") : System.getenv("DISCORD.BOATS_API_KEY");

    private static final Logger logger = LogManager.getLogger(DiscordBotsGG.class);

    public static void sendServerCount(long serverCount, String botID) throws IOException {
        logger.info("Updating Servers on " + baseURL);
        final Connection.Response response = Jsoup.connect(baseURL + "/bot/" + botID)
                .header("Authorization", apiKey)
                .data("server_count", String.valueOf(serverCount))
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        if (response.statusCode() == 200){
            logger.info("Success!");
        } else {
            logger.warn("Failed! (" + response.statusCode() + ")");
        }
    }

}
