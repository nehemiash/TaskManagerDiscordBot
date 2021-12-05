package de.bnder.taskmanager.utils;

import com.google.cloud.firestore.DocumentSnapshot;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class RegisterUser {

    public static void register(User user) {
        try {
            DocumentSnapshot a = Main.firestore.collection("users").document(user.getId()).get().get();
            for (Guild g : user.getMutualGuilds()) {
                if (!a.getReference().collection("servers").document(g.getId()).get().get().exists()) {
                    a.getReference().collection("servers").document(g.getId()).set(new HashMap<>() {{
                        put("is_owner", (g.getOwnerId().equals(user.getId())));
                    }});
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
