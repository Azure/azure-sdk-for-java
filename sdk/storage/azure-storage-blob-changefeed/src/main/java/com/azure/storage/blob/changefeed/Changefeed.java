// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.util.TimeUtils;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;

class Changefeed {

//    public const string InitalizationManifestPath = "/0000/";
//    public const string InitalizationSegment = "1601";
//    public const long ChunkBlockDownloadSize = MB;

    private static ClientLogger logger = new ClientLogger(Changefeed.class);

    private static final String SEGMENT_PREFIX = "idx/segments/";
    private static final String METADATA_SEGMENT_PATH = "meta/segments.json";

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* User provided start and end times. */
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;

    /* Last consumable time. */
    private OffsetDateTime lastConsumable; /* TODO (gapra) : DO something with last consumable. */

    /* Cursor associated with changefeed. */
    private final BlobChangefeedCursor cfCursor;

    /* User provided cursor. */
    private BlobChangefeedCursor userCursor;

    Changefeed(BlobContainerAsyncClient client, OffsetDateTime startTime, OffsetDateTime endTime) {
        this.client = client;
        this.startTime = startTime;
        this.endTime = endTime;
        cfCursor = new BlobChangefeedCursor(endTime);
    }

    Changefeed(BlobContainerAsyncClient client, String userCursor) {
        this.client = client;
        this.userCursor = BlobChangefeedCursor.deserialize(userCursor);
        this.startTime = OffsetDateTime.parse(this.userCursor.getSegmentTime());
        this.endTime = OffsetDateTime.parse(this.userCursor.getEndTime());
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
                    throw logger.logExceptionAsError(new RuntimeException("ChangeFeed has not been enabled for this "
                        + "account."));
                }
                return Mono.empty();
            });
    }

    /**
     * Populates the last consumable property from changefeed metadata.
     */
    private Mono<Void> populateLastConsumable() {
        return this.client.getBlobAsyncClient(METADATA_SEGMENT_PATH)
            .download().reduce(new ByteArrayOutputStream(), (os, buffer) -> {
                try {
                    os.write(FluxUtil.byteBufferToArray(buffer));
                } catch (IOException e) {
                    throw logger.logExceptionAsError(new UncheckedIOException(e));
                }
                return os;
            })
            /* We can keep the entire metadata file in memory since it is expected to only be a few hundred bytes. */
            .map(ByteArrayOutputStream::toString)
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
        if (TimeUtils.validYear(yearPath, startTime, endTime)) {
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
        if (TimeUtils.validSegment(segmentPath, startTime, endTime)) {
            return new Segment(client, segmentPath, cfCursor.toSegmentCursor(segmentTime), userCursor)
                .getEvents();
        } else {
            return Flux.empty();
        }
    }

}
