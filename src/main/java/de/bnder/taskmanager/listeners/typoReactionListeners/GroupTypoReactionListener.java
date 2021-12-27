package de.bnder.taskmanager.listeners.typoReactionListeners;

import de.bnder.taskmanager.commands.group.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import static de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener.*;

public class GroupTypoReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.isFromGuild())
        event.retrieveMember().queue(member -> {
            if (!member.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
                event.retrieveMessage().queue(message -> {
                    if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅") || event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                        if (isRightMessage(message, "group", member)) {
                            if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅")) {
                                final String command = getCommand(message, "group", member);

                                String beheaded = command.substring(1);
                                String[] splitBeheaded = beheaded.split(" ");
                                ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
                                String[] args = new String[split.size() - 1];
                                split.subList(1, split.size()).toArray(args);

                                message.delete().queue();
                                processGroupCommand(args, member, command, event.getTextChannel());

                            } else if (event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                                message.delete().queue();
                            }
                        }
                    }
                }, (error) -> {
                });
            }
        });
    }

    void processGroupCommand(String[] args, Member member, String commandRaw, TextChannel channel) {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("create")) {
                CreateGroup.createGroup(member, channel, args, null);
            } else if (args[0].equalsIgnoreCase("delete")) {
                DeleteGroup.deleteGroup(member, channel, args[1], null);
            } else if (args[0].equalsIgnoreCase("members")) {
                GroupMembers.getGroupMembers(member, channel, args[1], null);
            } else if (args[0].equalsIgnoreCase("add")) {
                AddGroupMember.addGroupMember(member, channel, args[1 + getMentionedMembers(commandRaw, member.getGuild()).size()], getMentionedMembers(commandRaw, member.getGuild()), null);
            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rem")) {
                RemoveGroupMember.removeGroupMember(member, channel, args[1 + getMentionedMembers(commandRaw, member.getGuild()).size()], getMentionedMembers(commandRaw, member.getGuild()), null);
            } else if (args[0].equalsIgnoreCase("notifications") || args[0].equalsIgnoreCase("notification")) {
                GroupNotifications.setGroupNotifications(member, channel, args, getMentionedChannels(commandRaw, member.getGuild()), null);
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                GroupList.getGroupList(member, channel, null);
            }
        }
    }
}
