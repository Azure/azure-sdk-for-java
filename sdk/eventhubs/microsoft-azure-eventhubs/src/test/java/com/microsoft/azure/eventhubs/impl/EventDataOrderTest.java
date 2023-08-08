// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.microsoft.azure.eventhubs.EventData;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Test;

public class EventDataOrderTest {

    private EventData constructMessage(long seqNumber) {
        HashMap<Symbol, Object> properties = new HashMap<>();
        properties.put(AmqpConstants.SEQUENCE_NUMBER, seqNumber);

        Message message = Message.Factory.create();

        message.setMessageAnnotations(new MessageAnnotations(properties));

        return new EventDataImpl(message);
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
}
