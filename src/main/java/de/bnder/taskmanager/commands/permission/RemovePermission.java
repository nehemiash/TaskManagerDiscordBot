package de.bnder.taskmanager.commands.permission;

import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.BoardPermission;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import de.bnder.taskmanager.utils.permissions.PermissionPermission;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.List;
import java.util.Locale;

import static de.bnder.taskmanager.commands.permission.AddPermission.*;

public class RemovePermission {

    public static void removePermission(Member member, TextChannel textChannel, String[] args, List<Member> mentionedMembers, List<Role> mentionedRoles, SlashCommandEvent slashCommandEvent) {
        final Locale langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("permissions_title", langCode);
        if (PermissionSystem.hasPermission(member, PermissionPermission.REMOVE_PERMISSION)) {
            int statusCode;
            if (mentionedMembers != null && mentionedMembers.size() > 0) {
                if (taskPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.removePermissionStatusCode(mentionedMembers.get(0), TaskPermission.valueOf(args[2].toUpperCase()));
                } else if (groupPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.removePermissionStatusCode(mentionedMembers.get(0), GroupPermission.valueOf(args[2].toUpperCase()));
                } else if (permissionPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.removePermissionStatusCode(mentionedMembers.get(0), PermissionPermission.valueOf(args[2].toUpperCase()));
                }  else if (boardPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.removePermissionStatusCode(mentionedMembers.get(0), BoardPermission.valueOf(args[2].toUpperCase()));
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("unknown_permission_name", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                    return;
                }
            } else if (mentionedRoles != null && mentionedRoles.size() > 0) {
                if (taskPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.removePermissionStatusCode(mentionedRoles.get(0), TaskPermission.valueOf(args[2].toUpperCase()));
                } else if (groupPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.removePermissionStatusCode(mentionedRoles.get(0), GroupPermission.valueOf(args[2].toUpperCase()));
                } else if (permissionPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.removePermissionStatusCode(mentionedRoles.get(0), PermissionPermission.valueOf(args[2].toUpperCase()));
                }  else if (boardPermissionContains(args[2].toUpperCase())) {
                    statusCode = PermissionSystem.removePermissionStatusCode(mentionedRoles.get(0), BoardPermission.valueOf(args[2].toUpperCase()));
                } else {
                    MessageSender.send(embedTitle, Localizations.getString("unknown_permission_name", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                    return;
                }
            } else {
                MessageSender.send(embedTitle, Localizations.getString("need_to_mention_user_or_role", langCode), textChannel, Color.red, langCode, slashCommandEvent);
                return;
            }
            if (statusCode == 200) {
                MessageSender.send(embedTitle, Localizations.getString("permission_removed", langCode), textChannel, Color.green, langCode, slashCommandEvent);
            } else if (statusCode == 903) {
                MessageSender.send(embedTitle, Localizations.getString("dont_has_permission", langCode), textChannel, Color.red, langCode, slashCommandEvent);
            }
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_be_serveradmin_or_have_admin_permissions", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
