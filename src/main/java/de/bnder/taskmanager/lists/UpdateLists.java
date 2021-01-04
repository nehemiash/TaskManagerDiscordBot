package de.bnder.taskmanager.lists;

import java.io.IOException;

public class UpdateLists {

    public static void updateBotLists(){
        //TODO: GET SERVER COUNT FROM API
        final int serverCount = 505;
        try {
            TopGG.sendServerCount(serverCount);
            DiscordbotlistCOM.sendServerCount(serverCount);
            DiscordBotsGG.sendServerCount(serverCount);
            TopcordXYZ.sendServerCount(serverCount);
            ArcaneCenterXYZ.sendServerCount(serverCount);
            BlistXYZ.sendServerCount(serverCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
