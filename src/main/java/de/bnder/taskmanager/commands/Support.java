package de.bnder.taskmanager.commands;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.Localizations;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.io.IOException;
import java.util.List;

public class Support implements Command {
    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, java.util.List<Member> mentionedMembers, java.util.List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException {
        textChannel.sendMessage(Localizations.getString("support_nachricht", Localizations.getGuildLanguage(guild))).setActionRow(Button.link("https://bnder.net/discord", "bnder.net Discord")).queue();
    }
}
