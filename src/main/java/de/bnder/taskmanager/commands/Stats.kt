package de.bnder.taskmanager.commands

import com.eclipsesource.json.Json
import de.bnder.taskmanager.main.Command
import de.bnder.taskmanager.main.Main
import de.bnder.taskmanager.utils.Localizations
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.jsoup.Jsoup
import java.awt.Color
import java.io.IOException

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
class Stats : Command {
    override fun action(args: Array<String>, event: GuildMessageReceivedEvent) {
        val server = event.jda.guilds.size
        val ping = event.jda.gatewayPing
        val langCode = Localizations.getGuildLanguage(event.guild)
        val builder = EmbedBuilder()
        builder.addField(Localizations.getString("stats_field_server", langCode), server.toString(), true)
        builder.addField(Localizations.getString("stats_field_ping", langCode), ping.toString(), true)
        try {
            val jsonResponse = Jsoup.connect(Main.requestURL + "getStats.php?requestToken=" + Main.requestToken).userAgent(Main.userAgent).execute().body()
            val `object` = Json.parse(jsonResponse).asObject()
            builder.addField(Localizations.getString("stats_field_tasks_done", langCode), `object`.getInt("tasks_done", -1).toString(), true)
            builder.addField(Localizations.getString("stats_field_tasks_created", langCode), `object`.getInt("tasks_created", -1).toString(), true)
            builder.addField("Shard", (event.jda.shardInfo.shardId + 1).toString(), true)
            builder.addField("Shards", event.jda.shardInfo.shardTotal.toString(), true)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        builder.setColor(Color.green)
        builder.setTitle(Localizations.getString("stats_message_title", langCode))
        event.channel.sendMessage(builder.build()).queue()
    }
}