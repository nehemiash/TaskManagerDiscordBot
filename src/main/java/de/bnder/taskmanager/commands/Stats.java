package de.bnder.taskmanager.commands;

import com.google.cloud.firestore.DocumentSnapshot;
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.Locale;

public class Stats implements Command {

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, java.util.List<Member> mentionedMembers, java.util.List<Role> mentionedRoles, java.util.List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) {
        guild.getJDA().getRestPing().queue(ping -> {
            final Locale langCode = Localizations.getGuildLanguage(guild);
            final EmbedBuilder builder = new EmbedBuilder();
            try {
                final Date apiRequestDateStart = new Date();
                DocumentSnapshot getStats = Main.firestore.collection("stats").document("alltime").get().get();
                final Date apiRequestDateEnd = new Date();

                long serversShard0 = 0;
                if (getStats.exists() && getStats.getData().containsKey("servers_shard_0")) {
                    serversShard0 = (long) getStats.get("servers_shard_0");
                }
                long serversShard1 = 0;
                if (getStats.exists() && getStats.getData().containsKey("servers_shard_1")) {
                    serversShard1 = (long) getStats.get("servers_shard_1");
                }
                long tasksDone = 0;
                if (getStats.exists() && getStats.getData().containsKey("tasks_done")) {
                    tasksDone = (long) getStats.get("tasks_done");
                }
                long tasksCreated = 0;
                if (getStats.exists() && getStats.getData().containsKey("tasks_created")) {
                    tasksCreated = (long) getStats.get("tasks_created");
                }
                long messagesSent = 0;
                if (getStats.exists() && getStats.getData().containsKey("messages_sent")) {
                    messagesSent = (long) getStats.get("messages_sent");
                }

                builder.addField(Localizations.getString("stats_field_server", langCode), String.valueOf(serversShard0 + serversShard1), true);
                builder.addField(Localizations.getString("stats_field_tasks_done", langCode), String.valueOf(tasksDone), true);
                builder.addField(Localizations.getString("stats_field_tasks_created", langCode), String.valueOf(tasksCreated), true);
                builder.addField("Shard", String.valueOf((guild.getJDA().getShardInfo().getShardId())), true);
                builder.addField("Shards", String.valueOf(guild.getJDA().getShardInfo().getShardTotal()), true);
                builder.addField(Localizations.getString("stats_field_server", langCode) + " Shard 0", String.valueOf(serversShard0), true);
                builder.addField(Localizations.getString("stats_field_server", langCode) + " Shard 1", String.valueOf(serversShard1), true);
                builder.addField(Localizations.getString("stats_field_messages_sent", langCode), String.valueOf(messagesSent), true);
                builder.addField(Localizations.getString("stats_field_ping_discord", langCode), ping + "ms", true);
                builder.addField(Localizations.getString("stats_field_ping_bnder", langCode), (apiRequestDateEnd.getTime() - apiRequestDateStart.getTime()) + "ms", true);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            builder.setColor(Color.green);
            builder.setTitle(Localizations.getString("stats_message_title", langCode));
            handleEmbedsOnSlashCommand(textChannel, slashCommandEvent, builder);
        });
    }

    public static void handleEmbedsOnSlashCommand(TextChannel textChannel, SlashCommandEvent slashCommandEvent, EmbedBuilder builder) {
        if (slashCommandEvent == null) {
            textChannel.sendMessageEmbeds(builder.build()).queue();
        } else {
            StringBuilder reply = new StringBuilder();
            for (MessageEmbed.Field field : builder.getFields()) {
                reply.append(field.getName()).append(": ").append(field.getValue()).append("\n");
            }
            slashCommandEvent.reply(reply.toString()).queue();
        }
    }
}
