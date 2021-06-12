package de.bnder.taskmanager.main;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.IOException;
import java.util.List;

public abstract interface Command {

    public abstract void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, List<Member> mentionedMembers, List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException;

}
