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

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Language implements Command {

    public static ArrayList<String> validLangCodes = new ArrayList<String>() {{
        add("de");
        add("en");
        add("bg");
        add("fr");
        add("ru");
        add("pl");
        add("tr");
    }};

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, java.util.List<Member> mentionedMembers, java.util.List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException {
        if (args.length == 0) {
            final String langCode = Localizations.getGuildLanguage(guild);
            final String embedTitle = Localizations.getString("language_message_title", langCode);
            MessageSender.send(embedTitle, Localizations.getString("language_message_invalid_params", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        } else if (args.length == 1) {
            final String language = args[0].toLowerCase();
            if (validLangCodes.contains(language)) {
                if (commandExecutor.hasPermission(Permission.ADMINISTRATOR)) {
                    Main.firestore.collection("server").document(guild.getId()).update(new HashMap<>() {{
                        put("language", language);
                    }});
                    final String embedTitle = Localizations.getString("language_message_title", language);
                    UpdateGuildSlashCommands.update(guild);
                    MessageSender.send(embedTitle, Localizations.getString("sprache_geaendert", language), textChannel, Color.green, language, slashCommandEvent);
                } else {
                    final String langCode = Localizations.getGuildLanguage(guild);
                    final String embedTitle = Localizations.getString("language_message_title", langCode);
                    MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } else {
                final String langCode = Localizations.getGuildLanguage(guild);
                final String embedTitle = Localizations.getString("language_message_title", langCode);
                MessageSender.send(embedTitle, Localizations.getString("language_message_invalid_params", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        }
    }
}