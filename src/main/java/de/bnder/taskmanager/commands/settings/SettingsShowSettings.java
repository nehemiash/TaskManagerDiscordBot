package de.bnder.taskmanager.commands.settings;

import de.bnder.taskmanager.commands.Stats;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.UserSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.Calendar;
import java.util.Locale;

public class SettingsShowSettings {

    public static void set(Member member, TextChannel textChannel, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("settings_title", langCode);
        final UserSettings userSettings = new UserSettings(member);
        final EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.cyan).setTimestamp(Calendar.getInstance().toInstant()).setTitle(embedTitle + " - " + member.getUser().getAsTag());
        if (userSettings.getDirectMessage()) {
            embedBuilder.addField(Localizations.getString("settings_list_direct_message", langCode), Localizations.getString("settings_list_enabled", langCode), false);
        } else {
            embedBuilder.addField(Localizations.getString("settings_list_direct_message", langCode), Localizations.getString("settings_list_disabled", langCode), false);
        }
        if (userSettings.getShowDoneTasks()) {
            embedBuilder.addField(Localizations.getString("settings_list_show_done_tasks", langCode), Localizations.getString("settings_list_enabled", langCode), false);
        } else {
            embedBuilder.addField(Localizations.getString("settings_list_show_done_tasks", langCode), Localizations.getString("settings_list_disabled", langCode), false);
        }
        if (userSettings.getNotifyChannelID() != null && member.getGuild().getTextChannelById(userSettings.getNotifyChannelID()) != null) {
            embedBuilder.addField(Localizations.getString("settings_list_notify_channel", langCode), member.getGuild().getTextChannelById(userSettings.getNotifyChannelID()).getAsMention(), false);
        } else {
            embedBuilder.addField(Localizations.getString("settings_list_notify_channel", langCode), "---", false);
        }
        embedBuilder.setDescription(Localizations.getString("settings_invalid_arg", langCode));
        Stats.handleEmbedsOnSlashCommand(textChannel, slashCommandEvent, embedBuilder);
    }

}
