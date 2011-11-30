/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.serviceBus.implementation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class CustomPropertiesMapper {
    // Fri, 04 Mar 2011 08:49:37 GMT
    private static final String RFC_1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    public String toString(Object value) {
        if (value == null) {
            return null;
        }

        Class<? extends Object> type = value.getClass();
        if (type == Byte.class) {
            return value.toString() + ";byte";
        }
        else if (type == Character.class) {
            return value.toString() + ";char";
        }
        else if (type == Short.class) {
            return value.toString() + ";short";
        }
        else if (type == Integer.class) {
            return value.toString() + ";int";
        }
        else if (type == Long.class) {
            return value.toString() + ";long";
        }
        else if (type == Float.class) {
            return value.toString() + ";float";
        }
        else if (type == Double.class) {
            return value.toString() + ";double";
        }
        else if (type == Boolean.class) {
            return value.toString() + ";bool";
        }
        else if (type == UUID.class) {
            return value.toString() + ";uuid";
        }
        else if (Calendar.class.isAssignableFrom(type)) {
            DateFormat format = new SimpleDateFormat(RFC_1123);
            Calendar calendar = (Calendar) value;
            format.setTimeZone(calendar.getTimeZone());
            String formatted = format.format(calendar.getTime());
            return formatted + ";date";
        }
        else if (Date.class.isAssignableFrom(type)) {
            DateFormat format = new SimpleDateFormat(RFC_1123);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String formatted = format.format((Date) value);
            return formatted + ";date";
        }
        else {
            return value.toString();
        }
    }

    public Object fromString(String value) throws ParseException {
        if (value == null) {
            return null;
        }

        if (value.endsWith(";byte")) {
            return Byte.parseByte(value.substring(0, value.length() - ";byte".length()));
        }
        else if (value.endsWith(";char") && value.length() == "X;char".length()) {
            return new Character(value.charAt(0));
        }
        else if (value.endsWith(";short")) {
            return Short.parseShort(value.substring(0, value.length() - ";short".length()));
        }
        else if (value.endsWith(";int")) {
            return Integer.parseInt(value.substring(0, value.length() - ";int".length()));
        }
        else if (value.endsWith(";long")) {
            return Long.parseLong(value.substring(0, value.length() - ";long".length()));
        }
        else if (value.endsWith(";float")) {
            return Float.parseFloat(value.substring(0, value.length() - ";float".length()));
        }
        else if (value.endsWith(";double")) {
            return Double.parseDouble(value.substring(0, value.length() - ";double".length()));
        }
        else if (value.endsWith(";bool")) {
            return Boolean.parseBoolean(value.substring(0, value.length() - ";bool".length()));
        }
        else if (value.endsWith(";uuid")) {
            return UUID.fromString(value.substring(0, value.length() - ";uuid".length()));
        }
        else if (value.endsWith(";date")) {
            SimpleDateFormat format = new SimpleDateFormat(RFC_1123);
            return format.parse(value.substring(0, value.length() - ";date".length()));
        }
        else {
            return value;
        }
    }
}
