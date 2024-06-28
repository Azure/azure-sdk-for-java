// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.Configuration;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubClientState;
import com.azure.messaging.webpubsub.client.models.WebPubSubClientCredential;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;

/**
 * Required environment variable for LIVE test:
 * - WEB_PUB_SUB_ENDPOINT: endpoint of the Web PubSub Service
 */
public class TestBase extends com.azure.core.test.TestBase {

    protected static WebPubSubClientBuilder getClientBuilder() {
        return getClientBuilder("user1");
    }

    protected static WebPubSubClientBuilder getClientBuilder(String userId) {
        WebPubSubServiceClient client = new WebPubSubServiceClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get(
                "WEB_PUB_SUB_ENDPOINT"))
            .credential(new AzurePowerShellCredentialBuilder().build())
            .hub("hub1")
            .buildClient();

        // client builder
        return new WebPubSubClientBuilder().credential(new WebPubSubClientCredential(() -> client.getClientAccessToken(
            new GetClientAccessTokenOptions().setUserId(userId)
                .addRole("webpubsub.joinLeaveGroup")
                .addRole("webpubsub.sendToGroup")).getUrl()));
    }

    protected static WebPubSubClient getClient() {
        return getClientBuilder().buildClient();
    }

    protected static void disconnect(WebPubSubClient client, boolean invalidPayload) {
        if (invalidPayload) {
            // use internal API to send an invalid frame, it would cause server to Disconnect
            client.getWebsocketSession().sendTextAsync("invalid", result -> {
                // NOOP
            });
            // server send Disconnected message
            // server send CloseFrame (code=1000) <-- this likely a bug that can be improved by service, it should be 1008
            // client try recover
            // connection open
            // server send CloseFrame (code=1008)
            // client try reconnect
            try {
                Thread.sleep(Duration.ofSeconds(1).toMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            // use internal API to close the socket
            client.getWebsocketSession().closeSocket();
        }

        try {
            // client would recover after some time
            int maxCount = 100;
            for (int i = 0; i < maxCount; ++i) {
                Thread.sleep(100);
                WebPubSubClientState state = client.getClientState();
                if (state == WebPubSubClientState.CONNECTED) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(WebPubSubClientState.CONNECTED, client.getClientState());
    }
}
