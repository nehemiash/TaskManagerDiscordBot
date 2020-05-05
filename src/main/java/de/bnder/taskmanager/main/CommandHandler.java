package de.bnder.taskmanager.main;

import net.dv8tion.jda.api.entities.Message;

import java.io.IOException;
import java.util.HashMap;

public class CommandHandler {

    public static final CommandParser parse = new CommandParser();
    public static final HashMap<String, Command> commands = new HashMap<>();

    public static void handleCommand(CommandParser.commandContainer cmd, Message msg) throws IOException {
        if (commands.containsKey(cmd.invoke.toLowerCase())) {
            commands.get(cmd.invoke.toLowerCase()).action(cmd.args, cmd.event);
        }
    }
}