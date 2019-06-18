// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.MessageConstant;
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
        EventData eventData = new EventData("Test".getBytes());
        Assert.assertTrue(eventData.systemProperties() != null);
        Assert.assertTrue(eventData.body() != null);
        Assert.assertTrue(eventData.properties() != null);
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
        properties.put(Symbol.getSymbol(MessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()), seqNumber);

        Message message = Message.Factory.create();
        message.setMessageAnnotations(new MessageAnnotations(properties));
        return new EventData(message);
    }
}
