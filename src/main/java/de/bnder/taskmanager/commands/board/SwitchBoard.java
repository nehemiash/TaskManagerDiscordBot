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

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class SwitchBoard {

    public static void switchBoard(String boardName, Member member, TextChannel textChannel, SlashCommandEvent slashCommandEvent) {
        final Guild guild = textChannel.getGuild();
        final Locale langCode = Localizations.getGuildLanguage(guild);
        final String embedTitle = Localizations.getString("board_title", langCode);

        try {
            String boardID = "default";
            if (!boardName.equals("default")) {
                final QuerySnapshot getBoards = Main.firestore.collection("server").document(member.getGuild().getId()).collection("boards").whereEqualTo("name", boardName).get().get();
                if (getBoards.size() == 0) {
                    MessageSender.send(embedTitle, Localizations.getString("board_not_found", langCode, new ArrayList<>() {
                        {
                            add(boardName);
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                    return;
                }
                final QueryDocumentSnapshot boardDoc = getBoards.getDocuments().get(0);
                boardID = boardDoc.getId();
            }
            final DocumentSnapshot getUserDoc = Main.firestore.collection("server").document(member.getGuild().getId())
                    .collection("server_member")
                    .document(member.getId())
                    .get().get();
            if (getUserDoc.exists()) {
                String finalBoardID = boardID;
                getUserDoc.getReference().update(new HashMap<>() {{
                    put("active_board_id", finalBoardID);
                }});
            } else {
                String finalBoardID = boardID;
                getUserDoc.getReference().set(new HashMap<>() {{
                    put("active_board_id", finalBoardID);
                }});
            }
            MessageSender.send(embedTitle, Localizations.getString("board_activated_successfully", langCode, new ArrayList<>() {
                {
                    add(boardName);
                }
            }), textChannel, Color.green, langCode, slashCommandEvent);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<>() {
                {
                    add("901");
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
