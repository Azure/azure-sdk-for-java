// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LockContainerTest {
    private final Duration interval = Duration.ofSeconds(4);

    @Mock
    private Consumer<String> onRemoved;
    private LockContainer<String> container;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
        container = new LockContainer<>(interval, onRemoved);
    }

    @AfterEach
    void afterEach() {
        if (container != null) {
            container.close();
        }
    }

    @Test
    void constructor() {
        assertThrows(NullPointerException.class, () -> new LockContainer<>(null));
        assertThrows(NullPointerException.class, () -> new LockContainer<>(interval, null));
        assertThrows(NullPointerException.class, () -> new LockContainer<>(null, onRemoved));
    }

    @Test
    void addsAndContains() {
        // Arrange
        final String key = "key1";
        final String value = "value";
        final OffsetDateTime expiration = OffsetDateTime.now().plusSeconds(10);

        // Act
        final OffsetDateTime added = container.addOrUpdate(key, expiration, value);
        final boolean contains = container.containsUnexpired(key);

        // Assert
        assertEquals(expiration, added);
        assertTrue(contains);
    }

    @Test
    void addsAndUpdates() {
        // Arrange
        final String key = "key1";
        final String value = "value";
        final String value2 = "value2";
        final OffsetDateTime expiration = OffsetDateTime.now();
        final OffsetDateTime expiration2 = expiration.plus(Duration.ofSeconds(10));

        // Act
        final OffsetDateTime added = container.addOrUpdate(key, expiration, value);
        final OffsetDateTime added2 = container.addOrUpdate(key, expiration2, value2);

        // Assert
        assertEquals(expiration, added);
        assertEquals(expiration2, added2);
    }

    @Test
    void remove() {
        // Arrange
        final String key = "key1";
        final String value = "value";
        final OffsetDateTime expiration = OffsetDateTime.now();

        container.addOrUpdate(key, expiration, value);

        // Act
        container.remove(key);

        // Assert
        assertFalse(container.containsUnexpired(key));
    }
}
