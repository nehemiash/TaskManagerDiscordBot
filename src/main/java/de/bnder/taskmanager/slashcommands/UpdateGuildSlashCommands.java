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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.Arrays;
import java.util.Locale;

public class UpdateGuildSlashCommands {

    public static void update(Guild guild) {
        try {
            final Locale langCode = Localizations.getGuildLanguage(guild);

            CommandListUpdateAction cmd = guild.updateCommands();

            cmd.addCommands(Arrays.asList(
                    GroupSlashCommands.commandData(guild, langCode),
                    TaskSlashCommands.commandData(langCode),
                    BoardSlashCommands.commandData(guild, langCode),
                    PermissionSlashCommands.commandData(langCode),
                    SettingsSlashCommands.commandData(langCode),
                    new CommandData("help", Localizations.getString("slashcommands_description_help", langCode)).addOptions(new OptionData(OptionType.STRING, "command", Localizations.getString("slashcommands_description_help_command", langCode)).setRequired(false)),
                    new CommandData("app", Localizations.getString("slashcommands_description_app", langCode)),
                    new CommandData("invite", Localizations.getString("slashcommands_description_invite", langCode)),
                    new CommandData("language", Localizations.getString("slashcommands_description_language", langCode))
                            .addOptions(new OptionData(OptionType.STRING, "language", "language").setRequired(true)
                                    .addChoice("de", "de").addChoice("en", "en").addChoice("bg", "bg").addChoice("tr", "tr")
                                    .addChoice("fr", "fr").addChoice("ru", "ru").addChoice("pl", "pl")),
                    new CommandData("prefix", Localizations.getString("slashcommands_description_prefix", langCode)).addOptions(new OptionData(OptionType.STRING, "prefix", "prefix").setRequired(true)),
                    new CommandData("search", Localizations.getString("slashcommands_description_search", langCode)).addOptions(new OptionData(OptionType.STRING, "term", "search term")),
                    new CommandData("stats", Localizations.getString("slashcommands_description_stats", langCode)),
                    new CommandData("support", Localizations.getString("slashcommands_description_support", langCode)),
                    new CommandData("version", Localizations.getString("slashcommands_description_version", langCode))
            ));
            cmd.queue((cmds) -> {
                System.out.println("Updated Slash Commands on Guild " + guild.getName());
            }, (error) -> {
                System.out.println("Updating Slash Commands on " + guild.getName() + " failed!");
            });
        } catch (Exception ignored) {
            System.out.println("Updating Slash Commands on " + guild.getName() + " failed!");
        }
    }

}
