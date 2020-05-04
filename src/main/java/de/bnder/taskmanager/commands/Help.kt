package de.bnder.taskmanager.commands

import de.bnder.taskmanager.main.Command
import de.bnder.taskmanager.utils.Localizations
import de.bnder.taskmanager.utils.MessageSender
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color

/*
 * Copyright (C) 2019 Jan Brinkmann
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
class Help : Command {
    override fun action(args: Array<String>, event: GuildMessageReceivedEvent) {
        val langCode = Localizations.getGuildLanguage(event.guild)
        MessageSender.send(Localizations.getString("help_message_title", langCode), Localizations.getString("help_message_group_commands", langCode) + Localizations.getString("help_message_task_commands", langCode) + Localizations.getString("help_message_other_commands", langCode), event.message, Color.cyan)
    }
}