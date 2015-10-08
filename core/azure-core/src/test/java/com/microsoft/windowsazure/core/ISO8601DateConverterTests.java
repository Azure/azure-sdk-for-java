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
package com.microsoft.windowsazure.core;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ISO8601DateConverterTests {
    @Test
    public void shortFormatWorks() throws Exception {
        // Arrange
        ISO8601DateConverter converter = new ISO8601DateConverter();
        String value = "2012-01-12T00:35:58Z";

        // Act
        Date result = converter.parse(value);
        String value2 = converter.format(result);

        // Assert
        assertNotNull(result);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals("Year", 2012, calendar.get(Calendar.YEAR));
        assertEquals("Month", 1, calendar.get(Calendar.MONTH) + 1);
        assertEquals("Day", 12, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("Hour", 0, calendar.get(Calendar.HOUR));
        assertEquals("Minute", 35, calendar.get(Calendar.MINUTE));
        assertEquals("Second", 58, calendar.get(Calendar.SECOND));
        assertEquals("Millisecond", 0, calendar.get(Calendar.MILLISECOND));

        assertEquals("2012-01-12T00:35:58.000Z", value2);
    }

    @Test
    public void longFormatWorks() throws Exception {
        // Arrange
        ISO8601DateConverter converter = new ISO8601DateConverter();
        String value = "2012-01-12T00:35:58.1234567Z";

        // Act
        Date result = converter.parse(value);
        String value2 = converter.format(result);

        // Assert
        assertNotNull(result);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals("Year", 2012, calendar.get(Calendar.YEAR));
        assertEquals("Month", 1, calendar.get(Calendar.MONTH) + 1);
        assertEquals("Day", 12, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("Hour", 0, calendar.get(Calendar.HOUR));
        assertEquals("Minute", 35, calendar.get(Calendar.MINUTE));
        assertEquals("Second", 58, calendar.get(Calendar.SECOND));
        assertEquals("Millisecond", 123, calendar.get(Calendar.MILLISECOND));

        assertEquals("2012-01-12T00:35:58.123Z", value2);
    }

    @Test
    public void mixedFormatWorks() throws Exception {
        // Arrange
        ISO8601DateConverter converter = new ISO8601DateConverter();
        String value = "2012-01-12T00:35:58.12Z";

        // Act
        Date result = converter.parse(value);
        String value2 = converter.format(result);

        // Assert
        assertNotNull(result);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals("Year", 2012, calendar.get(Calendar.YEAR));
        assertEquals("Month", 1, calendar.get(Calendar.MONTH) + 1);
        assertEquals("Day", 12, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("Hour", 0, calendar.get(Calendar.HOUR));
        assertEquals("Minute", 35, calendar.get(Calendar.MINUTE));
        assertEquals("Second", 58, calendar.get(Calendar.SECOND));
        assertEquals("Millisecond", 120, calendar.get(Calendar.MILLISECOND));

        assertEquals("2012-01-12T00:35:58.120Z", value2);
    }

    @Test
    public void shortFormatRoundTrips() throws Exception {
        // Arrange
        ISO8601DateConverter converter = new ISO8601DateConverter();
        String value = "2012-01-12T00:35:58Z";

        // Act
        Date result = converter.parse(value);
        String value2 = converter.shortFormat(result);
        String value3 = converter.format(result);

        // Assert
        assertNotNull(result);
        assertEquals(value, value2);
        assertEquals("2012-01-12T00:35:58.000Z", value3);
    }
}
