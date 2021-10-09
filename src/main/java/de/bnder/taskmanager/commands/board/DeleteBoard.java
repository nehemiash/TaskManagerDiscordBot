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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class DeleteBoard {

    public static void deleteBoard(@NotNull Member member, TextChannel textChannel, String boardName, SlashCommandEvent slashCommandEvent) {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("board_title", langCode);

        //User has permission DELETE_BOARD
        if (!PermissionSystem.hasPermission(member, BoardPermission.DELETE_BOARD)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        //Board with name "boardName" doesn't exist
        if (!CreateBoard.boardExists(boardName, textChannel.getGuild().getId())) {
            MessageSender.send(embedTitle, Localizations.getString("board_with_name_doesnt_exist", langCode, new ArrayList<>() {
                {
                    add(boardName);
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        try {
            deleteBoardFromFirestore(boardName, textChannel.getGuild().getId());
            MessageSender.send(embedTitle, Localizations.getString("board_was_deleted", langCode, new ArrayList<>() {
                {
                    add(boardName);
                }
            }), textChannel, Color.green, langCode, slashCommandEvent);
            UpdateGuildSlashCommands.update(member.getGuild());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            MessageSender.send(embedTitle, Localizations.getString("board_delete_unknown_error", langCode, new ArrayList<>() {
                {
                    add(boardName);
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

    /** Deletes board with specific name from guild with ID.
     *
     * @param boardName The Name of the Board that should be deleted.
     * @param guildID The ID of the Guild where the Board is.
     * @throws ExecutionException If Firestore throws an error.
     * @throws InterruptedException If Firestore throws an error.
     */
    static void deleteBoardFromFirestore(String boardName, String guildID) throws ExecutionException, InterruptedException {
        Main.firestore.collection("server").document(guildID).collection("boards").whereEqualTo("name", boardName).get().get().getDocuments().get(0).getReference().delete();
    }

}
