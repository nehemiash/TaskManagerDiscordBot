package de.bnder.taskmanager.commands;
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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.IOException;

public class Calendar implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        if (args.length >= 6) {
            //-calendar appointment Name 01.01.2020 13:00 02.01.2020 18:00
            if (args[0].equalsIgnoreCase("appointment")) {
                if (event.getMessage().getMentionedMembers().size() == 0) {
                    if (Group.Companion.serverHasGroup(args[1], event.getGuild())) {
                        sendAppointmentCreationToBackend(args, event.getMessage(), 2, args[1], null);
                    } else {
                        sendAppointmentCreationToBackend(args, event.getMessage(), 1, null, event.getMember());
                    }
                } else {
                    sendAppointmentCreationToBackend(args, event.getMessage(), 2, null, event.getMessage().getMentionedMembers().get(0));
                }
            }
        } else if (args.length == 1) {
            //-calendar appointments
            if (args[0].equalsIgnoreCase("appointments")) {
                //TODO: GET FROM BACKEND
            }
        } else if (args.length == 2) {
            //-calendar delete 01.01.2020
            if (args[0].equalsIgnoreCase("delete")) {
                final String date = args[1];
                if (date.split(".").length == 3) {
                    //TODO: SEND TO BACKEND
                } else {
                    //INVALID FORMAT
                }
            }
        } else if (args.length == 3) {
            //-calendar delete 01.01.2020 12:00
            if (args[0].equalsIgnoreCase("delete")) {
                final String date = args[1];
                final String time = args[2];
                if (date.split(".").length == 3 && time.split(":").length == 2) {
                    //TODO: SEND TO BACKEND
                } else {
                    //INVALID FORMAT
                }
            }
        }
    }

    public void sendAppointmentCreationToBackend(String[] args, Message message, final int argStart, String groupName, Member member) {
        StringBuilder appointmentNameBuilder = new StringBuilder();
        int range = args.length - 4;
        String dateStart = args[args.length - 4];
        String timeStart = args[args.length - 3];
        String dateEnd = args[args.length - 2];
        String timeEnd = args[args.length - 1];

        for (int i = argStart; i < range; i++) {
            appointmentNameBuilder.append(args[i]).append(" ");
        }
        final String appointmentName = appointmentNameBuilder.substring(0, appointmentNameBuilder.length() - 1);
        System.out.println(appointmentName + "\nVon: " + dateStart + " " + timeStart + " Uhr\nBis: " + dateEnd + " " + timeEnd + " Uhr");
        //TODO: SEND TO BACKEND
        if (groupName != null) {
            //APPOINTMENT FOR GROUP
            try {
                final String jsonResponse = Jsoup.connect(Main.requestURL + "createAppointment.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(message.getGuild().getId()) + "&appointment=" + Connection.encodeString(appointmentName) + "&start_date=" + Connection.encodeString(dateStart) + "&start_time=" + Connection.encodeString(timeStart) + "&end_date=" + Connection.encodeString(dateEnd) + "&end_time=" + Connection.encodeString(timeEnd) + "&group_name=" + Connection.encodeString(groupName)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                final int statusCode = jsonObject.getInt("status_code", 900);
                if (statusCode == 200) {
                    MessageSender.send("calendar", "group appointment created", message, Color.green);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //APPOINTMENT FOR USER
            try {
                final String jsonResponse = Jsoup.connect(Main.requestURL + "createAppointment.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(message.getGuild().getId()) + "&appointment=" + Connection.encodeString(appointmentName) + "&start_date=" + Connection.encodeString(dateStart) + "&start_time=" + Connection.encodeString(timeStart) + "&end_date=" + Connection.encodeString(dateEnd) + "&end_time=" + Connection.encodeString(timeEnd) + "&user_id=" + Connection.encodeString(member.getId())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                final int statusCode = jsonObject.getInt("status_code", 900);
                if (statusCode == 200) {
                    MessageSender.send("calendar", "user " + member.getAsMention() + " appointment created", message, Color.green);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
