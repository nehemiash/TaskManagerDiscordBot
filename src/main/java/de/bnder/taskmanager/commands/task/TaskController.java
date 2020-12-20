package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.LevenshteinDistance;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class TaskController implements Command {

    final ArrayList<String> commandArgs = new ArrayList<String>() {{
        add("add");
        add("delete");
        add("proceed");
        add("deadline");
        add("list");
        add("delete");
        add("edit");
        add("info");
        add("done");
    }};

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("add")) {
                AddTask.addTask(event.getMessage().getContentRaw(), event.getMember(), event.getMessage().getMentionedMembers(), event.getChannel(), args);
            } else if (args[0].equalsIgnoreCase("edit")) {
                EditTask.editTask(event.getMessage().getContentRaw(), event.getMember(), event.getChannel(), args);
            } else if (args[0].equalsIgnoreCase("deadline")) {
                SetDeadline.setDeadline(event.getMember(), event.getChannel(), args);
            } else {
                checkIfTypo(args, event.getMessage());
            }
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("list")) {
                ListTasksFromOthers.listTasks(event.getMember(), event.getMessage().getMentionedMembers(), event.getChannel(), args);
            } else if (args[0].equalsIgnoreCase("delete")) {
                DeleteTask.deleteTask(event.getMember(), event.getChannel(), args);
            } else if (args[0].equalsIgnoreCase("done")) {
                DeleteTask.deleteTask(event.getMember(), event.getChannel(), args);
            } else if (args[0].equalsIgnoreCase("proceed")) {
                ProceedTask.proceedTask(event.getMember(), event.getChannel(), args);
            } else if (args[0].equalsIgnoreCase("undo")) {
                UndoTask.undoTask(event.getMember(), event.getChannel(), args);
            } else if (args[0].equalsIgnoreCase("info")) {
                TaskInfo.taskInfo(event.getMember(), event.getChannel(), args);
            } else {
                checkIfTypo(args, event.getMessage());
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                SelfTaskList.selfTaskList(event.getMember(), event.getChannel());
            } else {
                checkIfTypo(args, event.getMessage());
            }
        } else {
            checkIfTypo(args, event.getMessage());
        }
    }

    void checkIfTypo(String[] args, Message message) {
        final String userArg1 = args[0];
        final StringBuilder possibleCommands = new StringBuilder();
        final String langCode = Localizations.getGuildLanguage(message.getGuild());
        for (String commandArg : commandArgs) {
            final int distance = LevenshteinDistance.levenshteinDistance(commandArg, userArg1);
            if (distance <= 2 && distance != 0) {
                final StringBuilder correctedMessage = new StringBuilder().append(message.getContentRaw().split(" ")[0]).append(" ");
                correctedMessage.append(commandArg).append(" ");
                for (int i = 1; i < args.length; i++) {
                    correctedMessage.append(args[i]).append(" ");
                }

                final String correctedMessageString = correctedMessage.substring(0, correctedMessage.length() /*- 1*/);
                possibleCommands.append(correctedMessageString)/*.append("\n")*/;
                break;
            }
        }
        if (possibleCommands.length() > 0) {
            EmbedBuilder builder = new EmbedBuilder().setColor(Color.orange);
            builder.setTitle("Vorschlag");
            builder.setDescription("Ich habe einen Fehler in der Nachricht entdeckt. Meintest du \n**" + possibleCommands.substring(0, possibleCommands.length() - 1) + "**");
            builder.addField("Befehl", possibleCommands.substring(0, possibleCommands.length() - 1), true);
            builder.addField("Nutzer", message.getAuthor().getAsTag(), true);
            final Message message1 = message.getChannel().sendMessage(builder.build()).complete();
            message1.addReaction("✅").and(message1.addReaction("❌")).queue();
        } else {
            final String embedTitle = Localizations.getString("task_message_title", langCode);
            final String prefix = String.valueOf(message.getContentRaw().charAt(0));
            MessageSender.send(embedTitle + " - " + Localizations.getString("task_message_title", langCode), Localizations.getString("help_message_task_commands", langCode, new ArrayList<String>() {{
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
            }}), message, Color.red);
        }
    }
}
