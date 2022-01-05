package de.bnder.taskmanager.commands.permission;

import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.RegisterUser;
import de.bnder.taskmanager.utils.permissions.BoardPermission;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import de.bnder.taskmanager.utils.permissions.PermissionPermission;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class AddPermission {

    public static void addPermission(Member member, TextChannel textChannel, String[] args, List<Member> mentionedMembers, List<Role> mentionedRoles, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("permissions_title", langCode);
        if (PermissionSystem.hasPermission(member, PermissionPermission.ADD_PERMISSION)) {
            int statusCode;
            if (mentionedMembers != null && mentionedMembers.size() > 0) {
                if (taskPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.addPermissionStatusCode(mentionedMembers.get(0), TaskPermission.valueOf(args[2].toUpperCase()));
                } else if (groupPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.addPermissionStatusCode(mentionedMembers.get(0), GroupPermission.valueOf(args[2].toUpperCase()));
                } else if (permissionPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.addPermissionStatusCode(mentionedMembers.get(0), PermissionPermission.valueOf(args[2].toUpperCase()));
                } else if (boardPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.addPermissionStatusCode(mentionedMembers.get(0), BoardPermission.valueOf(args[2].toUpperCase()));
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("unknown_permission_name", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                    return;
                }
                try {
                    RegisterUser.updateRoles(mentionedMembers.get(0));
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (mentionedRoles != null && mentionedRoles.size() > 0) {
                if (taskPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.addPermissionStatusCode(mentionedRoles.get(0), TaskPermission.valueOf(args[2].toUpperCase()));
                } else if (groupPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.addPermissionStatusCode(mentionedRoles.get(0), GroupPermission.valueOf(args[2].toUpperCase()));
                } else if (permissionPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.addPermissionStatusCode(mentionedRoles.get(0), PermissionPermission.valueOf(args[2].toUpperCase()));
                } else if (boardPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.addPermissionStatusCode(mentionedRoles.get(0), BoardPermission.valueOf(args[2].toUpperCase()));
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("unknown_permission_name", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                    return;
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("need_to_mention_user_or_role", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                return;
            }
            if (statusCode == 200) {
                MessageSender.send(embedTitle, Localizations.getString("permission_added", langCode, new ArrayList<>() {{
                    add(args[2].toUpperCase());
                    add((mentionedMembers != null && mentionedMembers.size() > 0) ? mentionedMembers.get(0).getAsMention() : mentionedRoles.get(0).getAsMention());
                }}), textChannel, Color.green, langCode, slashCommandEvent);
            } else if (statusCode == 903) {
                MessageSender.send(embedTitle, Localizations.getString("already_has_permission", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            } else {
                MessageSender.send(embedTitle, Localizations.getString("unknown_permission_name", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

    public static String hasPermString(boolean has) {
        return has ? "✅" : "❌";
    }

    public static boolean taskPermissionContains(String test) {
        for (TaskPermission taskPermission : TaskPermission.values()) {
            if (taskPermission.name().equals(test)) {
                return true;
            }
        }
        return false;
    }

    public static boolean groupPermissionContains(String test) {
        for (GroupPermission groupPermission : GroupPermission.values()) {
            if (groupPermission.name().equals(test)) {
                return true;
            }
        }
        return false;
    }

    public static boolean permissionPermissionContains(String test) {
        for (PermissionPermission groupPermission : PermissionPermission.values()) {
            if (groupPermission.name().equals(test)) {
                return true;
            }
        }
        return false;
    }

    public static boolean boardPermissionContains(String test) {
        for (BoardPermission boardPermission : BoardPermission.values()) {
            if (boardPermission.name().equals(test)) {
                return true;
            }
        }
        return false;
    }

}
