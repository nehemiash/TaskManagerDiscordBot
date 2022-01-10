package de.bnder.taskmanager.commands.task;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.commands.group.CreateGroup;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.UserSettings;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CreateTask {

    private static final Logger logger = LogManager.getLogger(CreateTask.class);

    public static void addTask(String commandMessage, Member member, List<Member> mentionedMembers, TextChannel textChannel, String[] args, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("task_message_title", langCode);

        if (!PermissionSystem.hasPermission(member, TaskPermission.CREATE_TASK)) {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_server_owner_have_admin_or_custom_permission", langCode, new ArrayList<>() {{
                add(TaskPermission.CREATE_TASK.name());
                add(member.getAsMention());
            }}), textChannel, Color.red, langCode, slashCommandEvent);
            return;
        }

        if (mentionedMembers != null && mentionedMembers.size() > 0) {
            final String task = getTaskFromArgs(1 + mentionedMembers.size(), commandMessage, true);
            for (Member mentionedMember : mentionedMembers) {
                final de.bnder.taskmanager.utils.Task taskObject = new de.bnder.taskmanager.utils.Task(member.getGuild(), task, null, mentionedMember, member);
                if (taskObject.exists()) {
                    String newLanguageSuggestionAppend = taskObject.newLanguageSuggestion() != null ? Localizations.getString("task_new_language_suggestion_text", new Locale(taskObject.newLanguageSuggestion()), new ArrayList<>() {{
                        add(String.valueOf(commandMessage.charAt(0)));
                        add(taskObject.newLanguageSuggestion());
                    }}) : "";
                    sendTaskMessage(mentionedMember, member, taskObject.getId(), langCode, task);
                    MessageSender.send(embedTitle + " - " + taskObject.getId(), Localizations.getString("task_created", langCode, new ArrayList<>() {
                        {
                            add(mentionedMember.getUser().getName());
                            add(taskObject.getBoardName());
                        }
                    }) + newLanguageSuggestionAppend, textChannel, Color.green, langCode, slashCommandEvent);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("task_created_unbekannter_fehler", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                }
            }
        } else if (CreateGroup.groupExists(args[1], member.getGuild().getId())) {
            final String groupName = args[1];
            final String task = getTaskFromArgs(3, commandMessage, false);
            final de.bnder.taskmanager.utils.Task taskObject = new de.bnder.taskmanager.utils.Task(textChannel.getGuild(), task, null, groupName, member);
            if (taskObject.exists()) {
                try {
                    final QuerySnapshot getGroupMembers = Main.firestore.collection("server").document(member.getGuild().getId()).collection("groups").whereEqualTo("name", groupName).get().get().getDocuments().get(0).getReference().collection("group-member").get().get();
                    int usersWhoReceivedTheTaskAmount = 0;
                    for (final QueryDocumentSnapshot groupMemberDoc : getGroupMembers) {
                        final String id = groupMemberDoc.getString("user_id");
                        try {
                            usersWhoReceivedTheTaskAmount++;
                            member.getGuild().retrieveMemberById(id).queue(groupMember -> {
                                if (groupMember != null) {
                                    sendTaskMessage(groupMember, member, taskObject.getId(), langCode, task);
                                }
                            }, throwable -> {
                            });
                        } catch (ErrorResponseException e) {
                            groupMemberDoc.getReference().delete();
                        }
                    }
                    int finalUsersWhoReceivedTheTaskAmount = usersWhoReceivedTheTaskAmount;
                    MessageSender.send(embedTitle + " - " + taskObject.getId(), Localizations.getString("sent_task_to_x_members", langCode, new ArrayList<>() {
                        {
                            add(String.valueOf(finalUsersWhoReceivedTheTaskAmount));
                        }
                    }), textChannel, Color.green, langCode, slashCommandEvent);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(e);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("no_group_on_server", langCode, new ArrayList<>() {
                    {
                        add(groupName);
                    }
                }), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("task_create_missing_arguments", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

    private static void sendTaskMessage(Member member, Member author, String taskID, Locale langCode, String task) {
        final UserSettings userSettings = new UserSettings(member);
        if (userSettings.getDirectMessage()) {
            try {
                member.getUser().openPrivateChannel().queue(channel -> {
                    channel.sendMessage(Localizations.getString("task_received", langCode, new ArrayList<>() {
                        {
                            add(author.getUser().getAsTag());
                            add(taskID);
                            add(member.getGuild().getName());
                        }
                    })).queue();
                    try {
                        channel.sendMessage(URLDecoder.decode(task, StandardCharsets.UTF_8.toString())).queue();
                    } catch (UnsupportedEncodingException e) {
                        logger.error(e);
                    }
                });
            } catch (Exception ignored) {
            }
        } else if (userSettings.getNotifyChannelID() != null) {
            final TextChannel channel = author.getGuild().getTextChannelById(userSettings.getNotifyChannelID());
            if (channel != null) {
                try {
                    channel.sendMessage(member.getAsMention() + Localizations.getString("task_received", langCode, new ArrayList<>() {
                        {
                            add(author.getUser().getAsTag());
                            add(taskID);
                            add(member.getGuild().getName());
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
        return taskBuilder.substring(0, taskBuilder.length() - 1);
    }

}
