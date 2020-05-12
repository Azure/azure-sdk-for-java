// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;


import com.azure.storage.blob.BlobServiceClientBuilder;

import java.time.OffsetDateTime;

/**
 * Code snippets for {@link BlobChangefeedClient}
 */
public class BlobChangefeedClientJavaDocCodeSnippets {
    private BlobChangefeedClient client = new BlobChangefeedClientBuilder(
        new BlobServiceClientBuilder().buildClient()).buildClient();

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
     * Code snippet for {@link BlobChangefeedClient#getEvents(String)}
     */
    public void getEvents3() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#String
        String cursor = "cursor";

        client.getEvents(cursor).forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
        // END: com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#String
    }
}
