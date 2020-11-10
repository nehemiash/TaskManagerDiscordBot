package de.bnder.taskmanager.utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Connection {

    public static int timeout = 15000;

    public void defineConnection() {
        try {
            BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream("teammanagerbotSqlPasswort.json")));
            StringBuilder sb = new StringBuilder();
            String line = buf.readLine();
            while (line != null) {
                sb.append(line);
                line = buf.readLine();
            }

            String jsonSource = sb.toString();
            JsonObject object = Json.parse(jsonSource).asObject();
            Main.requestURL = object.getString("requestURL", null);
            Main.requestToken = URLEncoder.encode(object.getString("password", null), StandardCharsets.UTF_8.toString());
            Main.shard = object.getInt("shard", 0);
            Main.totalShard = object.getInt("total_shards", 1);
            Main.authorizationToken = object.getString("authorization_token", null);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    public static String encodeString(String toEncode) {
        try {
            if (toEncode!= null) {
                return URLEncoder.encode(toEncode, StandardCharsets.UTF_8.toString());
            }
        } catch (Exception ignored) {

        }
        return toEncode;
    }

}
