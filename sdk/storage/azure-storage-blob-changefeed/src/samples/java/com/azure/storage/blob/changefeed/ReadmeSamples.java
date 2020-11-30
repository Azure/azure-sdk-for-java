// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public void changefeed() {
        client.getEvents().forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
    }

    public void changefeedBetweenDates() {
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.now();

        client.getEvents(startTime, endTime).forEach(event ->
            System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
    }

    public void changefeedResumeWithCursor() {
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

    public void changefeedPollForEventsWithCursor() {
        List<BlobChangefeedEvent> changefeedEvents = new ArrayList<BlobChangefeedEvent>();

        /* Get the start time.  The change feed client will round start time down to the nearest hour if you provide
           an OffsetDateTime with minutes and seconds. */
        OffsetDateTime startTime = OffsetDateTime.now();

        /* Get your polling interval. */
        long pollingInterval = 1000 * 60 * 5; /* 5 minutes. */

        /* Get initial set of events. */
        Iterable<BlobChangefeedPagedResponse> pages = client.getEvents(startTime, null).iterableByPage();

        String continuationToken = null;

        while (true) {
            for (BlobChangefeedPagedResponse page : pages) {
                changefeedEvents.addAll(page.getValue());
                /*
                 * Get the change feed cursor. The cursor is not required to get each page of events,
                 * it is intended to be saved and used to resume iterating at a later date.
                 */
                continuationToken = page.getContinuationToken();
            }

            /* Wait before processing next batch of events. */
            try {
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /* Resume from last continuation token and fetch latest set of events. */
            pages = client.getEvents(continuationToken).iterableByPage();
        }
    }
}

