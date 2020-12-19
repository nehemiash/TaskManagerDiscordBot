package de.bnder.taskmanager.commands;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Connection;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class Group implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        final String langCode = Localizations.getGuildLanguage(event.getGuild());
        final String embedTitle = Localizations.getString("group_title", langCode);
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (PermissionSystem.hasPermission(event.getMember(), GroupPermission.CREATE_GROUP)) {
                    final String groupName = Connection.encodeString(args[1]);
                    final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/group/create/" + event.getGuild().getId()).method(org.jsoup.Connection.Method.POST).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.getMember().getId()).data("group_name", groupName).postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                    final int statusCode = res.statusCode();
                    if (statusCode == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("group_created_successfully", langCode, new ArrayList<String>() {
                            {
                                add(groupName);
                            }
                        }), event.getMessage(), Color.green);
                    } else if (statusCode == 400) {
                        MessageSender.send(embedTitle, Localizations.getString("group_not_created_name_already_exists", langCode, new ArrayList<String>() {
                            {
                                add(groupName);
                            }
                        }), event.getMessage(), Color.red);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("group_not_created_unknown_error", langCode, new ArrayList<String>() {
                            {
                                add(groupName);
                                add(String.valueOf(statusCode));
                            }
                        }), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_habe_admin_permissions", langCode), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (PermissionSystem.hasPermission(event.getMember(), GroupPermission.DELETE_GROUP)) {
                    final String groupName = Connection.encodeString(args[1]);
                    final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/group/" + event.getGuild().getId() + "/" + groupName).method(org.jsoup.Connection.Method.DELETE).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.getMember().getId()).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                    final int statusCode = res.statusCode();
                    if (statusCode == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("group_was_deleted", langCode, new ArrayList<String>() {
                            {
                                add(groupName);
                            }
                        }), event.getMessage(), Color.green);
                    } else if (statusCode == 404) {
                        MessageSender.send(embedTitle, Localizations.getString("group_with_name_doesnt_exist", langCode, new ArrayList<String>() {
                            {
                                add(groupName);
                            }
                        }), event.getMessage(), Color.red);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("group_delete_unknown_error", langCode, new ArrayList<String>() {
                            {
                                add(groupName);
                            }
                        }), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_habe_admin_permissions", langCode), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("members")) {
                if (PermissionSystem.hasPermission(event.getMember(), GroupPermission.SHOW_MEMBERS)) {
                    final String groupName = Connection.encodeString(args[1]);
                    final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/group/members/" + event.getGuild().getId() + "/" + groupName).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.getMember().getId()).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                    final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
                    final int statusCode = res.statusCode();
                    if (statusCode == 200) {
                        final StringBuilder builder = new StringBuilder();
                        for (JsonValue value : jsonObject.get("members").asArray()) {
                            final String id = value.asObject().getString("user_id", null);
                            if (id != null) {
                                final Member member = event.getGuild().retrieveMemberById(id).complete();
                                if (member != null) {
                                    builder.append("- ").append(member.getUser().getAsTag()).append("\n");
                                }
                            }
                        }
                        MessageSender.send(embedTitle, Localizations.getString("group_members", langCode, new ArrayList<String>() {
                            {
                                add(builder.substring(0, builder.length() - 1));
                            }
                        }), event.getMessage(), Color.green);
                    } else if (statusCode == 404) {
                        MessageSender.send(embedTitle, Localizations.getString("group_with_name_doesnt_exist", langCode, new ArrayList<String>() {
                            {
                                add(groupName);
                            }
                        }), event.getMessage(), Color.red);
                    } else if (statusCode == 400) {
                        MessageSender.send(embedTitle, Localizations.getString("group_no_members", langCode), event.getMessage(), Color.red);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                            {
                                add(String.valueOf(statusCode));
                            }
                        }), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_habe_admin_permissions", langCode), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("add")) {
                if (PermissionSystem.hasPermission(event.getMember(), GroupPermission.ADD_MEMBERS)) {
                    if (args.length >= 3) {
                        if (event.getMessage().getMentionedMembers().size() > 0) {
                            final String groupName = Connection.encodeString(args[1 + event.getMessage().getMentionedMembers().size()]);
                            for (Member member : event.getMessage().getMentionedMembers()) {
                                final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/group/add-member/" + event.getGuild().getId()).method(org.jsoup.Connection.Method.PUT).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member.getId()).data("group_name", groupName).postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                                final int statusCode = res.statusCode();
                                if (statusCode == 200) {
                                    MessageSender.send(embedTitle, Localizations.getString("user_added_to_group", langCode, new ArrayList<String>() {
                                        {
                                            add(member.getUser().getName());
                                            add(groupName);
                                        }
                                    }), event.getMessage(), Color.green);
                                } else if (statusCode == 404) {
                                    MessageSender.send(embedTitle, Localizations.getString("group_with_name_doesnt_exist", langCode, new ArrayList<String>() {
                                        {
                                            add(groupName);
                                        }
                                    }), event.getMessage(), Color.red);
                                    return;
                                } else if (statusCode == 400) {
                                    MessageSender.send(embedTitle, Localizations.getString("user_already_in_group", langCode), event.getMessage(), Color.red);
                                } else {
                                    MessageSender.send(embedTitle, Localizations.getString("user_added_to_group_unknown_error", langCode, new ArrayList<String>() {
                                        {
                                            add(String.valueOf(statusCode));
                                        }
                                    }), event.getMessage(), Color.red);
                                    return;
                                }
                            }
                        } else {
                            MessageSender.send(embedTitle, Localizations.getString("user_needs_to_be_mentioned", langCode), event.getMessage(), Color.red);
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("group_name_needs_to_be_given", langCode), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_habe_admin_permissions", langCode), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rem")) {
                if (PermissionSystem.hasPermission(event.getMember(), GroupPermission.REMOVE_MEMBERS)) {
                    if (args.length >= 3) {
                        if (event.getMessage().getMentionedMembers().size() > 0) {
                            final String groupName = Connection.encodeString(args[1 + event.getMessage().getMentionedMembers().size()]);
                            for (Member member : event.getMessage().getMentionedMembers()) {
                                final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/group/remove-member/" + event.getGuild().getId()).method(org.jsoup.Connection.Method.PUT).header("authorization", "TMB " + Main.authorizationToken).header("user_id", member.getId()).data("group_name", groupName).postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                                final int statusCode = res.statusCode();
                                if (statusCode == 200) {
                                    MessageSender.send(embedTitle, Localizations.getString("nutzer_aus_gruppe_entfernt", langCode, new ArrayList<String>() {
                                        {
                                            add(member.getUser().getName());
                                            add(groupName);
                                        }
                                    }), event.getMessage(), Color.green);
                                } else if (statusCode == 404) {
                                    MessageSender.send(embedTitle, Localizations.getString("group_with_name_doesnt_exist", langCode, new ArrayList<String>() {
                                        {
                                            add(groupName);
                                        }
                                    }), event.getMessage(), Color.red);
                                    return;
                                } else if (statusCode == 400) {
                                    MessageSender.send(embedTitle, Localizations.getString("nutzer_ist_in_keiner_gruppe", langCode, new ArrayList<String>() {
                                        {
                                            add(member.getUser().getAsTag());
                                        }
                                    }), event.getMessage(), Color.red);
                                } else {
                                    MessageSender.send(embedTitle, Localizations.getString("nutzer_aus_gruppe_entfernen_unbekannter_fehler", langCode, new ArrayList<String>() {
                                        {
                                            add(String.valueOf(statusCode));
                                        }
                                    }), event.getMessage(), Color.red);
                                }
                            }
                        } else {
                            MessageSender.send(embedTitle, Localizations.getString("user_needs_to_be_mentioned", langCode), event.getMessage(), Color.red);
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("group_name_needs_to_be_given", langCode), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_habe_admin_permissions", langCode), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("notifications") || args[0].equalsIgnoreCase("notification")) {
                if (PermissionSystem.hasPermission(event.getMember(), GroupPermission.DEFINE_NOTIFY_CHANNEL)) {
                    if (event.getMessage().getMentionedChannels().size() > 0) {
                        String groupName = args[1];
                        if (serverHasGroup(groupName, event.getGuild())) {
                            final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/group/notify-channel/" + event.getGuild().getId()).method(org.jsoup.Connection.Method.PUT).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.getMember().getId()).data("group_name", groupName).data("notify_channel", event.getMessage().getMentionedChannels().get(0).getId()).postDataCharset("UTF-8").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                            if (res.statusCode() == 200) {
                                MessageSender.send(embedTitle, Localizations.getString("group_connect_channel_connected", langCode, new ArrayList<String>() {{
                                    add(groupName);
                                    add(event.getMessage().getMentionedChannels().get(0).getAsMention());
                                }}), event.getMessage(), Color.green);
                            } else {
                                MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                                    {
                                        add(String.valueOf(res.statusCode()));
                                    }
                                }), event.getMessage(), Color.red);
                            }
                        } else {
                            MessageSender.send(embedTitle, Localizations.getString("group_with_name_doesnt_exist", langCode, new ArrayList<String>() {
                                {
                                    add(groupName);
                                }
                            }), event.getMessage(), Color.red);
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("notify_mention_one_channel", langCode), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_habe_admin_permissions", langCode), event.getMessage(), Color.red);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_group_commands", langCode), event.getMessage(), Color.red);
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/group/list/" + event.getGuild().getId()).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", event.getMember().getId()).timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
                final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
                final int statusCode = res.statusCode();
                if (statusCode == 200) {
                    final JsonArray servers = jsonObject.get("groups").asArray();
                    if (servers.size() > 0) {
                        final StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < servers.size(); i++) {
                            final String serverName = servers.get(i).asString();
                            builder.append(serverName).append("\n");
                        }
                        MessageSender.send(embedTitle, builder.toString(), event.getMessage(), Color.green);
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("keine_gruppen_auf_server", langCode), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("abfrage_unbekannter_fehler", langCode, new ArrayList<String>() {
                        {
                            add(String.valueOf(statusCode));
                        }
                    }), event.getMessage(), Color.red);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_group_commands", langCode), event.getMessage(), Color.red);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("help_message_group_commands", langCode), event.getMessage(), Color.red);
        }

    }

    public static boolean serverHasGroup(String group, Guild guild) {
        try {
            final org.jsoup.Connection.Response res = Jsoup.connect(Main.requestURL + "/group/list/" + guild.getId()).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute();
            final JsonObject jsonObject = Json.parse(res.parse().body().text()).asObject();
            final int statusCode = res.statusCode();
            if (statusCode == 200) {
                final JsonArray groups = jsonObject.get("groups").asArray();
                if (groups.size() > 0) {
                    for (int i = 0; i < groups.size(); i++) {
                        final String groupName = groups.get(i).asString();
                        if (group.equalsIgnoreCase(groupName)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
