// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Disabled("Require manual verification")
public class SequenceAckTests extends TestBase {

    // at present, these tests required env variable AZURE_LOG_LEVEL=VERBOSE, and manual verification on the logs
    // we may want to add a message interceptor to client to inspect the wire messages

    @Test
    @LiveOnly
    public void testNewSequenceIdAfterStop() {
        String groupName = "testNewSequenceIdAfterStop";

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        client.start();
        client.joinGroup(groupName);

        client.sendToGroup(groupName, "message1");
        client.sendToGroup(groupName, "message2");

        delay();
        // observe sequenceId=2 in log

        client.stop();

        client.start();
        client.joinGroup(groupName);

        client.sendToGroup(groupName, "message3");

        delay();
        // observe sequenceId=1 in log

        client.stop();
    }

    @Test
    @LiveOnly
    public void testContinuedSequenceIdAfterRecover() {
        String groupName = "testContinuedSequenceIdAfterRecover";

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        client.start();
        client.joinGroup(groupName);

        client.sendToGroup(groupName, "message1");
        client.sendToGroup(groupName, "message2");

        delay();
        // observe sequenceId=2 in log

        disconnect(client, false);

        client.sendToGroup(groupName, "message3");

        delay();
        // observe sequenceId=3 in log

        client.stop();
    }

    @Test
    @LiveOnly
    public void testNewSequenceIdAfterReconnect() {
        String groupName = "testNewSequenceIdAfterReconnect";

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        client.start();
        client.joinGroup(groupName);

        client.sendToGroup(groupName, "message1");
        client.sendToGroup(groupName, "message2");

        delay();
        // observe sequenceId=2 in log

        disconnect(client, true);

        client.sendToGroup(groupName, "message3");

        delay();
        // observe sequenceId=1 in log

        client.stop();
    }

    private static void delay() {
        try {
            Thread.sleep(Duration.ofSeconds(8).toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
