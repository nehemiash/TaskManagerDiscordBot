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

import com.google.cloud.firestore.QuerySnapshot;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CreateBoard {

    public static void createBoard(String boardName, TextChannel textChannel, Member member, SlashCommandEvent slashCommandEvent) {
        final Guild guild = textChannel.getGuild();
        final String langCode = Localizations.getGuildLanguage(guild);
        final String embedTitle = Localizations.getString("board_title", langCode);

        //User doesn't have CREATE_BOARD permission
        if (!PermissionSystem.hasPermission(member, BoardPermission.CREATE_BOARD)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        //Board with name "boardName" already exists
        if (boardExists(boardName, textChannel.getGuild().getId()) || boardName.equals("default")) {
            MessageSender.send(embedTitle, Localizations.getString("board_not_created_name_already_exists", langCode, new ArrayList<>() {
                {
                    add(boardName);
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        addNewBoardToFirestore(boardName, textChannel.getGuild().getId());
        MessageSender.send(embedTitle, Localizations.getString("board_created_successfully", langCode, new ArrayList<>() {
            {
                add(boardName);
            }
        }), textChannel, Color.green, langCode, slashCommandEvent);
        UpdateGuildSlashCommands.update(member.getGuild());
    }

    /**
     * Add a board to Firestore.
     *
     * @param boardName The name of the board that should be created.
     * @param guildID   The ID of the guild where the board should be created.
     */
    static void addNewBoardToFirestore(String boardName, String guildID) {
        final Map<String, Object> boardMap = new HashMap<>();
        boardMap.put("name", boardName);
        Main.firestore.collection("server").document(guildID).collection("boards")
                .add(boardMap);
    }

    /**
     * Check if a board with name "boardName" exists on a guild with specified ID.
     *
     * @param boardName The name of the board which could exist on a guild.
     * @param guildID   The ID of the Guild where the board existance will be checked.
     * @return true if the board exits on the guild.
     */
    public static boolean boardExists(String boardName, String guildID) {
        try {
            QuerySnapshot a = Main.firestore.collection("server").document(guildID).collection("boards").whereEqualTo("name", boardName).get().get();
            if (a.size() > 0) {
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }

}
