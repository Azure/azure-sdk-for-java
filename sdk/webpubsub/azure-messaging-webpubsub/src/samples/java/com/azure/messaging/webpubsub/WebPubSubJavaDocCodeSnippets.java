// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;

import java.util.Arrays;
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
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll.String.String
        client.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll.String.String
    }

    public void asyncSendToAllList() {
        WebPubSubAsyncServiceClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll.String.List
        client.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll.String.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAllWithResponse("Hello world!", WebPubSubContentType.TEXT_PLAIN, excludedUsers).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll.String.List.2
    }

    public void asyncSendToAllBytesVarArgs() {
        WebPubSubAsyncServiceClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllBytes.byte.String
    }

    public void ayncSendToAllBytesList() {
        WebPubSubAsyncServiceClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAllWithResponse("Hello world!".getBytes(), WebPubSubContentType.TEXT_PLAIN, excludedUsers).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllBytes.byte.List.2
    }

    // ASYNC - GROUP

    public void asyncGroupClient() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.instance
        WebPubSubAsyncServiceClient client = new WebPubSubClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .hub("chat-portal")
            .buildAsyncClient();

        WebPubSubAsyncGroup adminGroup = client.getAsyncGroup("admins");
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.instance
    }

    public void groupAsyncSendToAllVarArgs() {
        WebPubSubAsyncGroup groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll.String.String
        groupClient.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll.String.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll.String.String.2
//        groupClient.sendToAllWithResponse("Hello world!", "connection-id-1", "connection-id-2").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll.String.String.2
    }

    public void groupAsyncSendToAllList() {
        WebPubSubAsyncGroup groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll.String.List
        groupClient.sendToAll("Hello world!").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll.String.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        groupClient.sendToAllWithResponse("Hello world!", WebPubSubContentType.TEXT_PLAIN, excludedUsers).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll.String.List.2
    }

    public void groupAsyncSendToAllBytesVarArgs() {
        WebPubSubAsyncGroup groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllBytes.byte.String
        groupClient.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllBytes.byte.String

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllBytes.byte.String.2
//        groupClient.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2").block();
//        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllBytes.byte.String.2
    }

    public void groupAsyncSendToAllBytesList() {
        WebPubSubAsyncGroup groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllBytes.byte.List
        groupClient.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        groupClient.sendToAllWithResponse(
            "Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            excludedUsers).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllBytes.byte.List.2
    }

    // SYNC - HUB

    public void groupClient() {
        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.instance
        WebPubSubServiceClient client = new WebPubSubClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .hub("chat-portal")
            .buildClient();

        WebPubSubGroup adminGroup = client.getGroup("admins");
        // END: com.azure.messaging.webpubsub.webpubsubgroup.instance
    }

    public void sendToAllVarArgs() {
        WebPubSubServiceClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll.String.String
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll.String.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll.String.String.2
        client.sendToAllWithResponse(
            "Hello world!",
            WebPubSubContentType.TEXT_PLAIN,
            Arrays.asList("excluded-connection-id-1", "excluded-connection-id-2"),
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll.String.String.2
    }

    public void sendToAllList() {
        WebPubSubServiceClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll.String.List
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll.String.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAllWithResponse("Hello world!", WebPubSubContentType.TEXT_PLAIN, excludedUsers, Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll.String.List.2
    }

    public void sendToAllBytesVarArgs() {
        WebPubSubServiceClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllBytes.byte.String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllBytes.byte.String.2
        client.sendToAllWithResponse(
            "Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            Arrays.asList("excluded-connection-id-1", "excluded-connection-id-2"),
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllBytes.byte.String.2
    }

    public void sendToAllBytesList() {
        WebPubSubServiceClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAllWithResponse(
            "Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            excludedUsers,
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllBytes.byte.List.2
    }

    // SYNC - GROUP

    public void groupSendToAllVarArgs() {
        WebPubSubGroup client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll.String.String
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll.String.String

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.String.2
//        client.sendToAll("Hello world!", "connection-id-1", "connection-id-2");
//        // END: com.azure.messaging.webpubsub.webpubsubgroupclient.sendToAll.String.String.2
    }

    public void groupSendToAllList() {
        WebPubSubGroup client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll.String.List
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll.String.List

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAllWithResponse("Hello world!", WebPubSubContentType.TEXT_PLAIN, excludedUsers, Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll.String.List.2
    }

    public void groupSendToAllBytesVarArgs() {
        WebPubSubGroup client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllBytes.byte.String

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllBytes.byte.String.2
//        client.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2");
//        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllBytes.byte.String.2
    }

    public void groupSendToAllBytesList() {
        WebPubSubGroup client = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllBytes.byte.List

//        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllBytes.byte.List.2
//        List<String> excludedUsers = getExcludedUsers();
//        client.sendToAll("Hello world!".getBytes(), excludedUsers);
//        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllBytes.byte.List.2
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
