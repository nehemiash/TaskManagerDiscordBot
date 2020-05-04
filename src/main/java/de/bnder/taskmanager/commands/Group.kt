package de.bnder.taskmanager.commands

import com.eclipsesource.json.Json
import de.bnder.taskmanager.main.Command
import de.bnder.taskmanager.main.Main
import de.bnder.taskmanager.utils.Connection
import de.bnder.taskmanager.utils.Localizations
import de.bnder.taskmanager.utils.MessageSender
import net.dv8tion.jda.api.Permission
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
                if (Objects.requireNonNull(event.member)!!.hasPermission(Permission.ADMINISTRATOR) || event.member!!.isOwner) {
                    val groupName = Connection.encodeString(args[1])
                    val jsonResponse = Jsoup.connect(Main.requestURL + "createGroup.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(event.guild.id) + "&groupName=" + groupName).userAgent(Main.userAgent).execute().body()
                    val `object` = Json.parse(jsonResponse).asObject()
                    val statusCode = `object`.getInt("status_code", 900)
                    if (statusCode == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("gruppe_erfolgreich_erstellt", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                            }
                        }), event.message, Color.green)
                    } else if (statusCode == 902) {
                        MessageSender.send(embedTitle, Localizations.getString("gruppe_nicht_erstellt_name_exisitert", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                            }
                        }), event.message, Color.red)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("gruppe_nicht_erstellt_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                                add(statusCode.toString())
                            }
                        }), event.message, Color.red)
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("delete", ignoreCase = true)) {
                if (Objects.requireNonNull(event.member)!!.hasPermission(Permission.ADMINISTRATOR) || event.member!!.isOwner) {
                    val groupName = Connection.encodeString(args[1])
                    val jsonResponse = Jsoup.connect(Main.requestURL + "deleteGroup.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(event.guild.id) + "&groupName=" + Connection.encodeString(groupName)).userAgent(Main.userAgent).execute().body()
                    val `object` = Json.parse(jsonResponse).asObject()
                    val statusCode = `object`.getInt("status_code", 900)
                    if (statusCode == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("gruppe_wurde_gelöscht", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                            }
                        }), event.message, Color.green)
                    } else if (statusCode == 902) {
                        MessageSender.send(embedTitle, Localizations.getString("gruppe_mit_namen_existiert_nicht", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                            }
                        }), event.message, Color.red)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("gruppe_löschen_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                            }
                        }), event.message, Color.red)
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("members", ignoreCase = true)) {
                if (event.member!!.hasPermission(Permission.ADMINISTRATOR) || event.member!!.isOwner) {
                    val groupName = Connection.encodeString(args[1])
                    val jsonResponse = Jsoup.connect(Main.requestURL + "getGroupMembers.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(event.guild.id) + "&group_name=" + Connection.encodeString(groupName)).userAgent(Main.userAgent).execute().body()
                    val `object` = Json.parse(jsonResponse).asObject()
                    val statusCode = `object`.getInt("status_code", 900)
                    if (statusCode == 200) {
                        val builder = StringBuilder()
                        for (value in `object`["members"].asArray()) {
                            val id = value.asObject().getString("user_id", null)
                            if (id != null) {
                                val member = event.guild.retrieveMemberById(id).complete()
                                if (member != null) {
                                    builder.append(member.user.asTag).append(", ")
                                }
                            }
                        }
                        MessageSender.send(embedTitle, Localizations.getString("group_members", langCode, object : ArrayList<String?>() {
                            init {
                                add(builder.substring(0, builder.length - 2))
                            }
                        }), event.message, Color.green)
                    } else if (statusCode == 902) {
                        MessageSender.send(embedTitle, Localizations.getString("gruppe_mit_namen_existiert_nicht", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                            }
                        }), event.message, Color.red)
                    } else if (statusCode == 404) {
                        MessageSender.send(embedTitle, Localizations.getString("group_no_members", langCode), event.message, Color.red)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                            init {
                                add(statusCode.toString())
                            }
                        }), event.message, Color.red)
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("add", ignoreCase = true)) {
                if (Objects.requireNonNull(event.member)!!.hasPermission(Permission.ADMINISTRATOR) || event.member!!.isOwner) {
                    if (args.size >= 3) {
                        if (event.message.mentionedMembers.size > 0) {
                            val member = event.message.mentionedMembers[0]
                            val groupName = Connection.encodeString(args[2])
                            val jsonResponse = Jsoup.connect(Main.requestURL + "addUserToGroup.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(event.guild.id) + "&groupName=" + groupName + "&userID=" + Connection.encodeString(member.id)).userAgent(Main.userAgent).execute().body()
                            val `object` = Json.parse(jsonResponse).asObject()
                            val statusCode = `object`.getInt("status_code", 900)
                            if (statusCode == 200) {
                                MessageSender.send(embedTitle, Localizations.getString("nutzer_zu_gruppe_hinzugefügt", langCode, object : ArrayList<String?>() {
                                    init {
                                        add(member.user.name)
                                        add(groupName)
                                    }
                                }), event.message, Color.green)
                            } else if (statusCode == 903) {
                                MessageSender.send(embedTitle, Localizations.getString("gruppe_mit_namen_existiert_nicht", langCode, object : ArrayList<String?>() {
                                    init {
                                        add(groupName)
                                    }
                                }), event.message, Color.red)
                            } else if (statusCode == 904) {
                                MessageSender.send(embedTitle, Localizations.getString("nutzer_bereits_in_gruppe", langCode), event.message, Color.red)
                            } else {
                                MessageSender.send(embedTitle, Localizations.getString("nutzer_zu_gruppe_hinzufügen_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                                    init {
                                        add(statusCode.toString())
                                    }
                                }), event.message, Color.red)
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
                if (Objects.requireNonNull(event.member)!!.hasPermission(Permission.ADMINISTRATOR) || event.member!!.isOwner) {
                    if (args.size >= 3) {
                        if (event.message.mentionedMembers.size > 0) {
                            val member = event.message.mentionedMembers[0]
                            val groupName = Connection.encodeString(args[2])
                            val jsonResponse = Jsoup.connect(Main.requestURL + "removeUserFromGroup.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(event.guild.id) + "&groupName=" + groupName + "&userID=" + Connection.encodeString(member.id)).userAgent(Main.userAgent).execute().body()
                            val `object` = Json.parse(jsonResponse).asObject()
                            val statusCode = `object`.getInt("status_code", 900)
                            if (statusCode == 200) {
                                MessageSender.send(embedTitle, Localizations.getString("nutzer_aus_gruppe_entfernt", langCode, object : ArrayList<String?>() {
                                    init {
                                        add(member.user.name)
                                        add(groupName)
                                    }
                                }), event.message, Color.green)
                            } else if (statusCode == 903) {
                                MessageSender.send(embedTitle, Localizations.getString("gruppe_mit_namen_existiert_nicht", langCode, object : ArrayList<String?>() {
                                    init {
                                        add(groupName)
                                    }
                                }), event.message, Color.red)
                            } else if (statusCode == 904) {
                                MessageSender.send(embedTitle, Localizations.getString("nutzer_ist_in_keiner_gruppe", langCode), event.message, Color.red)
                            } else {
                                MessageSender.send(embedTitle, Localizations.getString("nutzer_aus_gruppe_entfernen_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                                    init {
                                        add(statusCode.toString())
                                    }
                                }), event.message, Color.red)
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
                val jsonResponse = Jsoup.connect(Main.requestURL + "getGroups.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(event.guild.id)).userAgent(Main.userAgent).execute().body()
                val `object` = Json.parse(jsonResponse).asObject()
                val statusCode = `object`.getInt("status_code", 900)
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
        private fun getGroupID(group: String, guild: Guild): String? {
            try {
                val jsonResponse = Jsoup.connect(Main.requestURL + "getGroupID.php?requestToken=" + Main.requestToken + "&groupName=" + Connection.encodeString(group) + "&serverID=" + Connection.encodeString(guild.id)).userAgent(Main.userAgent).execute().body()
                val `object` = Json.parse(jsonResponse).asObject()
                val statusCode = `object`.getInt("status_code", 900)
                if (statusCode == 200) {
                    return `object`.getString("group_id", null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun serverHasGroup(group: String, guild: Guild): Boolean {
            try {
                val groupID = getGroupID(group, guild)
                val jsonResponse = Jsoup.connect(Main.requestURL + "groupExists.php?requestToken=" + Main.requestToken + "&groupID=" + Connection.encodeString(groupID) + "&serverID=" + Connection.encodeString(guild.id)).userAgent(Main.userAgent).execute().body()
                val `object` = Json.parse(jsonResponse).asObject()
                val statusCode = `object`.getInt("status_code", 900)
                return statusCode == 200
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }
}