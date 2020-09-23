package de.bnder.taskmanager.commands;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.GroupPermission;
import de.bnder.taskmanager.utils.TaskPermission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;

public class Permission implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")) {
                if (event.getMessage().getMentionedMembers().size() > 0) {
                    if (TaskPermission.valueOf(args[2].toUpperCase()) != null || GroupPermission.valueOf(args[2].toUpperCase()) != null) {
                        //TODO
                    }
                }
            }
        }
    }
}
