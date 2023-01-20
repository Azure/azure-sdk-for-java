// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import reactor.core.publisher.Mono;

import java.util.Map;

public class MainTests extends TestBase {

    public static void main(String[] args) throws Exception {
        runForSyncClient();

        runForAsyncClient();

        Thread.sleep(5 * 1000);
    }

    private static void runForSyncClient() throws InterruptedException {
        // client
        WebPubSubClient client = getClient();

        client.start();

        Thread receiveThread = new Thread(() -> {
            client.receiveGroupMessageEvents().stream().forEach(event -> {
                System.out.println("group: " + event.getMessage().getGroup() + ", data: " + event.getMessage().getData());
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
        printResult(client.sendToGroup("group1",
            BinaryData.fromString("abc"), WebPubSubDataType.TEXT));

        // send message to group1
        printResult(client.sendToGroup("group1",
            BinaryData.fromObject(Map.of("hello", "world")), WebPubSubDataType.JSON));

        client.leaveGroup("group1");

        client.close();

        receiveThread.join();
        connectThread.join();
    }

    private static void runForAsyncClient() {
        // client
        WebPubSubAsyncClient asyncClient = getClientBuilder().buildAsyncClient();

        // group data messages
        asyncClient.receiveGroupMessageEvents().filter(event -> event.getMessage().getGroup().equals("group1")).subscribe(event -> {
            System.out.println("group1: " + event.getMessage().getGroup() + ", data: " + event.getMessage().getData());
        });
        asyncClient.receiveGroupMessageEvents().filter(event -> event.getMessage().getGroup().equals("group2")).subscribe(event -> {
            System.out.println("group2: " + event.getMessage().getGroup() + ", data: " + event.getMessage().getData());
        });

        // connected events
        asyncClient.receiveConnectedEvents().subscribe(event -> {
            System.out.println("connected: " + event.getConnectionId());
        });

        // disconnected events
        asyncClient.receiveDisconnectedEvents().subscribe(event -> {
            System.out.println("disconnected: " + event.getDisconnectedMessage().getReason());
        });

        // start
        asyncClient.start().block();

        // join group1
        printResult(asyncClient.joinGroup("group1"));

        // send message to group1
        printResult(asyncClient.sendToGroup("group1",
            BinaryData.fromString("abc"), WebPubSubDataType.TEXT));

        // join group2
        printResult(asyncClient.joinGroup("group2"));

        // send message to group1
        printResult(asyncClient.sendToGroup("group1",
            BinaryData.fromObject(Map.of("hello", "world")), WebPubSubDataType.JSON));

        // send message to group2
        printResult(asyncClient.sendToGroup("group2",
            BinaryData.fromObject(Map.of("hello", "group2")), WebPubSubDataType.JSON));

        // leave group1
        printResult(asyncClient.leaveGroup("group1"));

        // send message to group2
        printResult(asyncClient.sendToGroup("group2",
            BinaryData.fromString("binary"), WebPubSubDataType.BINARY));

        asyncClient.stop().block();


        // start for more data
        asyncClient.start().block();

        printResult(asyncClient.joinGroup("group1"));

        printResult(asyncClient.sendToGroup("group1",
            BinaryData.fromString("dfg"), WebPubSubDataType.TEXT));


        // close
        asyncClient.closeAsync().block();
    }

    private static void printResult(Mono<WebPubSubResult> result) {
        printResult(result.block());
    }

    private static void printResult(WebPubSubResult result) {
        System.out.println("ack: " + result.getAckId());
    }
}
