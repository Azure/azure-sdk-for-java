// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PhotoAvatarBaseModes}.
 */
class PhotoAvatarBaseModesTest {

    @Test
    void testVasa1Constant() {
        // Assert
        assertNotNull(PhotoAvatarBaseModes.VASA_1);
        assertEquals("vasa-1", PhotoAvatarBaseModes.VASA_1.toString());
    }

    @Test
    void testFromString() {
        // Act
        PhotoAvatarBaseModes vasa1 = PhotoAvatarBaseModes.fromString("vasa-1");

        // Assert
        assertEquals(PhotoAvatarBaseModes.VASA_1, vasa1);
    }

    @Test
    void testValues() {
        // Act
        Collection<PhotoAvatarBaseModes> values = PhotoAvatarBaseModes.values();

        // Assert
        assertNotNull(values);
        assertTrue(values.size() >= 1);
        assertTrue(values.contains(PhotoAvatarBaseModes.VASA_1));
    }

    @Test
    void testEquality() {
        // Act
        PhotoAvatarBaseModes mode1 = PhotoAvatarBaseModes.fromString("vasa-1");
        PhotoAvatarBaseModes mode2 = PhotoAvatarBaseModes.VASA_1;

        // Assert
        assertEquals(mode1, mode2);
        assertEquals(mode1.hashCode(), mode2.hashCode());
    }

    @Test
    void testCustomValue() {
        // Act
        PhotoAvatarBaseModes custom = PhotoAvatarBaseModes.fromString("vasa-2");

        // Assert
        assertNotNull(custom);
        assertEquals("vasa-2", custom.toString());
    }
}
