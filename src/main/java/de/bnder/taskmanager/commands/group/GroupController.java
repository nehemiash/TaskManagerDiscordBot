package de.bnder.taskmanager.commands.group;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.LevenshteinDistance;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GroupController implements Command {

    final ArrayList<String> commandArgs = new ArrayList<>() {{
        add("create");
        add("delete");
        add("members");
        add("add");
        add("remove");
        add("rem");
        add("notifications");
        add("list");
    }};

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, List<Member> mentionedMembers, List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("create")) {
                CreateGroup.createGroup(commandExecutor, textChannel, args, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("delete")) {
                DeleteGroup.deleteGroup(commandExecutor, textChannel, args, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("members")) {
                GroupMembers.getGroupMembers(commandExecutor, textChannel, args, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("add")) {
                AddGroupMember.addGroupMember(commandExecutor, textChannel, args, mentionedMembers, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rem")) {
                RemoveGroupMember.removeGroupMember(commandExecutor, textChannel, args, mentionedMembers, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("notifications")) {
                GroupNotifications.setGroupNotifications(commandExecutor, textChannel, args, mentionedChannels, slashCommandEvent);
            } else {
                checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                GroupList.getGroupList(commandExecutor, textChannel, slashCommandEvent);
            } else {
                checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
            }
        } else {
            checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
        }
    }

    void checkIfTypo(String[] args, String messageContentRaw, Guild guild, TextChannel textChannel, Member commandExecutor, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(guild);
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
                textChannel.sendMessageEmbeds(builder.build()).queue(message1 -> {
                    message1.addReaction("✅").and(message1.addReaction("❌")).queue();
                });
            } else {
                final String embedTitle = Localizations.getString("group_title", langCode);
                final String prefix = String.valueOf(messageContentRaw.charAt(0));
                MessageSender.send(embedTitle, Localizations.getString("help_message_group_commands", langCode, new ArrayList<String>() {{
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                }}), textChannel, Color.red, langCode, null);
            }
        } else {
            final String embedTitle = Localizations.getString("group_title", langCode);
            final String prefix = String.valueOf(messageContentRaw.charAt(0));
            MessageSender.send(embedTitle, Localizations.getString("help_message_group_commands", langCode, new ArrayList<String>() {{
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
            }}), textChannel, Color.red, langCode, null);
        }
    }
}
