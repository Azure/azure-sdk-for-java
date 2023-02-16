// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.HangUpOptions;
import com.azure.communication.callautomation.models.RepeatabilityHeaders;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    public void handleApiIdempotencyHelperFunctionUnitTest() {
        HangUpOptions hangUpOptions = new HangUpOptions(true);

        // Case 1: default repeatability headers, it should be altered by handleApiIdempotency.
        RepeatabilityHeaders headers = CallAutomationAsyncClient.handleApiIdempotency(hangUpOptions.getRepeatabilityHeaders());
        assertNotEquals(UUID.fromString("0-0-0-0-0"), headers.getRepeatabilityRequestId());
        assertNotEquals(Instant.MIN, headers.getRepeatabilityFirstSent());

        // Case 2: user defined repeatability headers, it should not be altered by handleApiIdempotency.
        UUID uuid = UUID.randomUUID();
        Instant instant = Instant.now();
        hangUpOptions.setRepeatabilityHeaders(new RepeatabilityHeaders(uuid, instant));
        headers = CallAutomationAsyncClient.handleApiIdempotency(hangUpOptions.getRepeatabilityHeaders());
        assertEquals(uuid, headers.getRepeatabilityRequestId());
        assertEquals(instant, headers.getRepeatabilityFirstSent());

        // Case 3: user disabled repeatability headers.
        hangUpOptions = new HangUpOptions(true);
        hangUpOptions.setRepeatabilityHeaders(null);
        headers = CallAutomationAsyncClient.handleApiIdempotency(hangUpOptions.getRepeatabilityHeaders());
        assertNull(headers);
    }
}
