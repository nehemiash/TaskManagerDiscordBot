package de.bnder.taskmanager.main;

import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

public class CommandHandler {

    public static final CommandParser parse = new CommandParser();
    public static HashMap<String, Command> commands = new HashMap<>();

    public static void handleCommand(CommandParser.commandContainer cmd, Message msg) throws IOException {
        if (commands.containsKey(cmd.invoke.toLowerCase())) {
            String cmdString = cmd.invoke.toLowerCase();
            System.out.println("Befehl erhalten: " + cmdString);
            commands.get(cmd.invoke.toLowerCase()).action(cmd.args, cmd.event);
        } else {
            MessageSender.send("Fehler", "Dieser Befehl ist unbekannt! Benutze `" + Main.prefix + "help` um alle Befehle aufzulisten.", msg, Color.red);
        }
    }
}