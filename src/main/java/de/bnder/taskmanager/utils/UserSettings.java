package de.bnder.taskmanager.utils;
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

import com.google.cloud.firestore.DocumentSnapshot;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Member;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class UserSettings {

    private static final Logger logger = LogManager.getLogger(UserSettings.class);

    private String notifyChannelID = null;
    private boolean directMessage = true;
    private boolean showDoneTasks = true;
    DocumentSnapshot serverMemberDoc;

    public UserSettings(final Member member) {
        try {
            serverMemberDoc = Main.firestore.collection("server").document(member.getGuild().getId()).collection("server-member").document(member.getId()).get().get();
            if (serverMemberDoc.exists()) {
                if (serverMemberDoc.getData().containsKey("show_done_tasks")) {
                    showDoneTasks = Boolean.TRUE.equals(serverMemberDoc.getBoolean("show_done_tasks"));
                }
                if (serverMemberDoc.getData().containsKey("direct_message")) {
                    directMessage = Boolean.TRUE.equals(serverMemberDoc.getBoolean("direct_message"));
                }
                if (serverMemberDoc.getData().containsKey("notify_channel")) {
                    notifyChannelID = serverMemberDoc.getString("notify_channel");
                }
            } else {
                serverMemberDoc.getReference().set(new HashMap<>() {{
                    put("show_done_tasks", showDoneTasks);
                    put("direct_message", directMessage);
                    put("notify_channel", notifyChannelID);
                }});
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
        }
    }

    public void changeShowDoneTasks() {
        serverMemberDoc.getReference().update(new HashMap<>() {{
            put("show_done_tasks", !showDoneTasks);
        }});
    }

    public void changeDirectMessage() {
        serverMemberDoc.getReference().update(new HashMap<>() {{
            put("direct_message", !directMessage);
        }});
    }

    public void setNotifyChannelID(String channelID) {
        this.notifyChannelID = channelID;
        serverMemberDoc.getReference().update(new HashMap<>() {{
            put("notify_channel", channelID);
        }});
    }

    public boolean getShowDoneTasks() {
        return showDoneTasks;
    }

    public boolean getDirectMessage() {
        return directMessage;
    }

    public String getNotifyChannelID() {
        return notifyChannelID;
    }
}
