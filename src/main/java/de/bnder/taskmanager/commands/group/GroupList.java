package de.bnder.taskmanager.commands.group;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class GroupList {

    public static void getGroupList(Member member, TextChannel textChannel, SlashCommandEvent slashCommandEvent) throws IOException {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);
        final org.jsoup.Connection.Response res = Main.tmbAPI("group/list/" + member.getGuild().getId(), member.getId(), org.jsoup.Connection.Method.GET).execute();
        final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
        final int statusCode = res.statusCode();
        if (statusCode == 200) {
            final JsonArray servers = jsonObject.get("groups").asArray();
            if (servers.size() > 0) {
                final StringBuilder builder = new StringBuilder();
                for (int i = 0; i < servers.size(); i++) {
                    final String serverName = servers.get(i).asString();
                    builder.append(serverName).append("\n");
                }
                MessageSender.send(embedTitle, builder.toString(), textChannel, Color.green, langCode, slashCommandEvent);
            } else {
                MessageSender.send(embedTitle, Localizations.getString("keine_gruppen_auf_server", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else if (statusCode == 404) {
            MessageSender.send(embedTitle, Localizations.getString("keine_gruppen_auf_server", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        } else {
            MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                {
                    add(String.valueOf(statusCode));
                }
            }), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
