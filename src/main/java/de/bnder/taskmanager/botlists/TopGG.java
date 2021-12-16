package de.bnder.taskmanager.botlists;

import de.bnder.taskmanager.main.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class TopGG {

    private static final String baseURL = "https://top.gg/api";
    private static final String apiKey = Main.dotenv.get("TOP.GG_API_KEY") != null ? Main.dotenv.get("TOP.GG_API_KEY") : System.getenv("TOP.GG_API_KEY");

    private static final Logger logger = LogManager.getLogger(TopGG.class);

    public static void sendServerCount(long serverCount, String botID) throws IOException {
        logger.info("Updating Servers on " + baseURL);
        final Connection.Response response = Jsoup.connect(baseURL + "/bots/" + botID + "/stats")
                .header("Authorization", apiKey)
                .data("server_count", String.valueOf(serverCount))
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        if (response.statusCode() == 200){
            logger.info("Success!");
        } else {
            logger.warn("Failed! (" + response.statusCode() + ")");
        }
    }

}
