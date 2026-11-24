// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit.models;

import com.azure.ai.voicelive.models.VoiceLiveSessionResponse;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the session expiration feature on {@link VoiceLiveSessionResponse}
 * ({@code getExpiresAt()} / {@code setExpiresAt(OffsetDateTime)}).
 */
class VoiceLiveSessionExpirationTest {

    @Test
    void testSetAndGetExpiresAt() {
        // Arrange
        OffsetDateTime expiresAt = OffsetDateTime.of(2026, 7, 15, 12, 0, 0, 0, ZoneOffset.UTC);
        VoiceLiveSessionResponse response = new VoiceLiveSessionResponse();

        // Act
        VoiceLiveSessionResponse result = response.setExpiresAt(expiresAt);

        // Assert - fluent setter returns the same instance and value round-trips
        assertSame(response, result);
        assertEquals(expiresAt, response.getExpiresAt());
    }

    @Test
    void testExpiresAtNullByDefault() {
        assertNull(new VoiceLiveSessionResponse().getExpiresAt());
    }

    @Test
    void testSetExpiresAtNullClearsValue() {
        // Arrange
        VoiceLiveSessionResponse response
            = new VoiceLiveSessionResponse().setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        response.setExpiresAt(null);

        // Assert
        assertNull(response.getExpiresAt());
    }

    @Test
    void testExpiresAtJsonRoundTrip() {
        // Arrange
        OffsetDateTime expiresAt = OffsetDateTime.of(2026, 7, 15, 12, 0, 0, 0, ZoneOffset.UTC);
        VoiceLiveSessionResponse response = new VoiceLiveSessionResponse().setExpiresAt(expiresAt);

        // Act
        VoiceLiveSessionResponse deserialized
            = BinaryData.fromObject(response).toObject(VoiceLiveSessionResponse.class);

        // Assert
        assertEquals(expiresAt, deserialized.getExpiresAt());
    }

    @Test
    void testExpiresAtDeserializedFromEpochSeconds() {
        // Arrange - the wire format for expires_at is epoch seconds
        long epochSeconds = 1_784_000_000L;
        String json = "{\"id\":\"sess-expiry\",\"expires_at\":" + epochSeconds + "}";

        // Act
        VoiceLiveSessionResponse response = BinaryData.fromString(json).toObject(VoiceLiveSessionResponse.class);

        // Assert
        assertEquals(OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC),
            response.getExpiresAt());
    }
}
