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
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;

import java.util.Arrays;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class UpdateGuildSlashCommands {

    public static void update(Guild guild) {
        final String langCode = Localizations.getGuildLanguage(guild);

        CommandUpdateAction cmd = guild.updateCommands();

        cmd.addCommands(Arrays.asList(
                //GROUP COMMANDS
                new CommandData("group", Localizations.getString("slashcommands_description_group", langCode))
                        .addSubcommand(new SubcommandData("create", Localizations.getString("slashcommands_description_group_add", langCode))
                                .addOption(new OptionData(STRING, "name", Localizations.getString("slashcommands_name_of_group_description", langCode))
                                        .setRequired(true)))
                        .addSubcommand(new SubcommandData("delete", Localizations.getString("slashcommands_description_group_delete", langCode))
                                .addOption(new OptionData(STRING, "name", Localizations.getString("slashcommands_name_of_group_description", langCode))
                                        .setRequired(true)))
                        .addSubcommand(new SubcommandData("add", Localizations.getString("slashcommands_description_group_add", langCode))
                                .addOption(new OptionData(USER, "user", Localizations.getString("slashcommands_user_to_be_added_to_group_description", langCode)).setRequired(true))
                                .addOption(new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true)))
                        .addSubcommand(new SubcommandData("remove", Localizations.getString("slashcommands_description_group_remove", langCode))
                                .addOption(new OptionData(USER, "user", Localizations.getString("slashcommands_user_to_be_added_to_group_description", langCode)).setRequired(true))
                                .addOption(new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true)))
                        .addSubcommand(new SubcommandData("list", Localizations.getString("slashcommands_description_group_list", langCode)))
                        .addSubcommand(new SubcommandData("members", Localizations.getString("slashcommands_description_group_members", langCode))
                                .addOption(new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true)))
                        .addSubcommand(new SubcommandData("notifications", Localizations.getString("slashcommands_description_group_notifications", langCode))
                                .addOption(new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true))
                                .addOption(new OptionData(CHANNEL, "channel", Localizations.getString("slashcommands_textchannel_messages_are_send_to_description", langCode)).setRequired(true))
                        ),
                //TASK COMMANDS
                new CommandData("task", Localizations.getString("slashcommands_description_task", langCode))
                        .addSubcommand(new SubcommandData("add", Localizations.getString("slashcommands_description_task_add", langCode))
                                .addOption(new OptionData(USER, "user", "").setRequired(true))
                                .addOption(new OptionData(STRING, "task", "").setRequired(true)))
                        .addSubcommand(new SubcommandData("proceed", Localizations.getString("slashcommands_description_task_proceed", langCode))
                                .addOption(new OptionData(STRING, "task-id", "").setRequired(true)))
                        .addSubcommand(new SubcommandData("deadline", "")
                                .addOption(new OptionData(STRING, "task-id", "").setRequired(true))
                                .addOption(new OptionData(STRING, "date", "").setRequired(true))
                                .addOption(new OptionData(STRING, "time", "").setRequired(true)))
                        .addSubcommand(new SubcommandData("list", "")
                                .addOption(new OptionData(USER, "user", "").setRequired(true)))
                        .addSubcommand(new SubcommandData("delete", "")
                                .addOption(new OptionData(STRING, "task-id", "").setRequired(true)))
                        .addSubcommand(new SubcommandData("edit", "")
                                .addOption(new OptionData(STRING, "task-id", "").setRequired(true))
                                .addOption(new OptionData(STRING, "task", "").setRequired(true)))
                        .addSubcommand(new SubcommandData("info", "")
                                .addOption(new OptionData(STRING, "task-id", "").setRequired(true)))
                        .addSubcommand(new SubcommandData("done", "")
                                .addOption(new OptionData(STRING, "task-id", "").setRequired(true)))
        ));


        try {
            cmd.queue();
        } catch (Exception ignored) {
        }
    }

}
