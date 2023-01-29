// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import org.junit.jupiter.api.Test;

public class GroupTests extends TestBase {

    private static final String GROUP = "group";

    private final WebPubSubClient client = getClient();

    @Test
    public void testJoinLeaveGroup() {
        try {
            client.start();

            client.joinGroup(GROUP);

            client.leaveGroup(GROUP);
        } finally {
            client.stop();
        }
    }
}
