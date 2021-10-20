package de.bnder.taskmanager.lists;

import com.google.cloud.firestore.DocumentSnapshot;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.Stats;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class UpdateLists {

    public static void updateBotLists(int servers, String botID) {
        try {
            Stats.updateServers(servers, Main.shard);
            final long serverCount = getTotalServers();
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
        } catch (IOException ignored) {
        }
    }

    public static long getTotalServers() throws IOException {
        long servers = 0;
        try {
            DocumentSnapshot getStats = Main.firestore.collection("stats").document("alltime").get().get();
            if (getStats.exists()) {
                if (getStats.getData().containsKey("servers_shard_0")) {
                    servers += (long) getStats.get("servers_shard_0");
                }
                if (getStats.getData().containsKey("servers_shard_1")) {
                    servers += (long) getStats.get("servers_shard_1");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return servers;
    }

}
