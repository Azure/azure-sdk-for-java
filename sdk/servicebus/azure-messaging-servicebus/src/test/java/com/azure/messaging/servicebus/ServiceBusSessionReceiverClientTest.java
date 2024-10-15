// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusSessionReceiverClientTest {
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void afterEach(TestInfo testInfo) throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }


    @Test
    void acceptSession() {
        //
        final ServiceBusSessionReceiverAsyncClient sessionAsyncClient = mock(ServiceBusSessionReceiverAsyncClient.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(asyncClient.getInstrumentation()).thenReturn(new ServiceBusReceiverInstrumentation(null, null,
            "fqdn", "entity", null, ReceiverKind.ASYNC_RECEIVER));
        //
        when(sessionAsyncClient.acceptSession(anyString())).thenReturn(Mono.just(asyncClient));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient,
            false,
            Duration.ofMillis(100));

        assertNotNull(sessionClient.acceptSession("sessionId"));
    }

    @Test
    void acceptSessionTimeout() {
        //
        final ServiceBusSessionReceiverAsyncClient sessionAsyncClient = mock(ServiceBusSessionReceiverAsyncClient.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(asyncClient.getInstrumentation()).thenReturn(new ServiceBusReceiverInstrumentation(null, null,
            "fqdn", "entity", null, ReceiverKind.ASYNC_RECEIVER));
        //
        when(sessionAsyncClient.acceptSession(anyString())).thenReturn(Mono.just(asyncClient)
            .delayElement(Duration.ofMillis(500)));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient,
            false,
            Duration.ofMillis(50));

        assertThrows(IllegalStateException.class,
            () -> sessionClient.acceptSession("sessionId"));
    }

    @Test
    void acceptNextSession() {
        //
        final ServiceBusSessionReceiverAsyncClient sessionAsyncClient = mock(ServiceBusSessionReceiverAsyncClient.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(asyncClient.getInstrumentation()).thenReturn(new ServiceBusReceiverInstrumentation(null, null,
            "fqdn", "entity", null, ReceiverKind.ASYNC_RECEIVER));
        //
        when(sessionAsyncClient.acceptNextSession()).thenReturn(Mono.just(asyncClient));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient,
            false,
            Duration.ofMillis(100));

        assertNotNull(sessionClient.acceptNextSession());
    }

    @Test
    void acceptNextSessionTimeout() {
        //
        final ServiceBusSessionReceiverAsyncClient sessionAsyncClient = mock(ServiceBusSessionReceiverAsyncClient.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(asyncClient.getInstrumentation()).thenReturn(new ServiceBusReceiverInstrumentation(null, null,
            "fqdn", "entity", null, ReceiverKind.ASYNC_RECEIVER));
        //
        when(sessionAsyncClient.acceptNextSession()).thenReturn(Mono.just(asyncClient)
            .delayElement(Duration.ofMillis(500)));
        ServiceBusSessionReceiverClient sessionClient = new ServiceBusSessionReceiverClient(sessionAsyncClient,
            false,
            Duration.ofMillis(50));

        assertThrows(IllegalStateException.class,
            () -> sessionClient.acceptNextSession());
    }
}
