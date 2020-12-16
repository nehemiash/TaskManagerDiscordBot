package de.bnder.taskmanager.commands;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.Localizations;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;

public class Support implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        event.getChannel().sendMessage(Localizations.getString("support_nachricht", Localizations.getGuildLanguage(event.getGuild()))).queue();
    }
}
