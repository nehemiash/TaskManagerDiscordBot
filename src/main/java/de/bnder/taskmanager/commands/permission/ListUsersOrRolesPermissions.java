package de.bnder.taskmanager.commands.permission;

import de.bnder.taskmanager.utils.Localizations;
import de.bnder.taskmanager.utils.MessageSender;
import de.bnder.taskmanager.utils.PermissionSystem;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import de.bnder.taskmanager.utils.permissions.PermissionPermission;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.List;

import static de.bnder.taskmanager.commands.permission.AddPermission.hasPermString;

public class ListUsersOrRolesPermissions {

    public static void listUsersOrRolesPermissions(Member member, TextChannel textChannel, String[] args, List<Member> mentionedMembers, List<Role> mentionedRoles, SlashCommandEvent slashCommandEvent) {
        final String langCode = Localizations.getGuildLanguage(member.getGuild());
        final String embedTitle = Localizations.getString("permissions_title", langCode);
        if (mentionedMembers != null && mentionedMembers.size() > 0) {
            final Member mentionedMember = mentionedMembers.get(0);
            final StringBuilder stringBuilder = new StringBuilder();
            for (TaskPermission permission : TaskPermission.values()) {
                stringBuilder.append(permission.name()).append(": ").append(hasPermString(PermissionSystem.hasPermission(mentionedMember, permission))).append("\n");
            }
            for (GroupPermission permission : GroupPermission.values()) {
                stringBuilder.append(permission.name()).append(": ").append(hasPermString(PermissionSystem.hasPermission(mentionedMember, permission))).append("\n");
            }
            for (PermissionPermission permission : PermissionPermission.values()) {
                stringBuilder.append(permission.name()).append(": ").append(hasPermString(PermissionSystem.hasPermission(mentionedMember, permission))).append("\n");
            }
            MessageSender.send(embedTitle + " - " + mentionedMember.getUser().getAsTag(), stringBuilder.toString(), textChannel, Color.green, langCode, slashCommandEvent);
        } else if (mentionedRoles != null && mentionedRoles.size() > 0) {
            final Role role = mentionedRoles.get(0);
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
            MessageSender.send(embedTitle + " - " + role.getName(), stringBuilder.toString(), textChannel, Color.green, langCode, slashCommandEvent);
        } else {
            MessageSender.send(embedTitle, Localizations.getString("need_to_mention_user_or_role", langCode), textChannel, Color.red, langCode, slashCommandEvent);
        }
    }

}
