package de.bnder.taskmanager.main;

import de.bnder.taskmanager.utils.RegisterUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandParser {

    public commandContainer parse(String raw, Member commandExecutor, TextChannel textChannel, Guild guild, SlashCommandEvent slashCommandEvent) {
        String beheaded = raw.substring(1);
        String[] splitBeheaded = beheaded.split(" ");
        String invoke = splitBeheaded[0].toLowerCase();
        ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
        String[] args = new String[split.size() - 1];
        split.subList(1, split.size()).toArray(args);

        List<Member> mentionedMembers = new ArrayList<>();
        List<Role> mentionedRoles = new ArrayList<>();
        List<TextChannel> mentionedChannels = new ArrayList<>();

        for (String a : beheaded.split("<")) {
            for (String b : a.split(">")) {
                if (b.startsWith("#")) {
                    final String textChannelID = b.substring(1);
                    if (guild.getTextChannelById(textChannelID) != null) {
                        mentionedChannels.add(guild.getTextChannelById(textChannelID));
                    }
                } else if (b.startsWith("@!")) {
                    final String userID = b.substring(2);
                    Member member = null;
                    if (guild.getMemberById(userID) != null) {
                        member = guild.getMemberById(userID);
                    } else if (guild.retrieveMemberById(userID).complete() != null) {
                        member = guild.retrieveMemberById(userID).complete();
                    }
                    if (member != null) {
                        mentionedMembers.add(member);
                        RegisterUser.register(member);
                    }
                } else if (b.startsWith("@&")) {
                    final String roleID = b.substring(2);
                    if (guild.getRoleById(roleID) != null) {
                        mentionedRoles.add(guild.getRoleById(roleID));
                    }
                }
            }
        }

        return new commandContainer(raw, beheaded, splitBeheaded, invoke, args, commandExecutor, textChannel, guild, mentionedMembers, mentionedRoles, mentionedChannels, slashCommandEvent);
    }

    public commandContainer parseNormalCommand(String raw, Member commandExecutor, TextChannel textChannel, Guild guild, List<Member> mentionedMembers, List<Role> mentionedRoles, List<TextChannel> mentionedChannels) {

        String beheaded = raw.substring(1);
        String[] splitBeheaded = beheaded.split(" ");
        String invoke = splitBeheaded[0].toLowerCase();
        ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
        String[] args = new String[split.size() - 1];
        split.subList(1, split.size()).toArray(args);

        return new commandContainer(raw, beheaded, splitBeheaded, invoke, args, commandExecutor, textChannel, guild, mentionedMembers, mentionedRoles, mentionedChannels, null);
    }

    public commandContainer parseSlashCommand(String raw, Member commandExecutor, TextChannel textChannel, Guild guild, List<Member> mentionedMembers, List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) {

        String beheaded = raw.substring(1);
        String[] splitBeheaded = beheaded.split(" ");
        String invoke = splitBeheaded[0].toLowerCase();
        ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
        String[] args = new String[split.size() - 1];
        split.subList(1, split.size()).toArray(args);

        return new commandContainer(raw, beheaded, splitBeheaded, invoke, args, commandExecutor, textChannel, guild, mentionedMembers, mentionedRoles, mentionedChannels, slashCommandEvent);
    }


    public static class commandContainer {

        public final String raw;
        public final String beheaded;
        public final String[] splitBeheaded;
        public final String invoke;
        public final String[] args;
        public final Member commandExecutor;
        public final TextChannel textChannel;
        public final Guild guild;
        public final List<Member> mentionedMembers;
        public final List<Role> mentionedRoles;
        public final List<TextChannel> mentionedChannels;
        public final SlashCommandEvent slashCommandEvent;


        public commandContainer(String rw, String beheaded, String[] splitBeheaded, String invoke, String[] args, Member commandExecutor, TextChannel textChannel, Guild guild, List<Member> mentionedMembers, List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) {
            this.raw = rw;
            this.beheaded = beheaded;
            this.splitBeheaded = splitBeheaded;
            this.invoke = invoke;
            this.args = args;
            this.commandExecutor = commandExecutor;
            this.textChannel = textChannel;
            this.guild = guild;
            this.mentionedMembers = mentionedMembers;
            this.mentionedRoles = mentionedRoles;
            this.mentionedChannels = mentionedChannels;
            this.slashCommandEvent = slashCommandEvent;
        }

    }

}
