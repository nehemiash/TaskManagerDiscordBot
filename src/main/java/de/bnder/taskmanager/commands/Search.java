package de.bnder.taskmanager.commands;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class Search implements Command {

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, List<Member> mentionedMembers, List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(guild);
        final String embedTitle = Localizations.getString("search_title", langCode);
        final int taskResultLimit = 10;
        final int groupResultLimit = 10;
        if (args.length > 0) {
            final StringBuilder searchTermBuilder = new StringBuilder();
            final StringBuilder searchTermBuilderRaw = new StringBuilder();
            for (final String searchTermArg : args) {
                //Check if username is in searchtermarg
                Member member = guild.getMembersByName(searchTermArg, true).size() > 0 ? guild.getMembersByName(searchTermArg, true).get(0) : null;
                if (member == null) {
                    member = guild.getMembersByNickname(searchTermArg, true).size() > 0 ? guild.getMembersByNickname(searchTermArg, true).get(0) : null;
                }
                if (member != null) {
                    searchTermBuilder.append(member.getId()).append(" ");
                } else {
                    searchTermBuilder.append(searchTermArg).append(" ");
                }
                searchTermBuilderRaw.append(searchTermArg).append(" ");
            }
            final String searchTerm = searchTermBuilder.substring(0, searchTermBuilder.length() - 1);
            final String searchTermRaw = searchTermBuilderRaw.substring(0, searchTermBuilderRaw.length() - 1);
            if (searchTermRaw.length() >= 3) {
                try {
                    final QuerySnapshot groups = Main.firestore.collection("server").document(guild.getId()).collection("groups").whereEqualTo("name", searchTerm).limit(groupResultLimit).get().get();
                    final QuerySnapshot userTasksQuery = Main.firestore.collectionGroup("user-tasks").whereEqualTo("server_id", guild.getId()).whereEqualTo("text", searchTerm).whereEqualTo(FieldPath.documentId(), searchTerm).limit(taskResultLimit).get().get();
                    final ArrayList<String> taskIDs = new ArrayList<>();
                    for (final DocumentSnapshot userTaskDoc : userTasksQuery) {
                        if (!taskIDs.contains(userTaskDoc.getId())) {
                            taskIDs.add(userTaskDoc.getId());
                        }
                    }
                    for (QueryDocumentSnapshot groupTaskDoc : Main.firestore.collectionGroup("group-tasks").whereEqualTo("server_id", guild.getId()).whereEqualTo("text", searchTerm).whereEqualTo(FieldPath.documentId(), searchTerm).limit(taskResultLimit).get().get()) {
                        if (groupTaskDoc.exists() && !taskIDs.contains(groupTaskDoc.getId())) {
                            taskIDs.add(groupTaskDoc.getId());
                        }
                    }


                    if (taskIDs.size() > 0 || groups.size() > 0) {
                        int tasksResultArrayIterations = 0;
                        for (String taskID : taskIDs) {
                            if (tasksResultArrayIterations < taskResultLimit) {
                                final Task task = new Task(taskID, guild);
                                final EmbedBuilder builder = new EmbedBuilder().setColor(Color.cyan);
                                builder.setTitle(Localizations.getString("search_result_task_title", langCode));
                                builder.setDescription(Localizations.getString("search_result_task_description", langCode));
                                if (task.getStatus() == TaskStatus.TODO) {
                                    builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("task_status_to_do", langCode), true);
                                } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                                    builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("task_status_in_progress", langCode), true);
                                } else if (task.getStatus() == TaskStatus.DONE) {
                                    builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("task_status_done", langCode), true);
                                }
                                if (task.getDeadline() != null) {
                                    builder.addField(Localizations.getString("task_info_field_deadline", langCode), task.getDeadline(), true);
                                }
                                if (task.getType() == TaskType.USER) {
                                    builder.addField(Localizations.getString("task_info_field_type", langCode), Localizations.getString("task_info_field_type_user", langCode), true);
                                    final String userID = task.getHolder();
                                    if (userID != null && guild.retrieveMemberById(userID).complete() != null) {
                                        builder.addField(Localizations.getString("task_info_field_assigned", langCode), guild.retrieveMemberById(userID).complete().getUser().getAsTag(), true);
                                    }
                                } else if (task.getType() == TaskType.GROUP) {
                                    builder.addField(Localizations.getString("task_info_field_type", langCode), Localizations.getString("task_info_field_type_group", langCode), true);
                                    builder.addField(Localizations.getString("task_info_field_assigned", langCode), task.getHolder(), true);
                                }
                                builder.addField(Localizations.getString("task_info_field_task", langCode), task.getText(), false);
                                builder.addField(Localizations.getString("task_info_field_id", langCode), taskID, false);
                                textChannel.sendMessageEmbeds(builder.build()).queue();
                                tasksResultArrayIterations++;
                            } else {
                                MessageSender.send(embedTitle, Localizations.getString("search_results_hidden_text", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                                break;
                            }
                        }

                        int groupsResultArrayIterations = 0;
                        for (QueryDocumentSnapshot groupDoc : groups) {
                            if (groupsResultArrayIterations < groupResultLimit) {
                                final EmbedBuilder builder = new EmbedBuilder().setColor(Color.cyan);

                                final String groupName = groupDoc.getString("name");

                                if (groupDoc.contains("notify_channel") && groupDoc.get("notify_channel") != null) {
                                    final String channelID = groupDoc.getString("notify_channel");
                                    if (channelID != null && guild.getTextChannelById(channelID) != null) {
                                        builder.addField(Localizations.getString("search_notify_channel_field", langCode), guild.getTextChannelById(channelID).getAsMention(), true);
                                    }
                                }
                                final QuerySnapshot membersList = groupDoc.getReference().collection("group-member").get().get();
                                if (membersList.size() > 0) {
                                    final StringBuilder membersStringBuilder = new StringBuilder();
                                    for (QueryDocumentSnapshot membersValue : membersList) {
                                        final String userID = membersValue.getString("user_id");
                                        guild.retrieveMemberById(userID).queue(user -> membersStringBuilder.append(user.getUser().getAsTag()).append(", "), (error) -> {
                                        });
                                    }
                                    builder.addField(Localizations.getString("search_members_field", langCode), membersStringBuilder.substring(0, membersStringBuilder.length() - 2), false);
                                }

                                builder.setTitle(Localizations.getString("search_result_group_title", langCode));
                                builder.setDescription(Localizations.getString("search_result_group_description", langCode));
                                builder.addField(Localizations.getString("search_group_name_field", langCode), groupName, true);

                                textChannel.sendMessageEmbeds(builder.build()).queue();
                                groupsResultArrayIterations++;
                            } else {
                                MessageSender.send(embedTitle, Localizations.getString("search_results_hidden_text", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                                break;
                            }
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("no_search_results", langCode, new ArrayList<String>() {{
                            add(searchTermRaw);
                        }}), textChannel, Color.red, langCode, slashCommandEvent);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
