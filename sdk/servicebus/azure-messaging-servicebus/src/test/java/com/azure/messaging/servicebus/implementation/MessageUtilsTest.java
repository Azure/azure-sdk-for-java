// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link MessageUtils}.
 */
class MessageUtilsTest {
    @Test
    void convertDotNetTicksToInstant() {
        // Arrange
        final String dateTime = "2016-11-30T20:57:01.4638052Z";
        final OffsetDateTime expected = Instant.parse(dateTime).atOffset(ZoneOffset.UTC);
        final long dotNetTicks = 636161362214638052L;

        // Act
        final OffsetDateTime actual = MessageUtils.convertDotNetTicksToOffsetDateTime(dotNetTicks);

        // Assert
        assertEquals(expected, actual, "DateTime conversion from DotNet to Java failed");
    }

    @Test
    void convertDotNetBytesToUUID() {
        // Arrange
        final String guidString = "b5dc4a70-ac5d-43b3-b132-ec8fcdac3a9d";
        // Java bytes are signed where as dotNet bytes are unsigned. No problem type casting larger than 127 unsigned
        // bytes to java signed bytes as we are interested only in the individual bits for UUID conversion.
        final byte[] dotNetGuidBytes = {112, 74, (byte) 220, (byte) 181, 93, (byte) 172, (byte) 179, 67, (byte) 177,
            50, (byte) 236, (byte) 143, (byte) 205, (byte) 172, 58, (byte) 157};

        // Act
        final UUID convertedGuid = MessageUtils.convertDotNetBytesToUUID(dotNetGuidBytes);

        // Assert
        assertEquals(guidString, convertedGuid.toString(), "UUID conversion from DotNet to Java failed");
    }

    @Test
    void convertUUIDToDotNetBytes() {
        // Arrange
        String guidString = "b5dc4a70-ac5d-43b3-b132-ec8fcdac3a9d";
        UUID javaGuid = UUID.fromString(guidString);
        byte[] dotNetGuidBytes = {112, 74, (byte) 220, (byte) 181, 93, (byte) 172, (byte) 179, 67, (byte) 177, 50, (byte) 236, (byte) 143, (byte) 205, (byte) 172, 58, (byte) 157};

        // Act
        byte[] convertedBytes = MessageUtils.convertUUIDToDotNetBytes(javaGuid);

        // Assert
        assertArrayEquals(dotNetGuidBytes, convertedBytes, "UUID conversion from Java to DotNet failed");
    }
}
