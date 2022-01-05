package de.bnder.taskmanager.utils;

import com.google.cloud.firestore.*;
import de.bnder.taskmanager.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class RegisterUser {

    private static final Logger logger = LogManager.getLogger(RegisterUser.class);

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

            //Add current server to joined_server
            final CollectionReference collectionReference = userDoc.getReference().collection("joined-server");
            final DocumentSnapshot joinedServerDoc = collectionReference.document(g.getId()).get().get();
            if (!joinedServerDoc.exists()) {
                joinedServerDoc.getReference().set(new HashMap<>() {{
                    put("nav_position", collectionReference.get().get().size());
                    put("guild_id", g.getId());
                    //TODO: ATTENTION TO DIFFERENT TIMEZONE IN DEV & PROD
                    put("last_interaction", new Date());
                }});
            } else {
                joinedServerDoc.getReference().update(new HashMap<>() {{
                    //TODO: ATTENTION TO DIFFERENT TIMEZONE IN DEV & PROD
                    put("last_interaction", new Date());
                }});
            }

            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            cal.add(Calendar.MINUTE, -30);
            //TODO: REMOVE TRUE IN PROD
            if (true || joinedServerDoc.exists() && joinedServerDoc.get("last_interaction") != null && joinedServerDoc.getDate("last_interaction").before(cal.getTime())) {
                updateRoles(member);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
        }
    }

    public static void updateRoles(Member member) throws ExecutionException, InterruptedException {
        final QuerySnapshot roles = Main.firestore.collection("server").document(member.getGuild().getId()).collection("role-member").whereEqualTo("user_id", member.getId()).get().get();

        //Check for roles where member is no longer part of
        for (QueryDocumentSnapshot roleDoc : roles) {
            final String roleID = roleDoc.getString("role_id");
            boolean hasRole = false;
            for (Role role : member.getRoles()) {
                if (role.getId().equals(roleID)) {
                    hasRole = true;
                    break;
                }
            }

            if (member.getRoles().size() == 0 || !hasRole) {
                roleDoc.getReference().delete();
            }
        }

        //Add new roles
        for (final Role role : member.getRoles()) {
            final String roleID = role.getId();
            boolean isInFirestore = false;
            for (QueryDocumentSnapshot roleDoc : roles) {
                if (roleDoc.get("role_id") != null) {
                    if (roleDoc.getString("role_id").equals(roleID)) {
                        isInFirestore = true;
                        break;
                    }
                }
            }
            //Add role to Firestore
            if (!isInFirestore)
                Main.firestore.collection("server").document(member.getGuild().getId()).collection("role-member").add(new HashMap<>() {{
                    put("user_id", member.getId());
                    put("role_id", role.getId());
                }});
        }
    }

}
