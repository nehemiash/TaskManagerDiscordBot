package de.bnder.taskmanager.listeners;
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

import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.UpdateServerName;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

public class GuildJoin extends ListenerAdapter {

    public void onGuildJoin(GuildJoinEvent e) {
        try {
            UpdateServerName.update(e.getGuild());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String intro = "Thanks for using this bot. The default language is german. You can change the language to english by typing \"-language\".";
        String msg = "Danke für die Nutzung dieses Bots. Um zu erfahren was der Bot kann, benutze den Befehl `" + Main.prefix + "help`. Weitere Infors sind unter https://bnder.de zu finden.\n" +
                "\n " +
                "Wichtig! Für die optimale Nutzung aktiviere in deinen Einstellungen die Linkvorschau unter \"Text & Bilder\".";
        try {
            e.getGuild().getDefaultChannel().sendMessage(intro).queue();
            e.getGuild().getDefaultChannel().sendMessage(msg).queue();
        } catch (InsufficientPermissionException ex) {
            for (TextChannel tc : e.getGuild().getTextChannels()) {
                try {
                    if (tc.canTalk()) {
                        tc.sendMessage(intro).queue();
                        tc.sendMessage(msg).queue();
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

}
