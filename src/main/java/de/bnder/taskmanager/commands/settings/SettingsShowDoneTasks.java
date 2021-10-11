package de.bnder.taskmanager.commands.settings;

import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.UserSettings;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.io.IOException;

public class SettingsShowDoneTasks {

    public static void set(Member member, TextChannel textChannel, SlashCommandEvent slashCommandEvent) throws IOException {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("settings_title", langCode);

        final UserSettings userSettings = new UserSettings(member);
        userSettings.changeShowDoneTasks();
        if (!userSettings.getShowDoneTasks()) {
            MessageSender.send(embedTitle, Localizations.getString("settings_show_done_tasks_enabled", langCode), textChannel, Color.green, langCode, slashCommandEvent);
        } else {
            MessageSender.send(embedTitle, Localizations.getString("settings_show_done_tasks_disabled", langCode), textChannel, Color.green, langCode, slashCommandEvent);
        }
    }

}
