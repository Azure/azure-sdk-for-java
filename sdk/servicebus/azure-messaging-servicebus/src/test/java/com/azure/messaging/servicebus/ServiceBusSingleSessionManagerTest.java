// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ServiceBusSingleSessionManagerTest {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusSingleSessionManagerTest.class);

    @Mock
    private ServiceBusSessionReactorReceiver sessionReceiver;
    @Mock
    private MessageSerializer serializer;
    @Mock
    private ServiceBusReceiverInstrumentation instrumentation;

    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    void getLinkNameShouldMatchSessionIdCaseInsensitively() {
        final String acceptedSessionId = "Session-ABC";
        final String linkName = "link-1";

        when(sessionReceiver.getSessionId()).thenReturn(acceptedSessionId);
        when(sessionReceiver.getLinkName()).thenReturn(linkName);

        final ServiceBusSingleSessionManager manager = createManager();

        // Exact match
        assertEquals(linkName, manager.getLinkName("Session-ABC"));
        // All lowercase
        assertEquals(linkName, manager.getLinkName("session-abc"));
        // All uppercase
        assertEquals(linkName, manager.getLinkName("SESSION-ABC"));
        // Non-matching value
        assertNull(manager.getLinkName("other-session"));
    }

    @Test
    void updateDispositionShouldMatchSessionIdCaseInsensitively() {
        final String acceptedSessionId = "Session-ABC";
        final String lockToken = "lock-1";

        when(sessionReceiver.getSessionId()).thenReturn(acceptedSessionId);
        when(sessionReceiver.updateDisposition(any(), any())).thenReturn(Mono.empty());

        final ServiceBusSingleSessionManager manager = createManager();

        // Message carries a differently-cased session ID — disposition should still succeed.
        StepVerifier
            .create(manager.updateDisposition(lockToken, "session-abc", DispositionStatus.COMPLETED, null, null, null,
                null))
            .assertNext(result -> assertTrue(result))
            .verifyComplete();

        // Completely different session ID — disposition should error.
        StepVerifier
            .create(manager.updateDisposition(lockToken, "other-session", DispositionStatus.COMPLETED, null, null, null,
                null))
            .verifyError();
    }

    private ServiceBusSingleSessionManager createManager() {
        return new ServiceBusSingleSessionManager(LOGGER, "test-identifier", sessionReceiver, 0, serializer,
            new AmqpRetryOptions(), instrumentation);
    }
}
