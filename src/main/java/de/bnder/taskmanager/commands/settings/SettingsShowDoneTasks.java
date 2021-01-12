package de.bnder.taskmanager.commands.settings;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsShowDoneTasks {

    public static void set(Member member, TextChannel textChannel) throws IOException {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("settings_title", langCode);
        final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/user/settings/" + member.getGuild().getId()).method(org.jsoup.Connection.Method.POST).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member.getId()).timeout(Connection.timeout).userAgent(Main.userAgent).data("type", "show_done_tasks").postDataCharset("UTF-8").ignoreContentType(true).ignoreHttpErrors(true).execute();
        final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
        if (res.statusCode() == 200) {
            final boolean newValue = jsonObject.getBoolean("newValue", false);
            if (newValue) {
                MessageSender.send(embedTitle, Localizations.getString("settings_show_done_tasks_enabled", langCode), textChannel, Color.green);
            } else {
                MessageSender.send(embedTitle, Localizations.getString("settings_show_done_tasks_disabled", langCode), textChannel, Color.green);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {{
                add("SETTINGS-2.1-" + res.statusCode());
            }}), textChannel, Color.red);
        }
    }

}