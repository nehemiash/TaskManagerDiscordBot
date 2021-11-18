package de.bnder.taskmanager.commands;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jsoup.Connection;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Search implements Command {

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, List<Member> mentionedMembers, List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException {
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
                final Connection.Response res = Main.tmbAPI("global/search/" + guild.getId(), commandExecutor.getId(), Connection.Method.POST).data("search_term", searchTerm).execute();
                final JsonObject jsonObject = Json.parse(res.body()).asObject();
                final JsonArray tasksResultArray = jsonObject.get("task_results").asArray();
                final JsonArray groupsResultArray = jsonObject.get("group_results").asArray();
                if (tasksResultArray.size() > 0 || groupsResultArray.size() > 0) {
                    int tasksResultArrayIterations = 0;
                    for (JsonValue array : tasksResultArray) {
                        if (tasksResultArrayIterations < taskResultLimit) {
                            final String taskID = array.asString();
                            final Task task = new Task(taskID, guild);
                            final EmbedBuilder builder = new EmbedBuilder().setColor(Color.cyan);
                            builder.setTitle(Localizations.getString("search_result_task_title", langCode));
                            builder.setDescription(Localizations.getString("search_result_task_description", langCode));
                            if (task.getStatus() == TaskStatus.TODO) {
                                builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("task_status_to_do_keyword", langCode), true);
                            } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                                builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("task_status_in_progress_keyword", langCode), true);
                            } else if (task.getStatus() == TaskStatus.DONE) {
                                builder.addField(Localizations.getString("task_info_field_state", langCode), Localizations.getString("task_status_done_keyword", langCode), true);
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
                    for (JsonValue value : groupsResultArray) {
                        if (groupsResultArrayIterations < groupResultLimit) {
                            final String groupID = value.asString();
                            final EmbedBuilder builder = new EmbedBuilder().setColor(Color.cyan);

                            final Connection.Response groupInfoRes = Main.tmbAPI("group/info/" + guild.getId() + "/" + groupID, commandExecutor.getId(), Connection.Method.GET).execute();
                            final JsonObject groupInfoJsonObject = Json.parse(groupInfoRes.body()).asObject();
                            final String groupName = groupInfoJsonObject.getString("name", null);

                            if (!groupInfoJsonObject.get("notifyChannelID").isNull()) {
                                final String channelID = groupInfoJsonObject.getString("notifyChannelID", null);
                                if (channelID != null && guild.getTextChannelById(channelID) != null) {
                                    builder.addField(Localizations.getString("search_notify_channel_field", langCode), guild.getTextChannelById(channelID).getAsMention(), true);
                                }
                            }
                            final JsonArray membersList = groupInfoJsonObject.get("memberList").asArray();
                            if (membersList.size() > 0) {
                                final StringBuilder membersStringBuilder = new StringBuilder();
                                for (JsonValue membersValue : membersList) {
                                    final String userID = membersValue.asObject().get("user_id").asString();
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
            }
        }
    }
}
