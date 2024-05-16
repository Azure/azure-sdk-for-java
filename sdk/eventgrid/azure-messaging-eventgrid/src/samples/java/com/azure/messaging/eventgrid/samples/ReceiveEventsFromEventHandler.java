// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.samples.models.User;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.QueueMessageItem;

import java.util.Base64;
import java.util.List;

/**
 * An Event Grid Domain or Topic Subscription can use other Azure Services such as Event Hubs, Service Bus,
 * Storage Queue. This sample uses Storage Queue as the event handler to store the events sent to an Event Grid Topic.
 */
public class ReceiveEventsFromEventHandler {
    public static void main(String[] args) {
        QueueClient storageQueueClient = new QueueClientBuilder()
            .connectionString(System.getenv("AZURE_STORAGE_QUEUE_CONNECTION_STRING"))
            .queueName(System.getenv("AZURE_STORAGE_QUEUE_NAME_FOR_EVENTGRID"))
            .buildClient();

        Iterable<QueueMessageItem> messages = storageQueueClient.receiveMessages(10);
        for (QueueMessageItem messageItem : messages) {
            String eventJsonString = new String(Base64.getDecoder().decode(messageItem.getMessageText()));
            deserializeAndProcessEvents(eventJsonString);
        }
    }

    private static void deserializeAndProcessEvents(String eventJsonString) {
        // assuming all messages in the queue is of event grid event schema
        System.out.println(eventJsonString);
        List<EventGridEvent> events = EventGridEvent.fromString(eventJsonString);

        for (EventGridEvent event : events) {
            BinaryData eventData = event.getData();
            if (eventData != null) {
                User user = eventData.toObject(User.class);
                System.out.printf("The received event data is: %s%n", user);
            }
        }
    }
}
