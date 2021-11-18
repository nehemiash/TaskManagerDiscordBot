package de.bnder.taskmanager.commands;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

public class Token implements Command {
    @Override
    public void action(String[] args, String messageContentRaw, Member commandExecutor, TextChannel textChannel, Guild guild, java.util.List<Member> mentionedMembers, java.util.List<Role> mentionedRoles, java.util.List<TextChannel> mentionedChannels, SlashCommandEvent slashCommandEvent) throws IOException {
        final Locale langCode = Localizations.getGuildLanguage(guild);
        final String embedTitle = Localizations.getString("token_message_title", langCode);
        commandExecutor.getUser().openPrivateChannel().queue(channel -> {
            try {
                if (args.length == 1 && args[0].equalsIgnoreCase("new")) {
                    final String token = getNewToken(commandExecutor.getUser());
                    MessageSender.send(embedTitle, Localizations.getString("token_received", langCode), textChannel, Color.green, langCode, slashCommandEvent);
                    channel.sendMessage(token).queue();
                    channel.sendMessageEmbeds(new EmbedBuilder().setImage("http://api.qrserver.com/v1/create-qr-code/?color=000000&bgcolor=FFFFFF&data=" + Base64.getEncoder().encodeToString(("tmb_" + token).getBytes()) + "&qzone=1&margin=0&size=400x400&ecc=L").build()).queue();
                    channel.sendMessage(Localizations.getString("token_info", langCode)).queue();
                } else {
                    final String token = getToken(commandExecutor.getUser());
                    MessageSender.send(embedTitle, Localizations.getString("token_received", langCode), textChannel, Color.green, langCode, slashCommandEvent);
                    channel.sendMessage(token).queue();
                    channel.sendMessageEmbeds(new EmbedBuilder().setImage("http://api.qrserver.com/v1/create-qr-code/?color=000000&bgcolor=FFFFFF&data=" + Base64.getEncoder().encodeToString(("tmb_" + token).getBytes()) + "&qzone=1&margin=0&size=400x400&ecc=L").build()).queue();
                    channel.sendMessage(Localizations.getString("token_info", langCode)).queue();
                }
            } catch (Exception e) {
                MessageSender.send(embedTitle, Localizations.getString("token_could_not_be_sent", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        }, (error) -> {});
    }

    private String getNewToken(User user) {
        try {
            final org.jsoup.Connection.Response res = Main.tmbAPI("user/token/create", user.getId(), org.jsoup.Connection.Method.GET).execute();
            final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
            if (res.statusCode() == 200) {
                return jsonObject.getString("token", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getToken(User user) {
        try {
            final org.jsoup.Connection.Response res = Main.tmbAPI("user/token", user.getId(), org.jsoup.Connection.Method.GET).execute();
            final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
            if (res.statusCode() == 200) {
                return jsonObject.getString("token", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
