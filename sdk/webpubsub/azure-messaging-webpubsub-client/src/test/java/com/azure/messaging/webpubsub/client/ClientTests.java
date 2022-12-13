// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

public class ClientTests {

    public static void main(String[] args) throws Exception {
        runForSyncClient();

        runForAsyncClient();

        Thread.sleep(5 * 1000);
    }

    private static void runForSyncClient() throws InterruptedException {
        // client
        WebPubSubClient client = clientBuilder().buildClient();

        client.start();

        Thread receiveThread = new Thread(() -> {
            client.receiveGroupMessages().stream().forEach(message -> {
                System.out.println("group: " + message.getGroup() + ", data: " + message.getData());
            });
        });
        receiveThread.start();

        Thread connectThread = new Thread(() -> {
            client.receiveConnectedEvents().stream().forEach(event -> {
                System.out.println("connected: " + event.getConnectionId());
            });
        });
        connectThread.start();

        client.joinGroup("group1");

        // send message to group1
        printResult(client.sendMessageToGroup("group1",
            BinaryData.fromString("abc"), WebPubSubDataType.TEXT));

        // send message to group1
        printResult(client.sendMessageToGroup("group1",
            BinaryData.fromObject(Map.of("hello", "world")), WebPubSubDataType.JSON));

        client.leaveGroup("group1");

        client.close();

        receiveThread.join();
        connectThread.join();
    }

    private static void runForAsyncClient() {
        // client
        WebPubSubAsyncClient asyncClient = clientBuilder().buildAsyncClient();

        // group data messages
        asyncClient.receiveGroupMessages().subscribe(message -> {
            System.out.println("group: " + message.getGroup() + ", data: " + message.getData());
        });

        // connected events
        asyncClient.receiveConnectedEvents().subscribe(event -> {
            System.out.println("connected: " + event.getConnectionId());
        });

        // disconnected events
        asyncClient.receiveDisconnectedEvents().subscribe(event -> {
            System.out.println("disconnected: " + event.getReason());
        });

        // start
        asyncClient.start().block();

        // join group1
        printResult(asyncClient.joinGroup("group1"));

        // send message to group1
        printResult(asyncClient.sendMessageToGroup("group1",
            BinaryData.fromString("abc"), WebPubSubDataType.TEXT));

        // join group2
        printResult(asyncClient.joinGroup("group2"));

        // send message to group1
        printResult(asyncClient.sendMessageToGroup("group1",
            BinaryData.fromObject(Map.of("hello", "world")), WebPubSubDataType.JSON));

        // send message to group2
        printResult(asyncClient.sendMessageToGroup("group2",
            BinaryData.fromObject(Map.of("hello", "group2")), WebPubSubDataType.JSON));

        // leave group1
        printResult(asyncClient.leaveGroup("group1"));

        // send message to group2
        printResult(asyncClient.sendMessageToGroup("group2",
            BinaryData.fromString("binary"), WebPubSubDataType.BINARY));

        asyncClient.stop().block();


        // start for more data
        asyncClient.start().block();

        printResult(asyncClient.joinGroup("group1"));

        printResult(asyncClient.sendMessageToGroup("group1",
            BinaryData.fromString("dfg"), WebPubSubDataType.TEXT));


        // close
        asyncClient.close().block();
    }

    private static WebPubSubClientBuilder clientBuilder() {
        WebPubSubServiceClient client = new WebPubSubServiceClientBuilder()
            .connectionString(Configuration.getGlobalConfiguration().get("CONNECTION_STRING"))
            .hub("test_hub")
            .buildClient();

        Mono<WebPubSubClientAccessToken> accessToken = Mono.just(client.getClientAccessToken(new GetClientAccessTokenOptions()
            .setUserId("weidxu")
            .addRole("webpubsub.joinLeaveGroup")
            .addRole("webpubsub.sendToGroup"))).subscribeOn(Schedulers.boundedElastic());

        // client builder
        return new WebPubSubClientBuilder()
            .credential(new WebPubSubClientCredential(accessToken
                .map(WebPubSubClientAccessToken::getUrl)));
    }

    private static void printResult(Mono<WebPubSubResult> result) {
        printResult(result.block());
    }

    private static void printResult(WebPubSubResult result) {
        System.out.println("ack: " + result.getAckId());
    }
}
