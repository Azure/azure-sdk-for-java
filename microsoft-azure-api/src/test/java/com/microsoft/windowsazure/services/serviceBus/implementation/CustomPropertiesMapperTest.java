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
package com.microsoft.windowsazure.services.serviceBus.implementation;

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
    public void stringValuesShouldComeThroughUnmodified() {
        // Arrange

        // Act
        String text = mapper.toString("This is a string");

        // Assert
        assertEquals("This is a string", text);
    }

    @Test
    public void nonStringValuesShouldHaveTypeSuffix() {
        // Arrange

        // Act
        String text = mapper.toString(78);

        // Assert
        assertEquals("78;int", text);
    }

    @Test
    public void supportedJavaTypesHaveExpectedTypeSuffix() {
        // Arrange
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(1971, Calendar.OCTOBER, 14, 12, 34, 56);

        // Act

        // Assert
        //        assertEquals("78;byte", mapper.toString((byte) 78));
        assertEquals("78;byte", mapper.toString((byte) 78));
        assertEquals("a;char", mapper.toString('a'));
        assertEquals("-78;short", mapper.toString((short) -78));
        //      assertEquals("78;ushort", mapper.toString((unsigned short)78);
        assertEquals("-78;int", mapper.toString(-78));
        //     assertEquals("78;uint", mapper.toString(78));
        assertEquals("-78;long", mapper.toString((long) -78));
        //     assertEquals("78;ulong", mapper.toString(78));
        assertEquals("78.5;float", mapper.toString((float) 78.5));
        assertEquals("78.5;double", mapper.toString(78.5));
        //assertEquals("78;decimal", mapper.toString(78));
        assertEquals("true;bool", mapper.toString(true));
        assertEquals("false;bool", mapper.toString(false));
        assertEquals("12345678-9abc-def0-9abc-def012345678;uuid",
                mapper.toString(new UUID(0x123456789abcdef0L, 0x9abcdef012345678L)));
        assertEquals("Thu, 14 Oct 1971 12:34:56 GMT;date", mapper.toString(cal));
        assertEquals("Thu, 14 Oct 1971 12:34:56 GMT;date", mapper.toString(cal.getTime()));
        //assertEquals("78;date-seconds", mapper.toString(78));
    }

    @Test
    public void valuesComeBackAsStringsByDefault() throws ParseException {
        // Arrange

        // Act
        Object value = mapper.fromString("Hello world");

        // Assert
        assertEquals("Hello world", value);
        assertEquals(String.class, value.getClass());
    }

    @Test
    public void nonStringTypesWillBeParsedBySuffix() throws ParseException {
        // Arrange

        // Act
        Object value = mapper.fromString("5;int");

        // Assert
        assertEquals(5, value);
        assertEquals(Integer.class, value.getClass());
    }

    @Test
    public void unknownSuffixWillPassThroughAsString() throws ParseException {
        // Arrange

        // Act
        Object value = mapper.fromString("Hello;world");

        // Assert
        assertEquals("Hello;world", value);
        assertEquals(String.class, value.getClass());
    }

    @Test
    public void supportedTypeSuffixesHaveExpectedJavaTypes() throws ParseException {
        // Arrange
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(1971, Calendar.OCTOBER, 14, 12, 34, 56);

        // Act
        Date dt = (Date) mapper.fromString("Thu, 14 Oct 1971 12:34:56 GMT;date");

        // Assert
        //        assertEquals("78;byte", mapper.toString((byte) 78));
        assertEquals((byte) 78, mapper.fromString("78;byte"));
        assertEquals('a', mapper.fromString("a;char"));
        assertEquals((short) -78, mapper.fromString("-78;short"));
        //      assertEquals("78;ushort", mapper.toString((unsigned short)78);
        assertEquals(-78, mapper.fromString("-78;int"));
        //     assertEquals("78;uint", mapper.toString(78));
        assertEquals((long) -78, mapper.fromString("-78;long"));
        //     assertEquals("78;ulong", mapper.toString(78));
        assertEquals((float) 78.5, mapper.fromString("78.5;float"));
        assertEquals(78.5, mapper.fromString("78.5;double"));
        //assertEquals("78;decimal", mapper.toString(78));
        assertEquals(true, mapper.fromString("true;bool"));
        assertEquals(false, mapper.fromString("false;bool"));
        assertEquals(new UUID(0x123456789abcdef0L, 0x9abcdef012345678L),
                mapper.fromString("12345678-9abc-def0-9abc-def012345678;uuid"));

        assertEquals(cal.getTime().getTime(), dt.getTime(), 1000);
        //assertEquals("78;date-seconds", mapper.toString(78));
    }
}
