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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class Version implements Command {

    public static String version = "2021.2.0";

    @Override
    public void action(String[] args, Member commandExecutor, TextChannel textChannel, Guild guild) throws IOException {
        final String langCode = Localizations.getGuildLanguage(guild);
        MessageSender.send(Localizations.getString("version_title", langCode), Localizations.getString("version_text", langCode, new ArrayList<>() {{
            add(version);
        }}), textChannel, Color.cyan, langCode);
    }
}
