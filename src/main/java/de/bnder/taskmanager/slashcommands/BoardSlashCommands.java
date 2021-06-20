package de.bnder.taskmanager.slashcommands;
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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.io.IOException;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class BoardSlashCommands {

    public static CommandData commandData(Guild guild, String langCode) throws IOException {
        return new CommandData("board", Localizations.getString("slashcommands_description_board", langCode))
                .addSubcommands(new SubcommandData("create", Localizations.getString("slashcommands_description_board_create", langCode))
                        .addOptions(new OptionData(STRING, "name", Localizations.getString("slashcommands_description_board_name", langCode)).setRequired(true)))
                .addSubcommands(new SubcommandData("switch", Localizations.getString("slashcommands_description_board_switch", langCode))
                        .addOptions(boardNameOptions(guild, langCode)))
                .addSubcommands(new SubcommandData("delete", Localizations.getString("slashcommands_description_board_delete", langCode))
                        .addOptions(boardNameOptions(guild, langCode).setRequired(true)))
                .addSubcommands(new SubcommandData("list", Localizations.getString("slashcommands_description_board_list", langCode)));
    }

    public static OptionData boardNameOptions(Guild guild, String langCode) throws IOException {
        OptionData switchSubCommandDataOptionData = new OptionData(STRING, "name", Localizations.getString("slashcommands_description_board_name", langCode)).setRequired(true);

        final org.jsoup.Connection.Response res = Main.tmbAPI("board/list/" + guild.getId(), "---", org.jsoup.Connection.Method.GET).execute();
        final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
        final int statusCode = res.statusCode();
        if (statusCode == 200) {
            final JsonArray boardArray = jsonObject.get("boards").asArray();
            if (boardArray.size() > 0) {
                for (int i = 0; i < boardArray.size(); i++) {
                    final JsonObject board = boardArray.get(i).asObject();
                    final String boardName = board.getString("name", null);
                    if (boardName != null) {
                        switchSubCommandDataOptionData.addChoices(new Command.Choice(boardName, boardName));
                    }
                }
            }
        }
        switchSubCommandDataOptionData.addChoices(new Command.Choice("default", "default"));
        return switchSubCommandDataOptionData;
    }

}
