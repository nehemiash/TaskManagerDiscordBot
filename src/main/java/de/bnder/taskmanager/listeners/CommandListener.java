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

import com.google.cloud.firestore.DocumentSnapshot;
import de.bnder.taskmanager.main.CommandHandler;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.RegisterUser;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CommandListener extends ListenerAdapter {

    private static final Logger logger = LogManager.getLogger(CommandListener.class);

    /**
     * The listener which waits for a message received event
     *
     * @param event MessageReceivedEvent
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot() && event.isFromGuild()) {
            if (event.getMessage().getContentRaw().length() > 0) {
                if (CommandHandler.commands.containsKey(event.getMessage().getContentRaw().split(" ")[0].substring(1).toLowerCase())) {
                    try {
                        final DocumentSnapshot serverReference = Main.firestore.collection("server").document(event.getGuild().getId()).get().get();
                        if (serverReference.get("prefix") != null) {
                            final String prefix = serverReference.getString("prefix");
                            if (event.getMessage().getContentRaw().startsWith(prefix)) {
                                processNormalCommand(event);
                            }
                        } else if (event.getMessage().getContentRaw().startsWith(Main.prefix)) {
                            processNormalCommand(event);
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        final Locale langCode = Localizations.getGuildLanguage(event.getGuild());
                        MessageSender.send(Localizations.getString("error_title", langCode), Localizations.getString("error_text", langCode) + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber(), event.getMessage(), Color.red, langCode, null);
                    }
                } else if (event.getMessage().getContentRaw().startsWith("<@!" + event.getJDA().getSelfUser().getId() + ">")) {
                    processNormalCommandWithMentionAsPrefix(event);
                }
            }
        }
    }

    /**
     * Waits for the SlashCommandEvent
     *
     * @param event SlashCommandEvent
     */
    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        final String commandName = event.getName();
        final String arg0 = " " + (event.getSubcommandName() != null ? event.getSubcommandName() : "");
        final List<OptionMapping> options = event.getOptions();
        if (!event.getUser().isBot()) {
            StringBuilder msg = new StringBuilder("-" + commandName + arg0);
            final List<Member> mentionedMembers = new ArrayList<>();
            final List<Role> mentionedRoles = new ArrayList<>();
            final List<TextChannel> mentionedChannels = new ArrayList<>();
            for (OptionMapping option : options) {
                msg.append(" ");
                switch (option.getType()) {
                    case CHANNEL -> {
                        msg.append("<#").append(option.getAsGuildChannel().getId()).append(">");
                        if (option.getAsGuildChannel().getType() == ChannelType.TEXT) {
                            mentionedChannels.add(Objects.requireNonNull(event.getGuild()).getTextChannelById(option.getAsGuildChannel().getId()));
                        }
                    }
                    case STRING, INTEGER, BOOLEAN, SUB_COMMAND -> msg.append(option.getAsString());
                    case USER -> {
                        msg.append("<@!").append(option.getAsUser().getId()).append(">");
                        mentionedMembers.add(option.getAsMember());
                        RegisterUser.register(option.getAsMember());
                    }
                    case ROLE -> {
                        msg.append("<@&").append(option.getAsRole().getId()).append(">");
                        mentionedRoles.add(option.getAsRole());
                    }
                    case MENTIONABLE -> {
                        String id = option.getAsString();
                        if (event.getGuild().getRoleById(id) != null) {
                            msg.append("<@&").append(id).append(">");
                            mentionedRoles.add(event.getGuild().getRoleById(id));
                        } else if (event.getGuild().retrieveMemberById(id).complete() != null) {
                            final Member member = event.getGuild().retrieveMemberById(id).complete();
                            msg.append("<@!").append(member.getId()).append(">");
                            mentionedMembers.add(member);
                            RegisterUser.register(member);
                        }
                    }
                }
            }
            while (msg.toString().contains("  ")) {
                msg = new StringBuilder(msg.toString().replace("  ", " "));
            }


            CommandHandler.handleCommand(CommandHandler.parse.parseSlashCommand(msg.toString(), event.getMember(), event.getTextChannel(), event.getGuild(), mentionedMembers, mentionedRoles, mentionedChannels, event));
            RegisterUser.register(event.getMember());
        }
    }

    /**
     * Process the normal command by replacing all double spaces and registering the user.
     *
     * @param event MessageReceivedEvent given by the listener
     */
    private void processNormalCommand(MessageReceivedEvent event) {
        try {
            String msg = event.getMessage().getContentRaw();
            while (msg.contains("  ")) {
                msg = msg.replace("  ", " ");
            }
            CommandHandler.handleCommand(CommandHandler.parse.parseNormalCommand(msg, event.getMember(), event.getTextChannel(), event.getGuild(), event.getMessage().getMentionedMembers(), event.getMessage().getMentionedRoles(), event.getMessage().getMentionedChannels()));
        } catch (Exception e) {
            logger.error(e);
            final Locale langCode = Localizations.getGuildLanguage(event.getGuild());
            MessageSender.send(Localizations.getString("error_title", langCode), Localizations.getString("error_text", langCode), event.getMessage(), Color.red, langCode, null);
        }
        RegisterUser.register(event.getMember());
    }

    /**
     * Processes the normal command by replacing all double spaces and registering the user. Removes the mention from start of message.
     *
     * @param event MessageReceivedEvent given by the listener.
     */
    private void processNormalCommandWithMentionAsPrefix(MessageReceivedEvent event) {
        try {
            String msg = event.getMessage().getContentRaw();
            while (msg.contains("  ")) {
                msg = msg.replace("  ", " ");
            }
            CommandHandler.handleCommand(CommandHandler.parse.parseNormalCommandWithMentionPrefix(msg, event.getMember(), event.getTextChannel(), event.getGuild(), event.getMessage().getMentionedMembers(), event.getMessage().getMentionedRoles(), event.getMessage().getMentionedChannels()));
        } catch (Exception e) {
            logger.error(e);
            final Locale langCode = Localizations.getGuildLanguage(event.getGuild());
            MessageSender.send(Localizations.getString("error_title", langCode), Localizations.getString("error_text", langCode), event.getMessage(), Color.red, langCode, null);
        }
        RegisterUser.register(event.getMember());
    }
}
