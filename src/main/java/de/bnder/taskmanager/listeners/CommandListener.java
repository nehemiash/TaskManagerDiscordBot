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
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class CommandListener extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            if (event.getMessage().getContentRaw().length() > 0) {
                if (CommandHandler.commands.containsKey(event.getMessage().getContentRaw().split(" ")[0].substring(1))) {
                    try {
                        final org.jsoup.Connection.Response getPrefixRes = Main.tmbAPI("server/prefix/" + event.getGuild().getId(), event.getAuthor().getId(), org.jsoup.Connection.Method.GET).execute();
                        if (getPrefixRes.statusCode() == 200) {
                            final JsonObject jsonObject = Json.parse(getPrefixRes.parse().body().text()).asObject();
                            final String prefix = jsonObject.getString("prefix", Main.prefix);
                            if (event.getMessage().getContentRaw().startsWith(prefix)) {
                                processCommand(event);
                            }
                        } else if (event.getMessage().getContentRaw().startsWith(Main.prefix)) {
                            processCommand(event);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        final String langCode = Localizations.getGuildLanguage(event.getGuild());
                        MessageSender.send(Localizations.getString("error_title", langCode), Localizations.getString("error_text", langCode) + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber(), event.getMessage(), Color.red, langCode, null);
                    }
                }
            }
        }
    }

    public void onSlashCommand(SlashCommandEvent event) {
        final String commandName = event.getName();
        final String arg0 = event.getSubcommandName();
        final List<OptionMapping> options = event.getOptions();
        if (!event.getUser().isBot()) {
            StringBuilder msg = new StringBuilder("-" + commandName + " " + arg0);
            for (OptionMapping option : options) {
                msg.append(" ");
                switch (option.getType()) {
                    case CHANNEL:
                        msg.append("<#" + option.getAsGuildChannel().getId() + ">");
                        break;
                    case STRING:
                    case INTEGER:
                    case BOOLEAN:
                    case SUB_COMMAND:
                        msg.append(option.getAsString());
                        break;
                    case USER:
                        msg.append("<@!" + option.getAsUser().getId() + ">");
                        break;
                    case ROLE:
                        msg.append("<@&" + option.getAsRole().getId() + ">");
                        break;
                    case MENTIONABLE:
                        msg.append(option.getAsMentionable().getAsMention());
                        break;
                }
            }
            while (msg.toString().contains("  ")) {
                msg = new StringBuilder(msg.toString().replace("  ", " "));
            }

            try {
                CommandHandler.handleCommand(CommandHandler.parse.parse(msg.toString(), event.getMember(), event.getTextChannel(), event.getGuild(), event));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processCommand(GuildMessageReceivedEvent event) {
        try {
            String msg = event.getMessage().getContentRaw();
            while (msg.contains("  ")) {
                msg = msg.replace("  ", " ");
            }
            CommandHandler.handleCommand(CommandHandler.parse.parse(msg, event.getMember(), event.getChannel(), event.getGuild(), null));
        } catch (Exception e) {
            e.printStackTrace();
            final String langCode = Localizations.getGuildLanguage(event.getGuild());
            MessageSender.send(Localizations.getString("error_title", langCode), Localizations.getString("error_text", langCode), event.getMessage(), Color.red, langCode, null);
        }
    }
}
