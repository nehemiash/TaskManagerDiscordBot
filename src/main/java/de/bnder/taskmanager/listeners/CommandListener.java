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

import de.bnder.taskmanager.main.CommandHandler;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class CommandListener extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getMessage().getContentDisplay().startsWith(Main.prefix) && event.getMessage().getContentRaw().length() > 3) {
            try {
                String msg = event.getMessage().getContentRaw();
                while (msg.contains("  ")) {
                    msg = msg.replace("  ", " ");
                }
                CommandHandler.handleCommand(CommandHandler.parse.parse(msg, event), event.getMessage());
            } catch (Exception e) {
                final String langCode = Localizations.Companion.getGuildLanguage(event.getGuild());
                MessageSender.send(Localizations.Companion.getString("error_title", langCode), Localizations.Companion.getString("error_text", langCode), event.getMessage(), Color.red);
            }
        }
    }
}
