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
import de.bnder.taskmanager.listeners.*;
import de.bnder.taskmanager.utils.Connection;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Collections;

public class Main {

    public static String requestURL = null;
    public static String requestToken = null;
    public static final String userAgent = "TaskmanagerBot/1.0 (Windows; U; WindowsNT 5.1; de-DE; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    public static int shard = 0;
    public static int totalShard = 2;

    public static final String prefix = "-";

    //CHANGELOG:
    //-task add @USER @USER... TASK HERE
    //-group add @USER @USER... GROUPNAME
    //-group remove @USER @USER... GROUPNAME
    //Bug fix in "-group members"
    //IDS are now 5 Numbers long
    //-task info TASKID
    //Group members will get dm with task when task is assigned to their group

    public static void main(String[] args) {
        new Connection().defineConnection();

        final DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(Connection.botToken,
                Collections.singletonList(GatewayIntent.GUILD_MESSAGES));

        builder.disableCache(Arrays.asList(CacheFlag.VOICE_STATE, CacheFlag.EMOTE));

        builder.setShardsTotal(totalShard);
        builder.setShards(shard);

        builder.setAutoReconnect(true);

        builder.addEventListeners(new CommandListener());
        builder.addEventListeners(new Ready());
        builder.addEventListeners(new ServerNameUpdate());
        builder.addEventListeners(new GuildJoin());
        builder.addEventListeners(new GuildLeave());

        CommandHandler.commands.put("version", new Version());
        CommandHandler.commands.put("prefix", new Prefix());
        CommandHandler.commands.put("group", new Group());
        CommandHandler.commands.put("task", new Task());
        CommandHandler.commands.put("token", new Token());
        CommandHandler.commands.put("help", new Help());
        CommandHandler.commands.put("support", new Support());
        CommandHandler.commands.put("stats", new Stats());
        CommandHandler.commands.put("language", new Language());
        CommandHandler.commands.put("app", new App());

        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("https://bnder.de"));
        try {
            ShardManager shardManager = builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

}
