package de.bnder.taskmanager.listeners.typoReactionListeners;

import de.bnder.taskmanager.commands.settings.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import static de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener.*;

public class SettingsTypoReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.isFromGuild())
        event.retrieveMember().queue(member -> {
            if (!member.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
                event.retrieveMessage().queue(message -> {
                    if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅") || event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                        if (isRightMessage(message, "settings", member)) {
                            if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅")) {
                                final String command = getCommand(message, "settings", member);

                                String beheaded = command.substring(1);
                                String[] splitBeheaded = beheaded.split(" ");
                                ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
                                String[] args = new String[split.size() - 1];
                                split.subList(1, split.size()).toArray(args);

                                message.delete().queue();
                                processSettingsCommand(args, member, command, event.getTextChannel());
                            } else if (event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                                try {
                                    message.delete().queue();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }, (error) -> {
                });
            }
        });
    }

    void processSettingsCommand(String[] args, Member member, String commandRaw, TextChannel channel) {
        final String args0 = args.length > 0 ? args[0].replaceAll("-", "") : null;
        if (args.length == 1) {
            if (args0.equalsIgnoreCase("directmessage")) {
                SettingsDirectmessage.set(member, channel, null);
            } else if (args0.equalsIgnoreCase("showdonetasks")) {
                SettingsShowDoneTasks.set(member, channel, null);
            }
        } else if (args.length == 0) {
            SettingsShowSettings.set(member, channel, null);
        } else if (args.length == 2) {
            if (args0.equalsIgnoreCase("notifychannel")) {
                SettingsNotifyChannel.set(member, channel, args, getMentionedChannels(commandRaw, member.getGuild()), null);
            }
        } else if (args.length == 3) {
            if (args0.equalsIgnoreCase("notifications") || args0.equalsIgnoreCase("notification")) {
                SettingsSetNotifications.set(member, channel, getMentionedChannels(commandRaw, member.getGuild()), getMentionedMembers(commandRaw, member.getGuild()), null);
            }
        }
    }
}
