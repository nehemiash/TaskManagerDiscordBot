package de.bnder.taskmanager.commands;
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
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class Settings implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        final String langCode = Localizations.Companion.getGuildLanguage(event.getGuild());
        final String embedTitle = Localizations.Companion.getString("settings_title", langCode);
        if (args.length == 1) {
            final String arg0 = args[0].replaceAll("-", "").replaceAll("_", "");
            if (arg0.equalsIgnoreCase("directmessage")) {
                final String jsonResponse = Jsoup.connect(Main.requestURL + "updateSettings.php?requestToken=" + Main.requestToken + "&user_id=" + Connection.encodeString(event.getAuthor().getId()) + "&source=" + Connection.encodeString("direct_message") + "&default=0" + "&guild_id=" + Connection.encodeString(event.getGuild().getId())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                final int statusCode = jsonObject.getInt("status_code", 900);
                if (statusCode == 200) {
                    final int newValue = jsonObject.getInt("newValue", -1);
                    if (newValue == 1) {
                        MessageSender.send(embedTitle, Localizations.Companion.getString("settings_dm_enabled", langCode), event.getMessage(), Color.green);
                    } else if (newValue == 0) {
                        MessageSender.send(embedTitle, Localizations.Companion.getString("settings_dm_disabled", langCode), event.getMessage(), Color.green);
                    } else {
                        MessageSender.send(embedTitle, Localizations.Companion.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {{
                            add("SETTINGS-1-" + newValue);
                        }}), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.Companion.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {{
                        add("SETTINGS-1.1-" + statusCode);
                    }}), event.getMessage(), Color.red);
                }
            } else if (arg0.equalsIgnoreCase("showdonetasks")) {
                final String jsonResponse = Jsoup.connect(Main.requestURL + "updateSettings.php?requestToken=" + Main.requestToken + "&user_id=" + Connection.encodeString(event.getAuthor().getId()) + "&source=" + Connection.encodeString("show_done_tasks") + "&default=1" + "&guild_id=" + Connection.encodeString(event.getGuild().getId())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                final int statusCode = jsonObject.getInt("status_code", 900);
                if (statusCode == 200) {
                    final int newValue = jsonObject.getInt("newValue", -1);
                    if (newValue == 1) {
                        MessageSender.send(embedTitle, Localizations.Companion.getString("settings_show_done_tasks_enabled", langCode), event.getMessage(), Color.green);
                    } else if (newValue == 0) {
                        MessageSender.send(embedTitle, Localizations.Companion.getString("settings_show_done_tasks_disabled", langCode), event.getMessage(), Color.green);
                    } else {
                        MessageSender.send(embedTitle, Localizations.Companion.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {{
                            add("SETTINGS-2-" + newValue);
                        }}), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.Companion.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {{
                        add("SETTINGS-2.1-" + statusCode);
                    }}), event.getMessage(), Color.red);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.Companion.getString("settings_invalid_arg", langCode), event.getMessage(), Color.red);
            }
        } else if (args.length == 0) {
            final JsonObject userSettings = de.bnder.taskmanager.utils.Settings.getUserSettings(event.getMember());
            System.out.println(userSettings.toString());
            final EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.cyan).setTimestamp(Calendar.getInstance().toInstant()).setTitle(embedTitle + " - " + event.getAuthor().getAsTag());
            if (userSettings.getString("direct_message", "1").equals("1")) {
                embedBuilder.addField(Localizations.Companion.getString("settings_list_direct_message", langCode), Localizations.Companion.getString("settings_list_enabled", langCode), false);
            } else {
                embedBuilder.addField(Localizations.Companion.getString("settings_list_direct_message", langCode), Localizations.Companion.getString("settings_list_disabled", langCode), false);
            }
            if (userSettings.getString("show_done_tasks", "1").equals("1")) {
                embedBuilder.addField(Localizations.Companion.getString("settings_list_show_done_tasks", langCode), Localizations.Companion.getString("settings_list_enabled", langCode), false);
            } else {
                embedBuilder.addField(Localizations.Companion.getString("settings_list_show_done_tasks", langCode), Localizations.Companion.getString("settings_list_disabled", langCode), false);
            }
            if (userSettings.get("notify_channel") != null && !userSettings.get("notify_channel").isNull() && event.getGuild().getTextChannelById(userSettings.getString("notify_channel", "123")) != null) {
                embedBuilder.addField(Localizations.Companion.getString("settings_list_notify_channel", langCode), event.getGuild().getTextChannelById(userSettings.getString("notify_channel", "123")).getAsMention(), false);
            } else {
                embedBuilder.addField(Localizations.Companion.getString("settings_list_notify_channel", langCode), "---", false);
            }
            embedBuilder.setDescription(Localizations.Companion.getString("settings_invalid_arg", langCode));
            event.getChannel().sendMessage(embedBuilder.build()).queue();
        } else if (args.length == 2) {
            final String arg0 = args[0].replaceAll("-", "").replaceAll("_", "");
            if (arg0.equalsIgnoreCase("notifychannel")) {
                if (event.getMessage().getMentionedChannels().size() == 1) {
                    final TextChannel channel = event.getMessage().getMentionedChannels().get(0);
                    final String jsonResponse = Jsoup.connect(Main.requestURL + "updateSettings.php?requestToken=" + Main.requestToken + "&default=0" +  "&user_id=" + Connection.encodeString(event.getAuthor().getId()) + "&source=" + Connection.encodeString("notify_channel") + "&value=" + Connection.encodeString(channel.getId()) + "&guild_id=" + Connection.encodeString(event.getGuild().getId())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
                    final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                    final int statusCode = jsonObject.getInt("status_code", 900);
                    if (statusCode == 200) {
                        if (!de.bnder.taskmanager.utils.Settings.getUserSettings(event.getMember()).getString("direct_message", "1").equals("0")) {
                            MessageSender.send(embedTitle, Localizations.Companion.getString("notify_channel_set_but_dms_are_enabled", langCode, new ArrayList<String>(){{
                                add(channel.getAsMention());
                            }}), event.getMessage(), Color.green);
                        } else {
                            MessageSender.send(embedTitle, Localizations.Companion.getString("notify_channel_set", langCode, new ArrayList<String>(){{
                                add(channel.getAsMention());
                            }}), event.getMessage(), Color.green);
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.Companion.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {{
                            add("SETTINGS-3-" + statusCode);
                        }}), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.Companion.getString("notify_mention_one_channel", langCode), event.getMessage(), Color.red);
                }
            }else {
                MessageSender.send(embedTitle, Localizations.Companion.getString("settings_invalid_arg", langCode), event.getMessage(), Color.red);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.Companion.getString("settings_invalid_arg", langCode), event.getMessage(), Color.red);
        }
    }
}
