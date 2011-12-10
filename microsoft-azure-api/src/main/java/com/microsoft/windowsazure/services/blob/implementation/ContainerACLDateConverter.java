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
package com.microsoft.windowsazure.services.blob.implementation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
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
            return null;
        }
    }

    private DateFormat getFormat() {
        DateFormat iso8601Format = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        iso8601Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return iso8601Format;
    }
}
