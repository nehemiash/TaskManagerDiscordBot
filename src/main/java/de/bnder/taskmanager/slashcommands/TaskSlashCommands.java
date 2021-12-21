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
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Locale;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;
import static net.dv8tion.jda.api.interactions.commands.OptionType.USER;

public class TaskSlashCommands {

    public static CommandData commandData(Locale langCode) {
        return new CommandData("task", Localizations.getString("slashcommands_description_task", langCode))
                .addSubcommands(new SubcommandData("add", Localizations.getString("slashcommands_description_task_add", langCode))
                        .addOptions(new OptionData(USER, "user", Localizations.getString("slashcommands_user_mention_description", langCode)).setRequired(true))
                        .addOptions(new OptionData(STRING, "task", Localizations.getString("slashcommands_task_text_description", langCode)).setRequired(true)))
                .addSubcommands(new SubcommandData("addgroup", Localizations.getString("slashcommands_description_task_add", langCode))
                        .addOptions(new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true))
                        .addOptions(new OptionData(STRING, "task", Localizations.getString("slashcommands_task_text_description", langCode)).setRequired(true)))
                .addSubcommands(new SubcommandData("proceed", Localizations.getString("slashcommands_description_task_proceed", langCode))
                        .addOptions(new OptionData(STRING, "task-id", Localizations.getString("slashcommands_task_id_description", langCode)).setRequired(true)))
                .addSubcommands(new SubcommandData("deadline", Localizations.getString("slashcommands_description_task_deadline", langCode))
                        .addOptions(new OptionData(STRING, "task-id", Localizations.getString("slashcommands_task_id_description", langCode)).setRequired(true))
                        .addOptions(new OptionData(STRING, "date", Localizations.getString("slashcommands_task_deadline_date_description", langCode)).setRequired(true))
                        .addOptions(new OptionData(STRING, "time", Localizations.getString("slashcommands_task_deadline_date_description", langCode)).setRequired(true)))
                .addSubcommands(new SubcommandData("list", Localizations.getString("slashcommands_description_task_list", langCode))
                        .addOptions(new OptionData(USER, "user", Localizations.getString("slashcommands_user_mention_description", langCode)).setRequired(true)))
                .addSubcommands(new SubcommandData("listgroup", Localizations.getString("slashcommands_description_task_list", langCode))
                        .addOptions(new OptionData(STRING, "group", Localizations.getString("slashcommands_name_of_group_description", langCode)).setRequired(true)))
                .addSubcommands(new SubcommandData("delete", Localizations.getString("slashcommands_description_task_delete", langCode))
                        .addOptions(new OptionData(STRING, "task-id", Localizations.getString("slashcommands_task_id_description", langCode)).setRequired(true)))
                .addSubcommands(new SubcommandData("edit", Localizations.getString("slashcommands_description_task_edit", langCode))
                        .addOptions(new OptionData(STRING, "task-id", Localizations.getString("slashcommands_task_id_description", langCode)).setRequired(true))
                        .addOptions(new OptionData(STRING, "task", Localizations.getString("slashcommands_task_text_description", langCode)).setRequired(true)))
                .addSubcommands(new SubcommandData("info", Localizations.getString("slashcommands_description_task_info", langCode))
                        .addOptions(new OptionData(STRING, "task-id", Localizations.getString("slashcommands_task_id_description", langCode)).setRequired(true)))
                .addSubcommands(new SubcommandData("done", Localizations.getString("slashcommands_description_task_done", langCode))
                        .addOptions(new OptionData(STRING, "task-id", Localizations.getString("slashcommands_task_id_description", langCode)).setRequired(true)));
    }

}
