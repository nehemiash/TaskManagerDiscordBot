package de.bnder.taskmanager.utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Task {

    String id = null;
    String text = null;
    String deadline = null;
    TaskType type = null;
    TaskStatus status = TaskStatus.TODO;
    String holder = null;
    int statusCode = -1;
    boolean exists = false;
    final Guild guild;
    String newLanguageSuggestion = null;
    String notifyChannelMessageID = null;
    String boardName = "default";
    String boardID = "default";

    /** Get Task by id.
     *
     * @param taskID The id of the task.
     * @param guild The guild where the task could be.
     */
    public Task(String taskID, Guild guild) {
        this.id = taskID;
        this.guild = guild;

        try {
            //Try user task
            for (DocumentSnapshot boardDoc : Main.firestore.collection("server").document(guild.getId()).collection("boards").get().get().getDocuments()) {
                final DocumentSnapshot taskDoc = boardDoc.getReference().collection("user-tasks").document(taskID).get().get();
                if (taskDoc.exists()) {
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
                    break;
                }
            }

            if (!this.exists) {
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
        this.guild = guild;
        this.text = text;
        this.deadline = deadline;
        this.type = TaskType.USER;
        this.holder = member.getId();

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
            }});
            setStatusCode(200);


            this.id = taskID;
            this.boardName = boardName;
            this.boardID = boardID;
            this.newLanguageSuggestion = null;

            final DocumentSnapshot getServermember = Main.firestore.collection("server").document(guild.getId()).collection("server_member").document(member.getId()).get().get();
            if (getServermember.exists()) {
                if (Objects.requireNonNull(getServermember.getData()).containsKey("notify_channel")) {
                    final String channelID = getServermember.getString("notify_channel");
                    if (guild.getTextChannelById(channelID) != null) {
                        final String langCode = Localizations.getGuildLanguage(guild);
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
            setStatusCode(-1);
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
            if (Main.firestore.collection("server").document(guild.getId()).collection("boards").document(activeBoardID).collection("user-tasks").document(sb.toString()).get().get().exists()) {
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
        this.guild = guild;
        this.text = text;
        this.deadline = deadline;
        this.type = TaskType.GROUP;
        this.holder = groupName;
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
                        this.boardName = Main.firestore.collection("server").document(guild.getId()).collection("boards").document(boardID).get().get().getString("name");
                    }
                }
                final DocumentSnapshot groupDoc = getGroup.getDocuments().get(0);
                final String taskID = generateTaskID(guild, boardID);
                String finalBoardID = boardID;
                groupDoc.getReference().collection("group-tasks").document(taskID).set(new HashMap<>() {{
                    put("board_id", finalBoardID);
                    put("deadline", deadline);
                    put("deadline_reminded", false);
                    put("position", -1);
                    put("status", 0);
                    put("text", text);
                }});
                setStatusCode(200);
                this.id = taskID;
                this.boardID = boardID;

                //Send task into group notify channel
                if (groupDoc.getData().containsKey("notify_channel")) {
                    final String channelID = getGroup.getDocuments().get(0).getString("notify_channel");
                    if (guild.getTextChannelById(channelID) != null) {
                        final String langCode = Localizations.getGuildLanguage(guild);
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
            setStatusCode(-1);
            e.printStackTrace();
        }
    }

    public String getNotifyChannelMessageID() {
        if (this.notifyChannelMessageID == null) {
            if (this.type == TaskType.USER) {

            }

            try {
                final Response res = Main.tmbAPI("task/" + ((this.type == TaskType.GROUP) ? "group" : "user") + "/info/" + this.guild.getId() + "/" + this.id, null, Method.GET).execute();
                if (res.statusCode() == 200) {
                    this.type = TaskType.GROUP;
                    final Document a = res.parse();
                    final JsonObject jsonObject = Json.parse(a.body().text()).asObject();
                    this.notifyChannelMessageID = (jsonObject.get("notify_channel_message_id") != null && !jsonObject.get("notify_channel_message_id").isNull()) ? jsonObject.getString("notify_channel_message_id", null) : null;
                    return (jsonObject.get("notify_channel_message_id") != null && !jsonObject.get("notify_channel_message_id").isNull()) ? jsonObject.getString("notify_channel_message_id", null) : null;
                }
            } catch (Exception e) {
                setStatusCode(-2);
                e.printStackTrace();
            }
            return null;
        }
        return notifyChannelMessageID;
    }

    public void setText(String text) {
        try {
            Response res = Main.tmbAPI("task/" + (this.type == TaskType.USER ? "user" : "group") + "/edit/" + this.guild.getId() + "/" + this.id, null, Method.POST).data("task_text", text).execute();
            setStatusCode(res.statusCode());
            if (getStatusCode() == 200) {
                this.text = text;

                //Update in notify channel
                final String langCode = Localizations.getGuildLanguage(guild);
                updateNotifyChannel(this.guild, Localizations.getString("task_info_field_task", langCode), this.text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Task setDeadline(String deadline) {
        try {
            final Response res = Main.tmbAPI("task/" + (this.type == TaskType.USER ? "user" : "group") + "/set-deadline/" + guild.getId() + "/" + this.id, null, Method.POST).data("deadline", deadline).execute();
            setStatusCode(res.statusCode());
            if (getStatusCode() == 200) {
                this.deadline = deadline;

                //Update in notify channel
                final String langCode = Localizations.getGuildLanguage(guild);
                updateNotifyChannel(this.guild, Localizations.getString("task_info_field_deadline", langCode), this.deadline);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getNotifyChannelID(Guild guild) {
        try {
            final org.jsoup.Connection.Response getNotifyChannelRes = Main.tmbAPI(((this.type == TaskType.GROUP) ? "group" : "user") + "/notify-channel/" + guild.getId() + "/" + ((this.type == TaskType.GROUP) ? holder : ""), (this.type == TaskType.GROUP) ? null : holder, Method.GET).execute();
            if (getNotifyChannelRes.statusCode() == 200) {
                final JsonObject notifyChannelObject = Json.parse(getNotifyChannelRes.parse().body().text()).asObject();
                if (notifyChannelObject.get("channel") != null) {
                    final String channel = notifyChannelObject.getString("channel", null);
                    return channel;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateNotifyChannel(Guild guild, String valueTitle, String newValue) {
        final String messageID = getNotifyChannelMessageID();
        if (messageID != null) {
            final String channelID = getNotifyChannelID(guild);
            if (channelID != null && guild.getTextChannelById(channelID) != null) {
                guild.getTextChannelById(channelID).retrieveMessageById(messageID).queue(message -> {
                    if (message.getAuthor().isBot()) {
                        EmbedBuilder newEmbed = new EmbedBuilder();
                        for (MessageEmbed embed : message.getEmbeds()) {
                            newEmbed.setColor(embed.getColor());
                            newEmbed.setTitle(embed.getTitle());
                            for (MessageEmbed.Field field : embed.getFields()) {
                                if (!field.getName().equalsIgnoreCase(valueTitle)) {
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

    public Task delete() {
        try {
            final Response res = Main.tmbAPI("task/" + (this.type == TaskType.USER ? "user" : "group") + "/" + this.guild.getId() + "/" + this.id, null, Method.DELETE).execute();
            setStatusCode(res.statusCode());
            this.exists = false;

            //Delete message channel
            final String messageID = getNotifyChannelMessageID();
            if (messageID != null) {
                final String channelID = getNotifyChannelID(guild);
                if (channelID != null && guild.getTextChannelById(channelID) != null) {
                    guild.getTextChannelById(channelID).retrieveMessageById(messageID).queue(message -> message.delete().queue(), (error) -> {
                    });
                }
            }
            return this;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Task setStatus(TaskStatus status, Member member) {
        try {
            int number = 0;
            switch (status) {
                case DONE:
                    number = 2;
                    break;
                case IN_PROGRESS:
                    number = 1;
                    break;
            }
            final Response res = Main.tmbAPI("task/" + (this.type == TaskType.USER ? "user" : "group") + "/set-status/" + guild.getId() + "/" + this.id + "/" + number, member.getId(), Method.PUT).execute();
            setStatusCode(res.statusCode());
            if (getStatusCode() == 200) {
                this.status = status;

                //Update in notify channel
                final String langCode = Localizations.getGuildLanguage(guild);
                String newStatus = Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode);
                if (status == TaskStatus.IN_PROGRESS)
                    newStatus = Localizations.getString("aufgaben_status_wird_bearbeitet", langCode);
                else if (status == TaskStatus.DONE)
                    newStatus = Localizations.getString("aufgaben_status_erledigt", langCode);
                updateNotifyChannel(this.guild, Localizations.getString("task_info_field_state", langCode), newStatus);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return this;
    }

    public Task proceed(Member member) {
        try {
            final Response res = Main.tmbAPI("task/" + (this.type == TaskType.USER ? "user" : "group") + "/update/" + guild.getId() + "/" + this.id, member.getId(), Method.PUT).execute();
            processResponseProceedUndoCommand(res);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return this;
    }

    private void processResponseProceedUndoCommand(Response res) throws IOException {
        setStatusCode(res.statusCode());
        if (getStatusCode() == 200) {
            final Document document = res.parse();
            final String jsonResponse = document.body().text();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            this.status = TaskStatus.values()[jsonObject.getInt("status", 0)];

            //Update in notify channel
            final String langCode = Localizations.getGuildLanguage(guild);
            String newStatus = Localizations.getString("aufgaben_status_nicht_bearbeitet", langCode);
            if (status == TaskStatus.IN_PROGRESS)
                newStatus = Localizations.getString("aufgaben_status_wird_bearbeitet", langCode);
            else if (status == TaskStatus.DONE)
                newStatus = Localizations.getString("aufgaben_status_erledigt", langCode);
            updateNotifyChannel(this.guild, Localizations.getString("task_info_field_state", langCode), newStatus);
        }
    }

    public Task undo(Member member) {
        try {
            final Response res = Main.tmbAPI("task/" + (this.type == TaskType.USER ? "user" : "group") + "/undo/" + guild.getId() + "/" + this.id, member.getId(), Method.PUT).execute();
            processResponseProceedUndoCommand(res);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return this;
    }

    public String newLanguageSuggestion() {
        return newLanguageSuggestion;
    }

    private void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
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

