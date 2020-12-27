package de.bnder.taskmanager.commands;

import de.bnder.taskmanager.main.Command;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;

public class Search implements Command {

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        if (args.length > 0) {
            
        }
    }
}
