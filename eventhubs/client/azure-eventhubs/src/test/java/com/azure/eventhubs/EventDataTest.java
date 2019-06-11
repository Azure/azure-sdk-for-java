// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.AmqpConstants;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Test;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EventDataTest {

    @Test(expected = NullPointerException.class)
    public void eventDataByteArrayNotNull() {
        byte[] byteArray = null;
        new EventData(byteArray);
    }

    @Test(expected = NullPointerException.class)
    public void eventDataByteArrayNotNullBuffer() {
        final ByteBuffer buffer = null;
        new EventData(buffer);
    }

    // TODO: re investigate this test, it contains SystemProperties inner class
    @Test
    public void sendingEventsSysPropsShouldNotBeNull() {
        EventData eventData = new EventData("Test".getBytes());
        Map sys = eventData.systemProperties();
        Assert.assertTrue(eventData.systemProperties() != null);
    }

    @Test
    public void eventDataEmptyByteArray() {
        ArrayList<EventData> messages = new ArrayList<>();

        EventData first = constructMessage(19);
        EventData second = constructMessage(22);
        EventData third = constructMessage(25);
        EventData last = constructMessage(88);

        messages.add(second);
        messages.add(first);
        messages.add(last);
        messages.add(third);

        Collections.sort(messages);

        Assert.assertEquals(messages.get(0), first);
        Assert.assertEquals(messages.get(1), second);
        Assert.assertEquals(messages.get(2), third);
        Assert.assertEquals(messages.get(3), last);
    }

    private EventData constructMessage(long seqNumber) {
        HashMap<Symbol, Object> properties = new HashMap<>();
        properties.put(AmqpConstants.SEQUENCE_NUMBER, seqNumber);

        Message message = Message.Factory.create();

        message.setMessageAnnotations(new MessageAnnotations(properties));

        return new EventData(message);
    }
}
