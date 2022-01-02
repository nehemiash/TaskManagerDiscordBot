package de.bnder.taskmanager.commands.task;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.LevenshteinDistance;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.Task;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.apache.commons.lang3.math.NumberUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskController implements Command {

    final ArrayList<String> commandArgs = new ArrayList<>() {{
        add("add");
        add("delete");
        add("proceed");
        add("deadline");
        add("list");
        add("delete");
        add("edit");
        add("info");
        add("done");
        add("a"); //add
        add("l"); //list
        add("e"); //edit
        add("p"); //proceed
        add("i"); //info
        add("u"); //undo
    }};

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, List<Member> mentionedMembers, List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("e")) {
                int newTextStartIndex = 0;
                String taskID = null;
                // User defined Task-ID in his message
                if (args.length >= 2) {
                    taskID = args[1];
                }
                if (taskID != null || NumberUtils.isCreatable(formatPossibleTaskID(taskID))) {
                    newTextStartIndex = 3;
                } else {
                    String tempTaskID = getTaskIDFromContext(textChannel, commandExecutor, messageContentRaw);
                    if (NumberUtils.isCreatable(tempTaskID) && new Task(tempTaskID, guild).exists()) {
                        newTextStartIndex = 2;
                        taskID = tempTaskID;
                    }
                }
                EditTask.editTask(messageContentRaw, commandExecutor, textChannel, taskID, newTextStartIndex, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("deadline")) {
                int dateStartIndex = 0;
                String taskID = null;
                // User defined Task-ID in his message
                if (args.length >= 2) {
                    taskID = args[1];
                }
                if (taskID != null || NumberUtils.isCreatable(formatPossibleTaskID(taskID))) {
                    dateStartIndex = 2;
                } else {
                    String tempTaskID = getTaskIDFromContext(textChannel, commandExecutor, messageContentRaw);
                    if (NumberUtils.isCreatable(tempTaskID) && new Task(tempTaskID, guild).exists()) {
                        dateStartIndex = 1;
                        taskID = tempTaskID;
                    }
                }
                SetDeadline.setDeadline(commandExecutor, textChannel, args, taskID, dateStartIndex, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("proceed") || args[0].equalsIgnoreCase("p")) {
                String taskID = null;
                // User defined Task-ID in his message
                if (args.length >= 2) {
                    taskID = args[1];
                }
                if (taskID == null || !(NumberUtils.isCreatable(formatPossibleTaskID(taskID)))) {
                    String tempTaskID = getTaskIDFromContext(textChannel, commandExecutor, messageContentRaw);
                    if (NumberUtils.isCreatable(tempTaskID) && new Task(tempTaskID, guild).exists()) {
                        taskID = tempTaskID;
                    }
                }
                ProceedTask.proceedTask(commandExecutor, textChannel, taskID, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("delete")) {
                String taskID = null;
                // User defined Task-ID in his message
                if (args.length >= 2) {
                    taskID = args[1];
                }
                if (taskID == null || !(taskID.equalsIgnoreCase("done") || (NumberUtils.isCreatable(formatPossibleTaskID(taskID))))) {
                    String tempTaskID = getTaskIDFromContext(textChannel, commandExecutor, messageContentRaw);
                    if (NumberUtils.isCreatable(tempTaskID) && new Task(tempTaskID, guild).exists()) {
                        taskID = tempTaskID;
                    }
                }
                DeleteTask.deleteTask(commandExecutor, textChannel, taskID, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
                String taskID = null;
                // User defined Task-ID in his message
                if (args.length >= 2) {
                    taskID = args[1];
                }
                if (taskID == null || !(NumberUtils.isCreatable(formatPossibleTaskID(taskID)))) {
                    String tempTaskID = getTaskIDFromContext(textChannel, commandExecutor, messageContentRaw);
                    if (NumberUtils.isCreatable(tempTaskID) && new Task(tempTaskID, guild).exists()) {
                        taskID = tempTaskID;
                    }
                }
                TaskInfo.taskInfo(commandExecutor, textChannel, taskID, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("done")) {
                String taskID = null;
                // User defined Task-ID in his message
                if (args.length >= 2) {
                    taskID = args[1];
                }
                if (taskID == null || !(NumberUtils.isCreatable(formatPossibleTaskID(taskID)))) {
                    String tempTaskID = getTaskIDFromContext(textChannel, commandExecutor, messageContentRaw);
                    if (NumberUtils.isCreatable(tempTaskID) && new Task(tempTaskID, guild).exists()) {
                        taskID = tempTaskID;
                    }
                }
                TasksDone.tasksDone(commandExecutor, textChannel, taskID, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("undo") || args[0].equalsIgnoreCase("u")) {
                String taskID = null;
                // User defined Task-ID in his message
                if (args.length >= 2) {
                    taskID = args[1];
                }
                if (taskID == null || !(NumberUtils.isCreatable(formatPossibleTaskID(taskID)) && new Task(taskID, guild).exists())) {
                    String tempTaskID = getTaskIDFromContext(textChannel, commandExecutor, messageContentRaw);
                    if (NumberUtils.isCreatable(tempTaskID) && new Task(tempTaskID, guild).exists()) {
                        taskID = tempTaskID;
                    }
                }
                UndoTask.undoTask(commandExecutor, textChannel, taskID, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("addgroup")) {
                AddTask.addTask(messageContentRaw, commandExecutor, mentionedMembers, textChannel, args, slashCommandEvent);
            } else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("listgroup")) {
                if (args.length >= 2) {
                    ListTasksFromOthers.listTasks(commandExecutor, mentionedMembers, textChannel, args, slashCommandEvent);
                } else {
                    SelfTaskList.selfTaskList(commandExecutor, textChannel, slashCommandEvent);
                }
            } else {
                checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
            }
        } else {
            checkIfTypo(args, messageContentRaw, guild, textChannel, commandExecutor, slashCommandEvent);
        }
    }

    /**
     * Iterates through last 25 messages in channel and looks for messages by author "commandExecutor". If there is a message from the author it checks wether a task id can be extracted by that.
     *
     * @param textChannel     The text channel where the command was executed.
     * @param commandExecutor The member who executed the command.
     * @param sourceMessage   The text of the message.
     * @return ID if task id is found. Null if no id was found.
     */
    String getTaskIDFromContext(TextChannel textChannel, Member commandExecutor, String sourceMessage) {
        ArrayList<Message> messageArrayList = new ArrayList<>(textChannel.getHistoryBefore(textChannel.getLatestMessageId(), 25).complete().getRetrievedHistory());
        final Locale langCode = Localizations.getGuildLanguage(textChannel.getGuild());
        for (Message message : messageArrayList) {
            if (message.getAuthor().getId().equals(commandExecutor.getUser().getId())) {
                final String messageContentRaw = message.getContentRaw();
                if (messageContentRaw.startsWith(String.valueOf(sourceMessage.split(" ")[0]))) {
                    final String commandArg = messageContentRaw.split(" ")[1];

                    if (commandArg.equalsIgnoreCase("info") || commandArg.equalsIgnoreCase("i") ||
                            commandArg.equalsIgnoreCase("edit") || commandArg.equalsIgnoreCase("e")
                            || commandArg.equalsIgnoreCase("proceed") || commandArg.equalsIgnoreCase("p")
                            || commandArg.equalsIgnoreCase("deadline")
                            || commandArg.equalsIgnoreCase("delete")
                            || commandArg.equalsIgnoreCase("done")
                            || commandArg.equalsIgnoreCase("undo") || commandArg.equalsIgnoreCase("u")) {
                        if (messageContentRaw.split(" ").length >= 3) {
                            System.out.println("aaaa");
                            if (NumberUtils.isCreatable(messageContentRaw.split(" ")[2])) {
                                System.out.println("bbbb");
                                return messageContentRaw.split(" ")[2];
                            }
                        }
                    }
                }
            } else if (message.getAuthor().getId().equals(message.getJDA().getSelfUser().getId())) {
                if (message.getEmbeds().size() > 0) {
                    final MessageEmbed embed = message.getEmbeds().get(0);
                    // Task add message
                    if (embed.getColor() != null && embed.getColor().getGreen() == 255 && embed.getColor().getRed() == 0 && embed.getColor().getBlue() == 0 && (embed.getTitle() != null && embed.getTitle().startsWith(Localizations.getString("task_message_title", langCode) + " - "))) {
                        return embed.getTitle().split(Localizations.getString("task_message_title", langCode) + " - ")[1];
                    }
                    // Task info message
                    else if (embed.getColor() != null && embed.getColor().getGreen() == 255 && embed.getColor().getBlue() == 255 && embed.getColor().getRed() == 0 && (embed.getTitle() != null && embed.getTitle().startsWith(Localizations.getString("task_message_title", langCode) + " - "))) {
                        return embed.getTitle().split(Localizations.getString("task_message_title", langCode) + " - ")[1];
                    }
                }
            }
        }
        return null;
    }

    void checkIfTypo(String[] args, String messageContentRaw, Guild guild, TextChannel textChannel, Member commandExecutor, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(guild);
        if (args.length > 0) {
            final String userArg1 = args[0];
            final StringBuilder possibleCommands = new StringBuilder();
            for (String commandArg : commandArgs) {
                final int distance = LevenshteinDistance.levenshteinDistance(commandArg, userArg1);
                if (distance <= 2 && distance != 0) {
                    final StringBuilder correctedMessage = new StringBuilder().append(messageContentRaw.split(" ")[0]).append(" ");
                    correctedMessage.append(commandArg).append(" ");
                    for (int i = 1; i < args.length; i++) {
                        correctedMessage.append(args[i]).append(" ");
                    }

                    final String correctedMessageString = correctedMessage.substring(0, correctedMessage.length());
                    possibleCommands.append(correctedMessageString);
                    break;
                }
            }
            if (possibleCommands.length() > 0) {
                EmbedBuilder builder = new EmbedBuilder().setColor(Color.orange);
                builder.setTitle(Localizations.getString("typo_title", langCode));
                builder.setDescription(Localizations.getString("typo_description", langCode));
                builder.addField(Localizations.getString("typo_field_command_name", langCode), possibleCommands.substring(0, possibleCommands.length() - 1), true);
                builder.addField(Localizations.getString("typo_field_user_name", langCode), commandExecutor.getUser().getAsTag(), true);
                textChannel.sendMessageEmbeds(builder.build()).queue(message1 -> message1.addReaction("✅").and(message1.addReaction("❌")).queue());
            } else {
                final String embedTitle = Localizations.getString("task_message_title", langCode);
                final String prefix = String.valueOf(messageContentRaw.charAt(0));
                MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode, new ArrayList<String>() {{
                    add(prefix);
                }}), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            final String embedTitle = Localizations.getString("task_message_title", langCode);
            final String prefix = String.valueOf(messageContentRaw.charAt(0));
            MessageSender.send(embedTitle, Localizations.getString("help_message_task_commands", langCode, new ArrayList<String>() {{
                add(prefix);
            }}), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

    /**
     * Remove leading zero from Task ID
     *
     * @param taskID ID of Task
     * @return Formated task id for NumberUtils.isCreatable()
     */
    String formatPossibleTaskID(String taskID) {
        if (taskID == null || taskID.length() == 0) {
            return "";
        }
        String id = taskID;
        while (id.startsWith("0")) {
            id = taskID.substring(1);
        }
        // Prevent empty string when id is "00000"
        if (id.length() == 0) {
            id = "1";
        }
        return id;
    }

}
