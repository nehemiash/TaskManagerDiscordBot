package de.bnder.taskmanager.listeners;

import de.bnder.taskmanager.botlists.UpdateBotLists;
import de.bnder.taskmanager.slashcommands.UpdateGuildSlashCommands;
import de.bnder.taskmanager.utils.DeadlineReminders;
import de.bnder.taskmanager.utils.UpdateServerName;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ExecutionException;

public class Ready extends ListenerAdapter {

    public void onReady(ReadyEvent e) {
        for (Guild g : e.getJDA().getGuilds()) {
            try {
                UpdateServerName.update(g);
                UpdateGuildSlashCommands.update(g);
            } catch (ExecutionException | InterruptedException ignored) {}
        }
        DeadlineReminders.start(e.getJDA().getShardManager());
        UpdateBotLists.updateBotLists(e.getJDA().getGuilds().size(), e.getJDA().getSelfUser().getId());
        e.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
        System.out.println("\n \n##############################\n \nBot started!\n \n##############################");
    }
}
