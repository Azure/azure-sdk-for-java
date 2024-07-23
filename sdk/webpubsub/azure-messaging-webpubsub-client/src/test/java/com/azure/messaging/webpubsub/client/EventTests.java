// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.models.SendEventOptions;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class EventTests extends TestBase {

    private static final String EVENT_NAME = "event";
    private static final BinaryData HELLO = BinaryData.fromString("text");

    @Disabled("Require event handler configured in Azure")
    @Test
    @LiveOnly
    public void testSendEvent() {
        WebPubSubClient client = getClient();
        try {
            client.start();

            WebPubSubResult result = client.sendEvent(EVENT_NAME, HELLO, WebPubSubDataFormat.TEXT);
            Assertions.assertNotNull(result.getAckId());

            // send with explicit ackId
            long ackId = new Random().nextLong() & Long.MAX_VALUE;
            result = client.sendEvent(EVENT_NAME, HELLO, WebPubSubDataFormat.TEXT, new SendEventOptions()
                .setAckId(ackId));
            Assertions.assertEquals(ackId, result.getAckId());
        } finally {
            client.stop();
        }
    }
}
