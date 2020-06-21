// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.credential.AzureKeyCredential;

import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link SignalRClientBuilder}
 */
public final class SignalRJavaDocCodeSnippets {

    // BUILDER

    public void createSignalRAsyncClientConnectionString() {
        // BEGIN: com.azure.messaging.signalr.secretclientbuilder.connectionstring.async
        SignalRAsyncClient client = new SignalRClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .buildAsyncClient();
        // END: com.azure.messaging.signalr.secretclientbuilder.connectionstring.async
    }

    public void createSignalRSyncClientConnectionString() {
        // BEGIN: com.azure.messaging.signalr.secretclientbuilder.connectionstring.sync
        SignalRClient client = new SignalRClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .buildClient();
        // END: com.azure.messaging.signalr.secretclientbuilder.connectionstring.sync
    }

    public void createSignalRAsyncClientCredentialEndpoint() {
        // BEGIN: com.azure.messaging.signalr.secretclientbuilder.credential.endpoint.async
        SignalRAsyncClient client = new SignalRClientBuilder()
            .credential(new AzureKeyCredential("<Insert key from Azure Portal>"))
            .endpoint("<Insert endpoint from Azure Portal>")
            .buildAsyncClient();
        // END: com.azure.messaging.signalr.secretclientbuilder.credential.endpoint.async
    }

    // ASYNC - HUB

    public void signalrAsyncSendToAllVarArgs() {
        SignalRAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.String
        client.sendToAll("Hello world!").block();
        // END: com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.String

        // BEGIN: com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.String.2
        client.sendToAll("Hello world!", "connection-id-1", "connection-id-2").block();
        // END: com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.String.2
    }

    public void signalrAsyncSendToAllList() {
        SignalRAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.List
        client.sendToAll("Hello world!").block();
        // END: com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.List

        // BEGIN: com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!", excludedUsers).block();
        // END: com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.List.2
    }

    public void signalrAsyncSendToAllBytesVarArgs() {
        SignalRAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.String

        // BEGIN: com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.String.2
        client.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2").block();
        // END: com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.String.2
    }

    public void signalrAsyncSendToAllBytesList() {
        SignalRAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!".getBytes(), excludedUsers).block();
        // END: com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.List.2
    }

    // ASYNC - GROUP

    public void signalrAsyncGroupClient() {
        // BEGIN: com.azure.messaging.signalr.signalrgroupasyncclient.instance
        SignalRAsyncClient client = new SignalRClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .hub("chat-portal")
            .buildAsyncClient();

        SignalRGroupAsyncClient adminGroup = client.getGroupAsyncClient("admins");
        // END: com.azure.messaging.signalr.signalrgroupasyncclient.instance
    }

    public void signalrGroupAsyncSendToAllVarArgs() {
        SignalRGroupAsyncClient groupClient = null;

        // BEGIN: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.String
        groupClient.sendToAll("Hello world!").block();
        // END: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.String

        // BEGIN: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.String.2
        groupClient.sendToAll("Hello world!", "connection-id-1", "connection-id-2").block();
        // END: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.String.2
    }

    public void signalrGroupAsyncSendToAllList() {
        SignalRGroupAsyncClient groupClient = null;

        // BEGIN: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.List
        groupClient.sendToAll("Hello world!").block();
        // END: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.List

        // BEGIN: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        groupClient.sendToAll("Hello world!", excludedUsers).block();
        // END: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.List.2
    }

    public void signalrGroupAsyncSendToAllBytesVarArgs() {
        SignalRGroupAsyncClient groupClient = null;

        // BEGIN: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.String
        groupClient.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.String

        // BEGIN: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.String.2
        groupClient.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2").block();
        // END: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.String.2
    }

    public void signalrGroupAsyncSendToAllBytesList() {
        SignalRGroupAsyncClient groupClient = null;

        // BEGIN: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.List
        groupClient.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        groupClient.sendToAll("Hello world!".getBytes(), excludedUsers).block();
        // END: com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.List.2
    }

    // SYNC - HUB

    public void signalrGroupClient() {
        // BEGIN: com.azure.messaging.signalr.signalrgroupclient.instance
        SignalRClient client = new SignalRClientBuilder()
            .connectionString("<Insert connection string from Azure Portal>")
            .hub("chat-portal")
            .buildClient();

        SignalRGroupClient adminGroup = client.getGroupClient("admins");
        // END: com.azure.messaging.signalr.signalrgroupclient.instance
    }

    public void signalrSendToAllVarArgs() {
        SignalRClient client = getSyncClient();
        // BEGIN: com.azure.messaging.signalr.signalrclient.sendToAll.String.String
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.signalr.signalrclient.sendToAll.String.String

        // BEGIN: com.azure.messaging.signalr.signalrclient.sendToAll.String.String.2
        client.sendToAll("Hello world!", "connection-id-1", "connection-id-2");
        // END: com.azure.messaging.signalr.signalrclient.sendToAll.String.String.2
    }

    public void signalrSendToAllList() {
        SignalRClient client = getSyncClient();
        // BEGIN: com.azure.messaging.signalr.signalrclient.sendToAll.String.List
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.signalr.signalrclient.sendToAll.String.List

        // BEGIN: com.azure.messaging.signalr.signalrclient.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!", excludedUsers);
        // END: com.azure.messaging.signalr.signalrclient.sendToAll.String.List.2
    }

    public void signalrSendToAllBytesVarArgs() {
        SignalRClient client = getSyncClient();
        // BEGIN: com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.String

        // BEGIN: com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.String.2
        client.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2");
        // END: com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.String.2
    }

    public void signalrSendToAllBytesList() {
        SignalRClient client = getSyncClient();
        // BEGIN: com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!".getBytes(), excludedUsers);
        // END: com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.List.2
    }

    // SYNC - GROUP

    public void signalrGroupSendToAllVarArgs() {
        SignalRGroupClient client = null;

        // BEGIN: com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.String
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.String

        // BEGIN: com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.String.2
        client.sendToAll("Hello world!", "connection-id-1", "connection-id-2");
        // END: com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.String.2
    }

    public void signalrGroupSendToAllList() {
        SignalRGroupClient client = null;

        // BEGIN: com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.List
        client.sendToAll("Hello world!");
        // END: com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.List

        // BEGIN: com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!", excludedUsers);
        // END: com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.List.2
    }

    public void signalrGroupSendToAllBytesVarArgs() {
        SignalRGroupClient client = null;

        // BEGIN: com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.String
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.String

        // BEGIN: com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.String.2
        client.sendToAll("Hello world!".getBytes(), "connection-id-1", "connection-id-2");
        // END: com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.String.2
    }

    public void signalrGroupSendToAllBytesList() {
        SignalRGroupClient client = null;

        // BEGIN: com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.List
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.List

        // BEGIN: com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.List.2
        List<String> excludedUsers = getExcludedUsers();
        client.sendToAll("Hello world!".getBytes(), excludedUsers);
        // END: com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.List.2
    }


    // MISC

    private SignalRClient getSyncClient() {
        return null;
    }

    private SignalRAsyncClient getAsyncClient() {
        return null;
    }

    private List<String> getExcludedUsers() {
        return null;
    }
}
