// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Test for {@link BinaryData}.
 */
public class BinaryDataTest {

    private static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);

    /**
     * Verifies we correctly set values via constructor for {@link BinaryData}.
     */
    @Test
    public void constructorValidValues() {
        // Arrange & Act
        final BinaryData actual = new BinaryData(CONTENTS_BYTES);

        // Assert
        assertArrayEquals(CONTENTS_BYTES, actual.getData());
    }

    /**
     * Verifies {@link BinaryData} constructor for null valeus.
     */
    @Test
    public void constructorNullValidValues() {
        // Arrange, Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new BinaryData(null));
    }
}
