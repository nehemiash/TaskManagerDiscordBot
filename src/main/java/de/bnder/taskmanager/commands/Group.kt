package de.bnder.taskmanager.commands

import com.eclipsesource.json.Json
import de.bnder.taskmanager.main.Command
import de.bnder.taskmanager.main.Main
import de.bnder.taskmanager.utils.Connection
import de.bnder.taskmanager.utils.Localizations
import de.bnder.taskmanager.utils.MessageSender
import de.bnder.taskmanager.utils.PermissionSystem
import de.bnder.taskmanager.utils.permissions.GroupPermission
import net.dv8tion.jda.api.entities.Guild
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
class Group : Command {
    @Throws(IOException::class)
    override fun action(args: Array<String>, event: GuildMessageReceivedEvent) {
        val langCode = Localizations.getGuildLanguage(event.guild)
        val embedTitle = Localizations.getString("group_title", langCode)
        if (args.size > 1) {
            if (args[0].equals("create", ignoreCase = true)) {
                if (PermissionSystem.hasPermission(event.member, GroupPermission.CREATE_GROUP)) {
                    val groupName = Connection.encodeString(args[1])
                    val res = Jsoup.connect(Main.requestURL + "/group/create/" + event.guild.id).method(org.jsoup.Connection.Method.POST).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.member!!.id).data("group_name", groupName).postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                    when (val statusCode = res.statusCode()) {
                        200 -> {
                            MessageSender.send(embedTitle, Localizations.getString("gruppe_erfolgreich_erstellt", langCode, object : ArrayList<String?>() {
                                init {
                                    add(groupName)
                                }
                            }), event.message, Color.green)
                        }
                        400 -> {
                            MessageSender.send(embedTitle, Localizations.getString("gruppe_nicht_erstellt_name_exisitert", langCode, object : ArrayList<String?>() {
                                init {
                                    add(groupName)
                                }
                            }), event.message, Color.red)
                        }
                        else -> {
                            MessageSender.send(embedTitle, Localizations.getString("gruppe_nicht_erstellt_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                                init {
                                    add(groupName)
                                    add(statusCode.toString())
                                }
                            }), event.message, Color.red)
                        }
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("delete", ignoreCase = true)) {
                if (PermissionSystem.hasPermission(event.member, GroupPermission.DELETE_GROUP)) {
                    val groupName = Connection.encodeString(args[1])
                    val res = Jsoup.connect(Main.requestURL + "/group/" + event.guild.id + "/" + groupName).method(org.jsoup.Connection.Method.DELETE).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.member!!.id).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                    when (res.statusCode()) {
                        200 -> {
                            MessageSender.send(embedTitle, Localizations.getString("gruppe_wurde_gelöscht", langCode, object : ArrayList<String?>() {
                                init {
                                    add(groupName)
                                }
                            }), event.message, Color.green)
                        }
                        404 -> {
                            MessageSender.send(embedTitle, Localizations.getString("gruppe_mit_namen_existiert_nicht", langCode, object : ArrayList<String?>() {
                                init {
                                    add(groupName)
                                }
                            }), event.message, Color.red)
                        }
                        else -> {
                            MessageSender.send(embedTitle, Localizations.getString("gruppe_löschen_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                                init {
                                    add(groupName)
                                }
                            }), event.message, Color.red)
                        }
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("members", ignoreCase = true)) {
                if (PermissionSystem.hasPermission(event.member, GroupPermission.SHOW_MEMBERS)) {
                    val groupName = Connection.encodeString(args[1])
                    val res = Jsoup.connect(Main.requestURL + "/group/members/" + event.guild.id + "/" + groupName).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.member!!.id).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                    val jsonObject = Json.parse(res.parse().body().text()).asObject()
                    when (val statusCode = res.statusCode()) {
                        200 -> {
                            val builder = StringBuilder()
                            for (value in jsonObject["members"].asArray()) {
                                val id = value.asObject().getString("user_id", null)
                                if (id != null) {
                                    val member = event.guild.retrieveMemberById(id).complete()
                                    if (member != null) {
                                        builder.append("- ").append(member.user.asTag).append("\n")
                                    }
                                }
                            }
                            MessageSender.send(embedTitle, Localizations.getString("group_members", langCode, object : ArrayList<String?>() {
                                init {
                                    add(builder.substring(0, builder.length - 1))
                                }
                            }), event.message, Color.green)
                        }
                        902 -> {
                            MessageSender.send(embedTitle, Localizations.getString("gruppe_mit_namen_existiert_nicht", langCode, object : ArrayList<String?>() {
                                init {
                                    add(groupName)
                                }
                            }), event.message, Color.red)
                        }
                        404 -> {
                            MessageSender.send(embedTitle, Localizations.getString("group_no_members", langCode), event.message, Color.red)
                        }
                        else -> {
                            MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                                init {
                                    add(statusCode.toString())
                                }
                            }), event.message, Color.red)
                        }
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("add", ignoreCase = true)) {
                if (PermissionSystem.hasPermission(event.member, GroupPermission.ADD_MEMBERS)) {
                    if (args.size >= 3) {
                        if (event.message.mentionedMembers.size > 0) {
                            val groupName = Connection.encodeString(args[1 + event.message.mentionedMembers.size])
                            for (member in event.message.mentionedMembers) {
                                val res = Jsoup.connect(Main.requestURL + "/group/add-member/" + event.guild.id).method(org.jsoup.Connection.Method.PUT).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member!!.id).data("group_name", groupName).postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                                when (val statusCode = res.statusCode()) {
                                    200 -> {
                                        MessageSender.send(embedTitle, Localizations.getString("nutzer_zu_gruppe_hinzugefügt", langCode, object : ArrayList<String?>() {
                                            init {
                                                add(member.user.name)
                                                add(groupName)
                                            }
                                        }), event.message, Color.green)
                                    }
                                    404 -> {
                                        MessageSender.send(embedTitle, Localizations.getString("gruppe_mit_namen_existiert_nicht", langCode, object : ArrayList<String?>() {
                                            init {
                                                add(groupName)
                                            }
                                        }), event.message, Color.red)
                                        return
                                    }
                                    904 -> {
                                        MessageSender.send(embedTitle, Localizations.getString("nutzer_bereits_in_gruppe", langCode), event.message, Color.red)
                                    }
                                    else -> {
                                        MessageSender.send(embedTitle, Localizations.getString("nutzer_zu_gruppe_hinzufügen_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                                            init {
                                                add(statusCode.toString())
                                            }
                                        }), event.message, Color.red)
                                        return
                                    }
                                }
                            }
                        } else {
                            MessageSender.send(embedTitle, Localizations.getString("nutzer_muss_markiert_werden", langCode), event.message, Color.red)
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("gruppen_name_muss_angegeben_werden", langCode), event.message, Color.red)
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("remove", ignoreCase = true) || args[0].equals("rem", ignoreCase = true)) {
                if (PermissionSystem.hasPermission(event.member, GroupPermission.REMOVE_MEMBERS)) {
                    if (args.size >= 3) {
                        if (event.message.mentionedMembers.size > 0) {
                            val groupName = Connection.encodeString(args[1 + event.message.mentionedMembers.size])
                            for (member in event.message.mentionedMembers) {
                                val res = Jsoup.connect(Main.requestURL + "/group/remove-member/" + event.guild.id).method(org.jsoup.Connection.Method.PUT).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member!!.id).data("group_name", groupName).postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                                when (val statusCode = res.statusCode()) {
                                    200 -> {
                                        MessageSender.send(embedTitle, Localizations.getString("nutzer_aus_gruppe_entfernt", langCode, object : ArrayList<String?>() {
                                            init {
                                                add(member.user.name)
                                                add(groupName)
                                            }
                                        }), event.message, Color.green)
                                    }
                                    903 -> {
                                        MessageSender.send(embedTitle, Localizations.getString("gruppe_mit_namen_existiert_nicht", langCode, object : ArrayList<String?>() {
                                            init {
                                                add(groupName)
                                            }
                                        }), event.message, Color.red)
                                        return
                                    }
                                    904 -> {
                                        MessageSender.send(embedTitle, Localizations.getString("nutzer_ist_in_keiner_gruppe", langCode, object : ArrayList<String>() {
                                            init {
                                                add(member.user.asTag)
                                            }
                                        }), event.message, Color.red)
                                    }
                                    else -> {
                                        MessageSender.send(embedTitle, Localizations.getString("nutzer_aus_gruppe_entfernen_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                                            init {
                                                add(statusCode.toString())
                                            }
                                        }), event.message, Color.red)
                                    }
                                }
                            }
                        } else {
                            MessageSender.send(embedTitle, Localizations.getString("nutzer_muss_markiert_werden", langCode), event.message, Color.red)
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("gruppen_name_muss_angegeben_werden", langCode), event.message, Color.red)
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_group_commands", langCode), event.message, Color.red)
            }
        } else if (args.size == 1) {
            if (args[0].equals("list", ignoreCase = true)) {
                val res = Jsoup.connect(Main.requestURL + "/group/list/" + event.guild.id).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.member!!.id).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                val `object` = Json.parse(res.parse().body().text()).asObject()
                val statusCode = res.statusCode()
                if (statusCode == 200) {
                    val servers = `object`["groups"].asArray()
                    if (servers.size() > 0) {
                        val builder = StringBuilder()
                        for (i in 0 until servers.size()) {
                            val serverName = servers[i].asString()
                            builder.append(serverName).append("\n")
                        }
                        MessageSender.send(embedTitle, builder.toString(), event.message, Color.green)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("keine_gruppen_auf_server", langCode), event.message, Color.red)
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                        init {
                            add(statusCode.toString())
                        }
                    }), event.message, Color.red)
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_group_commands", langCode), event.message, Color.red)
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("help_message_group_commands", langCode), event.message, Color.red)
        }
    }

    companion object {
        fun serverHasGroup(group: String, guild: Guild): Boolean {
            try {
                val res = Jsoup.connect(Main.requestURL + "/group/list/" + guild.id).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                val `object` = Json.parse(res.parse().body().text()).asObject()
                val statusCode = res.statusCode()
                if (statusCode == 200) {
                    val groups = `object`["groups"].asArray()
                    if (groups.size() > 0) {
                        for (i in 0 until groups.size()) {
                            val groupName = groups[i].asString()
                            if (group == groupName) {
                                return true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }
}