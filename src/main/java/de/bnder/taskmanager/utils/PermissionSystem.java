package de.bnder.taskmanager.utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import de.bnder.taskmanager.utils.permissions.PermissionPermission;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jsoup.Jsoup;

public class PermissionSystem {

    //Permission checking
    public static boolean hasPermission(Member member, TaskPermission taskPermission) {
        return checkPerms(member, taskPermission.name());
    }

    public static boolean hasPermission(Member member, PermissionPermission taskPermission) {
        return checkPerms(member, taskPermission.name());
    }

    public static boolean hasPermission(Member member, GroupPermission groupPermission) {
        return checkPerms(member, groupPermission.name());
    }


    //Permission adding
    public static int addPermissionStatusCode(Member member, PermissionPermission taskPermission) {
        return sendAddPermRequestUser(member, taskPermission.name());
    }

    public static int addPermissionStatusCode(Role role, PermissionPermission taskPermission) {
        return sendAddPermRequestRole(role, taskPermission.name());
    }

    public static int addPermissionStatusCode(Member member, GroupPermission groupPermission) {
        return sendAddPermRequestUser(member, groupPermission.name());
    }

    public static int addPermissionStatusCode(Role role, GroupPermission taskPermission) {
        return sendAddPermRequestRole(role, taskPermission.name());
    }

    public static int addPermissionStatusCode(Member member, TaskPermission taskPermission) {
        return sendAddPermRequestUser(member, taskPermission.name());
    }

    public static int addPermissionStatusCode(Role role, TaskPermission taskPermission) {
        return sendAddPermRequestRole(role, taskPermission.name());
    }

    //Permission removing

    public static int removePermissionStatusCode(Member member, PermissionPermission taskPermission) {
        return sendRemovePermRequestUser(member, taskPermission.name());
    }

    public static int removePermissionStatusCode(Role role, PermissionPermission taskPermission) {
        return sendRemovePermRequestRole(role, taskPermission.name());
    }

    public static int removePermissionStatusCode(Member member, GroupPermission groupPermission) {
        return sendRemovePermRequestUser(member, groupPermission.name());
    }

    public static int removePermissionStatusCode(Role role, GroupPermission taskPermission) {
        return sendRemovePermRequestRole(role, taskPermission.name());
    }

    public static int removePermissionStatusCode(Member member, TaskPermission taskPermission) {
        return sendRemovePermRequestUser(member, taskPermission.name());
    }

    public static int removePermissionStatusCode(Role role, TaskPermission taskPermission) {
        return sendRemovePermRequestRole(role, taskPermission.name());
    }

    //Methods
    private static boolean checkPerms(Member member, String name) {
        if (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        } else {
            try {
                final StringBuilder rolesBuilder = new StringBuilder();
                for (Role role : member.getRoles()) {
                    rolesBuilder.append(role.getId()).append(",");
                }
                final String jsonResponse = Jsoup.connect(Main.requestURL + "hasPermission.php?requestToken=" + Main.requestToken + "&serverID=" + member.getGuild().getId() + "&userID=" + member.getUser().getId() + "&permission=" + Connection.encodeString(name) + "&roleIDs=" + Connection.encodeString(rolesBuilder.toString())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
                final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
                final int statusCode = jsonObject.getInt("status_code", 900);
                if (statusCode == 200) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static int sendAddPermRequestRole(Role role, String name) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "addPermissionRole.php?requestToken=" + Main.requestToken + "&serverID=" + role.getGuild().getId() + "&roleID=" + role.getId() + "&permission=" + Connection.encodeString(name)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            return jsonObject.getInt("status_code", 900);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 912;
    }

    private static int sendAddPermRequestUser(Member member, String name) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "addPermission.php?requestToken=" + Main.requestToken + "&serverID=" + member.getGuild().getId() + "&userID=" + member.getUser().getId() + "&permission=" + Connection.encodeString(name)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            return jsonObject.getInt("status_code", 900);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 912;
    }

    private static int sendRemovePermRequestUser(Member member, String name) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "removePermission.php?requestToken=" + Main.requestToken + "&serverID=" + member.getGuild().getId() + "&userID=" + member.getUser().getId() + "&permission=" + Connection.encodeString(name)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            return jsonObject.getInt("status_code", 900);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 912;
    }

    private static int sendRemovePermRequestRole(Role role, String name) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "removePermissionRole.php?requestToken=" + Main.requestToken + "&serverID=" + role.getGuild().getId() + "&roleID=" + role.getId() + "&permission=" + Connection.encodeString(name)).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            return jsonObject.getInt("status_code", 900);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 912;
    }

}
