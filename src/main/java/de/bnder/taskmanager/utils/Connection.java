package de.bnder.taskmanager.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Connection {

    public static int timeout = 30000;

    public static String encodeString(String toEncode) {
        try {
            if (toEncode!= null) {
                return URLEncoder.encode(toEncode, StandardCharsets.UTF_8.toString());
            }
        } catch (Exception ignored) {

        }
        return toEncode;
    }

}
