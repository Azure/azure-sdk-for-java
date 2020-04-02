// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.util.DownloadUtils;
import com.azure.storage.blob.changefeed.implementation.util.TimeUtils;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;

/**
 * Gets events for the changefeed as defined by the user.
 */
class Changefeed {

    private static ClientLogger logger = new ClientLogger(Changefeed.class);

    private static final String SEGMENT_PREFIX = "idx/segments/";
    private static final String METADATA_SEGMENT_PATH = "meta/segments.json";

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* User provided start and end times. */
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;

    /* User provided cursor. */
    private BlobChangefeedCursor userCursor;

    /* Last consumable time. The latest time the changefeed can safely be read from.*/
    private OffsetDateTime lastConsumable; /* TODO (gapra) : Do something with last consumable. */

    /* Soonest time between lastConsumable and endTime. TODO (gapra) : Figure out if I even need this extra param
                                                         (maybe I can overwrite endTime?)*/
    private OffsetDateTime safeEndTime;

    /* Cursor associated with changefeed. */
    private final BlobChangefeedCursor cfCursor;

    /**
     * Creates a new Changefeed from a start and end time.
     */
    Changefeed(BlobContainerAsyncClient client, OffsetDateTime startTime, OffsetDateTime endTime) {
        this.client = client;
        this.startTime = startTime;
        this.endTime = endTime;
        cfCursor = new BlobChangefeedCursor(endTime);
    }

    /**
     * Creates a new Changefeed from a cursor.
     */
    Changefeed(BlobContainerAsyncClient client, String userCursor) {
        this.client = client;
        this.userCursor = BlobChangefeedCursor.deserialize(userCursor);
        this.startTime = OffsetDateTime.parse(this.userCursor.getSegmentTime());
        this.endTime = OffsetDateTime.parse(this.userCursor.getEndTime());
        this.safeEndTime = this.endTime;
        this.cfCursor = new BlobChangefeedCursor(endTime);
    }

    /**
     * Get all the events for the Changefeed.
     * @return A reactive stream of {@link BlobChangefeedEventWrapper}
     */
    Flux<BlobChangefeedEventWrapper> getEvents() {
        return validateChangefeed()
            .then(populateLastConsumable())
            .thenMany(listYears()
                .concatMap(this::listSegmentsForYear)
                .concatMap(this::getEventsForSegment));
    }

    /**
     * Validates that changefeed has been enabled for the account.
     */
    private Mono<Void> validateChangefeed() {
        return this.client.exists()
            .flatMap(exists -> {
                if (exists == null || !exists) {
                    return Mono.error(new RuntimeException("ChangeFeed has not been enabled for this "
                        + "account."));
                }
                return Mono.empty();
            });
    }

    /**
     * Populates the last consumable property from changefeed metadata.
     */
    private Mono<Void> populateLastConsumable() {
        /* We can keep the entire metadata file in memory since it is expected to only be a few hundred bytes. */
        return DownloadUtils.downloadToString(this.client, METADATA_SEGMENT_PATH)
            /* Parse JSON for last consumable. */
            .flatMap(json -> {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = null;
                try {
                    jsonNode = objectMapper.readTree(json);
                } catch (IOException e) {
                    throw logger.logExceptionAsError(new UncheckedIOException(e));
                }
                this.lastConsumable = OffsetDateTime.parse(jsonNode.get("lastConsumable").asText());
                if (this.lastConsumable.isBefore(endTime)) {
                    this.safeEndTime = this.lastConsumable;
                }
                return Mono.empty();
            });
    }

    /**
     * List years for which changefeed data exists.
     */
    private Flux<BlobItem> listYears() {
        return this.client.listBlobsByHierarchy(SEGMENT_PREFIX);
    }

    /**
     * List segments for years of interest.
     */
    private Flux<BlobItem> listSegmentsForYear(BlobItem year) {
        String yearPath = year.getName();
        if (TimeUtils.validYear(yearPath, startTime, safeEndTime)) {
            return client.listBlobs(new ListBlobsOptions().setPrefix(yearPath));
        } else {
            return Flux.empty();
        }
    }

    /**
     * Get events for segments of interest.
     */
    private Flux<BlobChangefeedEventWrapper> getEventsForSegment(BlobItem segment) {
        String segmentPath = segment.getName();
        OffsetDateTime segmentTime = TimeUtils.convertPathToTime(segmentPath);
        if (TimeUtils.validSegment(segmentPath, startTime, safeEndTime)) {
            return new Segment(client, segmentPath, cfCursor.toSegmentCursor(segmentTime), userCursor)
                .getEvents();
        } else {
            return Flux.empty();
        }
    }

}
