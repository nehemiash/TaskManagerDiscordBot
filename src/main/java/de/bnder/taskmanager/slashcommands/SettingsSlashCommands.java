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

import java.util.Locale;

public class SettingsSlashCommands {

    public static CommandData commandData(Locale langCode) {
        return new CommandData("settings", Localizations.getString("slashcommands_description_settings", langCode))
                .addSubcommands(new SubcommandData("direct-message", Localizations.getString("slashcommands_description_settings_direct_message", langCode)))
                .addSubcommands(new SubcommandData("show-done-tasks", Localizations.getString("slashcommands_description_settings_direct_message", langCode)))
                .addSubcommands(new SubcommandData("notify-channel", Localizations.getString("slashcommands_description_settings_notify_channel", langCode))
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", Localizations.getString("slashcommands_description_settings_channel", langCode))
                                .setRequired(true)))
                .addSubcommands(new SubcommandData("notifications", Localizations.getString("slashcommands_description_settings_notifications", langCode))
                        .addOptions(new OptionData(OptionType.USER, "user", Localizations.getString("slashcommands_description_settings_user", langCode)).setRequired(true))
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", Localizations.getString("slashcommands_description_settings_channel", langCode)).setRequired(true)));
    }

}
