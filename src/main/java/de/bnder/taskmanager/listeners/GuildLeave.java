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
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class GuildLeave extends ListenerAdapter {

    private static final Logger logger = LogManager.getLogger(GuildLeave.class);

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        try {
            Main.firestore.collection("server").document(event.getGuild().getId()).delete();
        } catch (Exception e) {
            logger.error(e);
        }
        UpdateBotLists.updateBotLists(event.getJDA().getGuilds().size(), event.getJDA().getSelfUser().getId());
    }
}