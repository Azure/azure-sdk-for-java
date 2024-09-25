// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.implementation.AmqpLinkProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ProtonSessionWrapper;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventHubReactorSessionTest {
    private static final String SESSION_NAME = "session-name-test";
    private static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions().setMaxRetries(1);

    @Mock
    private AmqpConnection amqpConnection;
    @Mock
    private Session session;
    @Mock
    private SessionHandler sessionHandler;
    @Mock
    private TokenManagerProvider tokenManagerProvider;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private ReactorHandlerProvider handlerProvider;
    @Mock
    private Record record;
    @Mock
    private AmqpLinkProvider linkProvider;
    @Mock
    private ClaimsBasedSecurityNode claimsBasedSecurityNode;

    private EventHubReactorSession reactorSession;
    private AutoCloseable closeable;

    @BeforeEach
    public void beforeEach() {
        closeable = MockitoAnnotations.openMocks(this);

        when(amqpConnection.getShutdownSignals()).thenReturn(Flux.never());
        when(sessionHandler.getConnectionId()).thenReturn("Test-connection-id");
        when(sessionHandler.getEndpointStates()).thenReturn(Flux.never());

        ProtonSessionWrapper wrapper = new ProtonSessionWrapper(session, sessionHandler, reactorProvider);

        reactorSession = new EventHubReactorSession(amqpConnection, wrapper, handlerProvider, linkProvider,
            Mono.fromSupplier(() -> claimsBasedSecurityNode), tokenManagerProvider, RETRY_OPTIONS, messageSerializer,
            false);
    }

    @AfterEach
    public void afterEach() throws Exception {
        closeable.close();
    }

    /**
     * Tests that sequence number is preferred over offset and default replication segment is used.
     */
    @Test
    public void getSequenceNumberExpressionPreferred() {
        // Arrange
        final EventPosition eventPosition = mock(EventPosition.class);
        final Long sequenceNumber = 10L;
        final String offset = "test-offset";
        final Instant instant = Instant.ofEpochSecond(1704836768);

        // -1 because replication segment is null.
        final String expected = "amqp.annotation.x-opt-sequence-number > '-1:" + sequenceNumber + "'";

        when(eventPosition.getSequenceNumber()).thenReturn(sequenceNumber);
        when(eventPosition.getOffset()).thenReturn(offset);
        when(eventPosition.getEnqueuedDateTime()).thenReturn(instant);
        when(eventPosition.isInclusive()).thenReturn(false);
        when(eventPosition.getReplicationSegment()).thenReturn(null);

        // Act
        final String actual = EventHubReactorSession.getExpression(eventPosition);

        // Assert
        assertEquals(expected, actual);
    }

    /**
     * Tests that sequence number is preferred over offset and default replication segment is used.
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void getSequenceNumberExpressionReplicationSegment(boolean isInclusive) {
        // Arrange
        final EventPosition eventPosition = mock(EventPosition.class);
        final Long sequenceNumber = 10L;
        final String offset = "test-offset";
        final Instant instant = Instant.ofEpochSecond(1704836768);
        final Integer replicationSegment = 201;

        // -1 because replication segment is null.
        final String includesDisplay = isInclusive ? ">=" : ">";

        // amqp.annotation.x-opt-sequence
        final String expected = "amqp.annotation.x-opt-sequence-number " + includesDisplay + " '"
            + replicationSegment + ":" + sequenceNumber + "'";

        when(eventPosition.getSequenceNumber()).thenReturn(sequenceNumber);
        when(eventPosition.getOffset()).thenReturn(offset);
        when(eventPosition.getEnqueuedDateTime()).thenReturn(instant);
        when(eventPosition.isInclusive()).thenReturn(isInclusive);
        when(eventPosition.getReplicationSegment()).thenReturn(replicationSegment);

        // Act
        final String actual = EventHubReactorSession.getExpression(eventPosition);

        // Assert
        assertEquals(expected, actual);
    }

    /**
     * Tests that the correct offet expression is created.
     */
    @Test
    public void getOffsetExpression() {
        // Arrange
        final long offset = 2501L;
        final EventPosition eventPosition = EventPosition.fromOffset(offset);

        // -1 because replication segment is null.
        final String expected = "amqp.annotation.x-opt-offset > '" + offset + "'";

        // Act
        final String actual = EventHubReactorSession.getExpression(eventPosition);

        // Assert
        assertEquals(expected, actual);
    }

    public static Stream<Arguments> getExpression() {
        final long position = 2501;
        final int replicationSegment = 19;
        final Instant enqueuedTime = Instant.ofEpochMilli(1705519331970L);

        return Stream.of(
            Arguments.of(EventPosition.fromOffset(position), "amqp.annotation.x-opt-offset > '2501'"),

            Arguments.of(EventPosition.fromEnqueuedTime(enqueuedTime),
                "amqp.annotation.x-opt-enqueued-time > '1705519331970'"),

            // -1 because replication segment is null.
            Arguments.of(EventPosition.fromSequenceNumber(position),
                "amqp.annotation.x-opt-sequence-number > '-1:2501'"),
            Arguments.of(EventPosition.fromSequenceNumber(position, true),
                "amqp.annotation.x-opt-sequence-number >= '-1:2501'"),

            // Passing in a replication segment.
            Arguments.of(EventPosition.fromSequenceNumber(position, replicationSegment),
                "amqp.annotation.x-opt-sequence-number > '19:2501'"),
            Arguments.of(EventPosition.fromSequenceNumber(position, replicationSegment, true),
                "amqp.annotation.x-opt-sequence-number >= '19:2501'")
        );
    }

    @MethodSource
    @ParameterizedTest
    public void getExpression(EventPosition position, String expected) {
        // Act
        final String actual = EventHubReactorSession.getExpression(position);

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    public void throwsOnNoPosition() {
        final EventPosition eventPosition = mock(EventPosition.class);
        when(eventPosition.getOffset()).thenReturn(null);
        when(eventPosition.getSequenceNumber()).thenReturn(null);
        when(eventPosition.getEnqueuedDateTime()).thenReturn(null);

        when(eventPosition.getReplicationSegment()).thenReturn(3);

        assertThrows(IllegalArgumentException.class, () -> EventHubReactorSession.getExpression(eventPosition));
    }
}
