package de.bnder.taskmanager.commands;

import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.main.Command;
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

public class Prefix implements Command {

    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, java.util.List<Member> mentionedMembers, java.util.List<Role> mentionedRoles, List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException {
        final String langCode = Localizations.getGuildLanguage(guild);
        final String embedTitle = Localizations.getString("prefix_title", langCode);
        if (commandExecutor.isOwner() || commandExecutor.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.length == 1) {
                final String prefix = args[0];
                if (prefix.length() == 1) {
                    final Connection.Response res = Main.tmbAPI("server/prefix/" + guild.getId(), commandExecutor.getId(), Connection.Method.POST).data("prefix", prefix).execute();
                    if (res.statusCode() == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("prefix_changed", langCode, new ArrayList<String>(){{
                            add(prefix);
                        }}), textChannel, Color.green, langCode, slashCommandEvent);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>(){{
                            add("PREFIX-" + res.statusCode());
                        }}), textChannel, Color.red, langCode, slashCommandEvent);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("prefix_only_one_char", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("prefix_no_arg", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }
}
