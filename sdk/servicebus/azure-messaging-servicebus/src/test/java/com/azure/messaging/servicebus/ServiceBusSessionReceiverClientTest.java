// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    /**
     * Verifies the no-arg sync {@code listSessions()} delegates to the async client's
     * {@code listSessions()} and exposes every session ID through the resulting
     * {@link com.azure.core.http.rest.PagedIterable}.
     */
    @Test
    void listSessionsDelegatesToAsync() {
        when(sessionAsyncClient.listSessions())
            .thenReturn(new PagedFlux<>(() -> Mono.just(new PagedResponseBase<Void, String>(null, 200,
                new HttpHeaders(Collections.emptyMap()), Arrays.asList("s1", "s2"), null, null))));

        final ServiceBusSessionReceiverClient client
            = new ServiceBusSessionReceiverClient(sessionAsyncClient, false, Duration.ofSeconds(5));

        final List<String> ids = new ArrayList<>();
        client.listSessions().forEach(ids::add);

        assertEquals(Arrays.asList("s1", "s2"), ids);
    }

    /**
     * Verifies the {@code listSessions(OffsetDateTime)} sync overload threads the timestamp through
     * to the async client unchanged and surfaces every returned session ID.
     */
    @Test
    void listSessionsWithSessionStateUpdatedAfterDelegatesToAsync() {
        final OffsetDateTime sessionStateUpdatedAfter = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        when(sessionAsyncClient.listSessions(eq(sessionStateUpdatedAfter)))
            .thenReturn(new PagedFlux<>(() -> Mono.just(new PagedResponseBase<Void, String>(null, 200,
                new HttpHeaders(Collections.emptyMap()), Collections.singletonList("only"), null, null))));

        final ServiceBusSessionReceiverClient client
            = new ServiceBusSessionReceiverClient(sessionAsyncClient, false, Duration.ofSeconds(5));

        final List<String> ids = new ArrayList<>();
        client.listSessions(sessionStateUpdatedAfter).forEach(ids::add);

        assertEquals(Collections.singletonList("only"), ids);
    }

    /**
     * Verifies that an error in the async {@code PagedFlux} (e.g., a transient broker failure)
     * propagates out of the sync {@code PagedIterable} when iterated. Uses a generic
     * {@link RuntimeException} so the failure mode is unmistakably distinct from the eager
     * argument validation in {@link #listSessionsRejectsNullSessionStateUpdatedAfterEagerly()}.
     */
    @Test
    void listSessionsPropagatesAsyncError() {
        when(sessionAsyncClient.listSessions(any(OffsetDateTime.class)))
            .thenReturn(new PagedFlux<>(() -> Mono.error(new RuntimeException("Transient async failure."))));

        final ServiceBusSessionReceiverClient client
            = new ServiceBusSessionReceiverClient(sessionAsyncClient, false, Duration.ofSeconds(5));

        final OffsetDateTime sessionStateUpdatedAfter = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        assertThrows(RuntimeException.class, () -> client.listSessions(sessionStateUpdatedAfter).forEach(id -> {
        }));
    }

    /**
     * Verifies the sync wrapper rejects a null {@code sessionStateUpdatedAfter} eagerly (immediately on the
     * call) rather than deferring the error until the {@code PagedIterable} is iterated, matching
     * the behavior documented on the public method.
     */
    @Test
    void listSessionsRejectsNullSessionStateUpdatedAfterEagerly() {
        final ServiceBusSessionReceiverClient client
            = new ServiceBusSessionReceiverClient(sessionAsyncClient, false, Duration.ofSeconds(5));

        assertThrows(NullPointerException.class, () -> client.listSessions(null));
    }
}
