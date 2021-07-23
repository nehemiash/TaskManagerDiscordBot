package de.bnder.taskmanager.lists;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import de.bnder.taskmanager.main.Main;
import org.jsoup.Connection;

import java.io.IOException;

public class UpdateLists {

    public static void updateBotLists(int servers, String botID) {
        try {
            final int ownAPIStatusCode = updateOwnStats(servers);
            if (ownAPIStatusCode == 200) {
                final int serverCount = getTotalServers();
                TopGG.sendServerCount(serverCount, botID);
                DiscordbotlistCOM.sendServerCount(serverCount, botID);
                DiscordBotsGG.sendServerCount(serverCount, botID);
                TopcordXYZ.sendServerCount(serverCount, botID);
                BlistXYZ.sendServerCount(serverCount, botID);
                BotlistsCOM.sendServerCount(serverCount);
                DiscordBOATS.sendServerCount(serverCount, botID);
                VoidbotsNET.sendServerCount(serverCount, botID);
                InfinitybotlistCOM.sendServerCount(serverCount, botID);
                System.out.println("Updating lists finished");
            }
        } catch (IOException ignored) {}
    }

    private static int updateOwnStats(final int servers) throws IOException {
        final Connection.Response response = Main.tmbAPI("stats/shard/" + Main.shard + "/servers", null, Connection.Method.POST).data("servers", String.valueOf(servers)).execute();
        return response.statusCode();
    }

    public static int getTotalServers() throws IOException {
        final Connection.Response response = Main.tmbAPI("stats", null, Connection.Method.GET).execute();
        if (response.statusCode() == 200) {
            final JsonObject jsonObject = Json.parse(response.body()).asObject();
            final int shard0Servers = jsonObject.getInt("servers_shard_0", 0);
            final int shard1Servers = jsonObject.getInt("servers_shard_1", 0);
            return shard0Servers + shard1Servers;
        }
        return 0;
    }

}
