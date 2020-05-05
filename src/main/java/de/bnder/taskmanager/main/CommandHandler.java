package de.bnder.taskmanager.main;

import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

public class CommandHandler {

    public static final CommandParser parse = new CommandParser();
    public static final HashMap<String, Command> commands = new HashMap<>();

    public static void handleCommand(CommandParser.commandContainer cmd, Message msg) throws IOException {
        if (commands.containsKey(cmd.invoke.toLowerCase())) {
            commands.get(cmd.invoke.toLowerCase()).action(cmd.args, cmd.event);
        } else {
            final String langCode = Localizations.Companion.getGuildLanguage(msg.getGuild());
            MessageSender.send(Localizations.Companion.getString("error_title", langCode), Localizations.Companion.getString("unknown_command_message", langCode), msg, Color.red);
        }
    }
}