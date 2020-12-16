package de.bnder.taskmanager.commands;

import de.bnder.taskmanager.main.Command;
import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import de.bnder.taskmanager.utils.permissions.PermissionPermission;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class Permission implements Command {
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) throws IOException {
        final String langCode = Localizations.getGuildLanguage(event.getGuild());
        final String embedTitle = Localizations.getString("permissions_title", langCode);
        final String prefix = String.valueOf(event.getMessage().getContentRaw().charAt(0));
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")) {
                if (PermissionSystem.hasPermission(event.getMember(), PermissionPermission.ADD_PERMISSION)) {
                    int statusCode;
                    if (event.getMessage().getMentionedMembers().size() > 0) {
                        if (taskPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.addPermissionStatusCode(event.getMessage().getMentionedMembers().get(0), TaskPermission.valueOf(args[2].toUpperCase()));
                        } else if (groupPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.addPermissionStatusCode(event.getMessage().getMentionedMembers().get(0), GroupPermission.valueOf(args[2].toUpperCase()));
                        } else if (permissionPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.addPermissionStatusCode(event.getMessage().getMentionedMembers().get(0), PermissionPermission.valueOf(args[2].toUpperCase()));
                        } else {
                            MessageSender.send(embedTitle, Localizations.getString("unknown_permission_name", langCode), event.getMessage(), Color.red);
                            return;
                        }
                    } else if (event.getMessage().getMentionedRoles().size() > 0) {
                        if (taskPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.addPermissionStatusCode(event.getMessage().getMentionedRoles().get(0), TaskPermission.valueOf(args[2].toUpperCase()));
                        } else if (groupPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.addPermissionStatusCode(event.getMessage().getMentionedRoles().get(0), GroupPermission.valueOf(args[2].toUpperCase()));
                        } else if (permissionPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.addPermissionStatusCode(event.getMessage().getMentionedRoles().get(0), PermissionPermission.valueOf(args[2].toUpperCase()));
                        } else {
                            MessageSender.send(embedTitle, Localizations.getString("unknown_permission_name", langCode), event.getMessage(), Color.red);
                            return;
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("need_to_mention_user_or_role", langCode), event.getMessage(), Color.red);
                        return;
                    }
                    if (statusCode == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("permission_added", langCode), event.getMessage(), Color.green);
                    } else if (statusCode == 903) {
                        MessageSender.send(embedTitle, Localizations.getString("already_has_permission", langCode), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_habe_admin_permissions", langCode), event.getMessage(), Color.red);
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (PermissionSystem.hasPermission(event.getMember(), PermissionPermission.REMOVE_PERMISSION)) {
                    int statusCode;
                    if (event.getMessage().getMentionedMembers().size() > 0) {
                        if (taskPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.removePermissionStatusCode(event.getMessage().getMentionedMembers().get(0), TaskPermission.valueOf(args[2].toUpperCase()));
                        } else if (groupPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.removePermissionStatusCode(event.getMessage().getMentionedMembers().get(0), GroupPermission.valueOf(args[2].toUpperCase()));
                        } else if (permissionPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.removePermissionStatusCode(event.getMessage().getMentionedMembers().get(0), PermissionPermission.valueOf(args[2].toUpperCase()));
                        } else {
                            MessageSender.send(embedTitle, Localizations.getString("unknown_permission_name", langCode), event.getMessage(), Color.red);
                            return;
                        }
                    } else if (event.getMessage().getMentionedRoles().size() > 0) {
                        if (taskPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.removePermissionStatusCode(event.getMessage().getMentionedRoles().get(0), TaskPermission.valueOf(args[2].toUpperCase()));
                        } else if (groupPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.removePermissionStatusCode(event.getMessage().getMentionedRoles().get(0), GroupPermission.valueOf(args[2].toUpperCase()));
                        } else if (permissionPermissionContains(args[2].toUpperCase())) {
                            statusCode = PermissionSystem.removePermissionStatusCode(event.getMessage().getMentionedRoles().get(0), PermissionPermission.valueOf(args[2].toUpperCase()));
                        } else {
                            MessageSender.send(embedTitle, Localizations.getString("unknown_permission_name", langCode), event.getMessage(), Color.red);
                            return;
                        }
                    } else {
                        MessageSender.send(embedTitle, Localizations.getString("need_to_mention_user_or_role", langCode), event.getMessage(), Color.red);
                        return;
                    }
                    if (statusCode == 200) {
                        MessageSender.send(embedTitle, Localizations.getString("permission_removed", langCode), event.getMessage(), Color.green);
                    } else if (statusCode == 903) {
                        MessageSender.send(embedTitle, Localizations.getString("dont_has_permission", langCode), event.getMessage(), Color.red);
                    }
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_habe_admin_permissions", langCode), event.getMessage(), Color.red);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_permission_commands", langCode, new ArrayList<String>(){{
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                }}), event.getMessage(), Color.red);
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                if (event.getMessage().getMentionedMembers().size() > 0) {
                    final Member member = event.getMessage().getMentionedMembers().get(0);
                    final StringBuilder stringBuilder = new StringBuilder();
                    for (TaskPermission permission : TaskPermission.values()) {
                        stringBuilder.append(permission.name()).append(": ").append(hasPermString(PermissionSystem.hasPermission(member, permission))).append("\n");
                    }
                    for (GroupPermission permission : GroupPermission.values()) {
                        stringBuilder.append(permission.name()).append(": ").append(hasPermString(PermissionSystem.hasPermission(member, permission))).append("\n");
                    }
                    for (PermissionPermission permission : PermissionPermission.values()) {
                        stringBuilder.append(permission.name()).append(": ").append(hasPermString(PermissionSystem.hasPermission(member, permission))).append("\n");
                    }
                    MessageSender.send(embedTitle + " - " + member.getUser().getAsTag(), stringBuilder.toString(), event.getMessage(), Color.green);
                } else if (event.getMessage().getMentionedRoles().size() > 0) {
                    final Role role = event.getMessage().getMentionedRoles().get(0);
                    final StringBuilder stringBuilder = new StringBuilder();
                    for (TaskPermission permission : TaskPermission.values()) {
                        stringBuilder.append(permission.name()).append(": ").append(hasPermString(PermissionSystem.hasPermission(role, permission))).append("\n");
                    }
                    for (GroupPermission permission : GroupPermission.values()) {
                        stringBuilder.append(permission.name()).append(": ").append(hasPermString(PermissionSystem.hasPermission(role, permission))).append("\n");
                    }
                    for (PermissionPermission permission : PermissionPermission.values()) {
                        stringBuilder.append(permission.name()).append(": ").append(hasPermString(PermissionSystem.hasPermission(role, permission))).append("\n");
                    }
                    MessageSender.send(embedTitle + " - " + role.getName(), stringBuilder.toString(), event.getMessage(), Color.green);
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("need_to_mention_user_or_role", langCode), event.getMessage(), Color.red);
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("help_message_permission_commands", langCode, new ArrayList<String>(){{
                    add(prefix);
                    add(prefix);
                    add(prefix);
                    add(prefix);
                }}), event.getMessage(), Color.red);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("help_message_permission_commands", langCode, new ArrayList<String>(){{
                add(prefix);
                add(prefix);
                add(prefix);
                add(prefix);
            }}), event.getMessage(), Color.red);
        }
    }

    private String hasPermString(boolean has) {
        return has ? "✅" : "❌";
    }

    static boolean taskPermissionContains(String test) {
        for (TaskPermission taskPermission : TaskPermission.values()) {
            if (taskPermission.name().equals(test)) {
                return true;
            }
        }
        return false;
    }

    static boolean groupPermissionContains(String test) {
        for (GroupPermission groupPermission : GroupPermission.values()) {
            if (groupPermission.name().equals(test)) {
                return true;
            }
        }
        return false;
    }

    static boolean permissionPermissionContains(String test) {
        for (PermissionPermission groupPermission : PermissionPermission.values()) {
            if (groupPermission.name().equals(test)) {
                return true;
            }
        }
        return false;
    }

}
