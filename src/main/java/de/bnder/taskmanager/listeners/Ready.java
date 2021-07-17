package de.bnder.taskmanager.listeners;

import de.bnder.taskmanager.lists.UpdateLists;
import de.bnder.taskmanager.utils.DeadlineReminders;
import de.bnder.taskmanager.utils.UpdateServerName;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

public class Ready extends ListenerAdapter {

    public void onReady(ReadyEvent e) {
        for (Guild g : e.getJDA().getGuilds()) {
            try {
                UpdateServerName.update(g);
            } catch (IOException ignored) {
            }
        }
        DeadlineReminders.start(e.getJDA().getShardManager());
        UpdateLists.updateBotLists(e.getJDA().getGuilds().size(), e.getJDA().getSelfUser().getId());
        e.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
    }

    void sendToSUpdate(Guild guild) {
        final String msg = """
                ```Hello!

                Some changes are pending! The changes to the terms of use and data protection declaration for bnder's bots will come into force on July 14, 2021.

                The updated terms of use will replace the old terms of use. In addition, our data protection declaration will be updated.

                You can see the terms of use here: https://bnder.net/termsofuse. You can find the updated data protection declaration here: https://bnder.net/privacy.```""";
        if (guild.getDefaultChannel() != null) {
            try {
                guild.getDefaultChannel().sendMessage(msg).queue();
            } catch (Exception ignored) {
                for (TextChannel tc : guild.getTextChannels()) {
                    try {
                        tc.sendMessage(msg).queue();
                        System.out.println("Sent ToS message to Guild " + guild.getName());
                        break;
                    } catch (Exception ignored1) {
                    }
                }
            }
        } else {
            for (TextChannel tc : guild.getTextChannels()) {
                try {
                    tc.sendMessage(msg).queue();
                    System.out.println("Sent ToS message to Guild " + guild.getName());
                    break;
                } catch (Exception ignored1) {
                }
            }
        }
    }

}
