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

import de.bnder.taskmanager.botlists.UpdateBotLists;
import de.bnder.taskmanager.slashcommands.UpdateGuildSlashCommands;
import de.bnder.taskmanager.utils.UpdateServerName;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class GuildJoin extends ListenerAdapter {

    public void onGuildJoin(GuildJoinEvent e) {
        try {
            UpdateServerName.update(e.getGuild());
        } catch (IOException | ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
        }
        final String intro = "Thanks for using this bot. The default language is english but you can change the language with the command `-language`.";
        final String msg = "Type `-help` for a complete list of all commands.";
        try {
            e.getGuild().getDefaultChannel().sendMessage(intro + "\n" + msg).queue();
        } catch (InsufficientPermissionException | NullPointerException ex) {
            for (TextChannel tc : e.getGuild().getTextChannels()) {
                try {
                    tc.sendMessage(intro + "\n" + msg).queue();
                    break;
                } catch (Exception ignored) {
                }
            }
        }
        UpdateBotLists.updateBotLists(e.getJDA().getGuilds().size(), e.getJDA().getSelfUser().getId());
        UpdateGuildSlashCommands.update(e.getGuild());
    }

}
