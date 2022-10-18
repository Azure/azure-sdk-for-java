// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.RepeatabilityHeaders;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RepeatabilityHeadersUnitTests {
    @Test
    public void repeatabilityHeadersUUIDValidation() {
        assertDoesNotThrow(() -> new RepeatabilityHeaders(UUID.randomUUID(), Instant.now()));
        assertDoesNotThrow(() -> new RepeatabilityHeaders(UUID.fromString("1-1-1-1-1"), Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> new RepeatabilityHeaders(UUID.fromString("1-1-1-1"), Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> new RepeatabilityHeaders(UUID.fromString("1-1-1-1-1-1"), Instant.now()));
    }

    @Test
    public void repeatabilityHeadersDateValidation() {
        assertDoesNotThrow(() -> new RepeatabilityHeaders(UUID.randomUUID(), Instant.now()));
        assertThrows(DateTimeException.class, () -> new RepeatabilityHeaders(UUID.randomUUID(), Instant.MAX.plusSeconds(1)));
        assertThrows(DateTimeException.class, () -> new RepeatabilityHeaders(UUID.randomUUID(), Instant.MIN.minusSeconds(1)));
    }
}
