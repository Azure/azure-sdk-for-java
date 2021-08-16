// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.amqp.exception.AmqpErrorCondition.LINK_STOLEN;
import static com.azure.core.amqp.exception.AmqpErrorCondition.TRACKING_ID_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link LinkHandler}.
 */
public class LinkHandlerTest {
    private static final String CONNECTION_ID = "connection-id";
    private static final String HOSTNAME = "test-hostname";
    private static final String ENTITY_PATH = "test-entity-path";

    @Mock
    private Event event;
    @Mock
    private Link link;
    @Mock
    private Session session;

    private final ClientLogger logger = new ClientLogger(LinkHandlerTest.class);
    private final AmqpErrorCondition linkStolen = LINK_STOLEN;
    private final Symbol symbol = Symbol.getSymbol(linkStolen.getErrorCondition());
    private final String description = "test-description";
    private final LinkHandler handler = new MockLinkHandler(CONNECTION_ID, HOSTNAME, ENTITY_PATH, logger);
    private AutoCloseable mocksCloseable;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        when(event.getLink()).thenReturn(link);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMocks();

        handler.close();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Verifies that a close event will close the link session.
     */
    @Test
    void onLinkLocalClose() {
        // Arrange
        final ErrorCondition errorCondition = new ErrorCondition(symbol, description);
        when(link.getCondition()).thenReturn(errorCondition);
        when(link.getSession()).thenReturn(session);

        when(session.getLocalState()).thenReturn(EndpointState.ACTIVE);

        // Act
        handler.onLinkLocalClose(event);

        // Assert
        verify(session, never()).setCondition(same(errorCondition));
        verify(session, never()).close();
    }

    /**
     * Verifies that a close event will not close the link session if there is no error condition.
     */
    @Test
    void onLinkLocalCloseDoesNotClose() {
        // Arrange
        final ErrorCondition errorCondition = new ErrorCondition(symbol, description);
        when(link.getCondition()).thenReturn(errorCondition);
        when(link.getSession()).thenReturn(null);

        // Act
        handler.onLinkLocalClose(event);

        // Assert
        verifyNoInteractions(session);
    }

    /**
     * Verifies that a close event will not close the link session if the endpoint state is already closed.
     */
    @Test
    void onLinkLocalCloseDoesNotCloseEndpointState() {
        // Arrange
        final ErrorCondition errorCondition = new ErrorCondition(symbol, description);
        when(link.getCondition()).thenReturn(errorCondition);
        when(link.getSession()).thenReturn(session);

        when(session.getLocalState()).thenReturn(EndpointState.CLOSED);

        // Act
        handler.onLinkLocalClose(event);

        // Assert
        verify(session, never()).setCondition(any());
        verify(session, never()).close();
    }

