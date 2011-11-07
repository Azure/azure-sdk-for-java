package com.microsoft.azure.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ISO8601DateConverter {
    private static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'.0000000Z'";

    public String format(Date date) {
        return getFormat().format(date);
    }

    public Date parse(String date) throws ParseException {
        return getFormat().parse(date);
    }

    public Date parseNoThrow(String date) {
        try {
            return parse(date);
        }
        catch (ParseException e) {
            //TODO: Is it better to return null or throw a runtime exception?
            return null;
        }
    }

    private DateFormat getFormat() {
        DateFormat iso8601Format = new SimpleDateFormat(ISO8601_PATTERN, Locale.US);
        iso8601Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return iso8601Format;
    }
}
