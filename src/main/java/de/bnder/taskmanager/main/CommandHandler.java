package de.bnder.taskmanager.main;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;

public class CommandHandler {

    public static final CommandParser parse = new CommandParser();
    public static final HashMap<String, Command> commands = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(CommandHandler.class);

    public static void handleCommand(CommandParser.commandContainer cmd) {
        if (commands.containsKey(cmd.invoke.toLowerCase())) {
            final String[] args = cmd.args;
            final String msgContentRaw = cmd.raw;
            final Member commandExecutor = cmd.commandExecutor;
            final TextChannel textChannel = cmd.textChannel;
            final Guild guild = cmd.guild;
            final List<Member> mentionedMembers = cmd.mentionedMembers;
            final List<Role> mentionedRoles = cmd.mentionedRoles;
            final List<TextChannel> mentionedChannels = cmd.mentionedChannels;
            final SlashCommandEvent slashCommandEvent = cmd.slashCommandEvent;
            commands.get(cmd.invoke.toLowerCase()).action(args, msgContentRaw, commandExecutor, textChannel, guild, mentionedMembers, mentionedRoles, mentionedChannels, slashCommandEvent);
            logger.info("Received command \"" + msgContentRaw + "\"");
        }
    }
}