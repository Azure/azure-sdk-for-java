// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.messaging.webpubsub.client.exception.SendMessageFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class ClientTests extends TestBase {

    private final WebPubSubClient client = getClient();

    @Test
    @Order(1000)    // last
    public void testClosed() {
        client.close();

        Assertions.assertThrows(SendMessageFailedException.class, () -> client.joinGroup("group"));
    }
}