    /**
     * Verifies that a remote close event will close the link session.
     */
    @Test
    void onLinkRemoteClose() {
        // Arrange
        final ErrorCondition errorCondition = new ErrorCondition(symbol, description);

        when(link.getRemoteCondition()).thenReturn(errorCondition);
        when(link.getSession()).thenReturn(session);

        when(session.getLocalState()).thenReturn(EndpointState.ACTIVE);

        // Act
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> handler.onLinkRemoteClose(event))
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assertions.assertEquals(LINK_STOLEN, exception.getErrorCondition());
            })
            .verify();

        // Assert
        verify(link).setCondition(errorCondition);
        verify(link).close();

        verify(session, never()).setCondition(errorCondition);
        verify(session, never()).close();
    }

    /**
     * Verifies that a Remote Detach event.
     */
    @Test
    void onLinkRemoteDetach() {
        // Arrange
        final ErrorCondition errorCondition = new ErrorCondition(symbol, description);

        when(link.getRemoteCondition()).thenReturn(errorCondition);
        when(link.getSession()).thenReturn(session);

        when(session.getLocalState()).thenReturn(EndpointState.ACTIVE);

        // Act
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> handler.onLinkRemoteDetach(event))
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assertions.assertEquals(LINK_STOLEN, exception.getErrorCondition());
            })
            .verify();

        // Assert
        verify(link).setCondition(errorCondition);
        verify(link).close();

        verify(session, never()).setCondition(errorCondition);
        verify(session, never()).close();
    }

    /**
     * Verifies that an error is propagated if there is an error condition on close.
     */
    @Test
    void onLinkRemoteCloseWithErrorCondition() {
        // Arrange
        final ErrorCondition errorCondition = new ErrorCondition(symbol, description);

        when(link.getRemoteCondition()).thenReturn(errorCondition);
        when(link.getSession()).thenReturn(session);
        when(link.getLocalState()).thenReturn(EndpointState.CLOSED);

        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> handler.onLinkRemoteClose(event))
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assertions.assertEquals(LINK_STOLEN, exception.getErrorCondition());
            })
            .verify();

        // Assert
        verify(link, never()).setCondition(errorCondition);
        verify(link, never()).close();

        verify(session, never()).setCondition(errorCondition);
        verify(session, never()).close();
    }

    /**
     * Verifies that no error is propagated. And it is closed instead.
     */
    @Test
    void onLinkRemoteCloseNoErrorCondition() {
        // Arrange
        final ErrorCondition errorCondition = new ErrorCondition(null, description);
        final Event finalEvent = mock(Event.class);

        when(link.getRemoteCondition()).thenReturn(errorCondition);
        when(link.getSession()).thenReturn(session);
        when(link.getLocalState()).thenReturn(EndpointState.CLOSED);

        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> {
                handler.onLinkRemoteClose(event);
                handler.onLinkFinal(finalEvent);
            })
            .expectNext(EndpointState.CLOSED)
            .expectComplete()
            .verify();

        // Assert
        verify(link, never()).setCondition(errorCondition);
        verify(link, never()).close();

        verify(session, never()).setCondition(errorCondition);
        verify(session, never()).close();
    }

    /**
     * Verifies that an error is propagated and that onLinkFinal does not result in another close operation because it
     * is already at a terminal state.
     */
    @Test
    void onLinkRemoteCloseThenLinkFinal() {
        // Arrange
        final ErrorCondition errorCondition = new ErrorCondition(symbol, description);

        when(link.getRemoteCondition()).thenReturn(errorCondition);
        when(link.getSession()).thenReturn(session);
        when(link.getLocalState()).thenReturn(EndpointState.CLOSED);

        final Event finalEvent = mock(Event.class);
        final Link link2 = mock(Link.class);
        when(finalEvent.getLink()).thenReturn(link2);
        when(link2.getLocalState()).thenReturn(EndpointState.CLOSED);
        when(link2.getRemoteState()).thenReturn(EndpointState.CLOSED);

        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> {
                handler.onLinkRemoteClose(event);
                handler.onLinkFinal(finalEvent);
            })
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assertions.assertEquals(LINK_STOLEN, exception.getErrorCondition());
            })
            .verify();

        // Assert
        verify(link, never()).setCondition(errorCondition);
        verify(link, never()).close();

        verify(session, never()).setCondition(errorCondition);
        verify(session, never()).close();
    }

    /**
     * Tests that close operation is called on link final.
     */
    @Test
    public void onLinkFinal() {
        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .then(() -> handler.onLinkFinal(event))
            .expectNext(EndpointState.UNINITIALIZED, EndpointState.CLOSED)
            .verifyComplete();
    }

    /**
     * Verifies {@link NullPointerException}.
     */
    @Test
    public void constructor() {
        // Act
        assertThrows(NullPointerException.class,
            () -> new MockLinkHandler(null, HOSTNAME, ENTITY_PATH, logger));
        assertThrows(NullPointerException.class,
            () -> new MockLinkHandler(CONNECTION_ID, null, ENTITY_PATH, logger));
        assertThrows(NullPointerException.class,
            () -> new MockLinkHandler(CONNECTION_ID, HOSTNAME, null, logger));
        assertThrows(NullPointerException.class,
            () -> new MockLinkHandler(CONNECTION_ID, HOSTNAME, ENTITY_PATH, null));
    }

    /**
     * Tests that an error context can be created.
     */
    @Test
    public void errorContext() {
        // Arrange
        final Symbol trackingId = Symbol.getSymbol(TRACKING_ID_PROPERTY.getErrorCondition());
        final String referenceId = "reference-id-test";
        final Map<Symbol, Object> remoteProperties = Collections.singletonMap(trackingId, referenceId);
        when(link.getRemoteProperties()).thenReturn(remoteProperties);

        final String linkName = "test-link-name";
        when(link.getName()).thenReturn(linkName);

        final int linkCredit = 153;
        when(link.getCredit()).thenReturn(linkCredit);

        // Act
        final AmqpErrorContext actual = handler.getErrorContext(link);

        // Assert
        assertTrue(actual instanceof LinkErrorContext);

        final LinkErrorContext errorContext = (LinkErrorContext) actual;
        assertEquals(linkCredit, errorContext.getLinkCredit());
        assertEquals(referenceId, errorContext.getTrackingId());
        assertEquals(ENTITY_PATH, errorContext.getEntityPath());
        assertEquals(HOSTNAME, actual.getNamespace());
    }

    public static Stream<Map<Symbol, Object>> errorContextNoReferenceId() {
        return Stream.of(
            null,
            Collections.emptyMap(),
            Collections.singletonMap(Symbol.valueOf("foo"), "bar")
        );
    }

    /**
     * Tests that an error context can be created when there is no tracking id.
     */
    @MethodSource
    @ParameterizedTest
    public void errorContextNoReferenceId(Map<Symbol, Object> properties) {
        // Arrange
        when(link.getRemoteProperties()).thenReturn(properties);

        final String linkName = "test-link-name";
        when(link.getName()).thenReturn(linkName);

        final int linkCredit = 153;
        when(link.getCredit()).thenReturn(linkCredit);

        // Act
        final AmqpErrorContext actual = handler.getErrorContext(link);

        // Assert
        assertTrue(actual instanceof LinkErrorContext);

        final LinkErrorContext errorContext = (LinkErrorContext) actual;
        assertEquals(linkCredit, errorContext.getLinkCredit());
        assertEquals(linkName, errorContext.getTrackingId());
        assertEquals(ENTITY_PATH, errorContext.getEntityPath());
        assertEquals(HOSTNAME, actual.getNamespace());
    }

    private static final class MockLinkHandler extends LinkHandler {
        MockLinkHandler(String connectionId, String hostname, String entityPath, ClientLogger logger) {
            super(connectionId, hostname, entityPath, logger);
        }
    }
}
