package com.microsoft.windowsazure.services.serviceBus.implementation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class CustomPropertiesMapper {
    // Fri, 04 Mar 2011 08:49:37 GMT
    private static final String RFC_1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    public BrokerProperties fromString(String value) {
        throw new RuntimeException();
    }

    public String toString(Object value) {
        if (value == null)
            return null;

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
}
