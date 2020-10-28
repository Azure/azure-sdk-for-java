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
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient);

        assertNotNull(sessionClient.acceptSession("sessionId"));
    }

    @Test
    void acceptSessionTimeout() {
        when(sessionAsyncClient.acceptSession(anyString())).thenReturn(Mono.just(asyncClient)
            .delayElement(Duration.ofMillis(100)));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient);

        assertThrows(IllegalStateException.class,
            () -> sessionClient.acceptSession("sessionId", Duration.ofMillis(50)));
    }

    @Test
    void acceptNextSession() {
        when(sessionAsyncClient.acceptNextSession()).thenReturn(Mono.just(asyncClient));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient);

        assertNotNull(sessionClient.acceptNextSession());
    }

    @Test
    void acceptNextSessionTimeout() {
        when(sessionAsyncClient.acceptNextSession()).thenReturn(Mono.just(asyncClient)
            .delayElement(Duration.ofMillis(100)));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient);

        assertThrows(IllegalStateException.class,
            () -> sessionClient.acceptNextSession(Duration.ofMillis(50)));
    }
}
