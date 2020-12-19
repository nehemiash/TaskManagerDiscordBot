package de.bnder.taskmanager.commands;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class Help implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        final String prefix = String.valueOf(event.getMessage().getContentRaw().charAt(0));
        final String langCode = Localizations.getGuildLanguage(event.getGuild());
        final String embedTitle = Localizations.getString("help_message_title", langCode);
        MessageSender.send(embedTitle + " - " + Localizations.getString("group_title", langCode), Localizations.getString("help_message_group_commands", langCode, new ArrayList<String>(){{
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
        }}), event.getMessage(), Color.cyan);
        MessageSender.send(embedTitle + " - " + Localizations.getString("task_message_title", langCode), Localizations.getString("help_message_task_commands", langCode, new ArrayList<String>(){{
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
        }}), event.getMessage(), Color.cyan);
        MessageSender.send(embedTitle, Localizations.getString("help_message_other_commands", langCode, new ArrayList<String>(){{
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
        }}), event.getMessage(), Color.cyan);
        MessageSender.send(embedTitle + " - " + Localizations.getString("permissions_title", langCode), Localizations.getString("help_message_permission_commands", langCode, new ArrayList<String>(){{
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
        }}), event.getMessage(), Color.cyan);
    }
}
