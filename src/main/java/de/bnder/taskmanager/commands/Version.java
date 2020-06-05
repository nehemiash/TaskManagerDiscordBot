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
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class Version implements Command {

    public static String version = "2020.1.2";

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        final String langCode = Localizations.Companion.getGuildLanguage(event.getGuild());
        MessageSender.send(Localizations.Companion.getString("version_title", langCode), Localizations.Companion.getString("version_text", langCode, new ArrayList<String>(){{
            add(version);
        }}), event.getMessage(), Color.cyan);
    }
}
