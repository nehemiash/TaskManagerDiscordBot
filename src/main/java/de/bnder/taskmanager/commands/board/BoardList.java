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

import com.google.cloud.firestore.QueryDocumentSnapshot;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BoardList {

    public static void getBoardList(Member member, TextChannel textChannel, String prefix, SlashCommandEvent slashCommandEvent) {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("board_title", langCode);

        try {
            final List<QueryDocumentSnapshot> boardDocs = Main.firestore.collection("server").document(textChannel.getGuild().getId()).collection("boards").orderBy("name").get().get().getDocuments();

            //No boards on the guild
            if (boardDocs.size() == 0) {
                MessageSender.send(embedTitle, Localizations.getString("no_boards_on_server", langCode, new ArrayList<>() {{
                    add(prefix);
                }}), textChannel, Color.red, langCode, slashCommandEvent);
                return;
            }

            final StringBuilder stringBuilder = new StringBuilder();
            for (QueryDocumentSnapshot boardDoc : boardDocs) {
                final String boardName = boardDoc.getString("name");
                //TODO: GET ACTICE BOARD OF THE USER
                final boolean isActive = false;

                stringBuilder.append("- `").append(boardName).append("`");
                if (isActive) stringBuilder.append(" ✅");
                stringBuilder.append("\n");
            }
            MessageSender.send(embedTitle, "- `default` " + (stringBuilder.toString().contains("✅") ? "\n" : "✅\n") + stringBuilder, textChannel, Color.green, langCode, slashCommandEvent);
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
