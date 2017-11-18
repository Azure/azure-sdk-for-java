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

package com.microsoft.windowsazure.services.media.implementation;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter to convert OData time zone conventions into Java Dates and back.
 * 
 */
public class ODataDateAdapter extends XmlAdapter<String, Date> {

    private static final Pattern HAS_TIMEZONE_REGEX;
    private static final TimeZone UTC;

    static {
        HAS_TIMEZONE_REGEX = Pattern.compile("^.*(\\+|-)\\d\\d:\\d\\d$");

        UTC = TimeZone.getDefault();
        UTC.setRawOffset(0);
    }

    @Override
    public Date unmarshal(String dateString) throws Exception {
        if (!hasTimezone(dateString)) {
            dateString += "Z";
        }
        Calendar parsedDate = DatatypeConverter.parseDateTime(dateString);
        return parsedDate.getTime();
    }

    @Override
    public String marshal(Date date) throws Exception {
        Calendar dateToMarshal = Calendar.getInstance();
        dateToMarshal.setTime(date);
        dateToMarshal.setTimeZone(UTC);
        return DatatypeConverter.printDateTime(dateToMarshal);
    }

    private boolean hasTimezone(String dateString) {
        return dateString.endsWith("Z")
                || HAS_TIMEZONE_REGEX.matcher(dateString).matches();
    }
}
