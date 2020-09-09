// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobServiceClientBuilder;

import java.time.OffsetDateTime;

/**
 * Code snippets for {@link BlobChangefeedAsyncClient}
 */
public class BlobChangefeedAsyncClientJavaDocCodeSnippets {
    private String endpoint = "endpoint";
    private BlobChangefeedAsyncClient client = new BlobChangefeedClientBuilder(
        new BlobServiceClientBuilder().endpoint(endpoint).buildClient()).buildAsyncClient();

    /**
     * Code snippet for {@link BlobChangefeedAsyncClient#getEvents()}
     */
    public void getEvents() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedAsyncClient.getEvents
        client.getEvents().subscribe(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
        // END: com.azure.storage.blob.changefeed.BlobChangefeedAsyncClient.getEvents
    }

    /**
     * Code snippet for {@link BlobChangefeedAsyncClient#getEvents(OffsetDateTime, OffsetDateTime)}
     */
    public void getEvents2() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedAsyncClient.getEvents#OffsetDateTime-OffsetDateTime
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.now();

        client.getEvents(startTime, endTime).subscribe(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
        // END: com.azure.storage.blob.changefeed.BlobChangefeedAsyncClient.getEvents#OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link BlobChangefeedAsyncClient#getEvents(String)}
     */
    public void getEvents3() {
        // BEGIN: com.azure.storage.blob.changefeed.BlobChangefeedAsyncClient.getEvents#String
        String cursor = "cursor";

        client.getEvents(cursor).subscribe(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
        // END: com.azure.storage.blob.changefeed.BlobChangefeedAsyncClient.getEvents#String
    }
}
