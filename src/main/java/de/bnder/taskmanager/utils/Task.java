package de.bnder.taskmanager.utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

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

    public Task(String taskID, Guild guild) {
        this.id = taskID;
        this.guild = guild;

        try {
            JsonObject jsonObject = null;
            Response res;
            //Try user task
            res = Jsoup.connect(Main.requestURL + "/task/user/" + guild.getId() + "/" + taskID).method(Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
            setStatusCode(res.statusCode());
            if (getStatusCode() == 200) {
                this.type = TaskType.USER;
                final Document a = res.parse();
                jsonObject = Json.parse(a.body().text()).asObject();
            } else if (getStatusCode() == 404) {
                //Try group task
                res = Jsoup.connect(Main.requestURL + "/task/group/" + guild.getId() + "/" + taskID).method(Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                setStatusCode(res.statusCode());
                if (getStatusCode() == 200) {
                    this.type = TaskType.GROUP;
                    final Document a = res.parse();
                    jsonObject = Json.parse(a.body().text()).asObject();
                } else {
                    this.exists = false;
                    return;
                }
            }

            if (getStatusCode() == 200) {
                this.exists = true;
                final int taskStatusInt = jsonObject.getInt("status", 0);
                if (taskStatusInt >= 0) {
                    this.status = TaskStatus.values()[taskStatusInt];
                }
                this.deadline = jsonObject.get("deadline").toString();
                this.holder = this.type == TaskType.USER ? jsonObject.getString("user_id", null) : jsonObject.getString("group_name", null);
                this.text = jsonObject.getString("text", null);
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

    public Task(Guild guild, String text, String deadline, Member member) {
        this.guild = guild;
        this.text = text;
        this.deadline = deadline;
        this.type = TaskType.USER;
        this.holder = member.getId();
        try {
            final Document a = Jsoup.connect(Main.requestURL + "/task/user/" + guild.getId()).method(Method.PUT).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member.getId()).data("task_text", text).data("deadline", deadline != null ? deadline : "").postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).post();
            final String jsonResponse = a.body().text();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            setStatusCode(200);
            this.id = jsonObject.getString("id", null);
            this.newLanguageSuggestion = jsonObject.get("new_language_suggestion").isNull() ? null : jsonObject.getString("new_language_suggestion", null);
        } catch (Exception e) {
            setStatusCode(-1);
            e.printStackTrace();
        }
    }

    public Task(Guild guild, String text, String deadline, String holder) {
        this.guild = guild;
        this.text = text;
        this.deadline = deadline;
        this.type = TaskType.GROUP;
        this.holder = holder;
        try {
            final Document a = Jsoup.connect(Main.requestURL + "/task/group/" + guild.getId() + "/" + holder).method(Method.PUT).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").data("task_text", text).data("deadline", deadline != null ? deadline : "").postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).post();
            final String jsonResponse = a.body().text();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            setStatusCode(200);
            this.id = jsonObject.getString("id", null);
        } catch (Exception e) {
            setStatusCode(-1);
            e.printStackTrace();
        }
    }

    public String newLanguageSuggestion() {
        return newLanguageSuggestion;
    }

    public void setText(String text) {
        try {
            Response res = Jsoup.connect(Main.requestURL + "/task/" + (this.type == TaskType.USER ? "user" : "group") + "/edit/" + this.guild.getId() + "/" + this.id).method(Method.POST).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").data("task_text", text).postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
            setStatusCode(res.statusCode());
            if (getStatusCode() == 200) {
                this.text = text;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Task setDeadline(String deadline) {
        try {
            final Response res = Jsoup.connect(Main.requestURL + "/task/" + (this.type == TaskType.USER ? "user" : "group") + "/set-deadline/" + guild.getId() + "/" + this.id ).method(Method.POST).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").data("deadline", deadline).postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
            setStatusCode(res.statusCode());
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
            Response res = Jsoup.connect(Main.requestURL + "/task/" + (this.type == TaskType.USER ? "user" : "group") + "/" + this.guild.getId() + "/" + this.id).method(Method.DELETE).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
            setStatusCode(res.statusCode());
            this.exists = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            final Response res = Jsoup.connect(Main.requestURL + "/task/" + (this.type == TaskType.USER ? "user" : "group") + "/set-status/" + guild.getId() + "/" + this.id + "/" + number).method(Method.PUT).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member.getId()).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
            setStatusCode(res.statusCode());
            if (getStatusCode() == 200) {
                this.status = status;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return this;
    }

    public Task proceed(Member member) {
        try {
            final Response res = Jsoup.connect(Main.requestURL + "/task/" + (this.type == TaskType.USER ? "user" : "group") + "/update/" + guild.getId() + "/" + this.id).method(Method.PUT).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member.getId()).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
            setStatusCode(res.statusCode());
            if (getStatusCode() == 200) {
                final Document document = res.parse();
                final String jsonResponse = document.body().text();
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                this.status = TaskStatus.values()[jsonObject.getInt("status", 0)];
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return this;
    }

    public Task undo(Member member) {
        try {
            final Response res = Jsoup.connect(Main.requestURL + "/task/" + (this.type == TaskType.USER ? "user" : "group") + "/undo/" + guild.getId() + "/" + this.id).method(Method.PUT).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member.getId()).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
            setStatusCode(res.statusCode());
            if (getStatusCode() == 200) {
                final Document document = res.parse();
                final String jsonResponse = document.body().text();
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                this.status = TaskStatus.values()[jsonObject.getInt("status", 0)];
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

