package de.bnder.taskmanager.listeners.typoReactionListeners;

import de.bnder.taskmanager.commands.Language;
import de.bnder.taskmanager.main.CommandHandler;
import de.bnder.taskmanager.utils.Localizations;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TaskTypoReactionListener extends ListenerAdapter {

    public static boolean isRightMessage(Message message, String commandKeyword, Member member) {
        try {
            if (message.getAuthor().isBot() && message.getAuthor().getId().equals(message.getJDA().getSelfUser().getId())) {
                if (message.getEmbeds().size() == 1) {
                    final MessageEmbed embed = message.getEmbeds().get(0);
                    for (Locale langCode : Language.validLangCodes) {
                        if (embed.getTitle().equals(Localizations.getString("typo_title", langCode)) && embed.getDescription().equals(Localizations.getString("typo_description", langCode))) {
                            String command = null;
                            String author = null;
                            for (MessageEmbed.Field field : embed.getFields()) {
                                if (field.getName().equals(Localizations.getString("typo_field_command_name", langCode))) {
                                    command = field.getValue();
                                } else if (field.getName().equals(Localizations.getString("typo_field_user_name", langCode))) {
                                    author = field.getValue();
                                }
                            }
                            if (command != null && author != null) {
                                if (member.getUser().getAsTag().equals(author)) {
                                    if (command.substring(1).startsWith(commandKeyword)) {
                                        return true;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static String getCommand(Message message, String commandKeyword, Member member) {
        if (isRightMessage(message, commandKeyword, member)) {
            final MessageEmbed embed = message.getEmbeds().get(0);
            String command = null;
            String author = null;
            for (Locale langCode : Language.validLangCodes) {
                for (MessageEmbed.Field field : embed.getFields()) {
                    if (field.getName().equals(Localizations.getString("typo_field_command_name", langCode))) {
                        command = field.getValue();
                    } else if (field.getName().equals(Localizations.getString("typo_field_user_name", langCode))) {
                        author = field.getValue();
                    }
                }
                if (command != null && author != null) {
                    if (member.getUser().getAsTag().equals(author)) {
                        if (command.substring(1).startsWith(commandKeyword)) {
                            return command;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static List<Member> getMentionedMembers(String messageRaw, Guild guild) {
        List<Member> mentionedMembers = new ArrayList<>();
        if (messageRaw.contains("<@") && messageRaw.contains(">")) {
            for (String a : messageRaw.split("<@")) {
                for (String userID : a.split(">")) {
                    if (userID != null) {
                        userID = userID.replace("!", "");
                        if (userID.length() == 18) {
                            try {
                                final Member member = guild.retrieveMemberById(userID).submit().get();
                                if (member != null)
                                    mentionedMembers.add(member);
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return mentionedMembers;
        }
        return null;
    }

    public static List<Role> getMentionedRoles(String messageRaw, Guild guild) {
        List<Role> mentionedRoles = new ArrayList<>();
        if (messageRaw.contains("<@&") && messageRaw.contains(">")) {
            for (String a : messageRaw.split("<@&")) {
                for (String roleID : a.split(">")) {
                    if (roleID != null) {
                        roleID = roleID.replace("!", "");
                        if (roleID.length() == 18) {
                            if (guild.getRoleById(roleID) != null) {
                                mentionedRoles.add(guild.getRoleById(roleID));
                            }
                        }
                    }
                }
            }
            return mentionedRoles;
        }
        return null;
    }

    public static List<TextChannel> getMentionedChannels(String messageRaw, Guild guild) {
        List<TextChannel> mentionedChannels = new ArrayList<>();
        if (messageRaw.contains("<#") && messageRaw.contains(">")) {
            for (String a : messageRaw.split("<#")) {
                for (String channelID : a.split(">")) {
                    if (channelID != null) {
                        channelID = channelID.replace("!", "");
                        if (channelID.length() == 18) {
                            if (guild.getTextChannelById(channelID) != null) {
                                mentionedChannels.add(guild.getTextChannelById(channelID));
                            }
                        }
                    }
                }
            }
            return mentionedChannels;
        }
        return null;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.isFromGuild())
            event.retrieveMember().queue(member -> {
                if (!member.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
                    event.retrieveMessage().queue(message -> {
                        if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅") || event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                            if (isRightMessage(message, "task", member) || isRightMessage(message, "t", member)) {
                                if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅")) {
                                    String command = getCommand(message, "task", member);
                                    if (command == null)
                                        command = getCommand(message, "t", member);

                                    String beheaded = command.substring(1);
                                    String[] splitBeheaded = beheaded.split(" ");
                                    ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
                                    String[] args = new String[split.size() - 1];
                                    split.subList(1, split.size()).toArray(args);

                                    message.delete().queue();
                                    CommandHandler.handleCommand(CommandHandler.parse.parseSlashCommand(command, event.getMember(), event.getTextChannel(), event.getGuild(), getMentionedMembers(command, member.getGuild()), getMentionedRoles(command, member.getGuild()), getMentionedChannels(command, member.getGuild()), null));
                                } else if (event.getReaction().getReactionEmote().getAsReactionCode().equals("❌")) {
                                    try {
                                        message.delete().queue();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    }, (error) -> {
                    });
                }
            });
    }

}
