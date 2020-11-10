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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

public class MessageSender {
    public static void send(String title, String s, Message msg, Color red) {
        if (s.length() > 1990) {
            while (s.length() > 1990) {
                String textNow = s.substring(0, 1990);
                buildMessageBuilder(title, msg, red, textNow);
                s = s.substring(1990);
            }
            if (s.length() > 0) {
                buildMessageBuilder(title, msg, red, s);
            }
        } else {
            buildMessageBuilder(title, msg, red, s);
        }
    }

    private static void buildMessageBuilder(String title, Message msg, Color red, String textNow) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(textNow);
        builder.setColor(red);
        builder.setTimestamp(Calendar.getInstance().toInstant());
        if (new Random().nextInt(100) + 1 <= 5) {
            final String langCode = Localizations.Companion.getGuildLanguage(msg.getGuild());
            builder.setFooter(Localizations.Companion.getString("donate_alert", langCode));
        }
        msg.getChannel().sendMessage(builder.build()).queue();
        try {
            final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/stats/messages-sent").method(org.jsoup.Connection.Method.POST).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
