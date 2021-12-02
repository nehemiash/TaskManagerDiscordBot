package de.bnder.taskmanager.commands.settings;

import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.UserSettings;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsSetNotifications {

    public static void set(Member member, TextChannel textChannel, List<TextChannel> mentionedChannels, List<Member> mentionedMembers, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("settings_title", langCode);
        if (PermissionSystem.hasPermission(member, GroupPermission.DEFINE_NOTIFY_CHANNEL)) {
            if (mentionedMembers != null && mentionedMembers.size() > 0) {
                if (mentionedChannels != null && mentionedChannels.size() > 0) {
                    final User user = mentionedMembers.get(0).getUser();
                    final UserSettings userSettings = new UserSettings(mentionedMembers.get(0));
                    userSettings.setNotifyChannelID(mentionedChannels.get(0).getId());
                    if (userSettings.getNotifyChannelID() != null) {
                        MessageSender.send(embedTitle, Localizations.getString("user_connect_channel_connected", langCode, new ArrayList<String>() {{
                            add(user.getAsTag());
                            add(mentionedChannels.get(0).getAsMention());
                        }}), textChannel, Color.green, langCode, slashCommandEvent);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("request_unknown_error", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("notify_mention_one_channel", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("user_needs_to_be_mentioned", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
