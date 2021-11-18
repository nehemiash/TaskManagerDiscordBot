package de.bnder.taskmanager.commands;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.slashcommands.UpdateGuildSlashCommands;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jsoup.Connection;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Language implements Command {

    public static ArrayList<Locale> validLangCodes = new ArrayList<Locale>() {{
        add(Locale.GERMAN);
        add(Locale.ENGLISH);
        add(new Locale("bg"));
        add(Locale.FRENCH);
        add(new Locale("ru"));
        add(new Locale("pl"));
        add(new Locale("tr"));
        add(new Locale("pt"));
    }};

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, java.util.List<Member> mentionedMembers, java.util.List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException {
        if (args.length == 0) {
            final Locale langCode = Localizations.getGuildLanguage(guild);
            final String embedTitle = Localizations.getString("language_message_title", langCode);
            MessageSender.send(embedTitle, Localizations.getString("language_message_invalid_params", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        } else if (args.length == 1) {
            final Locale language = Locale.forLanguageTag(args[0].toLowerCase());
            if (validLangCodes.contains(language)) {
                if (commandExecutor.hasPermission(Permission.ADMINISTRATOR)) {
                    final Connection.Response res = Main.tmbAPI("server/language/" + guild.getId(), commandExecutor.getId(), Connection.Method.POST).data("language", language.getLanguage()).execute();
                    final String embedTitle = Localizations.getString("language_message_title", language);
                    if (res.statusCode() == 200) {
                        UpdateGuildSlashCommands.update(guild);
                        MessageSender.send(embedTitle, Localizations.getString("language_changed", language), textChannel, Color.green, language, slashCommandEvent);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("request_unknown_error", language), textChannel, Color.red, language, slashCommandEvent);
                    }
                } else {
                    final Locale langCode = Localizations.getGuildLanguage(guild);
                    final String embedTitle = Localizations.getString("language_message_title", langCode);
                    MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } else {
                final Locale langCode = Localizations.getGuildLanguage(guild);
                final String embedTitle = Localizations.getString("language_message_title", langCode);
                MessageSender.send(embedTitle, Localizations.getString("language_message_invalid_params", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        }
    }
}