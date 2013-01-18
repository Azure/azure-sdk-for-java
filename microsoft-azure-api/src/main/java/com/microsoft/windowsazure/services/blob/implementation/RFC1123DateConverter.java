/**
 * Copyright Microsoft Corporation
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
package com.microsoft.windowsazure.services.blob.implementation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
 * RFC 1123 date to string conversion
 */
public class RFC1123DateConverter {
    private static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

    public String format(Date date) {
        return getFormat().format(date);
    }

    public Date parse(String date) {
        try {
            return getFormat().parse(date);
        }
        catch (ParseException e) {
            String msg = String.format("The value \"%s\" is not a valid RFC 1123 date.", date);
            throw new IllegalArgumentException(msg, e);
        }
    }

    private DateFormat getFormat() {
        DateFormat rfc1123Format = new SimpleDateFormat(RFC1123_PATTERN, Locale.US);
        rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return rfc1123Format;
    }
}
