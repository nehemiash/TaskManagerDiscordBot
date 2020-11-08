package de.bnder.taskmanager.utils

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import de.bnder.taskmanager.main.Main
import net.dv8tion.jda.api.entities.Guild
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

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
class Localizations {
    companion object {
        private var toParse: JsonObject? = null
        fun getString(path: String, languageCode: String): String {
            if (toParse == null) {
                toParse = Json.parse(localizationsJSONSource).asObject()
            }
            return if (toParse!![path] != null) {
                toParse!![path].asObject().getString(languageCode, "null")
            } else path
        }

        fun getString(path: String, languageCode: String?, args: List<String?>): String {
            if (toParse == null) {
                toParse = Json.parse(localizationsJSONSource).asObject()
            }
            if (toParse!![path] != null) {
                var toReturn = toParse!![path].asObject().getString(languageCode, "null")
                var argCount = 0
                while (toReturn.contains("$")) {
                    if (args.size >= argCount + 1) {
                        toReturn = toReturn.replaceFirst("\\$".toRegex(), args[argCount]!!)
                        argCount++
                    } else {
                        toReturn = toReturn.replaceFirst("\\$".toRegex(), " ")
                    }
                }
                return toReturn
            }
            return path
        }

        fun getGuildLanguage(guild: Guild): String {
            try {
                val res = Jsoup.connect("http://localhost:5000" + "/server/language/" + guild.id).method(org.jsoup.Connection.Method.GET).header("authorization", "TMB " + Main.authorizationToken).header("user_id", "---").timeout(Connection.timeout).userAgent(Main.userAgent).ignoreContentType(true).ignoreHttpErrors(true).execute()
                if (res.statusCode() == 200) {
                    return Json.parse(res.parse().body().text()).asObject().getString("language", "en")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return "en"
        }

        private val localizationsJSONSource: String?
            get() {
                try {
                    val buf = BufferedReader(InputStreamReader(FileInputStream("teammanagerbotLocalizations.json")))
                    val sb = StringBuilder()
                    var line = buf.readLine()
                    while (line != null) {
                        sb.append(line)
                        line = buf.readLine()
                    }
                    return sb.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
    }
}