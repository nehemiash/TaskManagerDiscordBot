package de.bnder.taskmanager.session;
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
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Session {

    final String shardID;
    GenerateKeys keys;

    public Session(final String shard) {
        this.shardID = shard;
    }

    public Session create() {
        try {
            this.keys = new GenerateKeys();
            keys.createKeys();
            if (keys != null) {
                final Connection.Response resp = Jsoup.connect(Main.tmbApiUrl + "/session/create/" + shardID).method(Connection.Method.POST)
                        .header("authorization", "TMB " + Main.tmbApiAuthorizationToken)
                        .header("user_id", "---")
                        .data("bot_public_key", String.valueOf(keys.getPublicKey().getEncoded()))
                        .timeout(de.bnder.taskmanager.utils.Connection.timeout)
                        .userAgent(Main.userAgent)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .postDataCharset("UTF-8")
                        .followRedirects(true).execute();
                if (resp.statusCode() == 200) {
                    final String body = resp.body();
                    System.out.println(body);
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return this;
    }

}
