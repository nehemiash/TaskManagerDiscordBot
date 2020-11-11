package de.bnder.taskmanager.commands;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.Localizations;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.IOException;

public class Stats implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        final int server = event.getJDA().getGuilds().size();
        final long ping = event.getJDA().getGatewayPing();
        final String langCode = Localizations.getGuildLanguage(event.getGuild());
        final EmbedBuilder builder = new EmbedBuilder();
        builder.addField(Localizations.getString("stats_field_server", langCode), String.valueOf(server), true);
        builder.addField(Localizations.getString("stats_field_ping", langCode), String.valueOf(ping), true);
        try {
            final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/stats").method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.getMember().getId()).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
            final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
            builder.addField(Localizations.getString("stats_field_tasks_done", langCode), String.valueOf(jsonObject.getInt("tasks_done", -1)), true);
            builder.addField(Localizations.getString("stats_field_tasks_created", langCode), String.valueOf(jsonObject.getInt("tasks_created", -1)), true);
            builder.addField("Shard", String.valueOf((event.getJDA().getShardInfo().getShardId())), true);
            builder.addField("Shards", String.valueOf(event.getJDA().getShardInfo().getShardTotal()), true);
            builder.addField(Localizations.getString("stats_field_requests_received", langCode), String.valueOf(jsonObject.getInt("received_requests", -1)), true);
            builder.addField(Localizations.getString("stats_field_messages_sent", langCode), String.valueOf(jsonObject.getInt("messages_sent", -1)), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        builder.setColor(Color.green);
        builder.setTitle(Localizations.getString("stats_message_title", langCode));
        event.getChannel().sendMessage(builder.build()).queue();
    }
}
