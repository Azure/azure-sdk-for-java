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

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests around parsing dates - OData requires date strings without timezones be
 * treated as UTC. ISO spec, and Java default, is to use local timezone. So we
 * need to plug in to tweak Jaxb to use OData conventions.
 * 
 */
public class ODataDateParsingTest {
    private static TimeZone utc;
    private static TimeZone pst;

    @BeforeClass
    public static void setupClass() {
        utc = TimeZone.getDefault();
        utc.setRawOffset(0);

        pst = TimeZone.getDefault();
        pst.setRawOffset(-8 * 60 * 60 * 1000);
    }

    @Test
    public void canConvertDateToString() throws Exception {

        Calendar sampleTime = new GregorianCalendar(2012, 11, 28, 17, 43, 12);
        sampleTime.setTimeZone(utc);

        Date sampleDate = sampleTime.getTime();

        String formatted = new ODataDateAdapter().marshal(sampleDate);

        assertEquals("2012-12-28T17:43:12Z", formatted);

    }

    @Test
    public void stringWithTimezoneRoundTripsCorrectly() throws Exception {
        String exampleDate = "2012-11-28T17:43:12-08:00";

        Date parsedTime = new ODataDateAdapter().unmarshal(exampleDate);

        Calendar expectedTime = new GregorianCalendar(2012, 10, 28, 17, 43, 12);
        expectedTime.setTimeZone(pst);

        assertEquals(expectedTime.getTimeInMillis(), parsedTime.getTime());
    }

    @Test
    public void stringWithUTCTimezoneRoundTripsCorrectly() throws Exception {
        String exampleDate = "2012-11-28T17:43:12Z";

        Date parsedTime = new ODataDateAdapter().unmarshal(exampleDate);

        Calendar expectedTime = new GregorianCalendar(2012, 10, 28, 17, 43, 12);
        expectedTime.setTimeZone(utc);

        assertEquals(expectedTime.getTimeInMillis(), parsedTime.getTime());
    }

    @Test
    public void stringWithNoTimezoneActsAsUTC() throws Exception {
        String exampleDate = "2012-11-28T17:43:12";

        Date parsedTime = new ODataDateAdapter().unmarshal(exampleDate);

        Calendar expectedTime = new GregorianCalendar(2012, 10, 28, 17, 43, 12);
        expectedTime.setTimeZone(utc);

        assertEquals(expectedTime.getTimeInMillis(), parsedTime.getTime());
    }

    @Test
    public void stringWithFractionalTimeReturnsCorrectMillisecondsTo100nsBoundary()
            throws Exception {
        String exampleDate = "2012-11-28T17:43:12.1234567Z";

        Date parsedTime = new ODataDateAdapter().unmarshal(exampleDate);

        Calendar timeToNearestSecond = Calendar.getInstance();
        timeToNearestSecond.setTimeZone(utc);
        timeToNearestSecond.set(2012, 10, 28, 17, 43, 12);
        timeToNearestSecond.set(Calendar.MILLISECOND, 0);

        long millis = parsedTime.getTime()
                - timeToNearestSecond.getTimeInMillis();

        assertEquals(123, millis);
    }

    @Test
    public void stringWithFractionalTimeReturnsCorrectMillisecondsAsFractionNotCount()
            throws Exception {
        String exampleDate = "2012-11-28T17:43:12.1Z";

        Date parsedTime = new ODataDateAdapter().unmarshal(exampleDate);

        Calendar timeToNearestSecond = Calendar.getInstance();
        timeToNearestSecond.setTimeZone(utc);
        timeToNearestSecond.set(2012, 10, 28, 17, 43, 12);
        timeToNearestSecond.set(Calendar.MILLISECOND, 0);

        long millis = parsedTime.getTime()
                - timeToNearestSecond.getTimeInMillis();
        assertEquals(100, millis);

    }

    @Test
    public void stringWithFractionalSecondsAndTimezoneOffsetParses()
            throws Exception {
        String exampleDate = "2012-11-28T17:43:12.1-08:00";
        Date parsedTime = new ODataDateAdapter().unmarshal(exampleDate);

        Calendar expectedTime = new GregorianCalendar(2012, 10, 28, 17, 43, 12);
        expectedTime.setTimeZone(pst);
        expectedTime.set(Calendar.MILLISECOND, 100);

        assertEquals(expectedTime.getTimeInMillis(), parsedTime.getTime());

    }
}
