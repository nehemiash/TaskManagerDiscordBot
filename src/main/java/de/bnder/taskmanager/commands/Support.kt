package de.bnder.taskmanager.commands

import de.bnder.taskmanager.main.Command
import de.bnder.taskmanager.utils.Localizations
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

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
class Support : Command {
    override fun action(args: Array<String>, event: GuildMessageReceivedEvent) {
        event.channel.sendMessage(Localizations.getString("support_nachricht", Localizations.getGuildLanguage(event.guild))).queue()
    }
}