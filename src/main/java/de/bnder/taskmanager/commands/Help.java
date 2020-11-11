package de.bnder.taskmanager.commands;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.IOException;

public class Help implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        final String langCode = Localizations.getGuildLanguage(event.getGuild());
        final String embedTitle = Localizations.getString("help_message_title", langCode);
        MessageSender.send(embedTitle + " - " + Localizations.getString("group_title", langCode), Localizations.getString("help_message_group_commands", langCode), event.getMessage(), Color.cyan);
        MessageSender.send(embedTitle + " - " + Localizations.getString("task_message_title", langCode), Localizations.getString("help_message_task_commands", langCode), event.getMessage(), Color.cyan);
        MessageSender.send(embedTitle, Localizations.getString("help_message_other_commands", langCode), event.getMessage(), Color.cyan);
        MessageSender.send(embedTitle + " - " + Localizations.getString("permissions_title", langCode), Localizations.getString("help_message_permission_commands", langCode), event.getMessage(), Color.cyan);
    }
}
