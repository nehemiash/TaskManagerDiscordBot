package de.bnder.taskmanager.lists;

import de.bnder.taskmanager.main.Main;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class BlistXYZ {

    private static final String baseURL = "https://blist.xyz/api/v2";
    private static final String apiKey = Main.dotenv.get("BLIST.XYZ_API_KEY") != null ? Main.dotenv.get("BLIST.XYZ_API_KEY") : System.getenv("BLIST.XYZ_API_KEY");

    public static void sendServerCount(int serverCount) throws IOException {
        Jsoup.connect(baseURL + "/bot/602460094940184587/stats")
                .header("Authorization", apiKey)
                .data("server_count", String.valueOf(serverCount))
                //TODO: FIX
                .method(Connection.Method.PATCH)
                .ignoreContentType(true)
                .execute();
    }

}
