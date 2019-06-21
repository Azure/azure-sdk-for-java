// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.MessageConstant;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Test;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class EventDataTest {

    @Test(expected = NullPointerException.class)
    public void eventDataByteArrayNotNull() {
        new EventData((byte[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void eventDataByteArrayNotNullBuffer() {
        new EventData((ByteBuffer) null);
    }

    @Test
    public void sendingEventsPropsShouldNotBeNull() {
        final EventData eventData = new EventData("Test".getBytes());
        Assert.assertNotNull(eventData.systemProperties());
        Assert.assertNotNull(eventData.body());
        Assert.assertNotNull(eventData.properties());
    }

    @Test
    public void eventDataEmptyByteArray() {
        final ArrayList<EventData> messages = new ArrayList<>();

        final EventData first = constructMessage(19);
        final EventData second = constructMessage(22);
        final EventData third = constructMessage(25);
        final EventData last = constructMessage(88);

        messages.add(second);
        messages.add(first);
        messages.add(last);
        messages.add(third);

        Collections.sort(messages, (EventData o1, EventData o2) -> o1.sequenceNumber() - o2.sequenceNumber() > 0L ? 1 : -1);

        Assert.assertTrue(messages.get(0) == first);
        Assert.assertTrue(messages.get(1) == second);
        Assert.assertTrue(messages.get(2) == third);
        Assert.assertTrue(messages.get(3) == last);
    }

    private EventData constructMessage(long seqNumber) {
        final HashMap<Symbol, Object> properties = new HashMap<>();
        properties.put(Symbol.getSymbol(MessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()), seqNumber);

        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(properties));
        return new EventData(message);
    }
}
