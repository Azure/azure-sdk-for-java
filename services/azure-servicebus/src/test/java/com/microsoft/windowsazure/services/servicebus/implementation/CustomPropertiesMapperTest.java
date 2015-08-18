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
package com.microsoft.windowsazure.services.servicebus.implementation;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class CustomPropertiesMapperTest {
    private CustomPropertiesMapper mapper;

    @Before
    public void init() {
        mapper = new CustomPropertiesMapper();
    }

    @Test
    public void stringValuesShouldComeThroughInQuotes() {
        // Arrange

        // Act
        String text = mapper.toString("This is a string");

        // Assert
        assertEquals("\"This is a string\"", text);
    }

    @Test
    public void nonStringValuesShouldNotHaveQuotes() {
        // Arrange

        // Act
        String text = mapper.toString(78);

        // Assert
        assertEquals("78", text);
    }

    @Test
    public void supportedJavaTypesHaveExpectedRepresentations() {
        // Arrange
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(1971, Calendar.OCTOBER, 14, 12, 34, 56);

        // Act

        // Assert
        // assertEquals("78;byte", mapper.toString((byte) 78));
        assertEquals("78", mapper.toString((byte) 78));
        assertEquals("\"a\"", mapper.toString('a'));
        assertEquals("-78", mapper.toString((short) -78));
        // assertEquals("78;ushort", mapper.toString((unsigned short)78);
        assertEquals("-78", mapper.toString(-78));
        // assertEquals("78;uint", mapper.toString(78));
        assertEquals("-78", mapper.toString((long) -78));
        // assertEquals("78;ulong", mapper.toString(78));
        assertEquals("78.5", mapper.toString((float) 78.5));
        assertEquals("78.5", mapper.toString(78.5));
        // assertEquals("78;decimal", mapper.toString(78));
        assertEquals("true", mapper.toString(true));
        assertEquals("false", mapper.toString(false));
        assertEquals("\"12345678-9abc-def0-9abc-def012345678\"",
                mapper.toString(new UUID(0x123456789abcdef0L,
                        0x9abcdef012345678L)));
        assertEquals("\"Thu, 14 Oct 1971 12:34:56 GMT\"", mapper.toString(cal));
        assertEquals("\"Thu, 14 Oct 1971 12:34:56 GMT\"",
                mapper.toString(cal.getTime()));
        // assertEquals("78;date-seconds", mapper.toString(78));
    }

    @Test
    public void valuesComeBackAsStringsWhenInQuotes() throws ParseException {
        // Arrange

        // Act
        Object value = mapper.fromString("\"Hello world\"");

        // Assert
        assertEquals("Hello world", value);
        assertEquals(String.class, value.getClass());
    }

    @Test
    public void nonStringTypesWillBeParsedAsNumeric() throws ParseException {
        // Arrange

        // Act
        Object value = mapper.fromString("5");

        // Assert
        assertEquals(5, value);
        assertEquals(Integer.class, value.getClass());
    }

    @Test
    public void supportedFormatsHaveExpectedJavaTypes() throws ParseException {
        // Arrange
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(1971, Calendar.OCTOBER, 14, 12, 34, 56);

        // Act
        Date dt = (Date) mapper.fromString("\"Thu, 14 Oct 1971 12:34:56 GMT\"");

        // Assert
        // assertEquals("78;byte", mapper.toString((byte) 78));
        // assertEquals((byte) 78, mapper.fromString("78"));
        // assertEquals('a', mapper.fromString("a;char"));
        // assertEquals((short) -78, mapper.fromString("-78;short"));
        // assertEquals("78;ushort", mapper.toString((unsigned short)78);
        assertEquals(-78, mapper.fromString("-78"));
        // assertEquals("78;uint", mapper.toString(78));
        // assertEquals((long) -78, mapper.fromString("-78;long"));
        // assertEquals("78;ulong", mapper.toString(78));
        // assertEquals((float) 78.5, mapper.fromString("78.5;float"));
        assertEquals(78.5, mapper.fromString("78.5"));
        // assertEquals("78;decimal", mapper.toString(78));
        assertEquals(true, mapper.fromString("true"));
        assertEquals(false, mapper.fromString("false"));
        // assertEquals(new UUID(0x123456789abcdef0L, 0x9abcdef012345678L),
        // mapper.fromString("12345678-9abc-def0-9abc-def012345678;uuid"));

        assertEquals(cal.getTime().getTime(), dt.getTime(), 1000);
        // assertEquals("78;date-seconds", mapper.toString(78));
    }
}
