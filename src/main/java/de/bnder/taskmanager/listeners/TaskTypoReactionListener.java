package de.bnder.taskmanager.listeners;

import de.bnder.taskmanager.commands.task.AddTask;
import de.bnder.taskmanager.commands.task.EditTask;
import de.bnder.taskmanager.commands.task.SetDeadline;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskTypoReactionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        final Message message = event.retrieveMessage().complete();
        System.out.println(".");
        if (message.getAuthor().isBot() && message.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            System.out.println("a");
            if (message.getEmbeds().size() == 1) {
                System.out.println("b");
                final MessageEmbed embed = message.getEmbeds().get(0);
                String command = null;
                String author = null;
                for (MessageEmbed.Field field : embed.getFields()) {
                    if (field.getName().equals("Befehl")) {
                        command = field.getValue();
                    } else if (field.getName().equals("Nutzer")) {
                        author = field.getValue();
                    }
                }
                if (command != null && author != null) {
                    System.out.println("c");
                    if (event.getMember().getUser().getAsTag().equals(author)) {
                        System.out.println("d");
                        if (command.substring(1).startsWith("task")) {
                            System.out.println("e");
                            System.out.println(event.getReaction().getReactionEmote().getAsReactionCode());
                            if (event.getReaction().getReactionEmote().getAsReactionCode().equals("✅")) {
                                System.out.println("f");
                                String beheaded = command.substring(1);
                                String[] splitBeheaded = beheaded.split(" ");
                                ArrayList<String> split = new ArrayList<>(Arrays.asList(splitBeheaded));
                                String[] args = new String[split.size() - 1];
                                split.subList(1, split.size()).toArray(args);

                                try {
                                    processTaskCommand(args, event.getMember(), command, event.getChannel());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void processTaskCommand(String[] args, Member member, String commandRaw, TextChannel channel) throws IOException {
        if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("add")) {
                List<Member> mentionedMembers = new ArrayList<>();
                if (commandRaw.contains("<@") && commandRaw.contains(">")) {
                    for (String a : commandRaw.split("<@")) {
                        for (String userID : a.split(">")) {
                            if (userID != null) {
                                userID = userID.replace("!", "");
                                if (userID.length() == 18) {
                                    if (member.getGuild().getMemberById(userID) != null) {
                                        mentionedMembers.add(member.getGuild().getMemberById(userID));
                                        System.out.println(member.getGuild().getMemberById(userID).getUser().getAsTag());
                                    }
                                }
                            }
                        }
                    }
                }
                AddTask.addTask(commandRaw, member, mentionedMembers, channel, args);
            } else if (args[0].equalsIgnoreCase("edit")) {
                EditTask.editTask(commandRaw, member, channel, args);
            } else if (args[0].equalsIgnoreCase("deadline")) {
                SetDeadline.setDeadline(member, channel, args);
            }
        }
    }

}
