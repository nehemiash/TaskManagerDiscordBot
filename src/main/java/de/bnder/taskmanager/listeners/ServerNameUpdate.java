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

import de.bnder.taskmanager.utils.UpdateServer;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

public class ServerNameUpdate extends ListenerAdapter {

    private static final Logger logger = LogManager.getLogger(ServerNameUpdate.class);

    @Override
    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        try {
            UpdateServer.update(event.getGuild());
        } catch (ExecutionException | InterruptedException e) {
            logger.error(e);
        }
    }
}
