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

import com.google.cloud.firestore.QueryDocumentSnapshot;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class GroupSlashCommands {

    public static CommandData commandData(Guild guild, Locale langCode) throws ExecutionException, InterruptedException {
        return new CommandData("group", Localizations.getString("slashcommands_description_group", langCode))
                .addSubcommands(new SubcommandData("create", Localizations.getString("slashcommands_description_group_create", langCode))
                        .addOptions(new OptionData(STRING, "name", Localizations.getString("slashcommands_name_of_group_description", langCode))
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("delete", Localizations.getString("slashcommands_description_group_delete", langCode))
                        .addOptions(boardNameOptions(guild, langCode)))
                .addSubcommands(new SubcommandData("add", Localizations.getString("slashcommands_description_group_add", langCode))
                        .addOptions(new OptionData(USER, "user", Localizations.getString("slashcommands_user_to_be_added_to_group_description", langCode)).setRequired(true))
                        .addOptions(boardNameOptions(guild, langCode)))
                .addSubcommands(new SubcommandData("remove", Localizations.getString("slashcommands_description_group_remove", langCode))
                        .addOptions(new OptionData(USER, "user", Localizations.getString("slashcommands_user_to_be_added_to_group_description", langCode)).setRequired(true))
                        .addOptions(boardNameOptions(guild, langCode)))
                .addSubcommands(new SubcommandData("list", Localizations.getString("slashcommands_description_group_list", langCode)))
                .addSubcommands(new SubcommandData("members", Localizations.getString("slashcommands_description_group_members", langCode))
                        .addOptions(boardNameOptions(guild, langCode)))
                .addSubcommands(new SubcommandData("notifications", Localizations.getString("slashcommands_description_group_notifications", langCode))
                        .addOptions(boardNameOptions(guild, langCode))
                        .addOptions(new OptionData(CHANNEL, "channel", Localizations.getString("slashcommands_textchannel_messages_are_send_to_description", langCode)).setRequired(true))
                );
    }

    public static OptionData boardNameOptions(Guild guild, Locale langCode) throws ExecutionException, InterruptedException {
        OptionData groupNameOptionData = new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true);


        for (QueryDocumentSnapshot groupDoc : Main.firestore.collection("server").document(guild.getId()).collection("groups").get().get()) {
            final String groupName = groupDoc.getString("name");
            groupNameOptionData.addChoices(new Command.Choice(groupName, groupName));
        }
        return groupNameOptionData;
    }

}
