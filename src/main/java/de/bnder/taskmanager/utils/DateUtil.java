package de.bnder.taskmanager.utils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateUtil {

    private static final List<SimpleDateFormat> dateFormats = new ArrayList<>() {{
        add(new SimpleDateFormat("dd.MM.yyyy HH:mm"));
        add(new SimpleDateFormat("dd.MM.yy HH:mm"));
        add(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"));
        add(new SimpleDateFormat("dd.MM.yy HH:mm:ss"));
        add(new SimpleDateFormat("dd.MM.yyyy"));
        add(new SimpleDateFormat("dd.MM.yy"));
        add(new SimpleDateFormat("yyyy-MM-dd HH:mm"));
        add(new SimpleDateFormat("yy-MM-dd HH:mm"));
        add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        add(new SimpleDateFormat("yy-MM-dd HH:mm:ss"));
        add(new SimpleDateFormat("yyyy-MM-dd"));
        add(new SimpleDateFormat("yy-MM-dd"));
        add(new SimpleDateFormat("yyyy/MM/dd"));
        add(new SimpleDateFormat("yy/MM/dd"));
        add(new SimpleDateFormat("yyyy/MM/dd HH:mm"));
        add(new SimpleDateFormat("yy/MM/dd HH:mm"));
        add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
        add(new SimpleDateFormat("yy/MM/dd HH:mm:ss"));
        add(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"));
        add(new SimpleDateFormat("MM/dd/yyyy HH:mm"));
        add(new SimpleDateFormat("MM/dd/yyyy"));
    }};

    public static Date convertToDate(String input) {
        Date date = null;
        if (input == null) {
            return null;
        }
        for (SimpleDateFormat format : dateFormats) {
            try {
                format.setLenient(false);
                date = format.parse(input);
            } catch (ParseException ignored) {
            }
            if (date != null) {
                break;
            }
        }
        return date;
    }

}
