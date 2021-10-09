package de.bnder.taskmanager.main;
/*
 * Copyright (C) 2020 Jan Brinkmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import de.bnder.taskmanager.commands.*;
import de.bnder.taskmanager.commands.board.BoardController;
import de.bnder.taskmanager.commands.group.GroupController;
import de.bnder.taskmanager.commands.help.HelpController;
import de.bnder.taskmanager.commands.permission.PermissionController;
import de.bnder.taskmanager.commands.settings.SettingsController;
import de.bnder.taskmanager.commands.task.TaskController;
import de.bnder.taskmanager.listeners.*;
import de.bnder.taskmanager.listeners.typoReactionListeners.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static final String tmbApiUrl = dotenv.get("REQUEST_URL") != null ? dotenv.get("REQUEST_URL") : System.getenv("REQUEST_URL");
    public static final String tmbApiAuthorizationToken = dotenv.get("AUTHORIZATION_TOKEN") != null ? dotenv.get("AUTHORIZATION_TOKEN") : System.getenv("AUTHORIZATION_TOKEN");
    public static final String userAgent = "TaskmanagerBot/1.0 (Windows; U; WindowsNT 5.1; de-DE; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    public static final int shard = Integer.parseInt(dotenv.get("SHARD") != null ? dotenv.get("SHARD") : System.getenv("SHARD"));
    public static final int totalShards = Integer.parseInt(dotenv.get("TOTAL_SHARDS") != null ? dotenv.get("TOTAL_SHARDS") : System.getenv(("TOTAL_SHARDS")));

    public static Firestore firestore;

    public static Connection tmbAPI(String path, String userID, Connection.Method method) {
        return Jsoup.connect(tmbApiUrl + "/" + path).method(method)
                .header("authorization", "TMB " + Main.tmbApiAuthorizationToken)
                .header("user_id", userID == null ? "---" : userID)
                .timeout(de.bnder.taskmanager.utils.Connection.timeout)
                .userAgent(Main.userAgent)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .postDataCharset("UTF-8")
                .followRedirects(true);
    }

    public static final String prefix = "-";

    public static void main(String[] args) {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("serviceAccountKey.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);

            firestore = FirestoreClient.getFirestore();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        final DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createLight(dotenv.get("BOT_TOKEN"),
                Arrays.asList(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS));

        builder.setEnabledIntents(Arrays.asList(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS));

        /** Disable Caches for better memory usage */
        //builder.disableCache(Arrays.asList(CacheFlag.VOICE_STATE, CacheFlag.EMOTE, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ROLE_TAGS, CacheFlag.MEMBER_OVERRIDES));

        builder.setShardsTotal(totalShards);
        builder.setShards(shard);

        builder.setAutoReconnect(true);

        builder.addEventListeners(new CommandListener());
        builder.addEventListeners(new Ready());
        builder.addEventListeners(new ServerNameUpdate());
        builder.addEventListeners(new GuildJoin());
        builder.addEventListeners(new GuildLeave());
        builder.addEventListeners(new TaskTypoReactionListener());
        builder.addEventListeners(new GroupTypoReactionListener());
        builder.addEventListeners(new PermissionTypoReactionListener());
        builder.addEventListeners(new SettingsTypoReactionListener());
        builder.addEventListeners(new BoardTypoReactionListener());
        builder.addEventListeners(new TaskLogReaction());

        CommandHandler.commands.put("version", new Version());
        CommandHandler.commands.put("prefix", new Prefix());
        CommandHandler.commands.put("group", new GroupController());
        CommandHandler.commands.put("g", new GroupController()); //group
        CommandHandler.commands.put("task", new TaskController());
        CommandHandler.commands.put("t", new TaskController()); //task
        CommandHandler.commands.put("permission", new PermissionController());
        CommandHandler.commands.put("p", new PermissionController()); //permission
        CommandHandler.commands.put("settings", new SettingsController());
        CommandHandler.commands.put("s", new SettingsController()); //settings
        CommandHandler.commands.put("board", new BoardController());
        CommandHandler.commands.put("b", new BoardController()); //board
        CommandHandler.commands.put("token", new Token());
        CommandHandler.commands.put("help", new HelpController());
        CommandHandler.commands.put("h", new HelpController()); //help
        CommandHandler.commands.put("support", new Support());
        CommandHandler.commands.put("stats", new Stats());
        CommandHandler.commands.put("language", new Language());
        CommandHandler.commands.put("l", new Language()); //language
        CommandHandler.commands.put("app", new App());
        CommandHandler.commands.put("data", new Data());
        CommandHandler.commands.put("invite", new Invite());
        CommandHandler.commands.put("search", new Search());

        builder.setStatus(OnlineStatus.OFFLINE);
        builder.setActivity(Activity.playing("bnder.net"));


        try {
            builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }
}
