/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventPosition;
import org.apache.qpid.proton.message.Message;

import java.util.*;
import java.util.function.Consumer;

final class EventDataUtil {

    @SuppressWarnings("serial")
    static final Set<String> RESERVED_SYSTEM_PROPERTIES = Collections.unmodifiableSet(new HashSet<String>() {{
        add(AmqpConstants.OFFSET_ANNOTATION_NAME);
        add(AmqpConstants.PARTITION_KEY_ANNOTATION_NAME);
        add(AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME);
        add(AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME);
        add(AmqpConstants.PUBLISHER_ANNOTATION_NAME);
    }});

    private EventDataUtil() {
    }

    static LinkedList<EventData> toEventDataCollection(final Collection<Message> messages, final PassByRef<MessageWrapper> lastMessageRef) {

        if (messages == null) {
            return null;
        }

        LinkedList<EventData> events = new LinkedList<>();
        for (Message message : messages) {
            EventData eventData = new EventDataImpl(message);
            events.add(eventData);

            if (lastMessageRef != null) {
                lastMessageRef.set(new MessageWrapper(message,
                        EventPosition.fromSequenceNumber(eventData.getSystemProperties().getSequenceNumber(), true)));
            }
        }

        return events;
    }

    static Iterable<Message> toAmqpMessages(final Iterable<EventData> eventDatas, final String partitionKey) {

        final LinkedList<Message> messages = new LinkedList<>();
        eventDatas.forEach(new Consumer<EventData>() {
            @Override
            public void accept(EventData eventData) {
                EventDataImpl eventDataImpl = (EventDataImpl) eventData;
                Message amqpMessage = partitionKey == null ? eventDataImpl.toAmqpMessage() : eventDataImpl.toAmqpMessage(partitionKey);
                messages.add(amqpMessage);
            }
        });

        return messages;
    }

    static Iterable<Message> toAmqpMessages(final Iterable<EventData> eventDatas) {

        return EventDataUtil.toAmqpMessages(eventDatas, null);
    }
}
