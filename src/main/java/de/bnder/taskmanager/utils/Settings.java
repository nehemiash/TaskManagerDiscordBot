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
import de.bnder.taskmanager.main.Main;
import org.jsoup.Jsoup;

import java.io.IOException;

public class Settings {

    public static JsonObject getUserSettings(String userid) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "getSettings.php?requestToken=" + Main.requestToken + "&user_id=" + Connection.encodeString(userid)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            final int statusCode = jsonObject.getInt("status_code", 900);
            if (statusCode == 200) {
                return jsonObject.get("result").asObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JsonObject();
    }

    public static JsonObject getGuildSettings(String guildid) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "getSettings.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guildid)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            final int statusCode = jsonObject.getInt("status_code", 900);
            if (statusCode == 200) {
                return jsonObject.get("result").asObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JsonObject();
    }

}
