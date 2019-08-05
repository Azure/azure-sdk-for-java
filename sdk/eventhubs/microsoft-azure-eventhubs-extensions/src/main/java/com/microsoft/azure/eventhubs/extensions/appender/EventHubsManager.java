// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.extensions.appender;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class EventHubsManager extends AbstractManager {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
    private final String eventHubConnectionString;
    private EventHubClient eventHubSender;

    protected EventHubsManager(final String name, final String eventHubConnectionString) {
        super(LoggerContext.getContext(true), name);
        this.eventHubConnectionString = eventHubConnectionString;
    }

    public void send(final byte[] msg) throws EventHubException {
        if (msg != null) {
            EventData data = EventData.create(msg);
            this.eventHubSender.sendSync(data);
        }
    }

    public void send(final Iterable<byte[]> messages) throws EventHubException {
        if (messages != null) {
            LinkedList<EventData> events = new LinkedList<EventData>();
            for (byte[] message : messages) {
                events.add(EventData.create(message));
            }

            this.eventHubSender.sendSync(events);
        }
    }

    public void startup() throws EventHubException, IOException {
        this.eventHubSender = EventHubClient.createFromConnectionStringSync(this.eventHubConnectionString, EXECUTOR_SERVICE);
    }
}
