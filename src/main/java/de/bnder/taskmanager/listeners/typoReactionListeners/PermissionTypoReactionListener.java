package de.bnder.taskmanager.listeners.typoReactionListeners;

import de.bnder.taskmanager.main.CommandHandler;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import static de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener.*;

public class PermissionTypoReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.isFromGuild())
            event.retrieveMember().queue(member -> {
                if (!member.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
                    event.retrieveMessage().queue(message -> {
                        if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅") || event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                            if (isRightMessage(message, "permission", member) || isRightMessage(message, "p", member)) {
                                if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅")) {
                                    String command = getCommand(message, "permission", member);
                                    if (command == null)
                                        command = getCommand(message, "p", member);

                                    String beheaded = command.substring(1);
                                    String[] splitBeheaded = beheaded.split(" ");
                                    ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
                                    String[] args = new String[split.size() - 1];
                                    split.subList(1, split.size()).toArray(args);


                                    message.delete().queue();
                                    CommandHandler.handleCommand(CommandHandler.parse.parseSlashCommand(command, event.getMember(), event.getTextChannel(), event.getGuild(), getMentionedMembers(command, member.getGuild()), getMentionedRoles(command, member.getGuild()), getMentionedChannels(command, member.getGuild()), null));
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
}
