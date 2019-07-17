// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import static org.junit.jupiter.api.Assertions.*;

import com.azure.core.amqp.exception.ErrorContext;
import org.junit.jupiter.api.Test;

public class TransportTypeTest {

    /**
     * Verifies that we can parse the transport type from string
     */
    @Test
    public void createFromString() {
        String socketString = "Amqpwebsockets";
        TransportType actual = TransportType.fromString(socketString);

        assertEquals(TransportType.AMQP_WEB_SOCKETS, actual);
    }

    /**
     * Verifies that an exception is thrown when an unknown transport type string is passed.
     */
    @Test
    public void illegalTransportTypeString() {
        final String socketString = "AmqpNonExistent";

        assertThrows(IllegalArgumentException.class, () -> {
            TransportType actual = TransportType.fromString(socketString);
        });
    }
}
