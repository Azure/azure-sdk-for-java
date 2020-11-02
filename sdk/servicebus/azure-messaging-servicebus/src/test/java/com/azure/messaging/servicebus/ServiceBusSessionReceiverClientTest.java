// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ServiceBusSessionReceiverClientTest {

    @Mock
    private ServiceBusSessionReceiverAsyncClient sessionAsyncClient;

    @Mock
    private ServiceBusReceiverAsyncClient asyncClient;

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void afterEach(TestInfo testInfo) {
        Mockito.framework().clearInlineMocks();
    }


    @Test
    void acceptSession() {
        when(sessionAsyncClient.acceptSession(anyString())).thenReturn(Mono.just(asyncClient));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient,
            Duration.ofMillis(100));

        assertNotNull(sessionClient.acceptSession("sessionId"));
    }

    @Test
    void acceptSessionTimeout() {
        when(sessionAsyncClient.acceptSession(anyString())).thenReturn(Mono.just(asyncClient)
            .delayElement(Duration.ofMillis(500)));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient,
            Duration.ofMillis(50));

        assertThrows(IllegalStateException.class,
            () -> sessionClient.acceptSession("sessionId"));
    }

    @Test
    void acceptNextSession() {
        when(sessionAsyncClient.acceptNextSession()).thenReturn(Mono.just(asyncClient));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient,
            Duration.ofMillis(100));

        assertNotNull(sessionClient.acceptNextSession());
    }

    @Test
    void acceptNextSessionTimeout() {
        when(sessionAsyncClient.acceptNextSession()).thenReturn(Mono.just(asyncClient)
            .delayElement(Duration.ofMillis(500)));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient,
            Duration.ofMillis(50));

        assertThrows(IllegalStateException.class,
            () -> sessionClient.acceptNextSession());
    }
}
