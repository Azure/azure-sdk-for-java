// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.models.SendEventOptions;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class EventTests extends TestBase {

    private static final String EVENT_NAME = "event";
    private static final BinaryData HELLO = BinaryData.fromString("text");

    @Disabled
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testSendEvent() {
        // require event handler configured in Azure

        WebPubSubClient client = getClient();
        try {
            client.start();

            WebPubSubResult result = client.sendEvent(EVENT_NAME, HELLO, WebPubSubDataType.TEXT);
            Assertions.assertNotNull(result.getAckId());

            // send with explicit ackId
            long ackId = new Random().nextLong() & Long.MAX_VALUE;
            result = client.sendEvent(EVENT_NAME, HELLO, WebPubSubDataType.TEXT, new SendEventOptions().setAckId(ackId));
            Assertions.assertEquals(ackId, result.getAckId());
        } finally {
            client.stop();
        }
    }
}
