package de.bnder.taskmanager.listeners.typoReactionListeners;

import de.bnder.taskmanager.commands.board.BoardList;
import de.bnder.taskmanager.commands.board.CreateBoard;
import de.bnder.taskmanager.commands.board.DeleteBoard;
import de.bnder.taskmanager.commands.board.SwitchBoard;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import static de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener.getCommand;
import static de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener.isRightMessage;

public class BoardTypoReactionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        event.retrieveMember().queue(member -> {
            if (!member.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
                event.retrieveMessage().queue(message -> {
                    if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅") || event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                        if (isRightMessage(message, "board", member)) {
                            if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅")) {
                                final String command = getCommand(message, "board", member);

                                String beheaded = command.substring(1);
                                String[] splitBeheaded = beheaded.split(" ");
                                ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
                                String[] args = new String[split.size() - 1];
                                split.subList(1, split.size()).toArray(args);

                                message.delete().queue();
                                processGroupCommand(args, member, command, event.getChannel());

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

    void processGroupCommand(String[] args, Member member, String commandRaw, TextChannel channel) {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("create")) {
                CreateBoard.createBoard(args[1], channel, member, null);
            } else if (args[0].equalsIgnoreCase("switch")) {
                SwitchBoard.switchBoard(args[1], member, channel, null);
            } else if (args[0].equalsIgnoreCase("delete")) {
                DeleteBoard.deleteBoard(member, channel, args[1], null);
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                BoardList.getBoardList(member, channel, String.valueOf(commandRaw.charAt(0)), null);
            }
        }
    }
}
