// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.messaging.eventhubs.models.EventPosition;
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

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventHubReactorSessionTest {
    @Mock
    private AmqpConnection amqpConnection;
    @Mock
    private SessionHandler sessionHandler;

    private AutoCloseable closeable;

    @BeforeEach
    public void beforeEach() {
        closeable = MockitoAnnotations.openMocks(this);

        when(amqpConnection.getShutdownSignals()).thenReturn(Flux.never());
        when(sessionHandler.getConnectionId()).thenReturn("Test-connection-id");
        when(sessionHandler.getEndpointStates()).thenReturn(Flux.never());
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
        when(eventPosition.getOffsetString()).thenReturn(offset);
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
    @ValueSource(booleans = { true, false })
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
        final String expected = "amqp.annotation.x-opt-sequence-number " + includesDisplay + " '" + replicationSegment
            + ":" + sequenceNumber + "'";

        when(eventPosition.getSequenceNumber()).thenReturn(sequenceNumber);
        when(eventPosition.getOffsetString()).thenReturn(offset);
        when(eventPosition.getEnqueuedDateTime()).thenReturn(instant);
        when(eventPosition.isInclusive()).thenReturn(isInclusive);
        when(eventPosition.getReplicationSegment()).thenReturn(replicationSegment);

        // Act
        final String actual = EventHubReactorSession.getExpression(eventPosition);

        // Assert
        assertEquals(expected, actual);
    }

    /**
     * Tests that the correct offset expression is created.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void getOffsetExpression() {
        // Arrange
        final String offsetString = "2501";
        final long offset = Long.parseLong(offsetString);
        final EventPosition eventPosition = EventPosition.fromOffset(offset);
        final EventPosition eventPositionString = EventPosition.fromOffsetString(offsetString);

        // -1 because replication segment is null.
        final String expected = "amqp.annotation.x-opt-offset > '" + offset + "'";

        // Act
        final String actual = EventHubReactorSession.getExpression(eventPosition);
        final String actualString = EventHubReactorSession.getExpression(eventPositionString);

        // Assert
        assertEquals(expected, actual);
        assertEquals(expected, actualString);
    }

    @SuppressWarnings("deprecation")
    public static Stream<Arguments> getExpression() {
        final String positionString = "2506";
        final long position = Long.parseLong(positionString);
        final String offsetExpression = "amqp.annotation.x-opt-offset > '" + positionString + "'";

        final String offsetWithReplicationSegment = "10:0030";
        final String offsetWithReplicationExpression = "amqp.annotation.x-opt-offset > '"
            + offsetWithReplicationSegment + "'";

        final int replicationSegment = 19;
        final Instant enqueuedTime = Instant.ofEpochMilli(1705519331970L);


        return Stream.of(
            Arguments.of(EventPosition.fromOffsetString(offsetWithReplicationSegment), offsetWithReplicationExpression),
            Arguments.of(EventPosition.fromOffset(position), offsetExpression),
            Arguments.of(EventPosition.fromOffsetString(positionString), offsetExpression),

            Arguments.of(EventPosition.fromEnqueuedTime(enqueuedTime),
                "amqp.annotation.x-opt-enqueued-time > '1705519331970'"),

            // -1 because replication segment is null.
            Arguments.of(EventPosition.fromSequenceNumber(position),
                "amqp.annotation.x-opt-sequence-number > '-1:2506'"),
            Arguments.of(EventPosition.fromSequenceNumber(position, true),
                "amqp.annotation.x-opt-sequence-number >= '-1:2506'"),

            // Passing in a replication segment.
            Arguments.of(EventPosition.fromSequenceNumber(position, replicationSegment),
                "amqp.annotation.x-opt-sequence-number > '19:2506'"),
            Arguments.of(EventPosition.fromSequenceNumber(position, replicationSegment, true),
                "amqp.annotation.x-opt-sequence-number >= '19:2506'"));
    }

    @MethodSource
    @ParameterizedTest
    public void getExpression(EventPosition position, String expected) {
        // Act
        final String actual = EventHubReactorSession.getExpression(position);

        // Assert
        assertEquals(expected, actual);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void throwsOnNoPosition() {
        final EventPosition eventPosition = mock(EventPosition.class);
        when(eventPosition.getOffset()).thenReturn(null);
        when(eventPosition.getOffsetString()).thenReturn(null);
        when(eventPosition.getSequenceNumber()).thenReturn(null);
        when(eventPosition.getEnqueuedDateTime()).thenReturn(null);

        when(eventPosition.getReplicationSegment()).thenReturn(3);

        assertThrows(IllegalArgumentException.class, () -> EventHubReactorSession.getExpression(eventPosition));
    }
}
