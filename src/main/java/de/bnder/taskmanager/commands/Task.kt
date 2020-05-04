package de.bnder.taskmanager.commands

import com.eclipsesource.json.Json
import de.bnder.taskmanager.main.Command
import de.bnder.taskmanager.main.Main
import de.bnder.taskmanager.utils.Connection
import de.bnder.taskmanager.utils.DateUtil
import de.bnder.taskmanager.utils.Localizations
import de.bnder.taskmanager.utils.MessageSender
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang.StringEscapeUtils
import org.jsoup.Jsoup
import java.awt.Color
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
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
class Task : Command {
    @Throws(IOException::class)
    override fun action(args: Array<String>, event: GuildMessageReceivedEvent) {
        val guild = event.guild
        val langCode = Localizations.getGuildLanguage(guild)
        val embedTitle = Localizations.getString("task_message_title", langCode)
        if (args.size >= 3) {
            if (args[0].equals("add", ignoreCase = true)) {
                if (event.message.mentionedMembers.size > 0) {
                    val member = event.message.mentionedMembers[0]
                    val task = getTaskFromArgs(args, 2)
                    val jsonResponse = Jsoup.connect(Main.requestURL + "createTask.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(event.guild.id) + "&task=" + task + "&userID=" + Connection.encodeString(member.user.id)).userAgent(Main.userAgent).execute().body()
                    val `object` = Json.parse(jsonResponse).asObject()
                    val statusCode = `object`.getInt("status_code", 900)
                    if (statusCode == 200) {
                        val channel = member.user.openPrivateChannel().complete()
                        channel.sendMessage(Localizations.getString("aufgabe_erhalten", langCode, object : ArrayList<String?>() {
                            init {
                                add(event.author.asTag)
                            }
                        })).queue()
                        channel.sendMessage(URLDecoder.decode(task, StandardCharsets.UTF_8.toString())).queue()
                        MessageSender.send(embedTitle + " - " + `object`.getString("task_id", ""), Localizations.getString("aufgabe_erstellt", langCode, object : ArrayList<String?>() {
                            init {
                                add(member.user.name)
                            }
                        }), event.message, Color.green)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("aufgabe_erstellt_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                            init {
                                add(statusCode.toString())
                            }
                        }), event.message, Color.red)
                    }
                } else if (Group.Companion.serverHasGroup(args[1], event.guild)) {
                    val groupName = Connection.encodeString(args[1])
                    val task = getTaskFromArgs(args, 2)
                    val jsonResponse = Jsoup.connect(Main.requestURL + "createTask.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.id) + "&task=" + Connection.encodeString(task) + "&groupName=" + groupName).userAgent(Main.userAgent).execute().body()
                    val `object` = Json.parse(jsonResponse).asObject()
                    val statusCode = `object`.getInt("status_code", 900)
                    if (statusCode == 200) {
                        MessageSender.send(embedTitle + " - " + `object`.getString("task_id", ""), Localizations.getString("aufgabe_gruppe_erstellt", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                            }
                        }), event.message, Color.green)
                    } else if (statusCode == 902) {
                        MessageSender.send(embedTitle, Localizations.getString("keine_gruppen_auf_server", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                            }
                        }), event.message, Color.red)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("aufgabe_erstellt_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                            }
                        }), event.message, Color.red)
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("aufgabe_erstellen_fehlende_argumente", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("deadline", ignoreCase = true)) {
                val taskID = StringEscapeUtils.escapeSql(args[1])
                var date = args[2]
                if (args.size == 4) {
                    date += " " + args[3]
                }
                if (DateUtil.convertToDate(date) != null) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                    val newDate = dateFormat.format(DateUtil.convertToDate(date))
                    val jsonResponse = Jsoup.connect(Main.requestURL + "setDeadline.php?requestToken=" + Main.requestToken + "&taskID=" + taskID + "&date=" + Connection.encodeString(newDate) + "&serverID=" + Connection.encodeString(guild.id)).userAgent(Main.userAgent).execute().body()
                    val `object` = Json.parse(jsonResponse).asObject()
                    val statusCode = `object`.getInt("status_code", 900)
                    if (statusCode == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("deadline_gesetzt", langCode, object : ArrayList<String?>() {
                            init {
                                add(taskID)
                                add(newDate)
                            }
                        }), event.message, Color.green)
                    } else if (statusCode == 902) {
                        MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, object : ArrayList<String?>() {
                            init {
                                add(taskID)
                            }
                        }), event.message, Color.red)
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("ungültiges_datum_format", langCode), event.message, Color.red)
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode), event.message, Color.red)
            }
        } else if (args.size >= 2) {
            if (args[0].equals("list", ignoreCase = true)) {
                if (event.message.mentionedMembers.size > 0) {
                    val member = event.message.mentionedMembers[0]
                    val jsonResponse = Jsoup.connect(Main.requestURL + "getUserTasks.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.id) + "&userID=" + Connection.encodeString(member.user.id)).userAgent(Main.userAgent).execute().body()
                    val `object` = Json.parse(jsonResponse).asObject()
                    val statusCode = `object`.getInt("status_code", 900)
                    if (statusCode == 200) {
                        var array = `object`["todo"].asArray()
                        val builder = StringBuilder()
                        for (i in 0 until array.size()) {
                            val taskID = array[i].asString()
                            val task = `object`[taskID].asObject()["task"].asString()
                            val deadline = `object`[taskID].asObject()["deadline"].asString()
                            var dLine = ""
                            if (deadline.length > 0) {
                                dLine = "$deadline |"
                            }
                            builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                        }
                        array = `object`["doing"].asArray()
                        for (i in 0 until array.size()) {
                            val taskID = array[i].asString()
                            val task = `object`[taskID].asObject()["task"].asString()
                            val deadline = `object`[taskID].asObject()["deadline"].asString()
                            var dLine = ""
                            if (deadline.length > 0) {
                                dLine = "$deadline |"
                            }
                            builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_wird_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                        }
                        array = `object`["done"].asArray()
                        for (i in 0 until array.size()) {
                            val taskID = array[i].asString()
                            val task = `object`[taskID].asObject()["task"].asString()
                            val deadline = `object`[taskID].asObject()["deadline"].asString()
                            var dLine = ""
                            if (deadline.length > 0) {
                                dLine = "$deadline |"
                            }
                            builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_erledigt", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                        }
                        MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_nutzer", langCode, object : ArrayList<String?>() {
                            init {
                                add(member.asMention)
                                add(builder.toString())
                            }
                        }), event.message, Color.green)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("keine_aufgaben", langCode), event.message, Color.red)
                    }
                } else {
                    val groupName = args[1]
                    val jsonResponse = Jsoup.connect(Main.requestURL + "getGroupTasks.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.id) + "&group_name=" + Connection.encodeString(groupName)).userAgent(Main.userAgent).execute().body()
                    val `object` = Json.parse(jsonResponse).asObject()
                    val statusCode = `object`.getInt("status_code", 900)
                    if (statusCode == 200) {
                        var array = `object`["todo"].asArray()
                        val builder = StringBuilder()
                        for (i in 0 until array.size()) {
                            val taskID = array[i].asString()
                            val task = `object`[taskID].asObject()["task"].asString()
                            val deadline = `object`[taskID].asObject()["deadline"].asString()
                            var dLine = ""
                            if (deadline.length > 0) {
                                dLine = "$deadline |"
                            }
                            builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                        }
                        array = `object`["doing"].asArray()
                        for (i in 0 until array.size()) {
                            val taskID = array[i].asString()
                            val task = `object`[taskID].asObject()["task"].asString()
                            val deadline = `object`[taskID].asObject()["deadline"].asString()
                            var dLine = ""
                            if (deadline.length > 0) {
                                dLine = "$deadline |"
                            }
                            builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_wird_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                        }
                        array = `object`["done"].asArray()
                        for (i in 0 until array.size()) {
                            val taskID = array[i].asString()
                            val task = `object`[taskID].asObject()["task"].asString()
                            val deadline = `object`[taskID].asObject()["deadline"].asString()
                            var dLine = ""
                            if (deadline.length > 0) {
                                dLine = "$deadline |"
                            }
                            builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_erledigt", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                        }
                        MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_gruppe", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                                add(builder.toString())
                            }
                        }), event.message, Color.green)
                    } else if (statusCode == 902) {
                        MessageSender.send(embedTitle, Localizations.getString("gruppe_mit_namen_existiert_nicht", langCode), event.message, Color.red)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("keine_aufgaben", langCode), event.message, Color.red)
                    }
                }
            } else if (args[0].equals("delete", ignoreCase = true)) {
                val taskID = StringEscapeUtils.escapeSql(args[1])
                val jsonResponse = Jsoup.connect(Main.requestURL + "deleteTask.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(guild.id) + "&taskID=" + taskID).userAgent(Main.userAgent).execute().body()
                val `object` = Json.parse(jsonResponse).asObject()
                val status_code = `object`.getInt("status_code", 900)
                if (status_code == 200) {
                    MessageSender.send(embedTitle, Localizations.getString("aufgabe_gelöscht", langCode, object : ArrayList<String?>() {
                        init {
                            add(taskID)
                        }
                    }), event.message, Color.green)
                } else if (status_code == 902) {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, object : ArrayList<String?>() {
                        init {
                            add(taskID)
                        }
                    }), event.message, Color.red)
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("proceed", ignoreCase = true)) {
                val taskID = Connection.encodeString(args[1])
                val jsonResponse = Jsoup.connect(Main.requestURL + "updateTaskStatus.php?requestToken=" + Main.requestToken + "&task_id=" + taskID + "&server_id=" + Connection.encodeString(guild.id)).userAgent(Main.userAgent).execute().body()
                val `object` = Json.parse(jsonResponse).asObject()
                val statusCode = `object`.getInt("status_code", 900)
                if (statusCode == 200) {
                    val process = `object`.getInt("process", 0)
                    if (process == 1) {
                        MessageSender.send(embedTitle, Localizations.getString("aufgabe_wird_nun_bearbeitet", langCode), event.message, Color.green)
                    } else if (process == 2) {
                        MessageSender.send(embedTitle, Localizations.getString("aufgabe_erledigt", langCode), event.message, Color.green)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                            init {
                                add("Progress $process")
                            }
                        }), event.message, Color.red)
                    }
                } else if (statusCode == 902) {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, object : ArrayList<String?>() {
                        init {
                            add(taskID)
                        }
                    }), event.message, Color.red)
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                        init {
                            add(statusCode.toString())
                        }
                    }), event.message, Color.red)
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode), event.message, Color.red)
            }
        } else if (args.size == 1) {
            val member = event.member
            val jsonResponse = Jsoup.connect(Main.requestURL + "getUserTasks.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.id) + "&userID=" + Connection.encodeString(member!!.user.id)).userAgent(Main.userAgent).execute().body()
            val `object` = Json.parse(jsonResponse).asObject()
            val statusCode = `object`.getInt("status_code", 900)
            if (statusCode == 200) {
                var array = `object`["todo"].asArray()
                val builder = StringBuilder()
                for (i in 0 until array.size()) {
                    val taskID = array[i].asString()
                    val task = `object`[taskID].asObject()["task"].asString()
                    val deadline = `object`[taskID].asObject()["deadline"].asString()
                    var dLine = ""
                    if (deadline.length > 0) {
                        dLine = "$deadline |"
                    }
                    builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                }
                array = `object`["doing"].asArray()
                for (i in 0 until array.size()) {
                    val taskID = array[i].asString()
                    val task = `object`[taskID].asObject()["task"].asString()
                    val deadline = `object`[taskID].asObject()["deadline"].asString()
                    var dLine = ""
                    if (deadline.length > 0) {
                        dLine = "$deadline |"
                    }
                    builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_wird_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                }
                array = `object`["done"].asArray()
                for (i in 0 until array.size()) {
                    val taskID = array[i].asString()
                    val task = `object`[taskID].asObject()["task"].asString()
                    val deadline = `object`[taskID].asObject()["deadline"].asString()
                    var dLine = ""
                    if (deadline.length > 0) {
                        dLine = "$deadline |"
                    }
                    builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_erledigt", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                }
                MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_nutzer", langCode, object : ArrayList<String?>() {
                    init {
                        add(member.asMention)
                        add(builder.toString())
                    }
                }), event.message, Color.green)
            } else {
                MessageSender.send(embedTitle, Localizations.getString("keine_aufgaben", langCode), event.message, Color.red)
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode), event.message, Color.red)
        }
    }

    companion object {
        private fun getTaskFromArgs(args: Array<String>, beginIndex: Int): String {
            val taskBuilder = StringBuilder()
            for (i in beginIndex until args.size) {
                taskBuilder.append(args[i]).append(" ")
            }
            taskBuilder.subSequence(0, taskBuilder.length - 1)
            return Connection.encodeString(taskBuilder.toString())
        }
    }
}