/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.servicebus.implementation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CustomPropertiesMapper {
    // Fri, 04 Mar 2011 08:49:37 GMT
    private static final String RFC_1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    public String toString(Object value) {
        if (value == null) {
            return null;
        }

        Class<? extends Object> type = value.getClass();
        if (type == Byte.class) {
            return value.toString();
        } else if (type == Short.class) {
            return value.toString();
        } else if (type == Integer.class) {
            return value.toString();
        } else if (type == Long.class) {
            return value.toString();
        } else if (type == Float.class) {
            return value.toString();
        } else if (type == Double.class) {
            return value.toString();
        } else if (type == Boolean.class) {
            return value.toString();
        } else if (Calendar.class.isAssignableFrom(type)) {
            DateFormat format = new SimpleDateFormat(RFC_1123, Locale.US);
            Calendar calendar = (Calendar) value;
            format.setTimeZone(calendar.getTimeZone());
            String formatted = format.format(calendar.getTime());
            return "\"" + formatted + "\"";
        } else if (Date.class.isAssignableFrom(type)) {
            DateFormat format = new SimpleDateFormat(RFC_1123, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String formatted = format.format((Date) value);
            return "\"" + formatted + "\"";
        } else {
            return "\"" + value.toString() + "\"";
        }
    }

    public Object fromString(String value) throws ParseException {
        if (value == null) {
            return null;
        }

        if (value.startsWith("\"") && value.endsWith("\"")) {
            String text = value.substring(1, value.length() - 1);
            if (isRFC1123(text)) {
                SimpleDateFormat format = new SimpleDateFormat(RFC_1123,
                        Locale.US);
                return format.parse(text);
            }

            return text;
        } else if ("true".equals(value)) {
            return Boolean.TRUE;
        } else if ("false".equals(value)) {
            return Boolean.FALSE;
        } else if (isInteger(value)) {
            return Integer.parseInt(value);
        } else {
            return Double.parseDouble(value);
        }
    }

    private boolean isRFC1123(String text) {
        if (text.length() != RFC_1123.length()) {
            return false;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(RFC_1123, Locale.US);
            format.parse(text);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
