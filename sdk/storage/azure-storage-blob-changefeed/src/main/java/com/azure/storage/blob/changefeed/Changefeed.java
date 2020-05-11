// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.DownloadUtils;
import com.azure.storage.blob.changefeed.implementation.util.TimeUtils;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Gets events for the changefeed as defined by the user.
 */
class Changefeed {

    private static final String SEGMENT_PREFIX = "idx/segments/";
    private static final String METADATA_SEGMENT_PATH = "meta/segments.json";

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* User provided start and end times. */
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;

    /* User provided cursor. */
    private ChangefeedCursor userCursor;

    /* Last consumable time. The latest time the changefeed can safely be read from.*/
    private OffsetDateTime lastConsumable;

    /* Soonest time between lastConsumable and endTime. */
    private OffsetDateTime safeEndTime;

    /* Cursor associated with changefeed. */
    private final ChangefeedCursor cfCursor;

    private final SegmentFactory segmentFactory;

    /**
     * Creates a new Changefeed from a start and end time.
     */
    Changefeed(BlobContainerAsyncClient client, OffsetDateTime startTime, OffsetDateTime endTime,
        SegmentFactory segmentFactory) {
        this.client = client;
        this.startTime = startTime == null ? OffsetDateTime.MIN : startTime;
        this.endTime = endTime == null ? OffsetDateTime.MAX : endTime;
        this.cfCursor = new ChangefeedCursor(this.endTime);
        this.segmentFactory = segmentFactory;
    }

    /**
     * Creates a new Changefeed from a cursor.
     */
    Changefeed(BlobContainerAsyncClient client, String userCursor, SegmentFactory segmentFactory) {
        this.client = client;
        this.userCursor = ChangefeedCursor.deserialize(userCursor);
        this.startTime = OffsetDateTime.parse(this.userCursor.getSegmentTime());
        this.endTime = OffsetDateTime.parse(this.userCursor.getEndTime());
        this.safeEndTime = this.endTime;
        this.cfCursor = new ChangefeedCursor(endTime);
        this.segmentFactory = segmentFactory;
    }

    /**
     * Get all the events for the Changefeed.
     * @return A reactive stream of {@link BlobChangefeedEventWrapper}
     */
    Flux<BlobChangefeedEventWrapper> getEvents() {
        return validateChangefeed()
            .flatMap(ignore -> populateLastConsumable())
            .flatMapMany(ignore -> listYears())
            .concatMap(this::listSegmentsForYear)
            .concatMap(this::getEventsForSegment);
    }

    /**
     * Validates that changefeed has been enabled for the account.
     */
    private Mono<Boolean> validateChangefeed() {
        return this.client.exists()
            .flatMap(exists -> {
                if (exists == null || !exists) {
                    return Mono.error(new RuntimeException("Changefeed has not been enabled for this "
                        + "account."));
                }
                return Mono.just(true);
            });
    }

    /**
     * Populates the last consumable property from changefeed metadata.
     */
    private Mono<OffsetDateTime> populateLastConsumable() {
        /* We can keep the entire metadata file in memory since it is expected to only be a few hundred bytes. */
        return DownloadUtils.downloadToString(this.client, METADATA_SEGMENT_PATH)
            /* Parse JSON for last consumable. */
            .flatMap(json -> {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(json);
                    this.lastConsumable = OffsetDateTime.parse(jsonNode.get("lastConsumable").asText());
                    if (this.lastConsumable.isBefore(endTime)) {
                        this.safeEndTime = this.lastConsumable;
                    }
                    return Mono.just(this.lastConsumable);
                } catch (IOException e) {
                    return Mono.error(e);
                }
            });
    }

    /**
     * List years for which changefeed data exists.
     */
    private Flux<String> listYears() {
        return client.listBlobsByHierarchy(SEGMENT_PREFIX)
            .map(BlobItem::getName);
    }

    /**
     * List segments for years of interest.
     */
    private Flux<String> listSegmentsForYear(String year) {
        if (TimeUtils.validYear(year, startTime, safeEndTime)) {
            return client.listBlobs(new ListBlobsOptions().setPrefix(year))
                .map(BlobItem::getName);
        } else {
            return Flux.empty();
        }
    }

    /**
     * Get events for segments of interest.
     */
    private Flux<BlobChangefeedEventWrapper> getEventsForSegment(String segment) {
        OffsetDateTime segmentTime = TimeUtils.convertPathToTime(segment);
        if (TimeUtils.validSegment(segment, startTime, safeEndTime)) {
            return segmentFactory.getSegment(client, segment, cfCursor.toSegmentCursor(segmentTime), userCursor)
                .getEvents();
        } else {
            return Flux.empty();
        }
    }

}
