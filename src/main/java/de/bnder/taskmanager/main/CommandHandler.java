package de.bnder.taskmanager.main;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CommandHandler {

    public static final CommandParser parse = new CommandParser();
    public static final HashMap<String, Command> commands = new HashMap<>();

    public static void handleCommand(CommandParser.commandContainer cmd) throws IOException {
        if (commands.containsKey(cmd.invoke.toLowerCase())) {
            final String[] args = cmd.args;
            final String msgContentRaw = cmd.raw;
            final Member commandExecutor = null;
            final TextChannel textChannel = null;
            final Guild guild = null;
            final List<Member> mentionedMembers = null;
            final List<Role> mentionedRoles = null;
            final List<TextChannel> mentionedChannels = null;
            final SlashCommandEvent slashCommandEvent = null;
            commands.get(cmd.invoke.toLowerCase()).action(args, msgContentRaw, commandExecutor, textChannel, guild, mentionedMembers, mentionedRoles, mentionedChannels, slashCommandEvent);
        }
    }
}