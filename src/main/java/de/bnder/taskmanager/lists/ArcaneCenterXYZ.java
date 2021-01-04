package de.bnder.taskmanager.lists;

import de.bnder.taskmanager.main.Main;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class ArcaneCenterXYZ {

    private static final String baseURL = "https://arcane-botcenter.xyz/api";
    private static final String apiKey = Main.dotenv.get("ARCANE-CENTER.XYZ_API_KEY") != null ? Main.dotenv.get("ARCANE-CENTER.XYZ_API_KEY") : System.getenv("ARCANE-CENTER.XYZ_API_KEY");

    public static void sendServerCount(int serverCount) throws IOException {
        Jsoup.connect(baseURL + "/602460094940184587/stats")
                .header("Authorization", apiKey)
                .data("server_count", String.valueOf(serverCount))
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();
    }

}
