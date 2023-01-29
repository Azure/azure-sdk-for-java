// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.exception.SendMessageFailedException;
import com.azure.messaging.webpubsub.client.models.SendToGroupOptions;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class GroupMessageTests extends TestBase {

    private static final String GROUP = "group";
    private static final BinaryData HELLO = BinaryData.fromString("hello");

    private final WebPubSubClient client = getClient();

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testSendMessageBeforeStart() {
        Assertions.assertThrows(SendMessageFailedException.class, () -> client.sendToGroup(GROUP, HELLO, WebPubSubDataType.TEXT));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testSendMessage() {
        try {
            client.start();

            WebPubSubResult result = client.sendToGroup(GROUP, HELLO, WebPubSubDataType.TEXT);
            Assertions.assertNotNull(result.getAckId());

            // send with explicit ackId
            long ackId = new Random().nextLong() & Long.MAX_VALUE;
            result = client.sendToGroup(GROUP, HELLO, WebPubSubDataType.TEXT, new SendToGroupOptions().setAckId(ackId));
            Assertions.assertEquals(ackId, result.getAckId());
        } finally {
            client.stop();
        }
    }
}
