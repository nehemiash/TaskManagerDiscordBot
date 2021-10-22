package de.bnder.taskmanager.utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Guild;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class Localizations {

    private static JsonObject toParse = null;

    public static String getString(String path, Locale locale) {
        final ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        return bundle.getString(path);
    }

    public static String getString(String path, Locale locale, ArrayList<String> args) {
        final ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        final MessageFormat formatter = new MessageFormat(bundle.getString(path));
        return formatter.format(args.toArray());
    }

    public static Locale getGuildLanguage(Guild guild) {
        try {
            final org.jsoup.Connection.Response res = Main.tmbAPI("server/language/" + guild.getId(), null, org.jsoup.Connection.Method.GET).execute();
            if (res.statusCode() == 200) {
                return Locale.forLanguageTag(Json.parse(res.parse().body().text()).asObject().getString("language", "en"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Locale.ENGLISH;
    }
}
