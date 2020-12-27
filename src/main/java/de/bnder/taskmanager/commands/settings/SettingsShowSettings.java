package de.bnder.taskmanager.commands.settings;

import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.utils.Localizations;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.Calendar;

public class SettingsShowSettings {

    public static void set(Member member, TextChannel textChannel) {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("settings_title", langCode);
        final JsonObject userSettings = de.bnder.taskmanager.utils.Settings.getUserSettings(member);
        final EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.cyan).setTimestamp(Calendar.getInstance().toInstant()).setTitle(embedTitle + " - " + member.getUser().getAsTag());
        if (userSettings.getString("direct_message", "1").equals("1")) {
            embedBuilder.addField(Localizations.getString("settings_list_direct_message", langCode), Localizations.getString("settings_list_enabled", langCode), false);
        } else {
            embedBuilder.addField(Localizations.getString("settings_list_direct_message", langCode), Localizations.getString("settings_list_disabled", langCode), false);
        }
        if (userSettings.getString("show_done_tasks", "1").equals("1")) {
            embedBuilder.addField(Localizations.getString("settings_list_show_done_tasks", langCode), Localizations.getString("settings_list_enabled", langCode), false);
        } else {
            embedBuilder.addField(Localizations.getString("settings_list_show_done_tasks", langCode), Localizations.getString("settings_list_disabled", langCode), false);
        }
        if (userSettings.get("notify_channel") != null && !userSettings.get("notify_channel").isNull() && member.getGuild().getTextChannelById(userSettings.getString("notify_channel", "123")) != null) {
            embedBuilder.addField(Localizations.getString("settings_list_notify_channel", langCode), member.getGuild().getTextChannelById(userSettings.getString("notify_channel", "123")).getAsMention(), false);
        } else {
            embedBuilder.addField(Localizations.getString("settings_list_notify_channel", langCode), "---", false);
        }
        embedBuilder.setDescription(Localizations.getString("settings_invalid_arg", langCode));
        textChannel.sendMessage(embedBuilder.build()).queue();
    }

}
