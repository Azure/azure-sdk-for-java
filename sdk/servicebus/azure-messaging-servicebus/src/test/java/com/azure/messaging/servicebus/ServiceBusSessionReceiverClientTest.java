// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        when(asyncClient.getInstrumentation()).thenReturn(
            new ServiceBusReceiverInstrumentation(null, null, "fqdn", "entity", null, ReceiverKind.ASYNC_RECEIVER));
    }

    @AfterEach
    void afterEach(TestInfo testInfo) {
        Mockito.framework().clearInlineMock(this);
    }

    @Test
    void acceptSession() {
        when(sessionAsyncClient.acceptSession(anyString())).thenReturn(Mono.just(asyncClient));
        ServiceBusSessionReceiverClient sessionClient
            = new ServiceBusSessionReceiverClient(sessionAsyncClient, false, Duration.ofMillis(100));

        assertNotNull(sessionClient.acceptSession("sessionId"));
    }

    @Test
    void acceptSessionTimeout() {
        when(sessionAsyncClient.acceptSession(anyString()))
            .thenReturn(Mono.just(asyncClient).delayElement(Duration.ofMillis(500)));
        ServiceBusSessionReceiverClient sessionClient
            = new ServiceBusSessionReceiverClient(sessionAsyncClient, false, Duration.ofMillis(50));

        assertThrows(IllegalStateException.class, () -> sessionClient.acceptSession("sessionId"));
    }

    @Test
    void acceptNextSession() {
        when(sessionAsyncClient.acceptNextSession()).thenReturn(Mono.just(asyncClient));
        ServiceBusSessionReceiverClient sessionClient
            = new ServiceBusSessionReceiverClient(sessionAsyncClient, false, Duration.ofMillis(100));

        assertNotNull(sessionClient.acceptNextSession());
    }

    @Test
    void acceptNextSessionTimeout() {
        when(sessionAsyncClient.acceptNextSession())
            .thenReturn(Mono.just(asyncClient).delayElement(Duration.ofMillis(500)));
        ServiceBusSessionReceiverClient sessionClient
            = new ServiceBusSessionReceiverClient(sessionAsyncClient, false, Duration.ofMillis(50));

        assertThrows(IllegalStateException.class, () -> sessionClient.acceptNextSession());
    }

    @Test
    void acceptNextSessionTimeoutIsIllegalStateWithTimeoutCause() {
        when(sessionAsyncClient.acceptNextSession())
            .thenReturn(Mono.just(asyncClient).delayElement(Duration.ofMillis(500)));
        ServiceBusSessionReceiverClient sessionClient
            = new ServiceBusSessionReceiverClient(sessionAsyncClient, false, Duration.ofMillis(50));

        // The operation timeout is surfaced as IllegalStateException (NOT AmqpException), caused by a
        // TimeoutException. This backs the acceptNextSession() Javadoc @throws contract.
        final IllegalStateException ex
            = assertThrows(IllegalStateException.class, () -> sessionClient.acceptNextSession());
        assertInstanceOf(TimeoutException.class, ex.getCause());
    }

    @Test
    void acceptNextSessionPropagatesAmqpException() {
        // An AMQP-level failure from the underlying async accept propagates AS-IS (AmqpException), i.e.
        // AmqpException is NOT the operation-timeout signal. This backs the corrected Javadoc @throws.
        final AmqpException amqpError = new AmqpException(false, "AMQP-level failure", new AmqpErrorContext("fqdn"));
        when(sessionAsyncClient.acceptNextSession()).thenReturn(Mono.error(amqpError));
        ServiceBusSessionReceiverClient sessionClient
            = new ServiceBusSessionReceiverClient(sessionAsyncClient, false, Duration.ofSeconds(5));

        final AmqpException thrown = assertThrows(AmqpException.class, () -> sessionClient.acceptNextSession());
        assertSame(amqpError, thrown);
    }
}
