package de.bnder.taskmanager.commands.settings;

import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.UserSettings;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsNotifyChannel {

    public static void set(Member member, TextChannel textChannel, String[] args, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("settings_title", langCode);
        final String arg0 = args[0].replaceAll("-", "").replaceAll("_", "");
        if (arg0.equalsIgnoreCase("notifychannel")) {
            if (mentionedChannels != null && mentionedChannels.size() == 1) {
                final TextChannel channel = mentionedChannels.get(0);
                final UserSettings userSettings = new UserSettings(member);
                userSettings.setNotifyChannelID(channel.getId());
                if (!userSettings.getDirectMessage()) {
                    MessageSender.send(embedTitle, Localizations.getString("notify_channel_set_but_dms_are_enabled", langCode, new ArrayList<String>() {{
                        add(channel.getAsMention());
                    }}), textChannel, Color.green, langCode, slashCommandEvent);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("notify_channel_set", langCode, new ArrayList<String>() {{
                        add(channel.getAsMention());
                    }}), textChannel, Color.green, langCode, slashCommandEvent);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("notify_mention_one_channel", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("settings_invalid_arg", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
