package de.bnder.taskmanager.commands.group;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.LevenshteinDistance;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
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
        add("rem"); //remove
        add("notifications");
        add("list");
        add("l"); //list
    }};

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, List<Member> mentionedMembers, List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) {
        if (args[0].equalsIgnoreCase("delete")) {
            String groupName = null;
            if (args.length >= 2) {
                groupName = args[1];
            } else {
                String tempGroupName = getGroupNameFromContext(textChannel, commandExecutor, messageContentRaw, mentionedMembers);
                if (tempGroupName != null) {
                    groupName = tempGroupName;
                }
            }
            DeleteGroup.deleteGroup(commandExecutor, textChannel, groupName, slashCommandEvent);
        } else if (args[0].equalsIgnoreCase("members")) {
            String groupName = null;
            if (args.length >= 2) {
                groupName = args[1];
            } else {
                String tempGroupName = getGroupNameFromContext(textChannel, commandExecutor, messageContentRaw, mentionedMembers);
                if (tempGroupName != null) {
                    groupName = tempGroupName;
                }
            }
            GroupMembers.getGroupMembers(commandExecutor, textChannel, groupName, slashCommandEvent);
        } else if (args[0].equalsIgnoreCase("add")) {
            String groupName = null;
            if (args.length >= 3) {
                groupName = args[1 + mentionedMembers.size()];
            } else {
                String tempGroupName = getGroupNameFromContext(textChannel, commandExecutor, messageContentRaw, mentionedMembers);
                if (tempGroupName != null) {
                    groupName = tempGroupName;
                }
            }
            AddGroupMember.addGroupMember(commandExecutor, textChannel, groupName, mentionedMembers, slashCommandEvent);
        } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rem")) {
            String groupName = null;
            if (args.length >= 3) {
                groupName = args[1 + mentionedMembers.size()];
            } else {
                String tempGroupName = getGroupNameFromContext(textChannel, commandExecutor, messageContentRaw, mentionedMembers);
                if (tempGroupName != null) {
                    groupName = tempGroupName;
                }
            }
            RemoveGroupMember.removeGroupMember(commandExecutor, textChannel, groupName, mentionedMembers, slashCommandEvent);
        } else if (args.length > 1) {
            if (args[0].equalsIgnoreCase("create")) {
                CreateGroup.createGroup(commandExecutor, textChannel, args, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("notifications")) {
                GroupNotifications.setGroupNotifications(commandExecutor, textChannel, args, mentionedChannels, slashCommandEvent);
            } else {
                checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
            }
        } else {
            if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")) {
                GroupList.getGroupList(commandExecutor, textChannel, slashCommandEvent);
            } else {
                checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
            }
        }
    }

    String getGroupNameFromContext(TextChannel textChannel, Member commandExecutor, String sourceMessage, List<Member> mentionedMembers) {
        ArrayList<Message> messageArrayList = new ArrayList<>(textChannel.getHistoryBefore(textChannel.getLatestMessageId(), 25).complete().getRetrievedHistory());
        final Locale langCode = Localizations.getGuildLanguage(textChannel.getGuild());
        for (Message message : messageArrayList) {
            if (message.getAuthor().getId().equals(commandExecutor.getUser().getId())) {
                final String messageContentRaw = message.getContentRaw();
                if (messageContentRaw.startsWith(String.valueOf(sourceMessage.split(" ")[0]))) {
                    final String commandArg = messageContentRaw.split(" ")[1];

                    if (commandArg.equalsIgnoreCase("delete") || commandArg.equalsIgnoreCase("members") || commandArg.equalsIgnoreCase("notifications")) {
                        if (messageContentRaw.split(" ").length >= 3) {
                            return messageContentRaw.split(" ")[2];
                        }
                    } else if (commandArg.equalsIgnoreCase("add") || commandArg.equalsIgnoreCase("remove") || commandArg.equalsIgnoreCase("rem")) {
                        if (messageContentRaw.split(" ").length > 3) {
                            return messageContentRaw.split(" ")[2 + mentionedMembers.size()];
                        }
                    }
                }
            } else if (message.getAuthor().getId().equals(message.getJDA().getSelfUser().getId())) {
                if (message.getEmbeds().size() > 0) {
                    final MessageEmbed embed = message.getEmbeds().get(0);
                    // Group create message
                    if (embed.getColor() != null && embed.getColor().getGreen() == 255 && embed.getColor().getRed() == 0 && embed.getColor().getBlue() == 0 && (embed.getTitle() != null && embed.getTitle().startsWith(Localizations.getString("group_title", langCode) + " - "))) {
                        return embed.getTitle().split(Localizations.getString("group_title", langCode) + " - ")[1];
                    }
                }
            }
        }
        return null;
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

                    final String correctedMessageString = correctedMessage.substring(0, correctedMessage.length() - 1);
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
