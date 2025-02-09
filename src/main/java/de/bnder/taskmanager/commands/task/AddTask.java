package de.bnder.taskmanager.commands.task;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import de.bnder.taskmanager.commands.group.GroupNotifications;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddTask {

    public static void addTask(String commandMessage, Member member, List<Member> mentionedMembers, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) throws IOException {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);
        if (PermissionSystem.hasPermission(member, TaskPermission.CREATE_TASK)) {
            if (mentionedMembers != null && mentionedMembers.size() > 0) {
                final String task = getTaskFromArgs(1 + mentionedMembers.size(), commandMessage, true);
                for (Member mentionedMember : mentionedMembers) {
                    final de.bnder.taskmanager.utils.Task taskObject = new de.bnder.taskmanager.utils.Task(member.getGuild(), task, null, mentionedMember, member);
                    if (taskObject.getStatusCode() == 200) {
                        String newLanguageSuggestionAppend = taskObject.newLanguageSuggestion() != null ? Localizations.getString("task_new_language_suggestion_text", Locale.forLanguageTag(taskObject.newLanguageSuggestion()), new ArrayList<>() {{
                            add(String.valueOf(commandMessage.charAt(0)));
                            add(taskObject.newLanguageSuggestion());
                        }}) : "";
                        sendTaskMessage(mentionedMember, member, taskObject.getId(), langCode, task);
                        MessageSender.send(embedTitle + " - " + taskObject.getId(), Localizations.getString("task_created", langCode, new ArrayList<>() {
                            {
                                add(mentionedMember.getUser().getName());
                                add(taskObject.getActiveBoardName());
                            }
                        }) + newLanguageSuggestionAppend, textChannel, Color.green, langCode, slashCommandEvent);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("task_created_unknown_error", langCode, new ArrayList<>() {
                            {
                                add(taskObject.getStatusCode() + " " + taskObject.getResponseMessage());
                            }
                        }), textChannel, Color.red, langCode, slashCommandEvent);
                    }
                }
            } else if (GroupNotifications.serverHasGroup(args[1], member.getGuild())) {
                final String groupName = Connection.encodeString(args[1]);
                final String task = getTaskFromArgs(3, commandMessage, false);
                final de.bnder.taskmanager.utils.Task taskObject = new de.bnder.taskmanager.utils.Task(textChannel.getGuild(), task, null, groupName, member);
                final int statusCode = taskObject.getStatusCode();
                if (statusCode == 200) {
                    final org.jsoup.Connection.Response res = Main.tmbAPI("group/members/" + textChannel.getGuild().getId() + "/" + groupName, member.getId(), org.jsoup.Connection.Method.GET).execute();
                    final int getGroupMembersStatusCode = res.statusCode();
                    if (getGroupMembersStatusCode == 200) {
                        final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
                        int usersWhoReceivedTheTaskAmount = 0;
                        for (JsonValue value : jsonObject.get("members").asArray()) {
                            final String id = value.asObject().getString("user_id", null);
                            if (id != null) {
                                try {
                                    if (member.getGuild().retrieveMemberById(id).complete() != null) {
                                        final Member groupMember = member.getGuild().retrieveMemberById(id).complete();
                                        usersWhoReceivedTheTaskAmount++;
                                        sendTaskMessage(groupMember, member, taskObject.getId(), langCode, task);
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        int finalUsersWhoReceivedTheTaskAmount = usersWhoReceivedTheTaskAmount;
                        MessageSender.send(embedTitle + " - " + taskObject.getId(), Localizations.getString("sent_task_to_x_members", langCode, new ArrayList<>() {
                            {
                                add(String.valueOf(finalUsersWhoReceivedTheTaskAmount));
                            }
                        }), textChannel, Color.green, langCode, slashCommandEvent);
                    } else if (getGroupMembersStatusCode == 404) {
                        MessageSender.send(embedTitle + " - " + taskObject.getId(), Localizations.getString("sent_task_to_x_members", langCode, new ArrayList<>() {
                            {
                                add("0");
                            }
                        }), textChannel, Color.green, langCode, slashCommandEvent);
                    }
                } else if (statusCode == 404) {
                    MessageSender.send(embedTitle, Localizations.getString("no_group_on_server", langCode, new ArrayList<>() {
                        {
                            add(groupName);
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("task_created_unknown_error", langCode, new ArrayList<>() {
                        {
                            add(taskObject.getStatusCode() + " " + taskObject.getResponseMessage());
                        }
                    }), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("task_create_missing_arguments", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

    private static void sendTaskMessage(Member member, Member author, String task_id, Locale langCode, String task) {
        final JsonObject settings = de.bnder.taskmanager.utils.Settings.getUserSettings(member);
        if (settings.getString("direct_message", "1").equalsIgnoreCase("1")) {
            try {
                member.getUser().openPrivateChannel().queue(channel -> {
                    channel.sendMessage(Localizations.getString("task_received", langCode, new ArrayList<>() {
                        {
                            add(author.getUser().getAsTag());
                            add(task_id);
                        }
                    })).queue();
                    try {
                        channel.sendMessage(URLDecoder.decode(task, StandardCharsets.UTF_8.toString())).queue();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception ignored) {
            }
        } else if (!settings.get("notify_channel").isNull()) {
            final TextChannel channel = author.getGuild().getTextChannelById(settings.getString("notify_channel", ""));
            if (channel != null) {
                try {
                    channel.sendMessage(member.getAsMention() + Localizations.getString("task_received", langCode, new ArrayList<>() {
                        {
                            add(author.getUser().getAsTag());
                            add(task_id);
                        }
                    })).queue();
                    channel.sendMessage(URLDecoder.decode(task, StandardCharsets.UTF_8.toString())).queue();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static String getTaskFromArgs(int startIndex, String messageText, boolean mentionedUsers) {
        int beginIndex = startIndex;
        final StringBuilder taskBuilder = new StringBuilder();
        final String[] args = messageText.split(" ");
        if (mentionedUsers) beginIndex++;
        for (int i = beginIndex; i < args.length; i++) {
            taskBuilder.append(args[i]).append(" ");
        }
        if (taskBuilder.length() > 0) {
            taskBuilder.subSequence(0, taskBuilder.length() - 2);
        }
        return taskBuilder.toString();
    }

}
