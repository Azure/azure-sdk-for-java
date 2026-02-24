// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AvatarConfigTypes}.
 */
class AvatarConfigTypesTest {

    @Test
    void testVideoAvatarConstant() {
        // Assert
        assertNotNull(AvatarConfigTypes.VIDEO_AVATAR);
        assertEquals("video-avatar", AvatarConfigTypes.VIDEO_AVATAR.toString());
    }

    @Test
    void testPhotoAvatarConstant() {
        // Assert
        assertNotNull(AvatarConfigTypes.PHOTO_AVATAR);
        assertEquals("photo-avatar", AvatarConfigTypes.PHOTO_AVATAR.toString());
    }

    @Test
    void testFromString() {
        // Act
        AvatarConfigTypes videoAvatar = AvatarConfigTypes.fromString("video-avatar");
        AvatarConfigTypes photoAvatar = AvatarConfigTypes.fromString("photo-avatar");

        // Assert
        assertEquals(AvatarConfigTypes.VIDEO_AVATAR, videoAvatar);
        assertEquals(AvatarConfigTypes.PHOTO_AVATAR, photoAvatar);
    }

    @Test
    void testValues() {
        // Act
        Collection<AvatarConfigTypes> values = AvatarConfigTypes.values();

        // Assert
        assertNotNull(values);
        assertTrue(values.size() >= 2);
        assertTrue(values.contains(AvatarConfigTypes.VIDEO_AVATAR));
        assertTrue(values.contains(AvatarConfigTypes.PHOTO_AVATAR));
    }

    @Test
    void testEquality() {
        // Act
        AvatarConfigTypes type1 = AvatarConfigTypes.fromString("video-avatar");
        AvatarConfigTypes type2 = AvatarConfigTypes.VIDEO_AVATAR;

        // Assert
        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    void testCustomValue() {
        // Act
        AvatarConfigTypes custom = AvatarConfigTypes.fromString("custom-avatar");

        // Assert
        assertNotNull(custom);
        assertEquals("custom-avatar", custom.toString());
    }
}
