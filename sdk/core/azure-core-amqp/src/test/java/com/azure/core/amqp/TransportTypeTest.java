// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import org.junit.Assert;
import org.junit.Test;

public class TransportTypeTest {

    /**
     * Verifies that we can parse the transport type from string
     */
    @Test
    public void createFromString() {
        String socketString = "Amqpwebsockets";
        TransportType actual = TransportType.fromString(socketString);

        Assert.assertEquals(TransportType.AMQP_WEB_SOCKETS, actual);
    }

    /**
     * Verifies that an exception is thrown when an unknown transport type string is passed.
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalTransportTypeString() {
        String socketString = "AmqpNonExistent";

        TransportType actual = TransportType.fromString(socketString);
    }
}
