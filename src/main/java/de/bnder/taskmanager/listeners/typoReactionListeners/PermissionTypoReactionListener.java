package de.bnder.taskmanager.listeners.typoReactionListeners;

import de.bnder.taskmanager.commands.permission.AddPermission;
import de.bnder.taskmanager.commands.permission.ListUsersOrRolesPermissions;
import de.bnder.taskmanager.commands.permission.RemovePermission;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener.*;

public class PermissionTypoReactionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (!event.getMember().getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
            try {
                event.retrieveMessage().queue(message -> {
                    try {
                        if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅") || event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                            if (isRightMessage(event, "permission")) {
                                if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅")) {
                                    final String command = getCommand(event, "permission");

                                    String beheaded = command.substring(1);
                                    String[] splitBeheaded = beheaded.split(" ");
                                    ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
                                    String[] args = new String[split.size() - 1];
                                    split.subList(1, split.size()).toArray(args);

                                    try {
                                        message.delete().queue();
                                        processPermissionCommand(args, event.getMember(), command, event.getChannel());
                                    } catch (IOException e) {
                                        final String langCode = Localizations.getGuildLanguage(event.getGuild());
                                        MessageSender.send(Localizations.getString("error_title", langCode), Localizations.getString("error_text", langCode) + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber(), event.getChannel(), Color.red, langCode, null);
                                    }
                                } else if (event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                                    try {
                                        message.delete().queue();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                        //TODO: CAN THIS BE REMOVED?
                    } catch (ErrorResponseException | InsufficientPermissionException ignored) {
                    }
                }, (error) -> {});
            } catch (Exception ignored) {}
        }
    }

    void processPermissionCommand(String[] args, Member member, String commandRaw, TextChannel channel) throws IOException {
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")) {
                AddPermission.addPermission(member, channel, args, getMentionedMembers(commandRaw, member.getGuild()), getMentionedRoles(commandRaw, member.getGuild()), null);
            } else if (args[0].equalsIgnoreCase("remove")) {
                RemovePermission.removePermission(member, channel, args, getMentionedMembers(commandRaw, member.getGuild()), getMentionedRoles(commandRaw, member.getGuild()), null);
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                ListUsersOrRolesPermissions.listUsersOrRolesPermissions(member, channel, args, getMentionedMembers(commandRaw, member.getGuild()), getMentionedRoles(commandRaw, member.getGuild()), null);
            }
        }
    }

}
