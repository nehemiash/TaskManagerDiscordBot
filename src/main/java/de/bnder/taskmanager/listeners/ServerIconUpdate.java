package de.bnder.taskmanager.listeners;

import de.bnder.taskmanager.utils.UpdateServer;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

public class ServerIconUpdate extends ListenerAdapter {

    private static final Logger logger = LogManager.getLogger(ServerIconUpdate.class);

    @Override
    public void onGuildUpdateIcon(GuildUpdateIconEvent event) {
        try {
            UpdateServer.update(event.getGuild());
        } catch (ExecutionException | InterruptedException e) {
            logger.error(e);
        }
    }

}
