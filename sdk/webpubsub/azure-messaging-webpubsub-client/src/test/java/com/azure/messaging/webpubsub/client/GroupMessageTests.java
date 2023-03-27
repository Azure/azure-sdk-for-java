// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.messaging.webpubsub.client.models.SendMessageFailedException;
import com.azure.messaging.webpubsub.client.models.SendToGroupOptions;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class GroupMessageTests extends TestBase {

    private static final String HELLO = "hello";

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testSendMessageBeforeStart() {
        WebPubSubClient client = getClient();
        Assertions.assertThrows(SendMessageFailedException.class, () -> client.sendToGroup("testSendMessageBeforeStart", HELLO));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testSendMessage() {
        String groupName = "testSendMessage";
        WebPubSubClient client = getClient();
        try {
            client.start();

            WebPubSubResult result = client.sendToGroup(groupName, HELLO);
            Assertions.assertNotNull(result.getAckId());

            // send with explicit ackId
            long ackId = new Random().nextLong() & Long.MAX_VALUE;
            result = client.sendToGroup(groupName, HELLO, new SendToGroupOptions().setAckId(ackId));
            Assertions.assertEquals(ackId, result.getAckId());
        } finally {
            client.stop();
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testSendDuplicateMessage() {
        String groupName = "testSendDuplicateMessage";
        WebPubSubClient client = getClient();
        try {
            client.start();

            WebPubSubResult result = client.sendToGroup(groupName, HELLO, new SendToGroupOptions().setAckId(10));
            Assertions.assertEquals(10L, result.getAckId());
            Assertions.assertFalse(result.isDuplicated());

            result = client.sendToGroup(groupName, HELLO, new SendToGroupOptions().setAckId(10));
            Assertions.assertEquals(10L, result.getAckId());
            Assertions.assertTrue(result.isDuplicated());
        } finally {
            client.stop();
        }
    }
}
