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
package com.microsoft.windowsazure.services.serviceBus;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import com.microsoft.windowsazure.services.serviceBus.implementation.BrokerProperties;
import com.microsoft.windowsazure.services.serviceBus.implementation.BrokerPropertiesMapper;

public class BrokerPropertiesMapperTest {
    @Test
    public void jsonStringMapsToBrokerPropertiesObject() {
        // Arrange 
        BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();

        // Act
        BrokerProperties properties = mapper.fromString("{\"DeliveryCount\":5,\"MessageId\":\"something\"}");

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

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(1994, 10, 6, 8, 49, 37);
        Date schedTimeUtc = calendar.getTime();

        Calendar calendar2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar2.set(2011, 9, 14, 12, 34, 56);
        Date lockedUntilUtc = calendar2.getTime();

        // Act
        BrokerProperties properties = mapper.fromString(
                "{" +
                        "\"CorrelationId\": \"corid\"," +
                        "\"SessionId\": \"sesid\"," +
                        "\"DeliveryCount\": 5," +
                        "\"LockedUntilUtc\": \" Fri, 14 Oct 2011 12:34:56 GMT\"," +
                        "\"LockToken\": \"loctok\"," +
                        "\"MessageId\": \"mesid\"," +
                        "\"Label\": \"lab\"," +
                        "\"ReplyTo\": \"repto\"," +
                        "\"SequenceNumber\": 7," +
                        "\"TimeToLive\": 8.123," +
                        "\"To\": \"to\"," +
                        "\"ScheduledEnqueueTimeUtc\": \" Sun, 06 Nov 1994 08:49:37 GMT\"," +
                        "\"ReplyToSessionId\": \"reptosesid\"," +
                        "\"MessageLocation\": \"mesloc\"," +
                        "\"LockLocation\": \"locloc\"" +
                        "}");

        // Assert
        assertNotNull(properties);

        long lockedUntilDelta = properties.getLockedUntilUtc().getTime() - lockedUntilUtc.getTime();
        long schedTimeDelta = properties.getScheduledEnqueueTimeUtc().getTime() - schedTimeUtc.getTime();

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
}
