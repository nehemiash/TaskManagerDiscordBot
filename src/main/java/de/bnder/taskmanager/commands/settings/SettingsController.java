package de.bnder.taskmanager.commands.settings;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.LevenshteinDistance;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsController implements Command {

    final ArrayList<String> commandArgs = new ArrayList<>() {{
        add("direct-message");
        add("show-done-tasks");
        add("notifications");
    }};

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, java.util.List<Member> mentionedMembers, java.util.List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException {
        final String args0 = args.length > 0 ? args[0].replaceAll("-", "") : null;
        if (args.length == 1) {
            if (args0.equalsIgnoreCase("directmessage")) {
                SettingsDirectmessage.set(commandExecutor, textChannel, slashCommandEvent);
            } else if (args0.equalsIgnoreCase("showdonetasks")) {
                SettingsShowDoneTasks.set(commandExecutor, textChannel, slashCommandEvent);
            } else {
                checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
            }
        } else if (args.length == 0) {
            SettingsShowSettings.set(commandExecutor, textChannel);
        } else if (args.length == 2) {
            if (args0.equalsIgnoreCase("notifychannel")) {
                SettingsNotifyChannel.set(commandExecutor, textChannel, args, mentionedChannels, slashCommandEvent);
            } else {
                checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
            }
        } else if (args.length == 3) {
            if (args0.equalsIgnoreCase("notifications") || args0.equalsIgnoreCase("notification")) {
                SettingsSetNotifications.set(commandExecutor, textChannel, mentionedChannels, mentionedMembers, slashCommandEvent);
            } else {
                checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
            }
        } else {
            checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
        }
    }

    void checkIfTypo(String[] args, String messageContentRaw, Guild guild, TextChannel textChannel, Member commandExecutor, SlashCommandEvent slashCommandEvent) {
        final String langCode = Localizations.getGuildLanguage(guild);
        if (args.length > 0) {
            final String userArg1 = args[0];
            final StringBuilder possibleCommands = new StringBuilder();
            for (String commandArg : commandArgs) {
                final int distance = LevenshteinDistance.levenshteinDistance(commandArg, userArg1);
                if (distance <= 2 && distance != 0) {
                    final StringBuilder correctedMessage = new StringBuilder().append(messageContentRaw.split(" ")[0]).append(" ");
                    correctedMessage.append(commandArg).append(" ");
                    for (int i = 1; i < args.length; i++) {
                        correctedMessage.append(args[i]).append(" ");
                    }

                    final String correctedMessageString = correctedMessage.substring(0, correctedMessage.length());
                    possibleCommands.append(correctedMessageString);
                    break;
                }
            }
            if (possibleCommands.length() > 0) {
                EmbedBuilder builder = new EmbedBuilder().setColor(Color.orange);
                builder.setTitle(Localizations.getString("typo_title", langCode));
                builder.setDescription(Localizations.getString("typo_description", langCode));
                builder.addField(Localizations.getString("typo_field_command_name", langCode), possibleCommands.substring(0, possibleCommands.length() - 1), true);
                builder.addField(Localizations.getString("typo_field_user_name", langCode), commandExecutor.getUser().getAsTag(), true);
                final Message message1 = textChannel.sendMessage(builder.build()).complete();
                message1.addReaction("✅").and(message1.addReaction("❌")).queue();
            } else {
                final String embedTitle = Localizations.getString("permissions_title", langCode);
                final String prefix = String.valueOf(messageContentRaw.charAt(0));
                MessageSender.send(embedTitle, Localizations.getString("settings_invalid_arg", langCode, new ArrayList<String>() {{
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                }}), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            final String embedTitle = Localizations.getString("permissions_title", langCode);
            final String prefix = String.valueOf(messageContentRaw.charAt(0));
            MessageSender.send(embedTitle, Localizations.getString("settings_invalid_arg", langCode, new ArrayList<String>() {{
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
            }}), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }


}
