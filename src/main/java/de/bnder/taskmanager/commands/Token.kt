package de.bnder.taskmanager.commands

import com.eclipsesource.json.Json
import de.bnder.taskmanager.main.Command
import de.bnder.taskmanager.main.Main
import de.bnder.taskmanager.utils.Connection
import de.bnder.taskmanager.utils.Localizations
import de.bnder.taskmanager.utils.MessageSender
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.jsoup.Jsoup
import java.awt.Color
import java.io.IOException
import java.util.*

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
class Token : Command {
    override fun action(args: Array<String>, event: GuildMessageReceivedEvent) {
        val guild = event.guild
        val langCode = Localizations.getGuildLanguage(guild)
        val embedTitle = Localizations.getString("token_message_title", langCode)
        try {
            val channel = event.author.openPrivateChannel().complete()
            if (args.size == 1 && args[0].equals("new", ignoreCase = true)) {
                val token = getNewToken(event.author)
                MessageSender.send(embedTitle, Localizations.getString("token_erhalten", langCode), event.message, Color.green)
                channel.sendMessage(token!!).queue()
                channel.sendMessage(EmbedBuilder().setImage("http://api.qrserver.com/v1/create-qr-code/?color=000000&bgcolor=FFFFFF&data=" + Base64.getEncoder().encodeToString("tmb_$token".toByteArray()) + "&qzone=1&margin=0&size=400x400&ecc=L").build()).queue()
                channel.sendMessage(Localizations.getString("token_info", langCode)).queue()
            } else {
                val token = getToken(event.author)
                MessageSender.send(embedTitle, Localizations.getString("token_erhalten", langCode), event.message, Color.green)
                channel.sendMessage(token!!).queue()
                channel.sendMessage(EmbedBuilder().setImage("http://api.qrserver.com/v1/create-qr-code/?color=000000&bgcolor=FFFFFF&data=" + Base64.getEncoder().encodeToString("tmb_$token".toByteArray()) + "&qzone=1&margin=0&size=400x400&ecc=L").build()).queue()
                channel.sendMessage(Localizations.getString("token_info", langCode)).queue()
            }
        } catch (e: Exception) {
            MessageSender.send(embedTitle, Localizations.getString("token_konnte_nicht_gesendet_werden", langCode), event.message, Color.red)
        }
    }

    companion object {
        private fun getNewToken(user: User): String? {
            try {
                val res = Jsoup.connect("http://localhost:5000" + "/user/token/create").method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", user.id).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                val jsonObject = Json.parse(res.parse().body().text()).asObject()
                if (res.statusCode() == 200) {
                    return jsonObject.getString("token", null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        private fun getToken(user: User): String? {
            try {
                val res = Jsoup.connect("http://localhost:5000" + "/user/token").method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", user.id).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                val jsonObject = Json.parse(res.parse().body().text()).asObject()
                if (res.statusCode() == 200) {
                    return jsonObject.getString("token", null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }
}