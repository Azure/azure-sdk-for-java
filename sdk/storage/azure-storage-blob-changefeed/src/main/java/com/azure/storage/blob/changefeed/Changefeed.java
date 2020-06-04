// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
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
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;

/**
 * A class that represents a Changefeed.
 *
 * The changefeed is a log of changes that are organized into hourly segments.
 * The listing of the $blobchangefeed/idx/segments/ virtual directory shows these segments ordered by time.
 * The path of the segment describes the start of the hourly time-range that the segment represents.
 * This list can be used to filter out the segments of logs that are interest.
 *
 * Note: The time represented by the segment is approximate with bounds of 15 minutes. So to ensure consumption of
 * all records within a specified time, consume the consecutive previous and next hour segment.
 */
class Changefeed {

    private final ClientLogger logger = new ClientLogger(Changefeed.class);

    private static final String SEGMENT_PREFIX = "idx/segments/";
    private static final String METADATA_SEGMENT_PATH = "meta/segments.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final BlobContainerAsyncClient client; /* Changefeed container */
    private final OffsetDateTime startTime; /* User provided start time. */
    private final OffsetDateTime endTime; /* User provided end time. */
    private OffsetDateTime lastConsumable; /* Last consumable time. The latest time the changefeed can safely be
                                              read from.*/
    private OffsetDateTime safeEndTime; /* Soonest time between lastConsumable and endTime. */
    private final ChangefeedCursor cfCursor; /* Cursor associated with changefeed. */
    private final ChangefeedCursor userCursor; /* User provided cursor. */
    private final SegmentFactory segmentFactory; /* Segment factory. */

    /**
     * Creates a new Changefeed.
     */
    Changefeed(BlobContainerAsyncClient client, OffsetDateTime startTime, OffsetDateTime endTime,
        ChangefeedCursor userCursor, SegmentFactory segmentFactory) {
        this.client = client;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userCursor = userCursor;
        this.segmentFactory = segmentFactory;

        this.cfCursor = new ChangefeedCursor(this.endTime);
        this.safeEndTime = endTime;
    }

    /**
     * Get all the events for the Changefeed.
     * @return A reactive stream of {@link BlobChangefeedEventWrapper}
     */
    Flux<BlobChangefeedEventWrapper> getEvents() {
        return validateChangefeed()
            .then(populateLastConsumable())
            .thenMany(listYears())
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
                    return FluxUtil.monoError(logger, new RuntimeException("Changefeed has not been enabled for "
                        + "this account."));
                }
                return Mono.just(true);
            });
    }

    /**
     * Populates the last consumable property from changefeed metadata.
     * Log files in any segment that is dated after the date of the LastConsumable property in the
     * $blobchangefeed/meta/segments.json file, should not be consumed by your application.
     */
    private Mono<OffsetDateTime> populateLastConsumable() {
        /* We can keep the entire metadata file in memory since it is expected to only be a few hundred bytes. */
        return DownloadUtils.downloadToByteArray(this.client, METADATA_SEGMENT_PATH)
            /* Parse JSON for last consumable. */
            .flatMap(json -> {
                try {
                    JsonNode jsonNode = MAPPER.reader().readTree(json);
                    this.lastConsumable = OffsetDateTime.parse(jsonNode.get("lastConsumable").asText());
                    if (this.lastConsumable.isBefore(endTime)) {
                        this.safeEndTime = this.lastConsumable;
                    }
                    return Mono.just(this.lastConsumable);
                } catch (IOException e) {
                    return FluxUtil.monoError(logger, new UncheckedIOException(e));
                }
            });
    }

    /**
     * List years for which changefeed data exists.
     */
    private Flux<String> listYears() {
        return client.listBlobsByHierarchy(SEGMENT_PREFIX)
            .map(BlobItem::getName)
            .filter(yearPath -> TimeUtils.validYear(yearPath, startTime, safeEndTime));
    }

    /**
     * List segments for years of interest.
     */
    private Flux<String> listSegmentsForYear(String year) {
        return client.listBlobs(new ListBlobsOptions().setPrefix(year))
            .map(BlobItem::getName)
            .filter(segmentPath -> TimeUtils.validSegment(segmentPath, startTime, safeEndTime));
    }

    /**
     * Get events for segments of interest.
     */
    private Flux<BlobChangefeedEventWrapper> getEventsForSegment(String segment) {
        OffsetDateTime segmentTime = TimeUtils.convertPathToTime(segment);
        return segmentFactory.getSegment(segment, cfCursor.toSegmentCursor(segmentTime), userCursor)
            .getEvents();
    }

}
