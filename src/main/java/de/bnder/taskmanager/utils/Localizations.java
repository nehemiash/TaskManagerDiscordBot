package de.bnder.taskmanager.utils;

import com.eclipsesource.json.Json;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Guild;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localizations {

    public static String getString(String path, Locale locale) {
        try {
        final ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        return bundle.getString(path);
        } catch(MissingResourceException e) {
            // Try to return english message if no text is set for the selected language
            if (!locale.getLanguage().equals(Locale.ENGLISH.getLanguage()) && !getString(path, Locale.ENGLISH).equals(path)) {
                return getString(path, Locale.ENGLISH);
            }
        }
        return path;
    }

    public static String getString(String path, Locale locale, ArrayList<String> args) {
        final ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        try {
            // Escape singlequotes
            final String rawText = bundle.getString(path).replaceAll("'", "''");
            final MessageFormat formatter = new MessageFormat(rawText);
            return formatter.format(args.toArray());
        } catch(MissingResourceException e) {
            // Try to return english message if no text is set for the selected language
            if (!locale.getLanguage().equals(Locale.ENGLISH.getLanguage()) && !getString(path, Locale.ENGLISH, args).equals(path)) {
                return getString(path, Locale.ENGLISH, args);
            }
        }
        return path;
    }

    public static Locale getGuildLanguage(Guild guild) {
        try {
            final org.jsoup.Connection.Response res = Main.tmbAPI("server/language/" + guild.getId(), null, org.jsoup.Connection.Method.GET).execute();
            if (res.statusCode() == 200) {
                return new Locale(Json.parse(res.parse().body().text()).asObject().getString("language", "en"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Locale.ENGLISH;
    }
}
