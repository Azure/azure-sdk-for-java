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
package com.microsoft.windowsazure.services.servicebus;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.servicebus.implementation.BrokerProperties;
import com.microsoft.windowsazure.services.servicebus.implementation.BrokerPropertiesMapper;

public class BrokerPropertiesMapperTest {

    private final String testBrokerPropertiesString = "{"
            + "\"CorrelationId\": \"corid\","
            + "\"SessionId\": \"sesid\","
            + "\"DeliveryCount\": 5,"
            + "\"LockedUntilUtc\": \" Fri, 14 Oct 2011 12:34:56 GMT\","
            + "\"LockToken\": \"loctok\","
            + "\"MessageId\": \"mesid\","
            + "\"Label\": \"lab\","
            + "\"ReplyTo\": \"repto\","
            + "\"SequenceNumber\": 7,"
            + "\"TimeToLive\": 8.123,"
            + "\"To\": \"to\","
            + "\"ScheduledEnqueueTimeUtc\": \" Sun, 06 Nov 1994 08:49:37 GMT\","
            + "\"ReplyToSessionId\": \"reptosesid\","
            + "\"MessageLocation\": \"mesloc\","
            + "\"LockLocation\": \"locloc\"" + "}";

    private static Date schedTimeUtc, lockedUntilUtc;

    @BeforeClass
    public static void setup() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(1994, 10, 6, 8, 49, 37);
        schedTimeUtc = calendar.getTime();

        Calendar calendar2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar2.set(2011, 9, 14, 12, 34, 56);
        lockedUntilUtc = calendar2.getTime();

    }

    @Test
    public void jsonStringMapsToBrokerPropertiesObject() {
        // Arrange
        BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();

        // Act
        BrokerProperties properties = mapper
                .fromString("{\"DeliveryCount\":5,\"MessageId\":\"something\"}");

        // Assert
        assertNotNull(properties);
        assertEquals(new Integer(5), properties.getDeliveryCount());
        assertEquals("something", properties.getMessageId());
    }

    @Test
    public void nonDefaultPropertiesMapToJsonString() {
        // Arrange
        BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();

        // Act
        BrokerProperties properties = new BrokerProperties();
        properties.setMessageId("foo");
        properties.setDeliveryCount(7);
        String json = mapper.toString(properties);

        // Assert
        assertNotNull(json);
        assertEquals("{\"DeliveryCount\":7,\"MessageId\":\"foo\"}", json);
    }

    @Test
    public void deserializingAllPossibleValues() {
        // Arrange
        BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();

        // Act
        BrokerProperties properties = mapper
                .fromString(testBrokerPropertiesString);

        // Assert
        assertNotNull(properties);

        long lockedUntilDelta = properties.getLockedUntilUtc().getTime()
                - lockedUntilUtc.getTime();
        long schedTimeDelta = properties.getScheduledEnqueueTimeUtc().getTime()
                - schedTimeUtc.getTime();

        assertEquals("corid", properties.getCorrelationId());
        assertEquals("sesid", properties.getSessionId());
        assertEquals(5, (int) properties.getDeliveryCount());
        assertTrue(Math.abs(lockedUntilDelta) < 2000);
        assertEquals("loctok", properties.getLockToken());
        assertEquals("mesid", properties.getMessageId());
        assertEquals("lab", properties.getLabel());
        assertEquals("repto", properties.getReplyTo());
        assertEquals(7, (long) properties.getSequenceNumber());
        assertEquals(8.123, properties.getTimeToLive(), .001);
        assertEquals("to", properties.getTo());
        assertTrue(Math.abs(schedTimeDelta) < 2000);
        assertEquals("reptosesid", properties.getReplyToSessionId());
        assertEquals("mesloc", properties.getMessageLocation());
        assertEquals("locloc", properties.getLockLocation());
    }

    @Test
    public void missingDatesDeserializeAsNull() {
        // Arrange
        BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();

        // Act
        BrokerProperties properties = mapper.fromString("{}");

        // Assert
        assertNull(properties.getLockedUntilUtc());
        assertNull(properties.getScheduledEnqueueTimeUtc());
    }

    @Test
    public void deserializeDateInKrKrLocaleCorrectly() {
        // Arrange
        BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.KOREA);

        // Act
        BrokerProperties brokerProperties = mapper
                .fromString(testBrokerPropertiesString);
        Locale.setDefault(defaultLocale);

        // Assert
        long lockedUntilDelta = brokerProperties.getLockedUntilUtc().getTime()
                - lockedUntilUtc.getTime();
        assertTrue(Math.abs(lockedUntilDelta) < 2000);
    }

    @Test
    public void deserializeDateInEnUsLocaleCorrectly() {
        // Arrange
        BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        // Act
        BrokerProperties brokerProperties = mapper
                .fromString(testBrokerPropertiesString);
        Locale.setDefault(defaultLocale);

        // Assert
        long lockedUntilDelta = brokerProperties.getLockedUntilUtc().getTime()
                - lockedUntilUtc.getTime();
        assertTrue(Math.abs(lockedUntilDelta) < 2000);

    }

    @Test
    public void deserializeDateInZhCnLocaleCorrectly() {
        // Arrange
        BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.CHINA);

        // Act
        BrokerProperties brokerProperties = mapper
                .fromString(testBrokerPropertiesString);
        Locale.setDefault(defaultLocale);

        // Assert
        long lockedUntilDelta = brokerProperties.getLockedUntilUtc().getTime()
                - lockedUntilUtc.getTime();
        assertTrue(Math.abs(lockedUntilDelta) < 2000);
    }
    
    @Test
    public void dateSerializeCorrectlyToRFC2616() {
    	// Arrange
        BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();
        Date date = new Date(1432120118000L);

        // Act
        BrokerProperties properties = new BrokerProperties();
        properties.setScheduledEnqueueTimeUtc(date);
        String json = mapper.toString(properties);

        // Assert
        assertNotNull(json);
        assertEquals("{\"ScheduledEnqueueTimeUtc\":\"Wed, 20 May 2015 11:08:38 GMT\"}", json);
    	
    }
}
