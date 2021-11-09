package de.bnder.taskmanager.listeners.typoReactionListeners;

import de.bnder.taskmanager.commands.permission.AddPermission;
import de.bnder.taskmanager.commands.permission.ListUsersOrRolesPermissions;
import de.bnder.taskmanager.commands.permission.RemovePermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import static de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener.*;

public class PermissionTypoReactionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        event.retrieveMember().queue(member -> {
            if (!member.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
                event.retrieveMessage().queue(message -> {
                    if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅") || event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                        if (isRightMessage(message, "permission", member)) {
                            if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅")) {
                                final String command = getCommand(message, "permission", member);

                                String beheaded = command.substring(1);
                                String[] splitBeheaded = beheaded.split(" ");
                                ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
                                String[] args = new String[split.size() - 1];
                                split.subList(1, split.size()).toArray(args);


                                message.delete().queue();
                                processPermissionCommand(args, member, command, event.getChannel());
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

    void processPermissionCommand(String[] args, Member member, String commandRaw, TextChannel channel) {
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
