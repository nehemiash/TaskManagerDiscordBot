package de.bnder.taskmanager.listeners;

import de.bnder.taskmanager.utils.UpdateServer;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ExecutionException;

public class ServerIconUpdate extends ListenerAdapter {

    @Override
    public void onGuildUpdateIcon(GuildUpdateIconEvent event) {
        try {
            UpdateServer.update(event.getGuild());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
