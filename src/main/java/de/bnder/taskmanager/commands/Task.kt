package de.bnder.taskmanager.commands

import com.eclipsesource.json.Json
import de.bnder.taskmanager.main.Command
import de.bnder.taskmanager.main.Main
import de.bnder.taskmanager.utils.*
import de.bnder.taskmanager.utils.Settings
import de.bnder.taskmanager.utils.Task
import de.bnder.taskmanager.utils.permissions.TaskPermission
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang.StringEscapeUtils
import org.jsoup.Jsoup
import java.awt.Color
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar

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
                if (PermissionSystem.hasPermission(event.member, TaskPermission.CREATE_TASK)) {
                    if (event.message.mentionedMembers.size > 0) {
                        val task = getTaskFromArgs(1, event.message, true)
                        for (member in event.message.mentionedMembers) {
                            val taskObject = Task(guild, task, null, member)
                            if (taskObject.statusCode == 200) {
                                sendTaskMessage(member, event, taskObject.id, langCode, task)
                                MessageSender.send(embedTitle + " - " + taskObject.id, Localizations.getString("aufgabe_erstellt", langCode, object : ArrayList<String?>() {
                                    init {
                                        add(member.user.name)
                                    }
                                }), event.message, Color.green)
                            } else {
                                MessageSender.send(embedTitle, Localizations.getString("aufgabe_erstellt_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                                    init {
                                        add(taskObject.statusCode.toString())
                                    }
                                }), event.message, Color.red)
                            }
                        }
                    } else if (Group.serverHasGroup(args[1], event.guild)) {
                        val groupName = Connection.encodeString(args[1])
                        val task = getTaskFromArgs(3, event.message, false)
                        val taskObject = Task(guild, task, null, groupName)
                        if (taskObject.statusCode == 200) {
                            val groupMembersJsonResponse = Jsoup.connect(Main.requestURL + "getGroupMembers.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(event.guild.id) + "&group_name=" + groupName).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body()
                            val groupMembersObject = Json.parse(groupMembersJsonResponse).asObject()
                            val groupMembersStatusCode = groupMembersObject.getInt("status_code", 900)
                            if (groupMembersStatusCode == 200) {
                                var usersWhoReceivedTheTaskAmount = 0
                                for (value in groupMembersObject["members"].asArray()) {
                                    val id = value.asObject().getString("user_id", null)
                                    if (id != null) {
                                        val member = event.guild.retrieveMemberById(id).complete()
                                        if (member != null) {
                                            usersWhoReceivedTheTaskAmount++
                                            sendTaskMessage(member, event, taskObject.id, langCode, task)
                                        }
                                    }
                                }
                                MessageSender.send(embedTitle + " - " + taskObject.id, Localizations.getString("aufgabe_an_x_mitglieder_gesendet", langCode, object : ArrayList<String?>() {
                                    init {
                                        add(usersWhoReceivedTheTaskAmount.toString())
                                    }
                                }), event.message, Color.green)
                            }
                        } else if (taskObject.statusCode == 902) {
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
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("edit", ignoreCase = true)) {
                if (PermissionSystem.hasPermission(event.member, TaskPermission.EDIT_TASK)) {
                    val taskID = StringEscapeUtils.escapeSql(args[1])
                    val newTask = getTaskFromArgs(3, event.message, false)
                    val task = Task(taskID, guild)
                    task.text = newTask
                    when (val statusCode = task.statusCode) {
                        200 -> {
                            MessageSender.send(embedTitle, Localizations.getString("aufgabe_editiert", langCode, object : ArrayList<String?>() {
                                init {
                                    add(taskID)
                                }
                            }), event.message, Color.green)
                        }
                        902 -> {
                            MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, object : ArrayList<String?>() {
                                init {
                                    add(taskID)
                                }
                            }), event.message, Color.red)
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
            } else if (args[0].equals("deadline", ignoreCase = true)) {
                if (PermissionSystem.hasPermission(event.member, TaskPermission.EDIT_TASK)) {
                    val taskID = StringEscapeUtils.escapeSql(args[1])
                    val task = Task(taskID, guild)
                    var date = args[2]
                    if (args.size == 4) {
                        date += " " + args[3]
                    }
                    if (DateUtil.convertToDate(date) != null) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                        val newDate = dateFormat.format(DateUtil.convertToDate(date))
                        task.deadline = newDate;
                        if (task.statusCode == 200) {
                            MessageSender.send(embedTitle, Localizations.getString("deadline_gesetzt", langCode, object : ArrayList<String?>() {
                                init {
                                    add(taskID)
                                    add(newDate)
                                }
                            }), event.message, Color.green)
                        } else if (task.statusCode == 902) {
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
            } else {
                MessageSender.send(embedTitle, Localizations.getString("muss_serverbesitzer_oder_adminrechte_haben", langCode), event.message, Color.red)
            }
        } else if (args.size >= 2) {
            if (args[0].equals("list", ignoreCase = true)) {
                var jsonResponse = ""
                var text = ""
                if (event.message.mentionedMembers.size > 0) {
                    val member = event.message.mentionedMembers[0]
                    jsonResponse = Jsoup.connect(Main.requestURL + "getUserTasks.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.id) + "&userID=" + Connection.encodeString(member.user.id)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body()
                } else {
                    val groupName = args[1]
                    jsonResponse = Jsoup.connect(Main.requestURL + "getGroupTasks.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.id) + "&group_name=" + Connection.encodeString(groupName)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body()
                }
                val jsonObject = Json.parse(jsonResponse).asObject()
                val statusCode = jsonObject.getInt("status_code", 900)
                if (statusCode == 200) {
                    var array = jsonObject["todo"].asArray()
                    val builder = StringBuilder()
                    for (i in 0 until array.size()) {
                        val taskID = array[i].asString()
                        val task = jsonObject[taskID].asObject()["task"].asString()
                        val deadline = jsonObject[taskID].asObject()["deadline"].asString()
                        var dLine = ""
                        if (deadline.isNotEmpty()) {
                            dLine = "$deadline |"
                        }
                        builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                    }
                    array = jsonObject["doing"].asArray()
                    for (i in 0 until array.size()) {
                        val taskID = array[i].asString()
                        val task = jsonObject[taskID].asObject()["task"].asString()
                        val deadline = jsonObject[taskID].asObject()["deadline"].asString()
                        var dLine = ""
                        if (deadline.isNotEmpty()) {
                            dLine = "$deadline |"
                        }
                        builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_wird_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                    }
                    array = jsonObject["done"].asArray()
                    for (i in 0 until array.size()) {
                        val taskID = array[i].asString()
                        val task = jsonObject[taskID].asObject()["task"].asString()
                        val deadline = jsonObject[taskID].asObject()["deadline"].asString()
                        var dLine = ""
                        if (deadline.isNotEmpty()) {
                            dLine = "$deadline |"
                        }
                        builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_erledigt", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                    }
                    if (event.message.mentionedMembers.size > 0) {
                        val member = event.message.mentionedMembers[0]
                        text = Localizations.getString("alle_aufgaben_von_nutzer", langCode, object : ArrayList<String?>() {
                            init {
                                add(member.asMention)
                                add(builder.toString())
                            }
                        })
                    } else {
                        val groupName = args[1]
                        text = Localizations.getString("alle_aufgaben_von_gruppe", langCode, object : ArrayList<String?>() {
                            init {
                                add(groupName)
                                add(builder.toString())
                            }
                        })
                    }
                    MessageSender.send(embedTitle, text, event.message, Color.green)
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgaben", langCode), event.message, Color.red)
                }
            } else if (args[0].equals("delete", ignoreCase = true)) {
                if (PermissionSystem.hasPermission(event.member, TaskPermission.DELETE_TASK)) {
                    val taskID = StringEscapeUtils.escapeSql(args[1])
                    val task = Task(taskID, guild)
                    task.delete()
                    when (val statusCode = task.statusCode) {
                        200 -> {
                            MessageSender.send(embedTitle, Localizations.getString("aufgabe_gelöscht", langCode, object : ArrayList<String?>() {
                                init {
                                    add(taskID)
                                }
                            }), event.message, Color.green)
                        }
                        902 -> {
                            MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, object : ArrayList<String?>() {
                                init {
                                    add(taskID)
                                }
                            }), event.message, Color.red)
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
            } else if (args[0].equals("done", ignoreCase = true)) {
                val taskID = StringEscapeUtils.escapeSql(args[1])
                val task = Task(taskID, guild)
                when (val statusCode = task.setStatus(TaskStatus.DONE, event.member!!).statusCode) {
                    200 -> {
                        MessageSender.send(embedTitle, Localizations.getString("aufgabe_erledigt", langCode), event.message, Color.green)
                    }
                    902 -> {
                        MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, object : ArrayList<String?>() {
                            init {
                                add(taskID)
                            }
                        }), event.message, Color.red)
                    }
                    else -> {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                            init {
                                add(statusCode.toString())
                            }
                        }), event.message, Color.red)
                    }
                }
            } else if (args[0].equals("proceed", ignoreCase = true)) {
                val taskID = Connection.encodeString(args[1])
                val task = Task(taskID, guild)
                when (val statusCode = task.proceed(event.member).statusCode) {
                    200 -> {
                        when (task.status) {
                            TaskStatus.IN_PROGRESS -> {
                                MessageSender.send(embedTitle, Localizations.getString("aufgabe_wird_nun_bearbeitet", langCode), event.message, Color.green)
                            }
                            TaskStatus.DONE -> {
                                MessageSender.send(embedTitle, Localizations.getString("aufgabe_erledigt", langCode), event.message, Color.green)
                            }
                            else -> {
                                MessageSender.send(embedTitle, Localizations.getString("task_abfrage_unbekannter_fehler", langCode), event.message, Color.red)
                            }
                        }
                    }
                    902 -> {
                        MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, object : ArrayList<String?>() {
                            init {
                                add(taskID)
                            }
                        }), event.message, Color.red)
                    }
                    else -> {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                            init {
                                add(statusCode.toString())
                            }
                        }), event.message, Color.red)
                    }
                }
            } else if (args[0].equals("undo", ignoreCase = true)) {
                val taskID = Connection.encodeString(args[1])
                val task = Task(taskID, guild)
                when (val statusCode = task.undo(event.member).statusCode) {
                    200 -> {
                        when (task.status) {
                            TaskStatus.TODO -> {
                                MessageSender.send(embedTitle, Localizations.getString("aufgabe_wird_nicht_bearbeitet", langCode), event.message, Color.green)
                            }
                            TaskStatus.IN_PROGRESS -> {
                                MessageSender.send(embedTitle, Localizations.getString("aufgabe_wird_nun_bearbeitet", langCode), event.message, Color.green)
                            }
                            TaskStatus.DONE -> {
                                MessageSender.send(embedTitle, Localizations.getString("aufgabe_erledigt", langCode), event.message, Color.green)
                            }
                            else -> {
                                MessageSender.send(embedTitle, Localizations.getString("task_abfrage_unbekannter_fehler", langCode), event.message, Color.red)
                            }
                        }
                    }
                    902 -> {
                        MessageSender.send(embedTitle, Localizations.getString("keine_aufgabe_mit_id", langCode, object : ArrayList<String?>() {
                            init {
                                add(taskID)
                            }
                        }), event.message, Color.red)
                    }
                    else -> {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, object : ArrayList<String?>() {
                            init {
                                add(statusCode.toString())
                            }
                        }), event.message, Color.red)
                    }
                }
            } else if (args[0].equals("info", ignoreCase = true)) {
                val taskID = args[1]
                val task = Task(taskID, guild)
                if (task.exists()) {
                    val builder = EmbedBuilder().setColor(Color.cyan).setTimestamp(Calendar.getInstance().toInstant())
                    when (task.status!!) {
                        TaskStatus.TODO -> {
                            builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode), true)
                        }
                        TaskStatus.IN_PROGRESS -> {
                            builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_wird_bearbeitet", langCode), true)
                        }
                        TaskStatus.DONE -> {
                            builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_erledigt", langCode), true)
                        }
                    }
                    if (task.deadline != null) {
                        builder.addField(Localizations.getString("task_info_field_deadline", langCode), task.deadline, true)
                    }
                    if (task.type == TaskType.USER) {
                        builder.addField(Localizations.getString("task_info_field_type", langCode), Localizations.getString("task_info_field_type_user", langCode), true)
                        val userID = task.holder
                        if (userID != null && event.guild.retrieveMemberById(userID).complete() != null) {
                            builder.addField(Localizations.getString("task_info_field_assigned", langCode), event.guild.retrieveMemberById(userID).complete().user.asTag, true)
                        }
                    } else if (task.type == TaskType.GROUP) {
                        builder.addField(Localizations.getString("task_info_field_type", langCode), Localizations.getString("task_info_field_type_group", langCode), true)
                        builder.addField(Localizations.getString("task_info_field_assigned", langCode), task.holder, true)
                    }
                    builder.addField(Localizations.getString("task_info_field_task", langCode), task.text, false)
                    event.channel.sendMessage(builder.build()).queue()
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode), event.message, Color.red)
            }
        } else if (args.size == 1) {
            if (args[0].equals("list", ignoreCase = true)) {
                val member = event.member
                val jsonResponse = Jsoup.connect(Main.requestURL + "getUserTasks.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.id) + "&userID=" + Connection.encodeString(member!!.user.id)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body()
                val jsonObject = Json.parse(jsonResponse).asObject()
                val statusCode = jsonObject.getInt("status_code", 900)
                if (statusCode == 200) {
                    var array = jsonObject["todo"].asArray()
                    val builder = StringBuilder()
                    for (i in 0 until array.size()) {
                        val taskID = array[i].asString()
                        val task = jsonObject[taskID].asObject()["task"].asString()
                        val deadline = jsonObject[taskID].asObject()["deadline"].asString()
                        var dLine = ""
                        if (deadline.isNotEmpty()) {
                            dLine = "$deadline |"
                        }
                        builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                    }
                    array = jsonObject["doing"].asArray()
                    for (i in 0 until array.size()) {
                        val taskID = array[i].asString()
                        val task = jsonObject[taskID].asObject()["task"].asString()
                        val deadline = jsonObject[taskID].asObject()["deadline"].asString()
                        var dLine = ""
                        if (deadline.isNotEmpty()) {
                            dLine = "$deadline |"
                        }
                        builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_wird_bearbeitet", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                    }
                    array = jsonObject["done"].asArray()
                    val showDoneTasks = Settings.getUserSettings(event.member).getString("show_done_tasks", "1") as String
                    var tasksHidden = 0
                    for (i in 0 until array.size()) {
                        val taskID = array[i].asString()
                        val task = jsonObject[taskID].asObject()["task"].asString()
                        val deadline = jsonObject[taskID].asObject()["deadline"].asString()
                        var dLine = ""
                        if (deadline.isNotEmpty()) {
                            dLine = "$deadline |"
                        }

                        if (showDoneTasks == "1") {
                            builder.append("- ").append(task).append(" (" + Localizations.getString("aufgaben_status_erledigt", langCode) + " | ").append(dLine).append(" ").append(taskID).append(")").append("\n")
                        } else {
                            tasksHidden++
                        }
                    }
                    if (showDoneTasks == "1") {
                        MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_nutzer", langCode, object : ArrayList<String?>() {
                            init {
                                add(member.asMention)
                                add(builder.toString())
                            }
                        }), event.message, Color.green)
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("alle_aufgaben_von_nutzer_no_done_tasks", langCode, object : ArrayList<String?>() {
                            init {
                                add(member.asMention)
                                add(builder.toString())
                                add(tasksHidden.toString())
                            }
                        }), event.message, Color.green)
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("keine_aufgaben", langCode), event.message, Color.red)
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode), event.message, Color.red)
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode), event.message, Color.red)
        }
    }

    private fun sendTaskMessage(member: Member, event: GuildMessageReceivedEvent, task_id: String, langCode: String, task: String) {
        val settings = Settings.getUserSettings(member)
        if (settings.getString("direct_message", "1") == "1") {
            val channel = member.user.openPrivateChannel().complete()
            channel.sendMessage(Localizations.getString("aufgabe_erhalten", langCode, object : ArrayList<String?>() {
                init {
                    add(event.author.asTag)
                    add(task_id)
                }
            })).queue()
            channel.sendMessage(URLDecoder.decode(task, StandardCharsets.UTF_8.toString())).queue()
        } else if (settings.get("notify_channel") != null) {
            val channel: TextChannel? = event.guild.getTextChannelById(settings.getString("notify_channel", ""))
            if (channel != null) {
                channel.sendMessage(member.asMention + Localizations.getString("aufgabe_erhalten", langCode, object : ArrayList<String?>() {
                    init {
                        add(event.author.asTag)
                        add(task_id)
                    }
                })).queue()
                channel.sendMessage(URLDecoder.decode(task, StandardCharsets.UTF_8.toString())).queue()
            }
        }
    }

    companion object {
        private fun getTaskFromArgs(startIndex: Int, message: Message, mentionedUsers: Boolean): String {
            var beginIndex = startIndex
            val taskBuilder = StringBuilder()
            val args = message.contentDisplay.split(" ")
            if (mentionedUsers) {
                for (user in message.mentionedUsers) {
                    beginIndex += user.asTag.split(" ").size
                }
                beginIndex += 1
            }
            for (i in beginIndex until args.size) {
                taskBuilder.append(args[i]).append(" ")
            }
            if (taskBuilder.isNotEmpty()) {
                taskBuilder.subSequence(0, taskBuilder.length - 1)
            }
            return Connection.encodeString(taskBuilder.toString())
        }
    }
}