// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;

import java.util.Arrays;
import java.util.Collections;
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
        // BEGIN: com.azure.messaging.webpubsub.WebPubSubAsyncServiceClient.sendToAll#String
        client.sendToAll("{\"message\": \"Hello world!\"}").block();
        // END: com.azure.messaging.webpubsub.WebPubSubAsyncServiceClient.sendToAll#String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll#String-WebPubSubContentType
        client.sendToAll("Hello world!", WebPubSubContentType.TEXT_PLAIN).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll#String-WebPubSubContentType


        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse
        client.sendToAllWithResponse("Hello world!",
            WebPubSubContentType.TEXT_PLAIN,
            Collections.emptyList()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse.withexclusions
        List<String> excludedConnectionIds = getExcludedConnectionIds();
        client.sendToAllWithResponse("Hello world!",
            WebPubSubContentType.TEXT_PLAIN,
            excludedConnectionIds).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse.withexclusions
    }

    public void asyncSendToAllBytesVarArgs() {
        WebPubSubAsyncServiceClient client = getAsyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll#byte
        client.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll#byte

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll#byte-WebPubSubContentType
        client.sendToAll("Hello world!".getBytes(), WebPubSubContentType.APPLICATION_OCTET_STREAM).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll#byte-WebPubSubContentType


        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse.byte
        client.sendToAllWithResponse("Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            Collections.emptyList()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse.byte

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse.byte.withexclusion
        List<String> excludedConnectionIds = getExcludedConnectionIds();
        client.sendToAllWithResponse("Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            excludedConnectionIds).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse.byte.withexclusion
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

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#String
        groupClient.sendToAll("{\"message\": \"Hello world!\"}").block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#String-WebPubSubContentType
        groupClient.sendToAll("Hello world!", WebPubSubContentType.TEXT_PLAIN).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#String-WebPubSubContentType


        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse
        groupClient.sendToAllWithResponse("Hello world!",
            WebPubSubContentType.TEXT_PLAIN,
            Collections.emptyList()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse.withexclusions
        List<String> excludedConnectionIds = getExcludedConnectionIds();
        groupClient.sendToAllWithResponse("Hello world!",
            WebPubSubContentType.TEXT_PLAIN,
            excludedConnectionIds).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse.withexclusions
    }

    public void groupAsyncSendToAllBytesVarArgs() {
        WebPubSubAsyncGroup groupClient = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#byte
        groupClient.sendToAll("Hello world!".getBytes()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#byte

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#byte-WebPubSubContentType
        groupClient.sendToAll("Hello world!".getBytes(), WebPubSubContentType.APPLICATION_OCTET_STREAM).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#byte-WebPubSubContentType

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse.byte
        groupClient.sendToAllWithResponse("Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            Collections.emptyList()).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse.byte

        // BEGIN: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse.byte.withexclusion
        List<String> excludedConnectionIds = getExcludedConnectionIds();
        groupClient.sendToAllWithResponse("Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            excludedConnectionIds).block();
        // END: com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse.byte.withexclusion
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
        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#String
        client.sendToAll("{\"message\": \"Hello world!\"}");
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#String-WebPubSubContentType
        client.sendToAll("Hello world!", WebPubSubContentType.TEXT_PLAIN);
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#String-WebPubSubContentType

        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse
        client.sendToAllWithResponse(
            "Hello world!",
            WebPubSubContentType.TEXT_PLAIN,
            Collections.emptyList(),
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse

        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse.withexclusions
        client.sendToAllWithResponse(
            "Hello world!",
            WebPubSubContentType.TEXT_PLAIN,
            Arrays.asList("excluded-connection-id-1", "excluded-connection-id-2"),
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse.withexclusions
    }

    public void sendToAllBytesVarArgs() {
        WebPubSubServiceClient client = getSyncClient();
        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#byte
        client.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#byte

        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#byte-WebPubSubContentType
        client.sendToAll("Hello world!".getBytes(), WebPubSubContentType.APPLICATION_OCTET_STREAM);
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#byte-WebPubSubContentType

        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse.byte
        client.sendToAllWithResponse(
            "Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            Collections.emptyList(),
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse.byte

        // BEGIN: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse.byte.withexclusion
        client.sendToAllWithResponse(
            "Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            Arrays.asList("excluded-connection-id-1", "excluded-connection-id-2"),
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse.byte.withexclusion
    }

    // SYNC - GROUP

    public void groupSendToAllVarArgs() {
        WebPubSubGroup adminGroup = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#String
        adminGroup.sendToAll("{\"message\": \"Hello world!\"}");
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#String

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#String-WebPubSubContentType
        adminGroup.sendToAll("Hello world!", WebPubSubContentType.TEXT_PLAIN);
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#String-WebPubSubContentType

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse
        adminGroup.sendToAllWithResponse("Hello world!",
            WebPubSubContentType.TEXT_PLAIN,
            Collections.emptyList(),
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse.withexclusions
        List<String> excludedConnectionIds = getExcludedConnectionIds();
        adminGroup.sendToAllWithResponse("Hello world!",
            WebPubSubContentType.TEXT_PLAIN,
            excludedConnectionIds,
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse.withexclusions
    }

    public void groupSendToAllBytesVarArgs() {
        WebPubSubGroup adminGroup = null;

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#byte
        adminGroup.sendToAll("Hello world!".getBytes());
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#byte

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#byte-WebPubSubContentType
        adminGroup.sendToAll("Hello world!".getBytes(), WebPubSubContentType.APPLICATION_OCTET_STREAM);
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#byte-WebPubSubContentType

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse.byte
        adminGroup.sendToAllWithResponse("Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            Collections.emptyList(),
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse.byte

        // BEGIN: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse.byte.withexclusion
        List<String> excludedConnectionIds = getExcludedConnectionIds();
        adminGroup.sendToAllWithResponse("Hello world!".getBytes(),
            WebPubSubContentType.APPLICATION_OCTET_STREAM,
            excludedConnectionIds,
            Context.NONE);
        // END: com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse.byte.withexclusion
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

    private List<String> getExcludedConnectionIds() {
        return null;
    }
}
