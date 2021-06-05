// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.models.DeliveryOutcome;
import com.azure.core.amqp.models.DeliveryState;
import com.azure.core.amqp.models.ModifiedDeliveryOutcome;
import com.azure.core.amqp.models.ReceivedDeliveryOutcome;
import com.azure.core.amqp.models.RejectedDeliveryOutcome;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.amqp.messaging.Outcome;
import org.apache.qpid.proton.amqp.messaging.Received;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests utility methods in {@link MessageUtilsTest}.
 */
public class MessageUtilsTest {
    /**
     * Tests the received outcome is mapped to its delivery state.
     */
    @Test
    public void toProtonJDeliveryStateReceived() {
        // Arrange
        final ReceivedDeliveryOutcome expected = new ReceivedDeliveryOutcome(10, 1053L);

        // Act
        org.apache.qpid.proton.amqp.transport.DeliveryState actual = MessageUtils.toProtonJDeliveryState(expected);

        // Assert
        assertTrue(actual instanceof Received);

        final Received received = (Received) actual;
        assertNotNull(received.getSectionNumber());
        assertNotNull(received.getSectionOffset());

        assertEquals(expected.getSectionNumber(), received.getSectionNumber().intValue());
        assertEquals(expected.getSectionOffset(), received.getSectionOffset().longValue());
    }

    /**
     * Tests that the rejected delivery state is mapped correctly.
     */
    @Test
    public void toProtonJDeliveryStateRejected() {
        // Arrange
        final AmqpErrorCondition condition = AmqpErrorCondition.ILLEGAL_STATE;
        final Map<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("foo", 10);
        errorInfo.put("bar", "baz");
        final RejectedDeliveryOutcome expected = new RejectedDeliveryOutcome(condition)
            .setErrorInfo(errorInfo);

        // Act
        org.apache.qpid.proton.amqp.transport.DeliveryState actual = MessageUtils.toProtonJDeliveryState(expected);

        // Assert
        assertTrue(actual instanceof Rejected);
        assertRejected(expected, (Rejected) actual);
    }

    /**
     * Tests that the modified delivery state is mapped correctly.
     */
    @Test
    public void toProtonJDeliveryStateModified() {
        // Arrange
        final Map<String, Object> annotations = new HashMap<>();
        annotations.put("foo", 10);
        annotations.put("bar", "baz");
        final ModifiedDeliveryOutcome expected = new ModifiedDeliveryOutcome()
            .setDeliveryFailed(true).setUndeliverableHere(true)
            .setMessageAnnotations(annotations);

        // Act
        final org.apache.qpid.proton.amqp.transport.DeliveryState actual = MessageUtils.toProtonJDeliveryState(expected);

        // Assert
        assertTrue(actual instanceof Modified);
        assertModified(expected, (Modified) actual);
    }

    /**
     * Tests simple conversions where the delivery states are just their statuses.
     *
     * @param deliveryState Delivery state.
     * @param expected Expected outcome.
     * @param expectedType Expected type.
     */
    @MethodSource("deliveryStatesToTest")
    @ParameterizedTest
    public void toProtonJDeliveryState(DeliveryState deliveryState,
        org.apache.qpid.proton.amqp.transport.DeliveryState expected,
        org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType expectedType) {

        // Arrange
        final DeliveryOutcome outcome = new DeliveryOutcome(deliveryState);

        // Act
        final org.apache.qpid.proton.amqp.transport.DeliveryState actual = MessageUtils.toProtonJDeliveryState(outcome);

        // Assert
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.getType(), actual.getType());

