package de.bnder.taskmanager.lists;
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
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class InfinitybotlistCOM {

    private static final String baseURL = "https://infinitybotlist.com/api";
    private static final String apiKey = Main.dotenv.get("INFINITYBOTLIST.COM_API_KEY") != null ? Main.dotenv.get("INFINITYBOTLIST.COM_API_KEY") : System.getenv("INFINITYBOTLIST.COM_API_KEY");

    public static void sendServerCount(long serverCount, String botID) throws IOException {
        System.out.println("Updating Servers on " + baseURL);
        final Connection.Response response = Jsoup.connect(baseURL + "/bots/" + botID)
                .header("authorization", apiKey)
                .data("servers", String.valueOf(serverCount))
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        if (response.statusCode() == 200){
            System.out.println("Success!");
        } else {
            System.out.println("Failed! (" + response.statusCode() + ")");
        }
    }

}
