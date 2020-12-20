package de.bnder.taskmanager.main;
/*
 * Copyright (C) 2019 Jan Brinkmann
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

import de.bnder.taskmanager.commands.*;
import de.bnder.taskmanager.commands.group.GroupController;
import de.bnder.taskmanager.commands.permission.PermissionController;
import de.bnder.taskmanager.commands.task.TaskController;
import de.bnder.taskmanager.listeners.*;
import de.bnder.taskmanager.listeners.typoReactionListeners.GroupTypoReactionListener;
import de.bnder.taskmanager.listeners.typoReactionListeners.PermissionTypoReactionListener;
import de.bnder.taskmanager.listeners.typoReactionListeners.TaskTypoReactionListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Arrays;

public class Main {

    //CHANGELOG 2020.4.2:
    //-invite command added
    //Translators are shown when changing language if language wasn't translated by bnder
    //Task Command more detailed error messages
    //The Bot can now correct your typos automatically on the task, group & permission command

    public static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static String requestURL = dotenv.get("REQUEST_URL") != null ? dotenv.get("REQUEST_URL") : System.getenv("REQUEST_URL");
    public static String authorizationToken = dotenv.get("AUTHORIZATION_TOKEN") != null ? dotenv.get("AUTHORIZATION_TOKEN") : System.getenv("AUTHORIZATION_TOKEN");
    public static final String userAgent = "TaskmanagerBot/1.0 (Windows; U; WindowsNT 5.1; de-DE; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    public static int shard = Integer.parseInt(dotenv.get("SHARD") != null ? dotenv.get("SHARD") : System.getenv("SHARD"));
    public static int totalShard = Integer.parseInt(dotenv.get("TOTAL_SHARDS") != null ? dotenv.get("TOTAL_SHARDS") : System.getenv(("TOTAL_SHARDS")));

    public static final String prefix = "-";

    public static void main(String[] args) {

        final DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(dotenv.get("BOT_TOKEN"),
                Arrays.asList(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS));

        //Disable Caches for better memory usage
        builder.disableCache(Arrays.asList(CacheFlag.VOICE_STATE, CacheFlag.EMOTE, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ROLE_TAGS, CacheFlag.MEMBER_OVERRIDES));

        builder.setShardsTotal(totalShard);
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

        CommandHandler.commands.put("version", new Version());
        CommandHandler.commands.put("prefix", new Prefix());
        CommandHandler.commands.put("group", new GroupController());
        CommandHandler.commands.put("task", new TaskController());
        CommandHandler.commands.put("permission", new PermissionController());
        CommandHandler.commands.put("token", new Token());
        CommandHandler.commands.put("help", new Help());
        CommandHandler.commands.put("support", new Support());
        CommandHandler.commands.put("stats", new Stats());
        CommandHandler.commands.put("language", new Language());
        CommandHandler.commands.put("app", new App());
        CommandHandler.commands.put("data", new Data());
        CommandHandler.commands.put("settings", new Settings());
        CommandHandler.commands.put("invite", new Invite());

        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("bnder.net"));

        try {
            builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }
}
