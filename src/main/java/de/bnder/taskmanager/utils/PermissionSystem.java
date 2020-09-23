package de.bnder.taskmanager.utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.jsoup.Jsoup;

public class PermissionSystem {

    public static boolean hasPermission(Member member, TaskPermission taskPermission) {
        if (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        } else {
            try {
                final String jsonResponse = Jsoup.connect(Main.requestURL + "hasPermission.php?requestToken=" + Main.requestToken + "&serverID=" + member.getGuild().getId() + "&userID=" + member.getUser().getId() + "&permission=" + Connection.encodeString(taskPermission.name())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
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

    public static int addPermissionStatusCode(Member member, TaskPermission taskPermission) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "addPermission.php?requestToken=" + Main.requestToken + "&serverID=" + member.getGuild().getId() + "&userID=" + member.getUser().getId() + "&permission=" + Connection.encodeString(taskPermission.name())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            final int statusCode = jsonObject.getInt("status_code", 900);
            return statusCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 912;
    }

    public static boolean hasPermission(Member member, GroupPermission groupPermission) {
        if (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        } else {
            try {
                final String jsonResponse = Jsoup.connect(Main.requestURL + "hasPermission.php?requestToken=" + Main.requestToken + "&serverID=" + member.getGuild().getId() + "&userID=" + member.getUser().getId() + "&permission=" + Connection.encodeString(groupPermission.name())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
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

    public static int addPermissionStatusCode(Member member, GroupPermission groupPermission) {
        try {
            final String jsonResponse = Jsoup.connect(Main.requestURL + "addPermission.php?requestToken=" + Main.requestToken + "&serverID=" + member.getGuild().getId() + "&userID=" + member.getUser().getId() + "&permission=" + Connection.encodeString(groupPermission.name())).timeout(Connection.timeout).userAgent(Main.userAgent).execute().body();
            final JsonObject jsonObject = Json.parse(jsonResponse).asObject();
            final int statusCode = jsonObject.getInt("status_code", 900);
            return statusCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 912;
    }

}
