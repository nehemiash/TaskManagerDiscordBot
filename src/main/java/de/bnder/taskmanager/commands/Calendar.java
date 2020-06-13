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

import de.bnder.taskmanager.main.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.List;

public class Calendar implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        if (args.length >= 4) {
            //-calendar appointment Name 01.01.2020 13:00
            if (args[0].equalsIgnoreCase("appointment")) {
                if (event.getMessage().getMentionedMembers().size() == 0) {
                    if (args.length >= 5 && Group.Companion.serverHasGroup(args[1], event.getGuild())) {
                        sendAppointmentCreationToBackend(args, null, 2, args[1]);
                    } else {
                        sendAppointmentCreationToBackend(args, event.getMember(), 1, null);
                    }
                } else {
                    sendAppointmentCreationToBackend(args, event.getMessage().getMentionedMembers().get(0), 2, null);
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

    public void sendAppointmentCreationToBackend(String[] args, Member member, final int argStart, String groupName) {
        String time = null;
        StringBuilder appointmentNameBuilder = new StringBuilder();
        int range = args.length - 2;
        String date = args[args.length - 2];
        if (args[args.length - 1].contains(":")) {
            time = args[args.length - 1];
        } else {
            date = args[args.length - 1];
            range = args.length - 1;
        }
        for (int i = argStart; i < range; i++) {
            appointmentNameBuilder.append(args[i]).append(" ");
        }
        final String appointmentName = appointmentNameBuilder.substring(0, appointmentNameBuilder.length() - 1);
        //TODO: SEND TO BACKEND
        if (member == null) {
            //APPOINTMENT FOR GROUP
        } else {
            //APPOINTMENT FOR USER
        }
    }
}
