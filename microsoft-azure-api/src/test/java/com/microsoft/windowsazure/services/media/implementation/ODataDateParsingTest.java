/**
 * Copyright 2011 Microsoft Corporation
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

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Tests around parsing dates - OData requires date
 * strings without timezones be treated as UTC.
 * ISO spec, and Java default, is to use local timezone.
 * So we need to plug in to tweak Jaxb to use OData
 * conventions.
 * 
 */
public class ODataDateParsingTest {

    @Test
    public void canConvertDateToString() throws Exception {
        TimeZone utc = TimeZone.getDefault();
        //utc.setRawOffset(0);

        Calendar sampleTime = new GregorianCalendar(2012, 11, 28, 17, 43, 12);
        sampleTime.setTimeZone(utc);

        Date sampleDate = sampleTime.getTime();

        String formatted = new ODataDateAdapter().marshal(sampleDate);

        assertTrue(formatted.contains("2012"));

    }

    @Test
    public void stringWithTimezoneRoundTripsCorrectly() throws Exception {
        String exampleDate = "2012-11-28T17:43:12-08:00";

        Date parsedTime = new ODataDateAdapter().unmarshal(exampleDate);

        TimeZone pst = TimeZone.getDefault();
        pst.setRawOffset(-8 * 60 * 60 * 1000);

        Calendar expectedTime = new GregorianCalendar(2012, 10, 28, 17, 43, 12);
        expectedTime.setTimeZone(pst);

        assertEquals(expectedTime.getTimeInMillis(), parsedTime.getTime());
    }

    @Test
    public void stringWithUTCTimezoneRoundTripsCorrectly() throws Exception {
        String exampleDate = "2012-11-28T17:43:12Z";

        Date parsedTime = new ODataDateAdapter().unmarshal(exampleDate);

        TimeZone utc = TimeZone.getDefault();
        utc.setRawOffset(0);

        Calendar expectedTime = new GregorianCalendar(2012, 10, 28, 17, 43, 12);
        expectedTime.setTimeZone(utc);

        assertEquals(expectedTime.getTimeInMillis(), parsedTime.getTime());
    }

    @Test
    public void stringWithNoTimezoneActsAsUTC() throws Exception {
        String exampleDate = "2012-11-28T17:43:12";

        Date parsedTime = new ODataDateAdapter().unmarshal(exampleDate);

        TimeZone utc = TimeZone.getDefault();
        utc.setRawOffset(0);

        Calendar expectedTime = new GregorianCalendar(2012, 10, 28, 17, 43, 12);
        expectedTime.setTimeZone(utc);

        assertEquals(expectedTime.getTimeInMillis(), parsedTime.getTime());
    }
}
