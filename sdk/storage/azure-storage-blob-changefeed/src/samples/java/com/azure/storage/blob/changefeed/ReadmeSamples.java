// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.time.OffsetDateTime;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().buildClient();
    private BlobChangefeedClient client = new BlobChangefeedClientBuilder(blobServiceClient).buildClient();

    public void getClient() {
        client = new BlobChangefeedClientBuilder(blobServiceClient).buildClient();
    }

    public void getEvents() {
        client.getEvents().forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
    }

    public void getEvents2() {
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.now();

        client.getEvents(startTime, endTime).forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
    }

    public void getEvents3() {
        BlobChangefeedPagedIterable iterable = client.getEvents();
        Iterable<BlobChangefeedPagedResponse> pages = iterable.iterableByPage();

        String cursor = null;
        for (BlobChangefeedPagedResponse page : pages) {
            page.getValue().forEach(event ->
                System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
            /*
             * Get the change feed cursor. The cursor is not required to get each page of events,
             * it is intended to be saved and used to resume iterating at a later date.
             */
            cursor = page.getContinuationToken();
        }

        /* Resume iterating from the pervious position with the cursor. */
        client.getEvents(cursor).forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
    }

}

