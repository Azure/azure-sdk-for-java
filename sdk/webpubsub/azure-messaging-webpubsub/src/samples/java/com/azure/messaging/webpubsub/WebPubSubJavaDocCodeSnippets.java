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
        WebPubSubAsyncServiceClient client = new WebPubSubClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .buildAsyncClient();
        // END: com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.async
    }

    public void createSyncClientConnectionString() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.sync
        WebPubSubServiceClient client = new WebPubSubClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .buildClient();
        // END: com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.sync
    }

    public void createAsyncClientCredentialEndpoint() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclientbuilder.credential.endpoint.async
        WebPubSubAsyncServiceClient client = new WebPubSubClientBuilder()
            .credential(new AzureKeyCredential("<Insert key from Azure Portal>"))
            .endpoint("<Insert endpoint from Azure Portal>")
            .buildAsyncClient();
        // END: com.azure.messaging.webpubsub.webpubsubclientbuilder.credential.endpoint.async
    }

    // ASYNC - HUB

    public void asyncSendToAllVarArgs() {
        WebPubSubAsyncServiceClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.String
        client.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.String
    }

    public void asyncSendToAllList() {
        WebPubSubAsyncServiceClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List
        client.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List.2
//        List<String> excludedUsers = getExcludedUsers();
//        client.sendToAll("Hello world!", excludedUsers).block();
//        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List.2
    }

    public void asyncSendToAllBytesVarArgs() {
        WebPubSubAsyncServiceClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.String
    }

    public void ayncSendToAllBytesList() {
        WebPubSubAsyncServiceClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.List

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.List.2
//        List<String> excludedUsers = getExcludedUsers();
//        client.sendToAll("Hello world!".getBytes(), excludedUsers).block();
//        // END: com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.List.2
    }

    // ASYNC - GROUP

    public void asyncGroupClient() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.instance
        WebPubSubAsyncServiceClient client = new WebPubSubClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .hub("chat-portal")
            .buildAsyncClient();

        WebPubSubAsyncGroup adminGroup = client.getAsyncGroup("admins");
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.instance
    }

    public void groupAsyncSendToAllVarArgs() {
        WebPubSubAsyncGroup groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.String
        groupClient.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.String

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.String.2
//        groupClient.sendToAll("Hello world!", "connection-id-1", "connection-id-2").block();
//        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.String.2
    }

    public void groupAsyncSendToAllList() {
        WebPubSubAsyncGroup groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.List
        groupClient.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.List

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.List.2
//        List<String> excludedUsers = getExcludedUsers();
//        groupClient.sendToAll("Hello world!", excludedUsers).block();
//        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAll.String.List.2
    }

    public void groupAsyncSendToAllBytesVarArgs() {
        WebPubSubAsyncGroup groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.String
        groupClient.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.String

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.String.2
//        groupClient.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2").block();
//        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.String.2
    }

    public void groupAsyncSendToAllBytesList() {
        WebPubSubAsyncGroup groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.List
        groupClient.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.List

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.List.2
//        List<String> excludedUsers = getExcludedUsers();
//        groupClient.sendToAll("Hello world!".getBytes(), excludedUsers).block();
//        // END: com.azure.messaging.webpubsub.webpubsubgroupasyncclient.sendToAllBytes.byte.List.2
    }

    // SYNC - HUB

    public void groupClient() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.instance
        WebPubSubServiceClient client = new WebPubSubClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .hub("chat-portal")
            .buildClient();

        WebPubSubGroup adminGroup = client.getGroup("admins");
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.instance
    }

    public void sendToAllVarArgs() {
        WebPubSubServiceClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.String
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.String

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.String.2
//        client.sendToAll("Hello world!", "connection-id-1", "connection-id-2");
//        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.String.2
    }

    public void sendToAllList() {
        WebPubSubServiceClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.List
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.List

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.List.2
//        List<String> excludedUsers = getExcludedUsers();
//        client.sendToAll("Hello world!", excludedUsers);
//        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAll.String.List.2
    }

    public void sendToAllBytesVarArgs() {
        WebPubSubServiceClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.String

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.String.2
//        client.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2");
//        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.String.2
    }

    public void sendToAllBytesList() {
        WebPubSubServiceClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.List

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.List.2
//        List<String> excludedUsers = getExcludedUsers();
//        client.sendToAll("Hello world!".getBytes(), excludedUsers);
//        // END: com.azure.messaging.webpubsub.webpubsubclient.sendToAllBytes.byte.List.2
    }

    // SYNC - GROUP

    public void groupSendToAllVarArgs() {
        WebPubSubGroup client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.String
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.String

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.String.2
//        client.sendToAll("Hello world!", "connection-id-1", "connection-id-2");
//        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.String.2
    }

    public void groupSendToAllList() {
        WebPubSubGroup client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.List
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.List

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.List.2
//        List<String> excludedUsers = getExcludedUsers();
//        client.sendToAll("Hello world!", excludedUsers);
//        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.List.2
    }

    public void groupSendToAllBytesVarArgs() {
        WebPubSubGroup client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.String

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.String.2
//        client.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2");
//        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.String.2
    }

    public void groupSendToAllBytesList() {
        WebPubSubGroup client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.List

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.List.2
//        List<String> excludedUsers = getExcludedUsers();
//        client.sendToAll("Hello world!".getBytes(), excludedUsers);
//        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAllBytes.byte.List.2
    }


    // MISC

    private WebPubSubServiceClient getSyncClient() {
        return null;
    }

    private WebPubSubAsyncServiceClient getAsyncClient() {
        return null;
    }

    private List<String> getExcludedUsers() {
        return null;
    }
}
