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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class PermissionSlashCommands {

    public static CommandData commandData(String langCode) {
        return new CommandData("permission", Localizations.getString("slashcommands_description_permission", langCode))
                .addSubcommands(new SubcommandData("add", Localizations.getString("slashcommands_description_permission_add", langCode))
                        .addOptions(new OptionData(OptionType.MENTIONABLE, "mentionable", Localizations.getString("slashcommands_description_permission_mentionable_description", langCode)).setRequired(true))
                        .addOptions(permissionOptions(langCode)))
                .addSubcommands(new SubcommandData("remove", Localizations.getString("slashcommands_description_permission_remove", langCode))
                        .addOptions(new OptionData(OptionType.MENTIONABLE, "mentionable", Localizations.getString("slashcommands_description_permission_mentionable_description", langCode)).setRequired(true))
                        .addOptions(permissionOptions(langCode)))
                .addSubcommands(new SubcommandData("list", Localizations.getString("slashcommands_description_permission_list", langCode))
                        .addOptions(new OptionData(OptionType.MENTIONABLE, "mentionable", Localizations.getString("slashcommands_description_permission_mentionable_description", langCode)).setRequired(true)));
    }

    public static OptionData permissionOptions(String langCode) {
        return new OptionData(OptionType.STRING, "permission", Localizations.getString("slashcommands_description_permission_permission_description", langCode)).setRequired(true)
                .addChoice("CREATE_TASK", "CREATE_TASK")
                .addChoice("DELETE_TASK", "DELETE_TASK")
                .addChoice("EDIT_TASK", "EDIT_TASK")
                .addChoice("CREATE_GROUP", "CREATE_GROUP")
                .addChoice("DELETE_GROUP", "DELETE_GROUP")
                .addChoice("ADD_MEMBERS", "ADD_MEMBERS")
                .addChoice("REMOVE_MEMBERS", "REMOVE_MEMBERS")
                .addChoice("ADD_PERMISSION", "ADD_PERMISSION")
                .addChoice("REMOVE_PERMISSION", "REMOVE_PERMISSION")
                .addChoice("SHOW_PERMISSIONS", "SHOW_PERMISSIONS")
                .addChoice("CREATE_BOARD", "CREATE_BOARD")
                .addChoice("DELETE_BOARD", "DELETE_BOARD")
                .setRequired(true);
    }

}
