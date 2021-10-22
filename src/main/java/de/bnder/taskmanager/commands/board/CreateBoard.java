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

import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.slashcommands.UpdateGuildSlashCommands;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.BoardPermission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jsoup.Connection;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class CreateBoard {

    public static void createBoard(String boardName, TextChannel textChannel, Member member, SlashCommandEvent slashCommandEvent) throws IOException {
        final Guild guild = textChannel.getGuild();
        final Locale langCode = Localizations.getGuildLanguage(guild);
        final String embedTitle = Localizations.getString("board_title", langCode);
        if (PermissionSystem.hasPermission(member, BoardPermission.CREATE_BOARD)) {
            final org.jsoup.Connection.Response res = Main.tmbAPI("board/create/" + member.getGuild().getId(), member.getId(), Connection.Method.POST).data("board_name", boardName).execute();
            final int statusCode = res.statusCode();
            if (statusCode == 200) {
                MessageSender.send(embedTitle, Localizations.getString("board_created_successfully", langCode, new ArrayList<String>() {
                    {
                        add(boardName);
                    }
                }), textChannel, Color.green, langCode, slashCommandEvent);
                UpdateGuildSlashCommands.update(member.getGuild());
            } else if (statusCode == 400) {
                MessageSender.send(embedTitle, Localizations.getString("board_not_created_name_already_exists", langCode, new ArrayList<String>() {
                    {
                        add(boardName);
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            } else {
                MessageSender.send(embedTitle, Localizations.getString("board_not_created_unknown_error", langCode, new ArrayList<String>() {
                    {
                        add(boardName);
                        add(String.valueOf(statusCode));
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
