// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.BinaryData;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.WebPubSubServiceAsyncClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubClientState;
import com.azure.messaging.webpubsub.client.models.ConnectFailedException;
import com.azure.messaging.webpubsub.client.models.ConnectedEvent;
import com.azure.messaging.webpubsub.client.models.DisconnectedEvent;
import com.azure.messaging.webpubsub.client.models.GroupMessageEvent;
import com.azure.messaging.webpubsub.client.models.WebPubSubClientCredential;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import com.azure.messaging.webpubsub.client.models.WebPubSubProtocolType;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ClientTests extends TestBase {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testClientState() {
        WebPubSubAsyncClient asyncClient = getClientBuilder().buildAsyncClient();

        Assertions.assertEquals(WebPubSubClientState.STOPPED, asyncClient.getClientState());

        Mono<Void> startMono = asyncClient.start().doOnSuccess(ignored -> {
            Assertions.assertEquals(WebPubSubClientState.CONNECTED, asyncClient.getClientState());
        });
        // test transient state of CONNECTING
        Mono<Void> verifyMono = Mono.delay(Duration.ofMillis(10)).then().doOnSuccess(ignored -> {
            Assertions.assertEquals(WebPubSubClientState.CONNECTING, asyncClient.getClientState());
        });
        startMono.and(verifyMono).block();

        asyncClient.stop().doOnSuccess(ignored -> {
            Assertions.assertEquals(WebPubSubClientState.STOPPED, asyncClient.getClientState());
        }).block();

        asyncClient.start().block();
        Assertions.assertEquals(WebPubSubClientState.CONNECTED, asyncClient.getClientState());
        asyncClient.stop().block();
        Assertions.assertEquals(WebPubSubClientState.STOPPED, asyncClient.getClientState());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testTwoClients() throws InterruptedException {
        String groupName = "testTwoClients";
        CountDownLatch latch = new CountDownLatch(1);

        WebPubSubClient client1 = getClientBuilder("user1")
            .buildClient();

        client1.addOnGroupMessageEventHandler(event -> {
            latch.countDown();
        });

        WebPubSubClient client2 = getClientBuilder("user2")
            .buildClient();

        client1.start();
        client2.start();

        client1.joinGroup(groupName);
        client2.joinGroup(groupName);

        client2.sendToGroup(groupName, BinaryData.fromString("hello"), WebPubSubDataFormat.TEXT);

        client2.stop();

        boolean success = latch.await(10, TimeUnit.SECONDS);
        client1.stop();

        Assertions.assertTrue(success);
        Assertions.assertEquals(0, latch.getCount());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testClientCloseable() {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch stoppedLatch = new CountDownLatch(1);
        AtomicBoolean stoppedEventReceived = new AtomicBoolean(false);
        AtomicBoolean disconnectedEventReceived = new AtomicBoolean(false);

        try (WebPubSubClient client = getClientBuilder().buildClient()) {
            client.addOnStoppedEventHandler(stoppedEvent -> {
                stoppedEventReceived.set(true);
                stoppedLatch.countDown();
            });
            client.addOnConnectedEventHandler(connectedEvent -> {
                connectedLatch.countDown();
            });
            client.addOnDisconnectedEventHandler(disconnectedEvent -> {
                disconnectedEventReceived.set(true);
            });

            client.start();

            connectedLatch.countDown();

            // stop not called explicitly
        }

        stoppedLatch.countDown();

        // verify client stopped via Closeable
        Assertions.assertTrue(stoppedEventReceived.get());
        Assertions.assertTrue(disconnectedEventReceived.get());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testStopAndStart() throws InterruptedException {
        String groupName = "testStopAndStart";
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        client.addOnGroupMessageEventHandler(event -> {
            if (latch1.getCount() > 0) {
                latch1.countDown();
            }
            if (latch2.getCount() > 0) {
                latch2.countDown();
            }
        });

        // start and stop
        client.start();
        client.joinGroup(groupName);
        client.sendToGroup(groupName, BinaryData.fromString("hello"), WebPubSubDataFormat.TEXT);

        Assertions.assertNotNull(client.getConnectionId());

        boolean success = latch2.await(10, TimeUnit.SECONDS);
        client.stop();

        Assertions.assertNull(client.getConnectionId());

        Assertions.assertTrue(success);
        Assertions.assertEquals(0, latch1.getCount());

        client.start();
        client.joinGroup(groupName);
        client.sendToGroup(groupName, BinaryData.fromString("hello"), WebPubSubDataFormat.TEXT);

        success = latch2.await(10, TimeUnit.SECONDS);
        client.stop();

        Assertions.assertTrue(success);
        Assertions.assertEquals(0, latch2.getCount());

        Assertions.assertNull(client.getConnectionId());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testConcurrentStop() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        // start and stop
        client.start();

        Thread thread = new Thread(() -> {
            client.stop();
            latch.countDown();
        });
        Thread thread2 = new Thread(() -> {
            client.stop();
            latch.countDown();
        });
        thread.start();
        thread2.start();

        client.stop();

        thread.join(1000);
        thread2.join(1000);

        boolean success = latch.await(1, TimeUnit.SECONDS);

        Assertions.assertTrue(success);
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testStopWhenStopped() {
        WebPubSubClient client = getClientBuilder()
            .buildClient();

        client.stop();
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testStopBeforeConnected() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        Thread thread = new Thread(() -> {
            latch.countDown();
            client.start();
        });
        thread.start();

        // wait for "start" thread, before "stop"
        latch.await(1, TimeUnit.SECONDS);
        Thread.sleep(10);

        client.stop();

        thread.join(1000);
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testNoCredential() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            WebPubSubClient client = new WebPubSubClientBuilder().buildClient();
        });
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testBothCredential() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            WebPubSubClient client = new WebPubSubClientBuilder()
                .credential(new WebPubSubClientCredential(() -> "mock"))
                .clientAccessUrl("mock")
                .buildClient();
        });
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testInvalidCredential() {
        WebPubSubServiceAsyncClient client = new WebPubSubServiceClientBuilder()
            .connectionString(Configuration.getGlobalConfiguration().get("CONNECTION_STRING"))
            .hub("hub1")
            .buildAsyncClient();

        Mono<WebPubSubClientAccessToken> accessToken = client.getClientAccessToken(new GetClientAccessTokenOptions()
            .setUserId("user1")
            .addRole("webpubsub.joinLeaveGroup")
            .addRole("webpubsub.sendToGroup"));

        String invalidClientAccessUrl = accessToken.block().getUrl() + "invalid";

        AtomicBoolean stoppedEventReceived = new AtomicBoolean(false);
        AtomicBoolean disconnectedEventReceived = new AtomicBoolean(false);

        Assertions.assertThrows(ConnectFailedException.class, () -> {
            WebPubSubClient c = new WebPubSubClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId("AppInvalidCredential"))
                .clientAccessUrl(invalidClientAccessUrl)
                .buildClient();

            c.addOnStoppedEventHandler(stoppedEvent -> stoppedEventReceived.set(true));
            c.addOnDisconnectedEventHandler(disconnectedEvent -> disconnectedEventReceived.set(true));

            c.start();
        });

        Assertions.assertFalse(stoppedEventReceived.get());
        Assertions.assertFalse(disconnectedEventReceived.get());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testClientEvents() throws InterruptedException {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch stoppedLatch = new CountDownLatch(1);
        AtomicBoolean stoppedEventReceived = new AtomicBoolean(false);
        AtomicBoolean disconnectedEventReceived = new AtomicBoolean(false);
        AtomicBoolean connectedEventReceived = new AtomicBoolean(false);

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        client.addOnStoppedEventHandler(stoppedEvent -> {
            stoppedEventReceived.set(true);
            stoppedLatch.countDown();
        });
        client.addOnDisconnectedEventHandler(disconnectedEvent -> disconnectedEventReceived.set(true));
        client.addOnConnectedEventHandler(connectedEvent -> {
            connectedEventReceived.set(true);
            connectedLatch.countDown();
        });

        client.start();
        connectedLatch.await(1, TimeUnit.SECONDS);

        Assertions.assertTrue(connectedEventReceived.get());
        Assertions.assertFalse(stoppedEventReceived.get());
        Assertions.assertFalse(disconnectedEventReceived.get());

        client.stop();

        stoppedLatch.await(1, TimeUnit.SECONDS);

        Assertions.assertTrue(stoppedEventReceived.get());
        Assertions.assertTrue(disconnectedEventReceived.get());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testClientListener() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        List<String> messageReceived = new ArrayList<>();

        Consumer<GroupMessageEvent> eventHandler = event -> {
            messageReceived.add(event.getData().toString());
            latch.countDown();
        };

        // add handler
        client.addOnGroupMessageEventHandler(eventHandler);

        client.start();
        client.joinGroup("testClientListener");
        client.sendToGroup("testClientListener", "message1");

        latch.await(1, TimeUnit.SECONDS);

        // remove handler, so handler should get no more event
        client.removeOnGroupMessageEventHandler(eventHandler);

        client.sendToGroup("testClientListener", "message2");

        // remove non-exist, should not have exception
        client.removeOnGroupMessageEventHandler(eventHandler);
        client.removeOnGroupMessageEventHandler(event -> {
            // NOOP
        });

        client.stop();

        Assertions.assertEquals(1, messageReceived.size());
        Assertions.assertEquals("message1", messageReceived.iterator().next());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testConnectedDisconnectedEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<String> eventReceived = new ArrayList<>();

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        client.addOnConnectedEventHandler(event -> eventReceived.add(event.getClass().getSimpleName()));
        client.addOnDisconnectedEventHandler(event -> eventReceived.add(event.getClass().getSimpleName()));
        client.addOnStoppedEventHandler(event -> latch.countDown());

        client.start();
        client.stop();

        latch.await(1, TimeUnit.SECONDS);

        Assertions.assertEquals(2, eventReceived.size());
        Assertions.assertEquals(ConnectedEvent.class.getSimpleName(), eventReceived.get(0));
        Assertions.assertEquals(DisconnectedEvent.class.getSimpleName(), eventReceived.get(1));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testServerDisconnectedOnInvalidPayload() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<String> eventReceived = new ArrayList<>();

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        client.addOnConnectedEventHandler(event -> eventReceived.add(event.getClass().getSimpleName()));
        client.addOnDisconnectedEventHandler(event -> eventReceived.add(event.getClass().getSimpleName()));
        client.addOnStoppedEventHandler(event -> latch.countDown());

        client.start();

        disconnect(client, true);

        client.stop();

        latch.await(1, TimeUnit.SECONDS);
        Assertions.assertEquals(0, latch.getCount());

        Assertions.assertEquals(4, eventReceived.size());
        Assertions.assertEquals(ConnectedEvent.class.getSimpleName(), eventReceived.get(0));
        Assertions.assertEquals(DisconnectedEvent.class.getSimpleName(), eventReceived.get(1));
        Assertions.assertEquals(ConnectedEvent.class.getSimpleName(), eventReceived.get(2));
        Assertions.assertEquals(DisconnectedEvent.class.getSimpleName(), eventReceived.get(3));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testClientRecoveryOnSocketClose() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<String> eventReceived = new ArrayList<>();

        AtomicReference<String> connectionId = new AtomicReference<>();

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        client.addOnConnectedEventHandler(event -> {
            connectionId.compareAndSet(null, event.getConnectionId());
            eventReceived.add(event.getClass().getSimpleName());
        });
        client.addOnDisconnectedEventHandler(event -> eventReceived.add(event.getClass().getSimpleName()));
        client.addOnStoppedEventHandler(event -> latch.countDown());

        client.start();

        disconnect(client, false);

        // make sure connection indeed works
        WebPubSubResult result = client.sendToGroup("testClientRecoveryOnSocketClose", "message");
        Assertions.assertNotNull(result.getAckId());

        // validate that connectionId does not change
        Assertions.assertEquals(connectionId.get(), client.getConnectionId());

        client.stop();

        latch.await(1, TimeUnit.SECONDS);
        Assertions.assertEquals(0, latch.getCount());

        Assertions.assertEquals(2, eventReceived.size());
        Assertions.assertEquals(ConnectedEvent.class.getSimpleName(), eventReceived.get(0));
        Assertions.assertEquals(DisconnectedEvent.class.getSimpleName(), eventReceived.get(1));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testClientReconnectOnSocketClose() throws InterruptedException {
        String groupName = "testClientReconnectOnSocketClose";
        CountDownLatch latch = new CountDownLatch(1);
        List<String> eventReceived = new ArrayList<>();

        AtomicReference<String> connectionId = new AtomicReference<>();

        WebPubSubClient client = getClientBuilder()
            .protocol(WebPubSubProtocolType.JSON_PROTOCOL)
            .autoReconnect(true)
            .buildClient();

        client.addOnConnectedEventHandler(event -> {
            connectionId.compareAndSet(null, event.getConnectionId());
            eventReceived.add(event.getClass().getSimpleName());
        });
        client.addOnDisconnectedEventHandler(event -> eventReceived.add(event.getClass().getSimpleName()));
        client.addOnStoppedEventHandler(event -> latch.countDown());
        // RejoinGroupFailedEvent should not happen
        client.addOnRejoinGroupFailedEventHandler(event -> eventReceived.add(event.getClass().getSimpleName()));

        client.start();
        client.joinGroup(groupName);

        disconnect(client, false);

        // make sure connection indeed works
        WebPubSubResult result = client.sendToGroup(groupName, "message");
        Assertions.assertNotNull(result.getAckId());

        // validate that connectionId changed after RECONNECT
        Assertions.assertNotEquals(connectionId.get(), client.getConnectionId());

        client.stop();

        latch.await(1, TimeUnit.SECONDS);
        Assertions.assertEquals(0, latch.getCount());

        // after RECONNECT, it is a different connectionId, hence a different connection
        // therefore, 2 pairs of ConnectedEvent and DisconnectedEvent
        Assertions.assertEquals(4, eventReceived.size());
        Assertions.assertEquals(ConnectedEvent.class.getSimpleName(), eventReceived.get(0));
        Assertions.assertEquals(DisconnectedEvent.class.getSimpleName(), eventReceived.get(1));
        Assertions.assertEquals(ConnectedEvent.class.getSimpleName(), eventReceived.get(2));
        Assertions.assertEquals(DisconnectedEvent.class.getSimpleName(), eventReceived.get(3));
    }


    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testClientStopOnSocketClose() throws InterruptedException {
        String groupName = "testClientStopOnSocketClose";
        CountDownLatch latch = new CountDownLatch(1);
        List<String> eventReceived = new ArrayList<>();

        AtomicReference<String> connectionId = new AtomicReference<>();

        WebPubSubClient client = getClientBuilder()
            .protocol(WebPubSubProtocolType.JSON_PROTOCOL)
            .autoReconnect(false)
            .buildClient();

        client.addOnConnectedEventHandler(event -> {
            connectionId.compareAndSet(null, event.getConnectionId());
            eventReceived.add(event.getClass().getSimpleName());
        });
        client.addOnDisconnectedEventHandler(event -> eventReceived.add(event.getClass().getSimpleName()));
        client.addOnStoppedEventHandler(event -> latch.countDown());

        client.start();

        // use internal API to close the socket
        client.getWebsocketSession().closeSocket();

        // wait for client to stop, as autoReconnect = false

        latch.await(1, TimeUnit.SECONDS);
        Assertions.assertEquals(0, latch.getCount());

        Assertions.assertEquals(2, eventReceived.size());
        Assertions.assertEquals(ConnectedEvent.class.getSimpleName(), eventReceived.get(0));
        Assertions.assertEquals(DisconnectedEvent.class.getSimpleName(), eventReceived.get(1));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testStartInStoppedEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        WebPubSubClient client = getClientBuilder()
            .buildClient();

        client.addOnStoppedEventHandler(event -> {
            if (latch.getCount() > 0) {
                latch.countDown();
                client.start();
            }
        });

        client.start();
        client.stop();

        latch.await(1, TimeUnit.SECONDS);
        Assertions.assertEquals(0, latch.getCount());

        client.stop();
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testProtocol() {
        WebPubSubClient client = getClientBuilder()
            .protocol(WebPubSubProtocolType.JSON_PROTOCOL)
            .buildClient();

        client.start();
        client.joinGroup("testProtocol");
        client.sendToGroup("testProtocol", "message");
        client.stop();
    }
}
