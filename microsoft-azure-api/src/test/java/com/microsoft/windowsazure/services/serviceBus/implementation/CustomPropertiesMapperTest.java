package com.microsoft.windowsazure.services.serviceBus.implementation;

import static org.junit.Assert.*;

import java.util.Calendar;
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
}
