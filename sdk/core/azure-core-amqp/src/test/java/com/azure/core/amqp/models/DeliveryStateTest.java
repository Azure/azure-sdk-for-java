// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link DeliveryState}
 */
public class DeliveryStateTest {
    /**
     * Tests that all the values are available.
     */
    @Test
    public void values() {
        // Arrange
        final DeliveryState[] expected = new DeliveryState[] {
            DeliveryState.ACCEPTED, DeliveryState.MODIFIED, DeliveryState.RECEIVED, DeliveryState.REJECTED,
            DeliveryState.RELEASED, DeliveryState.TRANSACTIONAL
        };

        // Act
        final Collection<DeliveryState> actual = DeliveryState.values();

        // Assert
        for (DeliveryState state : expected) {
            assertTrue(actual.contains(state));
        }
    }

    /**
     * Arguments for fromString.
     * @return Test arguments.
     */
    public static Stream<String> fromString() {
        return Stream.of("MODIFIED", "FOO-BAR-NEW");
    }

    /**
     * Tests that we can get the corresponding value and a new one if it does not exist.
     *
     * @param deliveryState Delivery states to test.
     */
    @MethodSource
    @ParameterizedTest
    public void fromString(String deliveryState) {
        // Act
        final DeliveryState state = DeliveryState.fromString(deliveryState);

        // Assert
        assertNotNull(state);
        assertEquals(deliveryState, state.toString());
    }
}
