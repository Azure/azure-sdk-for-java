// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.credential.AzureKeyCredential;

import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link WebPubSubClientBuilder}
 */
public final class WebPubSubJavaDocCodeSnippets {

    // BUILDER

    public void createAsyncClientConnectionString() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.async
        WebPubSubAsyncClient client = new WebPubSubClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .buildAsyncClient();
        // END: com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.async
    }

    public void createSyncClientConnectionString() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.sync
        WebPubSubClient client = new WebPubSubClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .buildClient();
        // END: com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.sync
    }

    public void createAsyncClientCredentialEndpoint() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclientbuilder.credential.endpoint.async
        WebPubSubAsyncClient client = new WebPubSubClientBuilder()
            .credential(new AzureKeyCredential("<Insert key from Azure Portal>"))
            .endpoint("<Insert endpoint from Azure Portal>")
            .buildAsyncClient();
        // END: com.azure.messaging.webpubsub.webpubsubclientbuilder.credential.endpoint.async
    }

    // ASYNC - HUB

    public void asyncSendToAllVarArgs() {
        WebPubSubAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.String
        client.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.String.2
        client.sendToAll("Hello world!", "connection-id-1", "connection-id-2").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.String.2
    }

    public void asyncSendToAllList() {
        WebPubSubAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List
        client.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!", excludedUsers).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List.2
    }

    public void asyncSendToAllBytesVarArgs() {
        WebPubSubAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.String.2
        client.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.String.2
    }

    public void ayncSendToAllBytesList() {
        WebPubSubAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!".getBytes(), excludedUsers).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.List.2
    }

    // ASYNC - GROUP

    public void asyncGroupClient() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.instance
        WebPubSubAsyncClient client = new WebPubSubClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .hub("chat-portal")
            .buildAsyncClient();

        WebPubSubGroupAsyncClient adminGroup = client.getGroupAsyncClient("admins");
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.instance
    }

    public void groupAsyncSendToAllVarArgs() {
        WebPubSubGroupAsyncClient groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.String
        groupClient.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.String.2
        groupClient.sendToAll("Hello world!", "connection-id-1", "connection-id-2").block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.String.2
    }

    public void groupAsyncSendToAllList() {
        WebPubSubGroupAsyncClient groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.List
        groupClient.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        groupClient.sendToAll("Hello world!", excludedUsers).block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.List.2
    }

    public void groupAsyncSendToAllBytesVarArgs() {
        WebPubSubGroupAsyncClient groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.String
        groupClient.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.String.2
        groupClient.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2").block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.String.2
    }

    public void groupAsyncSendToAllBytesList() {
        WebPubSubGroupAsyncClient groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.List
        groupClient.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        groupClient.sendToAll("Hello world!".getBytes(), excludedUsers).block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.List.2
    }

    // SYNC - HUB

    public void groupClient() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.instance
        WebPubSubClient client = new WebPubSubClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .hub("chat-portal")
            .buildClient();

        WebPubSubGroupClient adminGroup = client.getGroupClient("admins");
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.instance
    }

    public void sendToAllVarArgs() {
        WebPubSubClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.String
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.String.2
        client.sendToAll("Hello world!", "connection-id-1", "connection-id-2");
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.String.2
    }

    public void sendToAllList() {
        WebPubSubClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.List
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!", excludedUsers);
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.List.2
    }

    public void sendToAllBytesVarArgs() {
        WebPubSubClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.String.2
        client.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2");
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.String.2
    }

    public void sendToAllBytesList() {
        WebPubSubClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!".getBytes(), excludedUsers);
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.List.2
    }

    // SYNC - GROUP

    public void groupSendToAllVarArgs() {
        WebPubSubGroupClient client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.String
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.String.2
        client.sendToAll("Hello world!", "connection-id-1", "connection-id-2");
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.String.2
    }

    public void groupSendToAllList() {
        WebPubSubGroupClient client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.List
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!", excludedUsers);
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.List.2
    }

    public void groupSendToAllBytesVarArgs() {
        WebPubSubGroupClient client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.String.2
        client.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2");
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.String.2
    }

    public void groupSendToAllBytesList() {
        WebPubSubGroupClient client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!".getBytes(), excludedUsers);
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.List.2
    }


    // MISC

    private WebPubSubClient getSyncClient() {
        return null;
    }

    private WebPubSubAsyncClient getAsyncClient() {
        return null;
    }

    private List<String> getExcludedUsers() {
        return null;
    }
}
