package de.bnder.taskmanager.lists;

import de.bnder.taskmanager.main.Main;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class TopcordXYZ {

    private static final String baseURL = "https://topcord.xyz/api";
    private static final String apiKey = Main.dotenv.get("TOPCORD.XYZ_API_KEY") != null ? Main.dotenv.get("TOPCORD.XYZ_API_KEY") : System.getenv("TOPCORD.XYZ_API_KEY");

    public static void sendServerCount(int serverCount) throws IOException {
        Jsoup.connect(baseURL + "/bot/stats/602460094940184587")
                .header("authorization", apiKey)
                .data("guilds", String.valueOf(serverCount))
                .data("shards", "0")
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();
    }

}
