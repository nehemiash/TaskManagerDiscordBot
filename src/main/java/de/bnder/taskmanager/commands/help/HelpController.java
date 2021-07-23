package de.bnder.taskmanager.commands.help;
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

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.IOException;

public class HelpController implements Command {

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        final String langCode = Localizations.getGuildLanguage(event.getGuild());
        String baseCommand;
        String arg0;
        String localizationsSourceString;
        final String advancedHelpEmbedFieldTitleCommand = Localizations.getString("advanced_help_embed_field_title_command", langCode);
        final String advancedHelpEmbedFieldTitleExample = Localizations.getString("advanced_help_embed_field_title_example", langCode);
        final String advancedHelpEmbedFieldTitleDescription = Localizations.getString("advanced_help_embed_field_title_description", langCode);
        if (args.length == 0) {
            GeneralCommandOverview.sendGeneralCommandOverview(event.getChannel(), event.getMessage().getContentRaw(), langCode);
            return;
        } else if (args.length == 1) {
            baseCommand = args[0];
            localizationsSourceString = "advanced_help_command_" + baseCommand.toLowerCase();
        } else {
            baseCommand = args[0];
            arg0 = args[1].toLowerCase().replaceAll("-", "").replaceAll("_", "");
            localizationsSourceString = "advanced_help_command_" + baseCommand.toLowerCase() + "_" + arg0;
        }
        if (!Localizations.getString(localizationsSourceString + "_usage", langCode).equals(localizationsSourceString + "_usage")) {
            final EmbedBuilder embed = new EmbedBuilder().setColor(Color.cyan).setTitle(Localizations.getString("help_message_title", langCode));
            embed.addField(advancedHelpEmbedFieldTitleCommand, event.getMessage().getContentRaw().charAt(0) + Localizations.getString(localizationsSourceString + "_usage", langCode), true);
            embed.addField(advancedHelpEmbedFieldTitleExample, event.getMessage().getContentRaw().charAt(0) + Localizations.getString(localizationsSourceString + "_example", langCode), true);
            embed.addField(advancedHelpEmbedFieldTitleDescription, Localizations.getString(localizationsSourceString + "_description", langCode), false);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        } else {
            MessageSender.send(Localizations.getString("help_message_title", langCode), Localizations.getString("advanced_help_command_not_supported", langCode), event.getMessage(), Color.red, langCode);
        }
    }
}
