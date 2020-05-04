package de.bnder.taskmanager.main;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;

public abstract interface Command {

    public abstract void action(String[] args, GuildMessageReceivedEvent event) throws IOException;

}
