package de.bnder.taskmanager.utils;
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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Component;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Calendar;
import java.util.Locale;

public class MessageSender {

    /**
     * Prepares a message and passes it to buildMessageBuilder, where the message will be sent.
     *
     * @param title             The message title.
     * @param text              The message text or content.
     * @param message           A message sent by a server member.
     * @param color             The embed color.
     * @param locale            The locale for the text.
     * @param slashCommandEvent The SlashCommandEvent if a user executed a slash command.
     */
    public static void send(final String title, String text, final Message message, final Color color, final Locale locale, @Nullable final SlashCommandEvent slashCommandEvent) {
        if (text.length() > 1990) {
            while (text.length() > 1990) {
                String textNow = text.substring(0, 1990);
                buildMessageBuilder(title, message, color, textNow, locale, slashCommandEvent);
                text = text.substring(1990);
            }
            if (text.length() > 0) {
                buildMessageBuilder(title, message, color, text, locale, slashCommandEvent);
            }
        } else {
            buildMessageBuilder(title, message, color, text, locale, slashCommandEvent);
        }
    }

    /**
     * Prepares a message and passes it to buildMessageBuilder, where the message will be sent.
     *
     * @param title             The message title.
     * @param text              The message text or content.
     * @param textChannel       The textchannel where the response will be sent in.
     * @param color             The embed color.
     * @param locale            The locale for the text.
     * @param slashCommandEvent The SlashCommandEvent if a user executed a slash command.
     */
    public static void send(final String title, String text, final TextChannel textChannel, final Color color, final Locale locale, @Nullable final SlashCommandEvent slashCommandEvent) {
        if (text.length() > 1990) {
            while (text.length() > 1990) {
                String textNow = text.substring(0, 1990);
                buildMessageBuilder(title, textChannel, color, textNow, locale, slashCommandEvent);
                text = text.substring(1990);
            }
            if (text.length() > 0) {
                buildMessageBuilder(title, textChannel, color, text, locale, slashCommandEvent);
            }
        } else {
            buildMessageBuilder(title, textChannel, color, text, locale, slashCommandEvent);
        }
    }

    /**
     * Prepares a message and passes it to buildMessageBuilder, where the message will be sent.
     *
     * @param title             The message title.
     * @param text              The message text or content.
     * @param textChannel       The textchannel where the response will be sent in.
     * @param color             The embed color.
     * @param locale            The locale for the text.
     * @param slashCommandEvent The SlashCommandEvent if a user executed a slash command.
     * @param components        Components for the SlashCommandEvent reply.
     */
    public static void send(final String title, String text, final TextChannel textChannel, final Color color, final Locale locale, @Nullable final SlashCommandEvent slashCommandEvent, Component... components) {
        if (text.length() > 1990) {
            while (text.length() > 1990) {
                String textNow = text.substring(0, 1990);
                buildMessageBuilder(title, textChannel, color, textNow, locale, slashCommandEvent, components);
                text = text.substring(1990);
            }
            if (text.length() > 0) {
                buildMessageBuilder(title, textChannel, color, text, locale, slashCommandEvent, components);
            }
        } else {
            buildMessageBuilder(title, textChannel, color, text, locale, slashCommandEvent, components);
        }
    }

