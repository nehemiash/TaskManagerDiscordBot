package de.bnder.taskmanager.listeners.typoReactionListeners;

import de.bnder.taskmanager.commands.board.BoardList;
import de.bnder.taskmanager.commands.board.CreateBoard;
import de.bnder.taskmanager.commands.board.DeleteBoard;
import de.bnder.taskmanager.commands.board.SwitchBoard;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener.getCommand;
import static de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener.isRightMessage;

public class BoardTypoReactionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (!event.getMember().getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
            try {
                final Message message = event.retrieveMessage().complete();
                if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅") || event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                    if (isRightMessage(event, "board")) {
                        if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅")) {
                            final String command = getCommand(event, "board");

                            String beheaded = command.substring(1);
                            String[] splitBeheaded = beheaded.split(" ");
                            ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
                            String[] args = new String[split.size() - 1];
                            split.subList(1, split.size()).toArray(args);

                            try {
                                message.delete().queue();
                                processGroupCommand(args, event.getMember(), command, event.getChannel());
                            } catch (IOException e) {
                                final String langCode = Localizations.getGuildLanguage(event.getGuild());
                                MessageSender.send(Localizations.getString("error_title", langCode), Localizations.getString("error_text", langCode) + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber(), event.getChannel(), Color.red, langCode);
                            }
                        } else if (event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                            try {
                                message.delete().queue();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            } catch (ErrorResponseException ignored) {
            }
        }
    }

    void processGroupCommand(String[] args, Member member, String commandRaw, TextChannel channel) throws IOException {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("create")) {
                CreateBoard.createBoard(args[1], channel, member);
            } else if (args[0].equalsIgnoreCase("switch")) {
                SwitchBoard.switchBoard(args[1], member, channel);
            } else if (args[0].equalsIgnoreCase("delete")) {
                DeleteBoard.deleteBoard(member, channel, args[1]);
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                BoardList.getBoardList(member, channel, String.valueOf(commandRaw.charAt(0)));
            }
        }
    }
}
