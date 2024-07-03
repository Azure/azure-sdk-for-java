// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Test;

public class GroupTests extends TestBase {

    private static final String GROUP = "group";

    @Test
    @LiveOnly
    public void testJoinLeaveGroup() {
        WebPubSubClient client = getClient();
        try {
            client.start();

            client.joinGroup(GROUP);

            client.leaveGroup(GROUP);
        } finally {
            client.stop();
        }
    }

    @Test
    @LiveOnly
    public void testLeaveGroupBeforeJoin() {
        WebPubSubClient client = getClient();
        try {
            client.start();

            client.leaveGroup(GROUP);
        } finally {
            client.stop();
        }
    }
}
