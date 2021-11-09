package de.bnder.taskmanager.botlists;

import com.google.cloud.firestore.DocumentSnapshot;
import de.bnder.taskmanager.main.Main;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class UpdateBotLists {

    public static void updateBotLists(int servers, String botID) {
        // Start new thread, so the main JDA isn't interrupted
        new Thread(() -> {
            try {
                updateOwnStats(servers);
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
            } catch (Exception ignored) {
                System.out.println("Updating lists failed.");
            }
            //Stop Thread
            Thread.currentThread().interrupt();
        }).start();
    }

    private static void updateOwnStats(final int servers) throws ExecutionException, InterruptedException {
        DocumentSnapshot allTimeStats = Main.firestore.collection("stats").document("alltime").get().get();
        if (allTimeStats.exists()) {
            allTimeStats.getReference().update("servers_shard_" + Main.shard, servers);
        } else {
            allTimeStats.getReference().create(new HashMap<>() {{
                put("servers_shard_" + Main.shard, servers);
            }});
        }
    }

    public static long getTotalServers() throws ExecutionException, InterruptedException {
        DocumentSnapshot getStats = Main.firestore.collection("stats").document("alltime").get().get();
        if (getStats.exists()) {
            long serversShard0 = 0;
            if (getStats.exists() && getStats.getData().containsKey("servers_shard_0")) {
                serversShard0 = (long) getStats.get("servers_shard_0");
            }
            long serversShard1 = 0;
            if (getStats.exists() && getStats.getData().containsKey("servers_shard_1")) {
                serversShard1 = (long) getStats.get("servers_shard_1");
            }
            return serversShard0 + serversShard1;
        }
        return 0;
    }

}
