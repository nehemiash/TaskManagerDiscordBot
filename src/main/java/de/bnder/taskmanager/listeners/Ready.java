package de.bnder.taskmanager.listeners;

import de.bnder.taskmanager.lists.UpdateLists;
import de.bnder.taskmanager.slashcommands.UpdateGuildSlashCommands;
import de.bnder.taskmanager.utils.DeadlineReminders;
import de.bnder.taskmanager.utils.UpdateServerName;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

public class Ready extends ListenerAdapter {

    public void onReady(ReadyEvent e) {
        for (Guild g : e.getJDA().getGuilds()) {
            try {
                UpdateServerName.update(g);
                UpdateGuildSlashCommands.update(g);
            } catch (IOException ignored) {}
        }
        DeadlineReminders.start(e.getJDA().getShardManager());
        UpdateLists.updateBotLists(e.getJDA().getGuilds().size(), e.getJDA().getSelfUser().getId());
        e.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
    }

}
