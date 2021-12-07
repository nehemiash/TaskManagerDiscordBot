package de.bnder.taskmanager.utils;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class RegisterUser {

    public static void register(Member member) {
        try {
            final DocumentSnapshot userDoc = Main.firestore.collection("users").document(member.getId()).get().get();
            final Guild g = member.getGuild();
            if (!userDoc.exists()) {
                userDoc.getReference().set(new HashMap<>() {{
                    put("avatar_url", member.getEffectiveAvatarUrl());
                    put("display_name", member.getUser().getName());
                    put("discriminator", member.getUser().getDiscriminator());
                }});
            } else {
                HashMap<String, Object> updatedValues = new HashMap<>();
                if (!userDoc.getString("avatar_url").equals(member.getEffectiveAvatarUrl())) ;
                {
                    updatedValues.put("avatar_url", member.getEffectiveAvatarUrl());
                }
                if (!userDoc.getString("display_name").equals(member.getUser().getName())) {
                    updatedValues.put("display_name", member.getUser().getName());
                }
                if (!userDoc.getString("discriminator").equals(member.getUser().getDiscriminator())) {
                    updatedValues.put("discriminator", member.getUser().getDiscriminator());
                }

                if (updatedValues.size() > 0) {
                    userDoc.getReference().update(updatedValues);
                }
            }
            final CollectionReference collectionReference = userDoc.getReference().collection("joined-server");
            if (!collectionReference.document(g.getId()).get().get().exists()) {
                collectionReference.document(g.getId()).set(new HashMap<>() {{
                    put("nav_position", collectionReference.get().get().size());
                    put("guild_id", g.getId());
                    put("last_interaction", System.currentTimeMillis());
                }});
            } else
                collectionReference.document(g.getId()).update(new HashMap<>() {{
                    put("last_interaction", System.currentTimeMillis());
                }});
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