    /**
     * Builds the message embed and sends it in the textchannel of message or replys to a SlashCommandEvent if not null.
     *
     * @param title             The message title.
     * @param message           A message a server member sent before.
     * @param color             The embed color.
     * @param text              The message text or content.
     * @param locale            The locale for the text.
     * @param slashCommandEvent The SlashCommandEvent if a user executed a slash command.
     */
    private static void buildMessageBuilder(final String title, final Message message, final Color color, final String text, final Locale locale, @Nullable final SlashCommandEvent slashCommandEvent) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(text);
        builder.setColor(color);
        builder.setTimestamp(Calendar.getInstance().toInstant());
        if (color != Color.red) setAdFooter(builder, locale);
        if (slashCommandEvent == null) {
            if (message.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
                message.getChannel().sendMessageEmbeds(builder.build()).queue();
            } else {
                message.getChannel().sendMessage("**" + title + "**\n" + text).queue();
            }
        } else {
            slashCommandEvent.reply("**" + title + "**\n" + text).queue();
        }
        try {
            Main.tmbAPI("stats/messages-sent", null, org.jsoup.Connection.Method.POST).execute();
        } catch (Exception ignored) {
        }
    }


    /**
     * Adds a footer with an ad message to an EmbedBuilder
     *
     * @param builder The EmbedBuilder the footer will be added to.
     * @param locale  The locale of the ad message.
     */
    private static void setAdFooter(final EmbedBuilder builder, final Locale locale) {
        final int randomNumber = getRandomInteger(4, 0);
        if (randomNumber == 1) {
            builder.setFooter(Localizations.getString("donate_alert_paypal", locale));
        } else if (randomNumber == 2) {
            builder.setFooter(Localizations.getString("donate_alert_buymeacoffe", locale));
        } else if (randomNumber == 3) {
            builder.setFooter(Localizations.getString("donate_alert_brave", locale));
        } else {
            builder.setFooter(Localizations.getString("donate_alert", locale));
        }
    }

    /**
     * Returns a random integer between minimum and maximum range.
     *
     * @param maximum Maximum value.
     * @param minimum Minimum value.
     */
    private static int getRandomInteger(int maximum, int minimum) {
        return ((int) (Math.random() * (maximum - minimum))) + minimum;
    }

    /**
     * Builds the message embed and sends it in the textchannel of message or replys to a SlashCommandEvent if not null.
     *
     * @param title             The message title.
     * @param textChannel       The textchannel the messsage will be sent in.
     * @param color             The embed color.
     * @param text              The message text or content.
     * @param locale            The locale for the text.
     * @param slashCommandEvent The SlashCommandEvent if a user executed a slash command.
     */
    private static void buildMessageBuilder(final String title, final TextChannel textChannel, final Color color, final String text, final Locale locale, @Nullable final SlashCommandEvent slashCommandEvent) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(text);
        builder.setColor(color);
        builder.setTimestamp(Calendar.getInstance().toInstant());
        if (color != Color.red) setAdFooter(builder, locale);
        if (slashCommandEvent == null) {
            if (textChannel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
                textChannel.sendMessageEmbeds(builder.build()).queue();
            } else {
                textChannel.sendMessage("**" + title + "**\n" + text).queue();
            }
        } else {
            slashCommandEvent.reply("**" + title + "**\n" + text).queue();
        }
        try {
            Main.tmbAPI("stats/messages-sent", null, org.jsoup.Connection.Method.POST).execute();
        } catch (Exception ignored) {
        }
    }

    /**
     * Builds the message embed and sends it in the textchannel of message or replys to a SlashCommandEvent if not null.
     *
     * @param title             The message title.
     * @param textChannel       The textchannel the messsage will be sent in.
     * @param color             The embed color.
     * @param text              The message text or content.
     * @param locale            The locale for the text.
     * @param slashCommandEvent The SlashCommandEvent if a user executed a slash command.
     * @param components        ActionRows for the SlashCommandEvent reply.
     */
    private static void buildMessageBuilder(final String title, final TextChannel textChannel, final Color color, final String text, final Locale locale, final SlashCommandEvent slashCommandEvent, Component... components) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(text);
        builder.setColor(color);
        builder.setTimestamp(Calendar.getInstance().toInstant());
        if (color != Color.red) setAdFooter(builder, locale);
        if (slashCommandEvent == null) {
            if (textChannel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
                textChannel.sendMessageEmbeds(builder.build()).queue();
            } else {
                textChannel.sendMessage("**" + title + "**\n" + text).queue();
            }
        } else {
            slashCommandEvent.reply("**" + title + "**\n" + text).addActionRow(components).queue();
        }
        try {
            Main.tmbAPI("stats/messages-sent", null, org.jsoup.Connection.Method.POST).execute();
        } catch (Exception ignored) {
        }
    }
}
