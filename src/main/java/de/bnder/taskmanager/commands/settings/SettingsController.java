package de.bnder.taskmanager.commands.settings;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.LevenshteinDistance;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsController implements Command {

    final ArrayList<String> commandArgs = new ArrayList<String>() {{
        add("direct-message");
        add("show-done-tasks");
        add("notifications");
    }};

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        final String args0 = args.length > 0 ? args[0].replaceAll("-", "") : null;
        if (args.length == 1) {
            if (args0.equalsIgnoreCase("directmessage")) {
                SettingsDirectmessage.set(event.getMember(), event.getChannel());
            } else if (args0.equalsIgnoreCase("showdonetasks")) {
                SettingsShowDoneTasks.set(event.getMember(), event.getChannel());
            } else {
                checkIfTypo(args, event.getMessage());
            }
        } else if (args.length == 0) {
            SettingsShowSettings.set(event.getMember(), event.getChannel());
        } else if (args.length == 2) {
            if (args0.equalsIgnoreCase("notifychannel")) {
                SettingsNotifyChannel.set(event.getMember(), event.getChannel(), args, event.getMessage().getMentionedChannels());
            } else {
                checkIfTypo(args, event.getMessage());
            }
        } else if (args.length == 3) {
            if (args0.equalsIgnoreCase("notifications") || args0.equalsIgnoreCase("notification")) {
                SettingsSetNotifications.set(event.getMember(), event.getChannel(), event.getMessage().getMentionedChannels(), event.getMessage().getMentionedMembers());
            } else {
                checkIfTypo(args, event.getMessage());
            }
        } else {
            checkIfTypo(args, event.getMessage());
        }
    }

    void checkIfTypo(String[] args, Message message) {
        final String langCode = Localizations.getGuildLanguage(message.getGuild());
        if (args.length > 0) {
            final String userArg1 = args[0];
            final StringBuilder possibleCommands = new StringBuilder();
            for (String commandArg : commandArgs) {
                final int distance = LevenshteinDistance.levenshteinDistance(commandArg, userArg1);
                if (distance <= 2 && distance != 0) {
                    final StringBuilder correctedMessage = new StringBuilder().append(message.getContentRaw().split(" ")[0]).append(" ");
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
                builder.addField(Localizations.getString("typo_field_user_name", langCode), message.getAuthor().getAsTag(), true);
                final Message message1 = message.getChannel().sendMessage(builder.build()).complete();
                message1.addReaction("✅").and(message1.addReaction("❌")).queue();
            } else {
                final String embedTitle = Localizations.getString("permissions_title", langCode);
                final String prefix = String.valueOf(message.getContentRaw().charAt(0));
                MessageSender.send(embedTitle, Localizations.getString("settings_invalid_arg", langCode, new ArrayList<String>() {{
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                }}), message.getTextChannel(), Color.red);
            }
        } else {
            final String embedTitle = Localizations.getString("permissions_title", langCode);
            final String prefix = String.valueOf(message.getContentRaw().charAt(0));
            MessageSender.send(embedTitle, Localizations.getString("settings_invalid_arg", langCode, new ArrayList<String>() {{
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
            }}), message.getTextChannel(), Color.red);
        }
    }


}
