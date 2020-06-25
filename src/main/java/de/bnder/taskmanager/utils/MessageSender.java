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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.util.Calendar;
import java.util.Random;

public class MessageSender {
    public static void send(String title, String s, Message msg, Color red) {
        if (s.length() > 1990) {
            while (s.length() > 1990) {
                String textNow = s.substring(0, 1990);
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
                s = s.substring(1990);
            }
            if (s.length() > 0) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(title);
                builder.setDescription(s);
                builder.setColor(red);
                builder.setTimestamp(Calendar.getInstance().toInstant());
                if (new Random().nextInt(100) + 1 <= 5) {
                    final String langCode = Localizations.Companion.getGuildLanguage(msg.getGuild());
                    builder.setFooter(Localizations.Companion.getString("donate_alert", langCode));
                }
                msg.getChannel().sendMessage(builder.build()).queue();
            }
        } else {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(title);
            builder.setDescription(s);
            builder.setColor(red);
            builder.setTimestamp(Calendar.getInstance().toInstant());
            if (new Random().nextInt(100) + 1 <= 5) {
                final String langCode = Localizations.Companion.getGuildLanguage(msg.getGuild());
                builder.setFooter(Localizations.Companion.getString("donate_alert", langCode));
            }
            msg.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
