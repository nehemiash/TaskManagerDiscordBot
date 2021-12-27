package de.bnder.taskmanager.main;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;

public interface Command {

    void action(final String[] args, final String messageContentRaw, final Member commandExecutor, final TextChannel textChannel, final Guild guild, final List<Member> mentionedMembers, final List<Role> mentionedRoles, final List<TextChannel> mentionedChannels, final SlashCommandEvent slashCommandEvent);

}
