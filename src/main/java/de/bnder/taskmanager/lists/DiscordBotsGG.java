package de.bnder.taskmanager.lists;

import de.bnder.taskmanager.main.Main;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class DiscordBotsGG {

    private static final String baseURL = "https://discord.bots.gg/api/v1";
    private static final String apiKey = Main.dotenv.get("DISCORD.BOTS.GG_API_KEY") != null ? Main.dotenv.get("DISCORD.BOTS.GG_API_KEY") : System.getenv("DISCORD.BOTS.GG_API_KEY");

    public static void sendServerCount(int serverCount) throws IOException {
        Jsoup.connect(baseURL + "/bots/602460094940184587/stats")
                .header("Authorization", apiKey)
                .data("guildCount", String.valueOf(serverCount))
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();
    }

}
