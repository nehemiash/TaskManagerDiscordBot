package de.bnder.taskmanager.listeners.typoReactionListeners;

import de.bnder.taskmanager.commands.group.*;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener.*;

public class GroupTypoReactionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (!event.getGuild().getId().equalsIgnoreCase("110373943822540800")) {
            final String langCode = Localizations.getGuildLanguage(event.getGuild());
            if (isRightMessage(event, "group", langCode)) {
                final Message message = event.retrieveMessage().complete();
                if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅")) {
                    final String command = TaskTypoReactionListener.getCommand(event, "group", langCode);

                    String beheaded = command.substring(1);
                    String[] splitBeheaded = beheaded.split(" ");
                    ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
                    String[] args = new String[split.size() - 1];
                    split.subList(1, split.size()).toArray(args);

                    try {
                        message.delete().queue();
                        processGroupCommand(args, event.getMember(), command, event.getChannel());
                    } catch (IOException e) {
                        e.printStackTrace();
                        MessageSender.send(Localizations.getString("error_title", langCode), Localizations.getString("error_text", langCode) + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber(), event.getChannel(), Color.red);
                    }
                } else if (event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                    message.delete().queue();
                }
            }
        }
    }

    void processGroupCommand(String[] args, Member member, String commandRaw, TextChannel channel) throws IOException {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("create")) {
                CreateGroup.createGroup(member, channel, args);
            } else if (args[0].equalsIgnoreCase("delete")) {
                DeleteGroup.deleteGroup(member, channel, args);
            } else if (args[0].equalsIgnoreCase("members")) {
                GroupMembers.getGroupMembers(member, channel, args);
            } else if (args[0].equalsIgnoreCase("add")) {
                AddGroupMember.addGroupMember(member, channel, args, getMentionedMembers(commandRaw, member.getGuild()));
            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rem")) {
                RemoveGroupMember.removeGroupMember(member, channel, args, getMentionedMembers(commandRaw, member.getGuild()));
            } else if (args[0].equalsIgnoreCase("notifications") || args[0].equalsIgnoreCase("notification")) {
                GroupNotifications.setGroupNotifications(member, channel, args, getMentionedChannels(commandRaw, member.getGuild()));
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                GroupList.getGroupList(member, channel);
            }
        }
    }
}