        assertEquals(expectedType, actual.getType());
    }

    /**
     * Tests that an exception is thrown when an unsupported state is passed.
     */
    @Test
    public void toProtonJOutcomeUnsupported() {
        // Arrange
        // Received is not an outcome because it represents a partial message.
        final DeliveryOutcome outcome = new DeliveryOutcome(DeliveryState.RECEIVED);

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> MessageUtils.toProtonJOutcome(outcome));
    }

    /**
     * Tests that the modified outcome is mapped correctly.
     */
    @Test
    public void toProtonJOutcomeModified() {
        // Arrange
        final Map<String, Object> annotations = new HashMap<>();
        annotations.put("foo", 10);
        annotations.put("bar", "baz");
        final ModifiedDeliveryOutcome expected = new ModifiedDeliveryOutcome()
            .setDeliveryFailed(true).setUndeliverableHere(true)
            .setMessageAnnotations(annotations);

        // Act
        final Outcome actual = MessageUtils.toProtonJOutcome(expected);

        // Assert
        assertTrue(actual instanceof Modified);
        assertModified(expected, (Modified) actual);
    }

    /**
     * Tests that the rejected outcome is mapped correctly.
     */
    @Test
    public void toProtonJOutcomeRejected() {
        // Arrange
        final AmqpErrorCondition condition = AmqpErrorCondition.RESOURCE_LIMIT_EXCEEDED;
        final Map<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("foo", 10);
        errorInfo.put("bar", "baz");
        final RejectedDeliveryOutcome expected = new RejectedDeliveryOutcome(condition)
            .setErrorInfo(errorInfo);

        // Act
        final Outcome actual = MessageUtils.toProtonJOutcome(expected);

        // Assert
        assertTrue(actual instanceof Rejected);
        assertRejected(expected, (Rejected) actual);
    }

    /**
     * Tests simple conversions where the outcomes are just their statuses.
     *
     * @param deliveryState Delivery state.
     * @param expectedType Expected type.
     * @param expected Expected outcome.
     */
    @MethodSource("deliveryStatesToTest")
    @ParameterizedTest
    public void toProtonJOutcome(DeliveryState deliveryState, Outcome expected,
        org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType expectedType) {
        // Arrange
        final DeliveryOutcome outcome = new DeliveryOutcome(deliveryState);

        // Act
        final Outcome actual = MessageUtils.toProtonJOutcome(outcome);

        // Assert
        assertEquals(expected.getClass(), actual.getClass());

        if (actual instanceof org.apache.qpid.proton.amqp.transport.DeliveryState) {
            assertEquals(expectedType, ((org.apache.qpid.proton.amqp.transport.DeliveryState) actual).getType());
        }
    }

    /**
     * Simple arguments where the proton-j delivery state is also its outcome.
     *
     * @return A stream of arguments.
     */
    public static Stream<Arguments> deliveryStatesToTest() {
        return Stream.of(
            Arguments.arguments(DeliveryState.ACCEPTED, Accepted.getInstance(),
                org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType.Accepted),
            Arguments.arguments(DeliveryState.RELEASED, Released.getInstance(),
                org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType.Released),
            Arguments.arguments(DeliveryState.MODIFIED, new Modified(),
                org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType.Modified),
            Arguments.arguments(DeliveryState.REJECTED, new Rejected(),
                org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType.Rejected));
    }

    /**
     * When input is null, returns null.
     */
    @Test
    public void nullInputs() {

        assertThrows(NullPointerException.class, () -> MessageUtils.toProtonJMessage(null));
        assertThrows(NullPointerException.class, () -> MessageUtils.toAmqpAnnotatedMessage(null));

        assertNull(MessageUtils.toProtonJOutcome(null));
        assertNull(MessageUtils.toProtonJDeliveryState(null));

        assertNull(MessageUtils.toDeliveryOutcome((Outcome) null));
        assertNull(MessageUtils.toDeliveryOutcome((org.apache.qpid.proton.amqp.transport.DeliveryState) null));
    }

    private static void assertRejected(RejectedDeliveryOutcome expected, Rejected actual) {
        final AmqpErrorCondition expectedCondition = expected.getErrorCondition();

        assertNotNull(actual.getError());
        assertEquals(expectedCondition.getErrorCondition(), actual.getError().getCondition().toString());

        @SuppressWarnings("unchecked") final Map<String, Object> actualMap = actual.getError().getInfo();
        assertMap(expected.getErrorInfo(), actualMap);
    }

    private static void assertModified(ModifiedDeliveryOutcome expected, Modified actual) {
        assertEquals(expected.isDeliveryFailed(), actual.getDeliveryFailed());
        assertEquals(expected.isUndeliverableHere(), actual.getUndeliverableHere());

        @SuppressWarnings("unchecked") final Map<String, Object> actualMap = actual.getMessageAnnotations();
        assertMap(expected.getMessageAnnotations(), actualMap);
    }

    private static void assertMap(Map<String, Object> expected, Map<String, Object> actual) {
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());

        expected.forEach((key, value) -> {
            assertTrue(actual.containsKey(key));
            assertEquals(value, actual.get(key));
        });
    }
}
