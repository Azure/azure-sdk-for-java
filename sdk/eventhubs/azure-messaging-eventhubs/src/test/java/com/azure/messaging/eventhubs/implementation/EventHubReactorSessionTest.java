package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.implementation.AmqpLinkProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

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
    public  void beforeEach() {
        closeable = MockitoAnnotations.openMocks(this);

        when(amqpConnection.getShutdownSignals()).thenReturn(Flux.never());
        when(sessionHandler.getConnectionId()).thenReturn("Test-connection-id");
        when(sessionHandler.getEndpointStates()).thenReturn(Flux.never());

        reactorSession = new EventHubReactorSession(amqpConnection, session, sessionHandler, SESSION_NAME,
            reactorProvider, handlerProvider, linkProvider, Mono.fromSupplier(() -> claimsBasedSecurityNode),
            tokenManagerProvider, RETRY_OPTIONS, messageSerializer);
    }

    @AfterEach
    public void afterEach() throws Exception {
        closeable.close();
    }

    /**
     * Tests that sequence number is preferred over offset and default replication segment is used.
     */
    @Test
    public void getSequenceNumberExpression() {
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
        final Long replicationSegment = 201L;

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

    @Test
    public void throwsOnNoPosition() {
        final EventPosition eventPosition = mock(EventPosition.class);
        when(eventPosition.getOffset()).thenReturn(null);
        when(eventPosition.getSequenceNumber()).thenReturn(null);
        when(eventPosition.getEnqueuedDateTime()).thenReturn(null);

        when(eventPosition.getReplicationSegment()).thenReturn(3L);

        assertThrows(IllegalArgumentException.class, () -> EventHubReactorSession.getExpression(eventPosition));
    }
}
