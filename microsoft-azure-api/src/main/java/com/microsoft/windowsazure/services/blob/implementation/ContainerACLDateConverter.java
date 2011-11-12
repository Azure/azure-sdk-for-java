package com.microsoft.windowsazure.services.blob.implementation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
 * TODO: Move to some other common package?
 * 
 * "not quite" ISO 8601 date time conversion routines
 */
public class ContainerACLDateConverter {
    // Note: because of the trailing "0000000", this is not quite ISO 8601 compatible
    private static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";

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
        DateFormat iso8601Format = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        iso8601Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return iso8601Format;
    }
}
