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
import de.bnder.taskmanager.utils.UpdateServer;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;

public class GuildJoin extends ListenerAdapter {

    private static final Logger logger = LogManager.getLogger(GuildJoin.class);

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        try {
            UpdateServer.update(event.getGuild());
        } catch (ExecutionException | InterruptedException e) {
            logger.error(e);
        }
        final String intro = "Thanks for using this bot. The default language is english but you can change the language with the command `-language`.";
        final String msg = "Type `-help` for a complete list of all commands.";
        try {
            event.getGuild().getDefaultChannel().sendMessage(intro + "\n" + msg).queue();
        } catch (InsufficientPermissionException | NullPointerException ex) {
            for (TextChannel tc : event.getGuild().getTextChannels()) {
                try {
                    tc.sendMessage(intro + "\n" + msg).queue();
                    break;
                } catch (Exception ignored) {}
            }
        }
        UpdateBotLists.updateBotLists(event.getJDA().getGuilds().size(), event.getJDA().getSelfUser().getId());
        UpdateGuildSlashCommands.update(event.getGuild());
    }
}
