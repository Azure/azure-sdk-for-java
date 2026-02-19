// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AvatarOutputProtocol}.
 */
class AvatarOutputProtocolTest {

    @Test
    void testWebRTCConstant() {
        // Assert
        assertNotNull(AvatarOutputProtocol.WEBRTC);
        assertEquals("webrtc", AvatarOutputProtocol.WEBRTC.toString());
    }

    @Test
    void testWebSocketConstant() {
        // Assert
        assertNotNull(AvatarOutputProtocol.WEBSOCKET);
        assertEquals("websocket", AvatarOutputProtocol.WEBSOCKET.toString());
    }

    @Test
    void testFromString() {
        // Act
        AvatarOutputProtocol webrtc = AvatarOutputProtocol.fromString("webrtc");
        AvatarOutputProtocol websocket = AvatarOutputProtocol.fromString("websocket");

        // Assert
        assertEquals(AvatarOutputProtocol.WEBRTC, webrtc);
        assertEquals(AvatarOutputProtocol.WEBSOCKET, websocket);
    }

    @Test
    void testValues() {
        // Act
        Collection<AvatarOutputProtocol> values = AvatarOutputProtocol.values();

        // Assert
        assertNotNull(values);
        assertTrue(values.size() >= 2);
        assertTrue(values.contains(AvatarOutputProtocol.WEBRTC));
        assertTrue(values.contains(AvatarOutputProtocol.WEBSOCKET));
    }

    @Test
    void testEquality() {
        // Act
        AvatarOutputProtocol protocol1 = AvatarOutputProtocol.fromString("webrtc");
        AvatarOutputProtocol protocol2 = AvatarOutputProtocol.WEBRTC;

        // Assert
        assertEquals(protocol1, protocol2);
        assertEquals(protocol1.hashCode(), protocol2.hashCode());
    }

    @Test
    void testCustomValue() {
        // Act
        AvatarOutputProtocol custom = AvatarOutputProtocol.fromString("custom-protocol");

        // Assert
        assertNotNull(custom);
        assertEquals("custom-protocol", custom.toString());
    }
}
