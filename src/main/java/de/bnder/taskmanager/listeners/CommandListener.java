package de.bnder.taskmanager.listeners;
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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.CommandHandler;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CommandListener extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().length() > 3) {
            if (CommandHandler.commands.containsKey(event.getMessage().getContentRaw().split(" ")[0].substring(1))) {
                try {
                    String jsonResponse = Jsoup.connect(Main.requestURL + "getPrefix.php?requestToken=" + Main.requestToken + "&serverID=" + de.bnder.taskmanager.utils.Connection.encodeString(event.getGuild().getId())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
                    JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                    int statusCode = jsonObject.getInt("status_code", 900);
                    if (statusCode == 200) {
                        String prefix = jsonObject.getString("prefix", "-");
                        if (event.getMessage().getContentRaw().startsWith(prefix)) {
                            processCommand(event);
                        }
                    } else if (event.getMessage().getContentRaw().startsWith("-")) {
                        processCommand(event);
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    final String langCode = Localizations.Companion.getGuildLanguage(event.getGuild());
                    MessageSender.send(Localizations.Companion.getString("error_title", langCode), Localizations.Companion.getString("error_text", langCode) + sw.toString().substring(0, 400), event.getMessage(), Color.red);
                }
            }
        }
    }

    private void processCommand(GuildMessageReceivedEvent event) {
        try {
            String msg = event.getMessage().getContentRaw();
            while (msg.contains("  ")) {
                msg = msg.replace("  ", " ");
            }
            CommandHandler.handleCommand(CommandHandler.parse.parse(msg, event), event.getMessage());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            final String langCode = Localizations.Companion.getGuildLanguage(event.getGuild());
            MessageSender.send(Localizations.Companion.getString("error_title", langCode), Localizations.Companion.getString("error_text", langCode) + sw.toString().substring(0, 400), event.getMessage(), Color.red);
        }
    }
}
