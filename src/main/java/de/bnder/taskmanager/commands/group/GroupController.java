package de.bnder.taskmanager.commands.group;

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

public class GroupController implements Command {

    final ArrayList<String> commandArgs = new ArrayList<String>() {{
        add("create");
        add("delete");
        add("members");
        add("add");
        add("remove");
        add("rem");
        add("notifications");
        add("notification");
        add("list");
    }};

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("create")) {
                CreateGroup.createGroup(event.getMember(), event.getChannel(), args);
            } else if (args[0].equalsIgnoreCase("delete")) {
                DeleteGroup.deleteGroup(event.getMember(), event.getChannel(), args);
            } else if (args[0].equalsIgnoreCase("members")) {
                GroupMembers.getGroupMembers(event.getMember(), event.getChannel(), args);
            } else if (args[0].equalsIgnoreCase("add")) {
                AddGroupMember.addGroupMember(event.getMember(), event.getChannel(), args, event.getMessage().getMentionedMembers());
            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rem")) {
                RemoveGroupMember.removeGroupMember(event.getMember(), event.getChannel(), args, event.getMessage().getMentionedMembers());
            } else if (args[0].equalsIgnoreCase("notifications") || args[0].equalsIgnoreCase("notification")) {
                GroupNotifications.setGroupNotifications(event.getMember(), event.getChannel(), args, event.getMessage().getMentionedChannels());
            } else {
                checkIfTypo(args, event.getMessage());
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                GroupList.getGroupList(event.getMember(), event.getChannel());
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
                final String embedTitle = Localizations.getString("task_message_title", langCode);
                final String prefix = String.valueOf(message.getContentRaw().charAt(0));
                MessageSender.send(embedTitle, Localizations.getString("help_message_group_commands", langCode, new ArrayList<String>() {{
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                }}), message, Color.red);
            }
        } else {
            final String embedTitle = Localizations.getString("task_message_title", langCode);
            final String prefix = String.valueOf(message.getContentRaw().charAt(0));
            MessageSender.send(embedTitle, Localizations.getString("help_message_group_commands", langCode, new ArrayList<String>() {{
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
            }}), message, Color.red);
        }
    }
}
