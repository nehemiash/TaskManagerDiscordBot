package de.bnder.taskmanager.utils;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Task {

    private String id = null;
    private String text = null;
    private String deadline = null;
    private TaskType type = null;
    private TaskStatus status = TaskStatus.TODO;
    private String holder = null;
    private boolean exists = false;
    private Guild guild;
    private String newLanguageSuggestion = null;
    private String notifyChannelMessageID = null;
    private String boardName = "default";
    private String boardID = "default";

    /**
     * Get Task by id.
     *
     * @param taskID The id of the task.
     * @param guild  The guild where the task could be.
     */
    public Task(String taskID, Guild guild) {
        this.id = taskID;
        this.guild = guild;

        System.out.println("Searching for task id:" + taskID + ".");

        try {
            //Try user task
            for (DocumentSnapshot boardDoc : Main.firestore.collection("server").document(guild.getId()).collection("boards").get().get().getDocuments()) {
                System.out.println("Checking board " + boardDoc.getId());
                final DocumentSnapshot taskDoc = boardDoc.getReference().collection("user-tasks").document(taskID).get().get();
                System.out.println("Checking task id " + taskDoc.getId());
                System.out.println(taskDoc.getId());
                if (taskDoc.exists()) {
                    System.out.println("Exists!");
                    this.type = TaskType.USER;
                    this.exists = true;
                    this.text = taskDoc.getString("text");
                    this.deadline = taskDoc.getString("deadline");
                    this.status = TaskStatus.values()[Integer.parseInt(taskDoc.get("status").toString())];
                    this.holder = taskDoc.getString("user_id");
                    if (taskDoc.getData().containsKey("notify_channel_message_id"))
                        this.notifyChannelMessageID = taskDoc.getString("notify_channel_message_id");
                    this.boardName = boardDoc.getString("name");
                    this.boardID = boardDoc.getId();
                    return;
                }
            }

            if (!this.exists) {
                System.out.println("Checking for group tasks");
                //Try group task
                for (DocumentSnapshot groupDoc : Main.firestore.collection("server").document(guild.getId()).collection("groups").get().get().getDocuments()) {
                    final DocumentSnapshot taskDoc = groupDoc.getReference().collection("group-tasks").document(taskID).get().get();
                    if (taskDoc.exists()) {
                        this.type = TaskType.GROUP;
                        this.exists = true;
                        this.text = taskDoc.getString("text");
                        this.deadline = taskDoc.getString("deadline");
                        this.status = TaskStatus.values()[Integer.parseInt(taskDoc.get("status").toString())];
                        this.holder = groupDoc.getId();
                        if (taskDoc.getData().containsKey("notify_channel_message_id"))
                            this.notifyChannelMessageID = taskDoc.getString("notify_channel_message_id");

                        //Get board name
                        final String boardID = taskDoc.getString("board_id");
                        this.boardName = Main.firestore.collection("server").document(guild.getId()).collection("boards").document(boardID).get().get().getString("name");
                        this.boardID = boardID;
                        break;
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a new task for a user.
     *
     * @param guild            The Guild where the task will be created in.
     * @param text             The text of the task.
     * @param deadline         Deadline for the task. Can be null!
     * @param member           Member which will be the owner of the task.
     * @param commandProcessor The one who created the task by performing the creation command.
     */
    public Task(Guild guild, String text, String deadline, Member member, Member commandProcessor) {
        try {
            String boardID = "default";
            String boardName = "default";

            //Get the board of the command processor
            final DocumentSnapshot getServerMemberDoc = Main.firestore.collection("server").document(guild.getId()).collection("server_member").document(commandProcessor.getId()).get().get();
            if (getServerMemberDoc.exists()) {
                if (Objects.requireNonNull(getServerMemberDoc.getData()).containsKey("active_board_id")) {
                    boardID = getServerMemberDoc.getString("active_board_id");
                }
            }
            final String taskID = generateTaskID(guild, boardID);

            if (taskID == null) {
                throw new Exception("TaskID can't be null!");
            }

            final DocumentSnapshot boardDoc = Main.firestore.collection("server").document(guild.getId()).collection("boards").document(boardID).get().get();
            //Get actice board name if it isn't default
            if (!boardID.equals("default")) boardName = boardDoc.getString("name");
            boardDoc.getReference().collection("user-tasks").document(taskID).set(new HashMap<>() {{
                put("user_id", member.getId());
                put("deadline_reminded", false);
                put("position", -1);
                put("status", 0);
                put("text", text);
                put("deadline", deadline);
                put("server_id", guild.getId());
            }});
            Stats.updateTasksCreated();


            this.exists = true;
            this.id = taskID;
            this.boardName = boardName;
            this.boardID = boardID;
            this.newLanguageSuggestion = null;
            this.guild = guild;
            this.text = text;
            this.deadline = deadline;
            this.type = TaskType.USER;
            this.holder = member.getId();

            final DocumentSnapshot getServermember = Main.firestore.collection("server").document(guild.getId()).collection("server_member").document(member.getId()).get().get();
            if (getServermember.exists()) {
                if (Objects.requireNonNull(getServermember.getData()).containsKey("notify_channel")) {
                    final String channelID = getServermember.getString("notify_channel");
                    if (guild.getTextChannelById(channelID) != null) {
                        final Locale langCode = Localizations.getGuildLanguage(guild);
                        EmbedBuilder builder = new EmbedBuilder().setColor(Color.cyan);
                        builder.addField(Localizations.getString("task_info_field_task", langCode), text, false);
                        builder.addField(Localizations.getString("task_info_field_type_user", langCode), member.getUser().getAsTag(), true);
                        builder.addField(Localizations.getString("task_info_field_deadline", langCode), (deadline != null) ? deadline : "---", true);
                        builder.addField(Localizations.getString("task_info_field_id", langCode), id, true);
                        builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode), true);
                        guild.getTextChannelById(channelID).sendMessageEmbeds(builder.build()).queue(message -> {
                            boardDoc.getReference().collection("user-tasks").document(taskID).update(new HashMap<>() {{
                                put("notify_channel_message_id", message.getId());
                            }});
                            message.addReaction("↩️").queue();
                            message.addReaction("⏭️").queue();
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a new unique task id.
     *
     * @param guild         The guild which will receive the task.
     * @param activeBoardID The board in which the task will be created.
     * @return The 5 numbers long id.
     */
    String generateTaskID(Guild guild, String activeBoardID) {
        final int len = 5;
        final String AB = "0123456789";
        final SecureRandom rnd = new SecureRandom();
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));

        try {
            DocumentSnapshot a = Main.firestore.collection("server").document(guild.getId()).collection("boards").document(activeBoardID).get().get();
            if (!a.exists()) {
                final String boardName = this.boardName;
                a.getReference().set(new HashMap<>() {{
                    put("name", boardName);
                }});
            }

            if (a.getReference().collection("user-tasks").document(sb.toString()).get().get().exists()) {
                return generateTaskID(guild, activeBoardID);
            }

            for (DocumentSnapshot groupDoc : Main.firestore.collection("server").document(guild.getId()).collection("groups").get().get().getDocuments()) {
                if (groupDoc.getReference().collection("group-tasks").document(sb.toString()).get().get().exists()) {
                    return generateTaskID(guild, activeBoardID);
                }
            }

            return sb.toString();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new task for a group.
     *
     * @param guild            The guild on which the task will be created.
     * @param text             The text of the task.
     * @param deadline         The deadline of the task. Can be null.
     * @param groupName        The ID of the group owning the task.
     * @param commandProcessor The member performing the create command.
     */
    public Task(Guild guild, String text, String deadline, String groupName, Member commandProcessor) {
        try {
            //Get group name
            final QuerySnapshot getGroup = Main.firestore.collection("server").document(guild.getId()).collection("groups").whereEqualTo("name", groupName).get().get();
            if (getGroup.size() > 0) {
                String boardID = "default";
                String boardName = "default";
                //Get the board of the command processor
                final DocumentSnapshot getServerMemberDoc = Main.firestore.collection("server").document(guild.getId()).collection("server_member").document(commandProcessor.getId()).get().get();
                if (getServerMemberDoc.exists()) {
                    if (Objects.requireNonNull(getServerMemberDoc.getData()).containsKey("active_board_id")) {
                        boardID = getServerMemberDoc.getString("active_board_id");
                        boardName = Main.firestore.collection("server").document(guild.getId()).collection("boards").document(boardID).get().get().getString("name");
                    }
                }
                final DocumentSnapshot groupDoc = getGroup.getDocuments().get(0);
                final String taskID = generateTaskID(guild, boardID);
                final String finalBoardID = boardID;
                groupDoc.getReference().collection("group-tasks").document(taskID).set(new HashMap<>() {{
                    put("board_id", finalBoardID);
                    put("deadline", deadline);
                    put("deadline_reminded", false);
                    put("position", -1);
                    put("status", 0);
                    put("text", text);
                    put("server_id", guild.getId());
                }});
                Stats.updateTasksCreated();

                this.exists = true;
                this.guild = guild;
                this.text = text;
                this.deadline = deadline;
                this.type = TaskType.GROUP;
                this.holder = groupName;
                this.id = taskID;
                this.boardID = boardID;
                this.boardName = boardName;

                //Send task into group notify channel
                if (groupDoc.getData().containsKey("notify_channel")) {
                    final String channelID = getGroup.getDocuments().get(0).getString("notify_channel");
                    if (guild.getTextChannelById(channelID) != null) {
                        final Locale langCode = Localizations.getGuildLanguage(guild);
                        EmbedBuilder builder = new EmbedBuilder().setColor(Color.cyan);
                        builder.addField(Localizations.getString("task_info_field_task", langCode), text, false);
                        builder.addField(Localizations.getString("task_info_field_type_group", langCode), groupName, true);
                        builder.addField(Localizations.getString("task_info_field_deadline", langCode), (deadline != null) ? deadline : "---", true);
                        builder.addField(Localizations.getString("task_info_field_id", langCode), id, true);
                        builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode), true);
                        guild.getTextChannelById(channelID).sendMessageEmbeds(builder.build()).queue(message -> {
                            groupDoc.getReference().collection("group-tasks").document(taskID).update(new HashMap<>() {{
                                put("notify_channel_message_id", message.getId());
                            }});
                            message.addReaction("↩️").queue();
                            message.addReaction("⏭️").queue();
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the ID of the message sent in the notify channel. If the task is a group task the channel is set for the group. If it's a user task the channel is the one set for the owner of the task.
     *
     * @return The ID of the message.
     */
    public String getNotifyChannelMessageID() {
        if (this.notifyChannelMessageID == null) {
            try {
                if (this.type == TaskType.USER) {
                    this.notifyChannelMessageID = Main.firestore.collection("server").document(guild.getId()).collection("boards").document(boardID).collection("user-tasks")
                            .document(this.id).get().get().getString("notify_channel_message_id");
                } else if (this.type == TaskType.GROUP) {
                    this.notifyChannelMessageID = Main.firestore.collection("server").document(guild.getId()).collection("groups")
                            .document(this.holder).collection("group-tasks").document(this.id).get().get().getString("notify_channel_message_id");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return this.notifyChannelMessageID;
    }

    /**
     * Changes the text of a task.
     *
     * @param text The new text of the task.
     */
    public void setText(String text) {
        if (this.type == TaskType.USER) {
            Main.firestore.collection("server").document(guild.getId()).collection("boards").document(boardID).collection("user-tasks").document(this.id).update(new HashMap<>() {{
                put("text", text);
            }});
        } else if (this.type == TaskType.GROUP) {
            Main.firestore.collection("server").document(guild.getId()).collection("groups").document(this.holder).collection("group-tasks").document(this.id).update(new HashMap<>() {{
                put("text", text);
            }});
        }
        this.text = text;

        //Update in notify channel
        final Locale langCode = Localizations.getGuildLanguage(guild);
        updateNotifyChannelMessage(Localizations.getString("task_info_field_task", langCode), this.text);
    }

    /**
     * Change the deadline for a task.
     *
     * @param deadline The new formated deadline.
     */
    public void setDeadline(String deadline) {
        if (this.type == TaskType.USER) {
            Main.firestore.collection("server").document(this.guild.getId()).collection("boards").document(this.boardID).collection("user-tasks").document(this.id).update(new HashMap<>() {{
                put("deadline", deadline);
            }});
        } else if (this.type == TaskType.GROUP) {
            Main.firestore.collection("server").document(guild.getId()).collection("groups").document(this.holder).collection("group-tasks").document(this.id).update(new HashMap<>() {{
                put("deadline", deadline);
            }});
        }
        this.deadline = deadline;

        //Update in notify channel
        final Locale langCode = Localizations.getGuildLanguage(guild);
        updateNotifyChannelMessage(Localizations.getString("task_info_field_deadline", langCode), this.deadline);
    }

    /**
     * Get the channel where update notifications for a task will be sent.
     *
     * @return The ID of the channel.
     */
    public String getNotifyChannelID() {
        try {
            if (this.type == TaskType.USER) {
                final DocumentSnapshot getMemberDoc = Main.firestore.collection("server").document(guild.getId()).collection("server_member").document(holder).get().get();
                if (getMemberDoc.exists()) {
                    if (getMemberDoc.getData().containsKey("notify_channel")) {
                        return getMemberDoc.getString("notify_channel");
                    }
                }
            } else if (this.type == TaskType.GROUP) {
                final DocumentSnapshot getGroupDoc = Main.firestore.collection("server").document(guild.getId()).collection("groups").document(this.holder).get().get();
                if (getGroupDoc.exists()) {
                    if (getGroupDoc.getData().containsKey("notify_channel")) {
                        return getGroupDoc.getString("notify_channel");
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates the value of a field of the notification message in notify channel.
     *
     * @param fieldName The name of the field to be changed.
     * @param newValue  The new text of the field.
     */
    public void updateNotifyChannelMessage(String fieldName, String newValue) {
        final String messageID = getNotifyChannelMessageID();
        if (messageID != null) {
            final String channelID = getNotifyChannelID();
            if (channelID != null && guild.getTextChannelById(channelID) != null) {
                guild.getTextChannelById(channelID).retrieveMessageById(messageID).queue(message -> {
                    if (message.getAuthor().isBot()) {
                        EmbedBuilder newEmbed = new EmbedBuilder();
                        for (MessageEmbed embed : message.getEmbeds()) {
                            newEmbed.setColor(embed.getColor());
                            newEmbed.setTitle(embed.getTitle());
                            for (MessageEmbed.Field field : embed.getFields()) {
                                if (!field.getName().equalsIgnoreCase(fieldName)) {
                                    newEmbed.addField(field.getName(), field.getValue(), field.isInline());
                                } else {
                                    newEmbed.addField(field.getName(), newValue, field.isInline());
                                }
                            }
                        }
                        message.editMessageEmbeds(newEmbed.build()).queue();
                    }
                }, (error) -> {
                });
            }
        }
    }

    /**
     * Deletes the task from firestore.
     */
    public void delete() {
        if (this.type == TaskType.USER) {
            Main.firestore.collection("server").document(guild.getId()).collection("boards").document(this.boardID).collection("user-tasks").document(this.id)
                    .delete();
        } else if (this.type == TaskType.GROUP) {
            Main.firestore.collection("server").document(guild.getId()).collection("groups").document(this.holder).collection("group-tasks").document(this.id).delete();
        }

        //Delete message in notify channel
        final String messageID = getNotifyChannelMessageID();
        if (messageID != null) {
            final String channelID = getNotifyChannelID();
            if (channelID != null && guild.getTextChannelById(channelID) != null) {
                guild.getTextChannelById(channelID).retrieveMessageById(messageID).queue(message -> message.delete().queue(), (error) -> {
                });
            }
        }
        this.exists = false;
    }

    /**
     * Sets the status of the task to the defined one.
     *
     * @param status The new status which will be set.
     */
    public void setStatus(TaskStatus status) {
        try {
            int number = switch (status) {
                case DONE -> 2;
                case IN_PROGRESS -> 1;
                default -> 0;
            };
            if (this.type == TaskType.USER) {
                int finalNumber = number;
                Main.firestore.collection("server").document(guild.getId()).collection("boards").document(this.boardID).collection("user-tasks").document(this.id).update(new HashMap<>() {{
                    put("status", finalNumber);
                }});
            } else if (this.type == TaskType.GROUP) {
                int finalNumber = number;
                Main.firestore.collection("server").document(guild.getId()).collection("groups").document(this.holder).collection("group-tasks").document(this.id).update(new HashMap<>() {{
                    put("status", finalNumber);
                }});
            }

            this.status = status;

            if (this.status == TaskStatus.DONE) {
                Stats.updateTasksDone();
            }

            //Update in notify channel
            final Locale langCode = Localizations.getGuildLanguage(guild);
            String newStatus = Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode);
            if (status == TaskStatus.IN_PROGRESS)
                newStatus = Localizations.getString("aufgaben_status_wird_bearbeitet", langCode);
            else if (status == TaskStatus.DONE)
                newStatus = Localizations.getString("aufgaben_status_erledigt", langCode);
            updateNotifyChannelMessage(Localizations.getString("task_info_field_state", langCode), newStatus);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Changes the task status from "to-do" to "in progress" to "done".
     */
    public void proceed() {
        try {
            if (this.type == TaskType.USER) {
                final DocumentSnapshot getTaskDoc = Main.firestore.collection("server").document(guild.getId()).collection("boards").document(this.boardID).collection("user-tasks").document(this.id).get().get();
                if (getTaskDoc.exists()) {
                    long status = 0;
                    if (getTaskDoc.getData().containsKey("status")) {
                        status = (long) getTaskDoc.get("status");
                    }
                    final long finalStatus = status;
                    getTaskDoc.getReference().update(new HashMap<>() {{
                        put("status", finalStatus + 1);
                    }});
                    this.status = TaskStatus.values()[Math.toIntExact(status + 1)];

                    if (this.status == TaskStatus.DONE) {
                        Stats.updateTasksDone();
                    }
                }
            } else if (this.type == TaskType.GROUP) {
                final DocumentSnapshot getTaskDoc = Main.firestore.collection("server").document(guild.getId()).collection("groups").document(this.holder).collection("group-tasks").document(this.id).get().get();
                if (getTaskDoc.exists()) {
                    int status = 0;
                    if (getTaskDoc.getData().containsKey("status")) {
                        status = (int) getTaskDoc.get("status");
                    }
                    final int finalStatus = status;
                    getTaskDoc.getReference().update(new HashMap<>() {{
                        put("status", finalStatus + 1);
                    }});
                    this.status = TaskStatus.values()[status + 1];

                    if (this.status == TaskStatus.DONE) {
                        Stats.updateTasksDone();
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void undo() {
        TaskStatus newStatus = TaskStatus.TODO;
        if (this.status == TaskStatus.DONE) {
            newStatus = TaskStatus.IN_PROGRESS;
        }
        setStatus(newStatus);

        //Update in notify channel
        final Locale langCode = Localizations.getGuildLanguage(guild);
        String newStatusString = Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode);
        if (status == TaskStatus.IN_PROGRESS)
            newStatusString = Localizations.getString("aufgaben_status_wird_bearbeitet", langCode);
        else if (status == TaskStatus.DONE)
            newStatusString = Localizations.getString("aufgaben_status_erledigt", langCode);
        updateNotifyChannelMessage(Localizations.getString("task_info_field_state", langCode), newStatusString);
    }

    public String newLanguageSuggestion() {
        return newLanguageSuggestion;
    }

    public String getHolder() {
        return holder;
    }

    public boolean exists() {
        return exists;
    }

    public String getText() {
        return text;
    }

    public TaskType getType() {
        return type;
    }

    public String getDeadline() {
        return deadline;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public Guild getGuild() {
        return guild;
    }

    public String getBoardName() {
        return boardName;
    }
}

