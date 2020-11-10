package de.bnder.taskmanager.commands

import de.bnder.taskmanager.main.Command
import de.bnder.taskmanager.main.Main
import de.bnder.taskmanager.utils.Connection
import de.bnder.taskmanager.utils.Localizations
import de.bnder.taskmanager.utils.MessageSender
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.jsoup.Jsoup
import java.awt.Color

/*
 * Copyright (C) 2020 Jan Brinkmann
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

class Prefix : Command {
    override fun action(args: Array<out String>, event: GuildMessageReceivedEvent) {
        val langCode = Localizations.getGuildLanguage(event.guild);
        val embedTitle = Localizations.getString("prefix_title", langCode)
        if (event.member!!.isOwner || event.member!!.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.size == 1) {
                val prefix = args[0]
                if (prefix.length == 1) {
                    val res = Jsoup.connect(Main.requestURL + "/server/prefix/" + event.guild.id).method(org.jsoup.Connection.Method.POST).header("authorization", "TMB " + Main.authorizationToken).data("prefix", prefix).postDataCharset("UTF-8").header("user_id", event.member!!.id).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                    if (res.statusCode() == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("prefix_changed", langCode, object : ArrayList<String?>() {
                            init {
                                add(prefix)
                            }
                        }), event.message, Color.green)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                            init {
                                add("PREFIX-${res.statusCode()}")
                            }
                        }), event.message, Color.red)
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("prefix_only_one_char", langCode), event.message, Color.red)
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("prefix_no_arg", langCode), event.message, Color.red)
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
        }
    }
}