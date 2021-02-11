package de.bnder.taskmanager.commands.help;

import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;

public class GeneralCommandOverview {

    public static void sendGeneralCommandOverview(TextChannel channel, String messageRaw, String langCode) {
        final String prefix = String.valueOf(messageRaw.charAt(0));
        final String embedTitle = Localizations.getString("help_message_title", langCode);
        final String groupHelp = Localizations.getString("help_message_group_commands", langCode, new ArrayList<String>(){{
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
        }});
        final String boardHelp = Localizations.getString("help_message_board_commands", langCode, new ArrayList<String>(){{
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
        }});
        final String taskHelp = Localizations.getString("help_message_task_commands", langCode, new ArrayList<String>(){{
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
        }});
        final String otherHelp = Localizations.getString("help_message_other_commands", langCode, new ArrayList<String>(){{
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
            add(prefix);
        }});
        final String permissionHelp = Localizations.getString("help_message_permission_commands", langCode, new ArrayList<String>(){{
            add(prefix);
            add(prefix);
            add(prefix);
            add(prefix);
        }});
        MessageSender.send(embedTitle, taskHelp + "\n" +"\n" + groupHelp, channel, Color.cyan, langCode);
        MessageSender.send(embedTitle, permissionHelp, channel, Color.cyan, langCode);
        MessageSender.send(embedTitle, boardHelp + "\n" +"\n" + otherHelp, channel, Color.cyan, langCode);
    }
}
