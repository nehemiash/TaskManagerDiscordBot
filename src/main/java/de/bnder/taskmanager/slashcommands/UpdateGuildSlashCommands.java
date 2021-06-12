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

import de.bnder.taskmanager.utils.Localizations;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.Arrays;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class UpdateGuildSlashCommands {

    public static void update(Guild guild) {
        final String langCode = Localizations.getGuildLanguage(guild);

        CommandListUpdateAction cmd = guild.updateCommands();

        cmd.addCommands(Arrays.asList(
                //GROUP COMMANDS
                new CommandData("group", Localizations.getString("slashcommands_description_group", langCode))
                        .addSubcommands(new SubcommandData("create", Localizations.getString("slashcommands_description_group_add", langCode))
                                .addOptions(new OptionData(STRING, "name", Localizations.getString("slashcommands_name_of_group_description", langCode))
                                        .setRequired(true)))
                        .addSubcommands(new SubcommandData("delete", Localizations.getString("slashcommands_description_group_delete", langCode))
                                .addOptions(new OptionData(STRING, "name", Localizations.getString("slashcommands_name_of_group_description", langCode))
                                        .setRequired(true)))
                        .addSubcommands(new SubcommandData("add", Localizations.getString("slashcommands_description_group_add", langCode))
                                .addOptions(new OptionData(USER, "user", Localizations.getString("slashcommands_user_to_be_added_to_group_description", langCode)).setRequired(true))
                                .addOptions(new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true)))
                        .addSubcommands(new SubcommandData("remove", Localizations.getString("slashcommands_description_group_remove", langCode))
                                .addOptions(new OptionData(USER, "user", Localizations.getString("slashcommands_user_to_be_added_to_group_description", langCode)).setRequired(true))
                                .addOptions(new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true)))
                        .addSubcommands(new SubcommandData("list", Localizations.getString("slashcommands_description_group_list", langCode)))
                        .addSubcommands(new SubcommandData("members", Localizations.getString("slashcommands_description_group_members", langCode))
                                .addOptions(new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true)))
                        .addSubcommands(new SubcommandData("notifications", Localizations.getString("slashcommands_description_group_notifications", langCode))
                                .addOptions(new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true))
                                .addOptions(new OptionData(CHANNEL, "channel", Localizations.getString("slashcommands_textchannel_messages_are_send_to_description", langCode)).setRequired(true))
                        ),
                //TASK COMMANDS
                new CommandData("task", Localizations.getString("slashcommands_description_task", langCode))
                        .addSubcommands(new SubcommandData("add", Localizations.getString("slashcommands_description_task_add", langCode))
                                .addOptions(new OptionData(USER, "user", "a").setRequired(true))
                                .addOptions(new OptionData(STRING, "task", "a").setRequired(true)))
                        .addSubcommands(new SubcommandData("proceed", Localizations.getString("slashcommands_description_task_proceed", langCode))
                                .addOptions(new OptionData(STRING, "task-id", "a").setRequired(true)))
                        .addSubcommands(new SubcommandData("deadline", "a")
                                .addOptions(new OptionData(STRING, "task-id", "a").setRequired(true))
                                .addOptions(new OptionData(STRING, "date", "a").setRequired(true))
                                .addOptions(new OptionData(STRING, "time", "a").setRequired(true)))
                        .addSubcommands(new SubcommandData("list", "a")
                                .addOptions(new OptionData(USER, "user", "a").setRequired(true)))
                        .addSubcommands(new SubcommandData("delete", "a")
                                .addOptions(new OptionData(STRING, "task-id", "a").setRequired(true)))
                        .addSubcommands(new SubcommandData("edit", "a")
                                .addOptions(new OptionData(STRING, "task-id", "a").setRequired(true))
                                .addOptions(new OptionData(STRING, "task", "a").setRequired(true)))
                        .addSubcommands(new SubcommandData("info", "a")
                                .addOptions(new OptionData(STRING, "task-id", "a").setRequired(true)))
                        .addSubcommands(new SubcommandData("done", "a")
                                .addOptions(new OptionData(STRING, "task-id", "a").setRequired(true)))
        ));


        try {
            cmd.queue();
        } catch (Exception ignored) {
        }
    }

}
