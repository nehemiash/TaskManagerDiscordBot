package de.bnder.taskmanager.utils;

import com.google.cloud.firestore.DocumentSnapshot;
import de.bnder.taskmanager.main.Main;
import de.bnder.taskmanager.utils.permissions.BoardPermission;
import de.bnder.taskmanager.utils.permissions.GroupPermission;
import de.bnder.taskmanager.utils.permissions.PermissionPermission;
import de.bnder.taskmanager.utils.permissions.TaskPermission;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PermissionSystem {

    private static final Logger logger = LogManager.getLogger(PermissionSystem.class);

    //Permission checking
    public static boolean hasPermission(Member member, TaskPermission taskPermission) {
        return checkMemberPermissions(member, taskPermission.name());
    }

    public static boolean hasPermission(Member member, PermissionPermission taskPermission) {
        return checkMemberPermissions(member, taskPermission.name());
    }

    public static boolean hasPermission(Member member, GroupPermission groupPermission) {
        return checkMemberPermissions(member, groupPermission.name());
    }

    public static boolean hasPermission(Member member, BoardPermission boardPermission) {
        return checkMemberPermissions(member, boardPermission.name());
    }

    public static boolean hasPermission(Role role, TaskPermission taskPermission) {
        return checkRolePermissions(role, taskPermission.name());
    }

    public static boolean hasPermission(Role role, PermissionPermission taskPermission) {
        return checkRolePermissions(role, taskPermission.name());
    }

    public static boolean hasPermission(Role role, GroupPermission groupPermission) {
        return checkRolePermissions(role, groupPermission.name());
    }

    public static boolean hasPermission(Role role, BoardPermission boardPermission) {
        return checkRolePermissions(role, boardPermission.name());
    }


    //Permission adding
    public static int addPermissionStatusCode(Member member, PermissionPermission taskPermission) {
        return addPermissionToUser(member, taskPermission.name());
    }

    public static int addPermissionStatusCode(Role role, PermissionPermission taskPermission) {
        return addPermissionToRole(role, taskPermission.name());
    }

    public static int addPermissionStatusCode(Member member, GroupPermission groupPermission) {
        return addPermissionToUser(member, groupPermission.name());
    }

    public static int addPermissionStatusCode(Role role, GroupPermission taskPermission) {
        return addPermissionToRole(role, taskPermission.name());
    }

    public static int addPermissionStatusCode(Member member, TaskPermission taskPermission) {
        return addPermissionToUser(member, taskPermission.name());
    }

    public static int addPermissionStatusCode(Role role, TaskPermission taskPermission) {
        return addPermissionToRole(role, taskPermission.name());
    }

    public static int addPermissionStatusCode(Member member, BoardPermission boardPermission) {
        return addPermissionToUser(member, boardPermission.name());
    }

    public static int addPermissionStatusCode(Role role, BoardPermission boardPermission) {
        return addPermissionToRole(role, boardPermission.name());
    }

    //Permission removing

    public static int removePermissionStatusCode(Member member, PermissionPermission taskPermission) {
        return removePermFromUser(member, taskPermission.name());
    }

    public static int removePermissionStatusCode(Role role, PermissionPermission taskPermission) {
        return removePermFromRole(role, taskPermission.name());
    }

    public static int removePermissionStatusCode(Member member, GroupPermission groupPermission) {
        return removePermFromUser(member, groupPermission.name());
    }

    public static int removePermissionStatusCode(Role role, GroupPermission taskPermission) {
        return removePermFromRole(role, taskPermission.name());
    }

    public static int removePermissionStatusCode(Member member, TaskPermission taskPermission) {
        return removePermFromUser(member, taskPermission.name());
    }

    public static int removePermissionStatusCode(Role role, TaskPermission taskPermission) {
        return removePermFromRole(role, taskPermission.name());
    }

    public static int removePermissionStatusCode(Member member, BoardPermission boardPermission) {
        return removePermFromUser(member, boardPermission.name());
    }

    public static int removePermissionStatusCode(Role role, BoardPermission boardPermission) {
        return removePermFromRole(role, boardPermission.name());
    }

    //Methods
    private static boolean checkMemberPermissions(Member member, String permission) {
        if (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        } else {
            try {
                final DocumentSnapshot serverMemberDoc = Main.firestore.collection("server").document(member.getGuild().getId()).collection("server-member").document(member.getId()).get().get();
                if (serverMemberDoc.exists()) {
                    if (serverMemberDoc.getData().containsKey("permissions")) {
                        Map<String, Boolean> map = (Map<String, Boolean>) serverMemberDoc.getData().get("permissions");
                        if (map.containsKey(permission)) {
                            if (map.get(permission).booleanValue()) {
                                return true;
                            }
                        }
                    }
                }
                for (Role role : member.getRoles()) {
                    boolean hasPerms = checkRolePermissions(role, permission);
                    if (hasPerms) {
                        return true;
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return false;
    }

    private static boolean checkRolePermissions(Role role, String permission) {
        if (role.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        } else {
            try {
                final DocumentSnapshot roleDoc = Main.firestore.collection("server").document(role.getGuild().getId()).collection("roles").document(role.getId()).get().get();
                if (roleDoc.exists()) {
                    if (roleDoc.getData().containsKey("permissions")) {
                        Map<String, Boolean> map = (Map<String, Boolean>) roleDoc.getData().get("permissions");
                        if (map.containsKey(permission)) {
                            if (map.get(permission).booleanValue()) {
                                return true;
                            }
                        }
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                logger.error(e);
            }
        }
        return false;
    }

    private static int addPermissionToRole(Role role, String name) {
        try {
            final DocumentSnapshot roleDoc = Main.firestore.collection("server").document(role.getGuild().getId()).collection("roles").document(role.getId()).get().get();
            if (roleDoc.exists()) {
                Map<String, Boolean> map = new HashMap<>();
                if (roleDoc.getData().containsKey("permissions")) {
                    map = (Map<String, Boolean>) roleDoc.getData().get("permissions");
                }
                map.remove(name);
                map.put(name, true);
                Map<String, Boolean> finalMap = map;
                roleDoc.getReference().update(new HashMap<>() {{
                    put("permissions", finalMap);
                    if (!roleDoc.getString("name").equals(role.getName())) put("name", role.getName());
                }});
            } else {
                roleDoc.getReference().set(new HashMap<>() {{
                    put("permissions", new HashMap<>() {{
                        put(name, true);
                    }});
                    put("name", role.getName());
                }});
            }
            return 200;
        } catch (ExecutionException | InterruptedException e) {
            logger.error(e);
        }
        return 901;
    }

    private static int addPermissionToUser(Member member, String name) {
        try {
            final DocumentSnapshot serverMemberDoc = Main.firestore.collection("server").document(member.getGuild().getId()).collection("server-member").document(member.getId()).get().get();
            if (serverMemberDoc.exists()) {
                Map<String, Boolean> map = new HashMap<>();
                if (serverMemberDoc.getData().containsKey("permissions")) {
                    map = (Map<String, Boolean>) serverMemberDoc.getData().get("permissions");
                }
                map.remove(name);
                map.put(name, true);
                Map<String, Boolean> finalMap = map;
                serverMemberDoc.getReference().update(new HashMap<>() {{
                    put("permissions", finalMap);
                }});
            } else {
                serverMemberDoc.getReference().set(new HashMap<>() {{
                    put("permissions", new HashMap<>() {{
                        put(name, true);
                    }});
                }});
            }
            return 200;
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
        }
        return 901;
    }

    private static int removePermFromUser(Member member, String name) {
        try {
            final DocumentSnapshot serverMemberDoc = Main.firestore.collection("server").document(member.getGuild().getId()).collection("server-member").document(member.getId()).get().get();
            if (serverMemberDoc.exists()) {
                Map<String, Boolean> map = new HashMap<>();
                if (serverMemberDoc.getData().containsKey("permissions")) {
                    map = (Map<String, Boolean>) serverMemberDoc.getData().get("permissions");
                }
                map.remove(name);
                Map<String, Boolean> finalMap = map;
                serverMemberDoc.getReference().update(new HashMap<>() {{
                    put("permissions", finalMap);
                }});
            }
            return 200;
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
        }
        return 901;
    }

    private static int removePermFromRole(Role role, String name) {
        try {
            final DocumentSnapshot roleDoc = Main.firestore.collection("server").document(role.getGuild().getId()).collection("roles").document(role.getId()).get().get();
            if (roleDoc.exists()) {
                Map<String, Boolean> map = new HashMap<>();
                if (roleDoc.getData().containsKey("permissions")) {
                    map = (Map<String, Boolean>) roleDoc.getData().get("permissions");
                }
                map.remove(name);
                Map<String, Boolean> finalMap = map;
                roleDoc.getReference().update(new HashMap<>() {{
                    put("permissions", finalMap);
                    if (!roleDoc.getString("name").equals(role.getName())) put("name", role.getName());
                }});
            }
            return 200;
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
        }
        return 901;
    }

}
