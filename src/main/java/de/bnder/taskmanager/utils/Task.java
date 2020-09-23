package de.bnder.taskmanager.utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jsoup.Jsoup;

import java.io.IOException;

public class Task {

    final String id;
    String text = null;
    String deadline = null;
    TaskType type = null;
    TaskStatus status = TaskStatus.TODO;
    String holder = null;
    int statusCode = -1;
    boolean exists = false;
    final Guild guild;

    public Task(String taskID, Guild guild) {
        this.id = taskID;
        this.guild = guild;

        try {
            String jsonResponse = Jsoup.connect(Main.requestURL + "getTaskInfo.php?requestToken=" + Main.requestToken + "&task_id=" + taskID + "&server_id=" + Connection.encodeString(guild.getId())).userAgent(Main.userAgent).timeout(Connection.timeout).execute().body();
            JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            setStatusCode(jsonObject.getInt("status_code", 900));
            if (getStatusCode() == 200) {
                this.exists = true;
                final int taskStatusInt = jsonObject.getInt("task_status", 0);
                if (taskStatusInt >= 0) {
                    this.status = TaskStatus.values()[taskStatusInt];
                }
                this.deadline = jsonObject.getString("task_deadline", null);
                this.type = jsonObject.getString("task_type", null).equalsIgnoreCase("user") ? TaskType.USER : TaskType.GROUP;
                this.holder = this.type == TaskType.USER ? jsonObject.getString("user_id", null) : jsonObject.getString("group_name", null);
                this.text = jsonObject.getString("task_text", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Task(String taskID, Guild guild, String text, String deadline, TaskType type, TaskStatus status, String holder) {
        this.id = taskID;
        this.guild = guild;
        this.text = text;
        this.deadline = deadline;
        this.type = type;
        this.status = status;
        this.holder = holder;
        this.exists = true;
        this.statusCode = 200;
    }

    public void setText(String text) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "editTask.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(guild.getId()) + "&task=" + Connection.encodeString(text) + "&taskID=" + Connection.encodeString(this.id)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
            JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            setStatusCode(jsonObject.getInt("status_code", 900));
            if (getStatusCode() == 200) {
                this.text = text;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Task setDeadline(String deadline) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "setDeadline.php?requestToken=" + Main.requestToken + "&taskID=" + this.id + "&date=" + Connection.encodeString(deadline) + "&serverID=" + Connection.encodeString(guild.getId())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            setStatusCode(jsonObject.getInt("status_code", 900));
            if (getStatusCode() == 200) {
                this.deadline = deadline;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void delete() {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "deleteTask.php?requestToken=" + Main.requestToken + "&serverID=" + Connection.encodeString(guild.getId()) + "&taskID=" + id).userAgent(Main.userAgent).timeout(Connection.timeout).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            setStatusCode(jsonObject.getInt("status_code", 900));
            this.exists = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Task setStatus(TaskStatus status, Member member) {
        try {
            if (status == TaskStatus.DONE) {
                final String jsonResponse = Jsoup.connect(Main.requestURL + "updateTaskStatus.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.getId()) + "&task_id=" + this.id + "&user_id=" + member.getId()).userAgent(Main.userAgent).timeout(Connection.timeout).execute().body();
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                setStatusCode(jsonObject.getInt("status_code", 900));
                if (getStatusCode() == 200) {
                    Jsoup.connect(Main.requestURL + "updateTaskStatus.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.getId()) + "&task_id=" + this.id + "&user_id=" + member.getId()).userAgent(Main.userAgent).timeout(Connection.timeout).execute().body();
                } else if (getStatusCode() == 904) {
                    final String jsonResponse2 = Jsoup.connect(Main.requestURL + "updateTaskStatus.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.getId()) + "&task_id=" + this.id + "&user_id=" + member.getId() + "&force=1").userAgent(Main.userAgent).timeout(Connection.timeout).execute().body();
                    final JsonObject jsonObject2 = Json.parse(jsonResponse2).asObject();
                    setStatusCode(jsonObject2.getInt("status_code", 900));
                    if (getStatusCode() == 200) {
                        Jsoup.connect(Main.requestURL + "updateTaskStatus.php?requestToken=" + Main.requestToken + "&server_id=" + Connection.encodeString(guild.getId()) + "&task_id=" + this.id + "&user_id=" + member.getId() + "&force=1").userAgent(Main.userAgent).timeout(Connection.timeout).execute().body();
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (getStatusCode() == 200) {
            this.status = status;
        }
        return this;
    }

    public Task proceed(Member member) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "updateTaskStatus.php?requestToken=" + Main.requestToken + "&task_id=" + this.id + "&server_id=" + Connection.encodeString(guild.getId()) + "&user_id=" + Connection.encodeString(member.getId())).userAgent(Main.userAgent).timeout(Connection.timeout).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            setStatusCode(jsonObject.getInt("status_code", 900));
            if (getStatusCode() == 200) {
                this.status = TaskStatus.values()[jsonObject.getInt("process", 0)];
            } else if (getStatusCode() == 904) {
                if (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
                    final String jsonResponse2 = Jsoup.connect(Main.requestURL + "updateTaskStatus.php?requestToken=" + Main.requestToken + "&task_id=" + this.id + "&server_id=" + Connection.encodeString(guild.getId()) + "&user_id=" + Connection.encodeString(member.getId()) + "&force=1").userAgent(Main.userAgent).timeout(Connection.timeout).execute().body();
                    final JsonObject jsonObject2 = Json.parse(jsonResponse2).asObject();
                    setStatusCode(jsonObject2.getInt("status_code", 900));
                    if (getStatusCode() == 200) {
                        this.status = TaskStatus.values()[jsonObject.getInt("process", 0)];
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return this;
    }

    public Task undo(Member member) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "undoTaskStatus.php?requestToken=" + Main.requestToken + "&task_id=" + this.id + "&server_id=" + Connection.encodeString(guild.getId())).userAgent(Main.userAgent).timeout(Connection.timeout).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            setStatusCode(jsonObject.getInt("status_code", 900));
            if (getStatusCode() == 200) {
                if (jsonObject.getInt("process", 0) >= 0) {
                    this.status = TaskStatus.values()[jsonObject.getInt("process", 0)];
                } else {
                    setStatusCode(931);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return this;
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
}

