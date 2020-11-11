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
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class Main {

    public static final Dotenv dotenv = Dotenv.load();
    public static String requestURL = dotenv.get("REQUEST_URL");
    public static String authorizationToken = dotenv.get("AUTHORIZATION_TOKEN");
    public static final String userAgent = "TaskmanagerBot/1.0 (Windows; U; WindowsNT 5.1; de-DE; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    public static int shard = Integer.parseInt(Objects.requireNonNull(dotenv.get("SHARD")));
    public static int totalShard = Integer.parseInt(Objects.requireNonNull(dotenv.get("TOTAL_SHARDS")));

    public static final String prefix = "-";

    public static void main(String[] args) {
        final DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(dotenv.get("BOT_TOKEN"),
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
        CommandHandler.commands.put("permission", new Permission());
        CommandHandler.commands.put("token", new Token());
        CommandHandler.commands.put("help", new Help());
        CommandHandler.commands.put("support", new Support());
        CommandHandler.commands.put("stats", new Stats());
        CommandHandler.commands.put("language", new Language());
        CommandHandler.commands.put("app", new App());
        CommandHandler.commands.put("data", new Data());

        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("bnder.net"));
        try {
            builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

}
