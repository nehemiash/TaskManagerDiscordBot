package de.bnder.taskmanager.commands;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.io.IOException;
import java.util.Date;

public class Stats implements Command {

    //TODO: CHANGE TO FIRESTORE

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, java.util.List<Member> mentionedMembers, java.util.List<Role> mentionedRoles, java.util.List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException {
        guild.getJDA().getRestPing().queue(ping -> {
            final String langCode = Localizations.getGuildLanguage(guild);
            final EmbedBuilder builder = new EmbedBuilder();
            try {
                final Date apiRequestDateStart = new Date();
                final org.jsoup.Connection.Response res = Main.tmbAPI("stats", commandExecutor.getId(), org.jsoup.Connection.Method.GET).execute();
                final Date apiRequestDateEnd = new Date();
                final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
                final int serversShard0 = jsonObject.getInt("servers_shard_0", 0);
                final int serversShard1 = jsonObject.getInt("servers_shard_1", 0);
                builder.addField(Localizations.getString("stats_field_server", langCode), String.valueOf(serversShard0 + serversShard1), true);
                builder.addField(Localizations.getString("stats_field_tasks_done", langCode), String.valueOf(jsonObject.getInt("tasks_done", -1)), true);
                builder.addField(Localizations.getString("stats_field_tasks_created", langCode), String.valueOf(jsonObject.getInt("tasks_created", -1)), true);
                builder.addField("Shard", String.valueOf((guild.getJDA().getShardInfo().getShardId())), true);
                builder.addField("Shards", String.valueOf(guild.getJDA().getShardInfo().getShardTotal()), true);
                builder.addField(Localizations.getString("stats_field_server", langCode) + " Shard 0", String.valueOf(serversShard0), true);
                builder.addField(Localizations.getString("stats_field_server", langCode) + " Shard 1", String.valueOf(serversShard1), true);
                builder.addField(Localizations.getString("stats_field_requests_received", langCode), String.valueOf(jsonObject.getInt("received_requests", -1)), true);
                builder.addField(Localizations.getString("stats_field_messages_sent", langCode), String.valueOf(jsonObject.getInt("messages_sent", -1)), true);
                builder.addField(Localizations.getString("stats_field_ping_discord", langCode), ping + "ms", true);
                builder.addField(Localizations.getString("stats_field_ping_bnder", langCode), (apiRequestDateEnd.getTime() - apiRequestDateStart.getTime()) + "ms", true);
            } catch (IOException e) {
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
