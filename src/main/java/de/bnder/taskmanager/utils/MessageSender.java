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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Component;

import java.awt.*;
import java.util.Calendar;

public class MessageSender {
    public static void send(String title, String s, Message msg, Color red, final String langCode, SlashCommandEvent slashCommandEvent) {
        if (s.length() > 1990) {
            while (s.length() > 1990) {
                String textNow = s.substring(0, 1990);
                buildMessageBuilder(title, msg, red, textNow, langCode, false, slashCommandEvent);
                s = s.substring(1990);
            }
            if (s.length() > 0) {
                buildMessageBuilder(title, msg, red, s, langCode, false, slashCommandEvent);
            }
        } else {
            buildMessageBuilder(title, msg, red, s, langCode, false, slashCommandEvent);
        }
    }

    public static void send(String title, String s, TextChannel textChannel, Color red, final String langCode, SlashCommandEvent slashCommandEvent) {
        if (s.length() > 1990) {
            while (s.length() > 1990) {
                String textNow = s.substring(0, 1990);
                buildMessageBuilder(title, textChannel, red, textNow, langCode, false, slashCommandEvent);
                s = s.substring(1990);
            }
            if (s.length() > 0) {
                buildMessageBuilder(title, textChannel, red, s, langCode, false, slashCommandEvent);
            }
        } else {
            buildMessageBuilder(title, textChannel, red, s, langCode, false, slashCommandEvent);
        }
    }

    public static void send(String title, String s, TextChannel textChannel, Color red, final String langCode, SlashCommandEvent slashCommandEvent, Component... components) {
        if (s.length() > 1990) {
            while (s.length() > 1990) {
                String textNow = s.substring(0, 1990);
                buildMessageBuilder(title, textChannel, red, textNow, langCode, false, slashCommandEvent, components);
                s = s.substring(1990);
            }
            if (s.length() > 0) {
                buildMessageBuilder(title, textChannel, red, s, langCode, false, slashCommandEvent, components);
            }
        } else {
            buildMessageBuilder(title, textChannel, red, s, langCode, false, slashCommandEvent, components);
        }
    }

    public static void send(String title, String s, TextChannel textChannel, Color red, final String langCode, boolean showAd, SlashCommandEvent slashCommandEvent) {
        if (s.length() > 1990) {
            while (s.length() > 1990) {
                String textNow = s.substring(0, 1990);
                buildMessageBuilder(title, textChannel, red, textNow, langCode, showAd, slashCommandEvent);
                s = s.substring(1990);
            }
            if (s.length() > 0) {
                buildMessageBuilder(title, textChannel, red, s, langCode, showAd, slashCommandEvent);
            }
        } else {
            buildMessageBuilder(title, textChannel, red, s, langCode, showAd, slashCommandEvent);
        }
    }

    private static void buildMessageBuilder(String title, Message msg, Color red, String textNow, final String langCode, boolean showAd, SlashCommandEvent slashCommandEvent) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(textNow);
        builder.setColor(red);
        builder.setTimestamp(Calendar.getInstance().toInstant());
        if (red != Color.red && showAd) setAdFooter(builder, langCode);
        if (slashCommandEvent == null) {
            if (msg.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
                msg.getChannel().sendMessageEmbeds(builder.build()).queue();
            }
        } else {
            slashCommandEvent.reply("**" + title + "**\n" + textNow).queue();
        }
        Stats.updateMessagesSent();
    }

    private static void setAdFooter(EmbedBuilder builder, String langCode) {
        final int randomNumber = getRandomInteger(4, 0);
        if (randomNumber == 1) {
            builder.setFooter(Localizations.getString("donate_alert_paypal", langCode));
        } else if (randomNumber == 2) {
            builder.setFooter(Localizations.getString("donate_alert_buymeacoffe", langCode));
        } else if (randomNumber == 3) {
            builder.setFooter(Localizations.getString("donate_alert_brave", langCode));
        } else {
            builder.setFooter(Localizations.getString("donate_alert", langCode));
        }
    }

    /**
     * returns random integer between minimum and maximum range
     */
    private static int getRandomInteger(int maximum, int minimum) {
        return ((int) (Math.random() * (maximum - minimum))) + minimum;
    }

    private static void buildMessageBuilder(String title, TextChannel textChannel, Color red, String textNow, final String langCode, boolean showAd, SlashCommandEvent slashCommandEvent) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(textNow);
        builder.setColor(red);
        builder.setTimestamp(Calendar.getInstance().toInstant());
        if (red != Color.red && showAd) setAdFooter(builder, langCode);
        if (slashCommandEvent == null) {
            if (textChannel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
                textChannel.sendMessageEmbeds(builder.build()).queue();
            }
        } else {
            slashCommandEvent.reply("**" + title + "**\n" + textNow).queue();
        }
        Stats.updateMessagesSent();
    }

    private static void buildMessageBuilder(String title, TextChannel textChannel, Color red, String textNow, final String langCode, boolean showAd, SlashCommandEvent slashCommandEvent, Component... actionRows) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(textNow);
        builder.setColor(red);
        builder.setTimestamp(Calendar.getInstance().toInstant());
        if (red != Color.red && showAd) setAdFooter(builder, langCode);
        if (slashCommandEvent == null) {
            if (textChannel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
                textChannel.sendMessageEmbeds(builder.build()).queue();
            }
        } else {
            slashCommandEvent.reply("**" + title + "**\n" + textNow).addActionRow(actionRows).queue();
        }
        Stats.updateMessagesSent();
    }
}
