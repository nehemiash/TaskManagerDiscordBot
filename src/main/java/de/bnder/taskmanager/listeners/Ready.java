package de.bnder.taskmanager.listeners;

import de.bnder.taskmanager.botlists.UpdateBotLists;
import de.bnder.taskmanager.slashcommands.UpdateGuildSlashCommands;
import de.bnder.taskmanager.utils.DeadlineReminders;
import de.bnder.taskmanager.utils.UpdateServer;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

public class Ready extends ListenerAdapter {

    private static final Logger logger = LogManager.getLogger(Ready.class);

    @Override
    public void onReady(ReadyEvent event) {
        for (Guild g : event.getJDA().getGuilds()) {
            try {
                UpdateServer.update(g);
                UpdateGuildSlashCommands.update(g);
            } catch (ExecutionException | InterruptedException ignored) {}
        }
        DeadlineReminders.start(event.getJDA().getShardManager());
        UpdateBotLists.updateBotLists(event.getJDA().getGuilds().size(), event.getJDA().getSelfUser().getId());
        event.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
        logger.info("Bot started!");
    }
}
