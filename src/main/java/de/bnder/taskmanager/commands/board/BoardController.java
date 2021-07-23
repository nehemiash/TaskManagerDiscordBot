package de.bnder.taskmanager.commands.board;
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

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.LevenshteinDistance;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class BoardController implements Command {

    final ArrayList<String> commandArgs = new ArrayList<>() {{
        add("create");
        add("switch");
        add("delete");
        add("list");
    }};

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("create")) {
                CreateBoard.createBoard(args[1], event.getChannel(), event.getMember());
            } else if (args[0].equalsIgnoreCase("switch")) {
                SwitchBoard.switchBoard(args[1], event.getMember(), event.getChannel());
            } else if (args[0].equalsIgnoreCase("delete")) {
                DeleteBoard.deleteBoard(event.getMember(), event.getChannel(), args[1]);
            } else {
                checkIfTypo(args, event.getMessage());
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                BoardList.getBoardList(event.getMember(), event.getChannel(), String.valueOf(event.getMessage().getContentRaw().charAt(0)));
            } else {
                checkIfTypo(args, event.getMessage());
            }
        } else {
            checkIfTypo(args, event.getMessage());
        }
    }

    void checkIfTypo(String[] args, Message message) {
        final String langCode = Localizations.getGuildLanguage(message.getGuild());
        if (args.length > 0) {
            final String userArg1 = args[0];
            final StringBuilder possibleCommands = new StringBuilder();
            for (String commandArg : commandArgs) {
                final int distance = LevenshteinDistance.levenshteinDistance(commandArg, userArg1);
                if (distance <= 2 && distance != 0) {
                    final StringBuilder correctedMessage = new StringBuilder().append(message.getContentRaw().split(" ")[0]).append(" ");
                    correctedMessage.append(commandArg).append(" ");
                    for (int i = 1; i < args.length; i++) {
                        correctedMessage.append(args[i]).append(" ");
                    }

                    final String correctedMessageString = correctedMessage.substring(0, correctedMessage.length());
                    possibleCommands.append(correctedMessageString);
                    break;
                }
            }
            if (possibleCommands.length() > 0) {
                EmbedBuilder builder = new EmbedBuilder().setColor(Color.orange);
                builder.setTitle(Localizations.getString("typo_title", langCode));
                builder.setDescription(Localizations.getString("typo_description", langCode));
                builder.addField(Localizations.getString("typo_field_command_name", langCode), possibleCommands.substring(0, possibleCommands.length() - 1), true);
                builder.addField(Localizations.getString("typo_field_user_name", langCode), message.getAuthor().getAsTag(), true);
                message.getChannel().sendMessageEmbeds(builder.build()).queue(message1 -> message1.addReaction("✅").and(message1.addReaction("❌")).queue());
            } else {
                final String embedTitle = Localizations.getString("board_title", langCode);
                final String prefix = String.valueOf(message.getContentRaw().charAt(0));
                MessageSender.send(embedTitle, Localizations.getString("help_message_board_commands", langCode, new ArrayList<String>() {{
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                }}), message, Color.red, langCode);
            }
        } else {
            final String embedTitle = Localizations.getString("board_title", langCode);
            final String prefix = String.valueOf(message.getContentRaw().charAt(0));
            MessageSender.send(embedTitle, Localizations.getString("help_message_board_commands", langCode, new ArrayList<String>() {{
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
            }}), message, Color.red, langCode);
        }
    }
}

