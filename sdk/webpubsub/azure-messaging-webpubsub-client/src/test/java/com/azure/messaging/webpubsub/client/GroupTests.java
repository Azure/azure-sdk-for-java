// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.test.annotation.DoNotRecord;
import org.junit.jupiter.api.Test;

public class GroupTests extends TestBase {

    private static final String GROUP = "group";

    private final WebPubSubClient client = getClient();

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testJoinLeaveGroup() {
        try {
            client.start();

            client.joinGroup(GROUP);

            client.leaveGroup(GROUP);
        } finally {
            client.stop();
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testLeaveGroupBeforeJoin() {
        try {
            client.start();

            client.leaveGroup(GROUP);
        } finally {
            client.stop();
        }
    }
}
