// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.outbound;

import com.azure.spring.integration.core.handler.DefaultMessageHandler;
import com.azure.spring.integration.core.handler.DefaultMessageHandlerTests;
import com.azure.spring.messaging.eventhubs.core.EventHubsTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventHubsMessageHandlerTests extends DefaultMessageHandlerTests<EventHubsTemplate> {

    private AutoCloseable closeable;

    @BeforeEach
    @Override
    @SuppressWarnings("unchecked")
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.sendOperation = mock(EventHubsTemplate.class);
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class))).thenReturn(mono);
        when(
            this.sendOperation.sendAsync(eq(this.dynamicDestination), isA(Message.class)))
            .thenReturn(mono);
        this.handler = new DefaultMessageHandler(this.destination, this.sendOperation);
    }

    @AfterEach
    void close() throws Exception {
        closeable.close();
    }
}
