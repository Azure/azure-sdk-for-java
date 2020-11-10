// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;


import com.azure.core.util.Context;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.time.OffsetDateTime;

/**
 * Code snippets for {@link BlobChangefeedClient}
 */
public class BlobChangefeedClientJavaDocCodeSnippets {
    private String endpoint = "endpoint";
    private BlobChangefeedClient client = new BlobChangefeedClientBuilder(
        new BlobServiceClientBuilder().endpoint(endpoint).buildClient()).buildClient();

    /**
     * Code snippet for {@link BlobChangefeedClient#getEvents()}
     */
    public void getEvents() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents
        client.getEvents().forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
        // END: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents
    }

    /**
     * Code snippet for {@link BlobChangefeedClient#getEvents(OffsetDateTime, OffsetDateTime)}
     */
    public void getEvents2() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#OffsetDateTime-OffsetDateTime
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.now();

        client.getEvents(startTime, endTime).forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
        // END: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link BlobChangefeedClient#getEvents(OffsetDateTime, OffsetDateTime, Context)}
     */
    public void getEvents2Context() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#OffsetDateTime-OffsetDateTime-Context
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.now();

        client.getEvents(startTime, endTime, new Context("key", "value")).forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
        // END: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#OffsetDateTime-OffsetDateTime-Context
    }

    /**
     * Code snippet for {@link BlobChangefeedClient#getEvents(String)}
     */
    public void getEvents3() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#String
        String cursor = "cursor";

        client.getEvents(cursor).forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
        // END: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#String
    }

    /**
     * Code snippet for {@link BlobChangefeedClient#getEvents(String, Context)}
     */
    public void getEvents3Context() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#String-Context
        String cursor = "cursor";

        client.getEvents(cursor, new Context("key", "value")).forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
        // END: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#String-Context
    }
}
