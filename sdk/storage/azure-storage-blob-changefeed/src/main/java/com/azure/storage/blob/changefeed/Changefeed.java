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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Objects;

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

    private final BlobContainerAsyncClient client; /* Changefeed container */
    private final OffsetDateTime startTime; /* User provided start time. */
    private final OffsetDateTime endTime; /* User provided end time. */
    private final ChangefeedCursor changefeedCursor; /* Cursor associated with changefeed. */
    private final ChangefeedCursor userCursor; /* User provided cursor. */
    private final SegmentFactory segmentFactory; /* Segment factory. */

    /**
     * Creates a new Changefeed.
     */
    Changefeed(BlobContainerAsyncClient client, OffsetDateTime startTime, OffsetDateTime endTime,
        ChangefeedCursor userCursor, SegmentFactory segmentFactory) {
        this.client = client;
        this.startTime = TimeUtils.roundDownToNearestHour(startTime);
        this.endTime = TimeUtils.roundUpToNearestHour(endTime);
        this.userCursor = userCursor;
        this.segmentFactory = segmentFactory;
        String urlHost = null;
        try {
            urlHost = new URL(client.getBlobContainerUrl()).getHost();
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
        this.changefeedCursor = new ChangefeedCursor(urlHost, this.endTime);

        /* Validate the cursor. */
        if (userCursor != null) {
            if (userCursor.getCursorVersion() != 1) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Unsupported cursor version."));
            }
            if (!Objects.equals(urlHost, userCursor.getUrlHost())) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Cursor URL host does not match "
                    + "container URL host."));
            }
        }
    }

    /**
     * Get all the events for the Changefeed.
     * @return A reactive stream of {@link BlobChangefeedEventWrapper}
     */
    Flux<BlobChangefeedEventWrapper> getEvents() {
        return validateChangefeed()
            .then(populateLastConsumable())
            .flatMapMany(safeEndTime ->
                listYears(safeEndTime).map(str -> Tuples.of(safeEndTime, str))
            )
            .concatMap(tuple2 -> {
                OffsetDateTime safeEndTime = tuple2.getT1();
                String year = tuple2.getT2();
                return listSegmentsForYear(safeEndTime, year);
            })
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
            .flatMap(DownloadUtils::parseJson)
            /* Parse JSON for last consumable. */
            .flatMap(jsonNode -> {
                /* Last consumable time. The latest time the changefeed can safely be read from.*/
                OffsetDateTime lastConsumableTime = OffsetDateTime.parse(jsonNode.get("lastConsumable").asText());
                /* Soonest time between lastConsumable and endTime. */
                OffsetDateTime safeEndTime = this.endTime;
                if (lastConsumableTime.isBefore(endTime)) {
                    safeEndTime = lastConsumableTime.plusHours(1); /* Add an hour since end time is non inclusive. */
                }
                return Mono.just(safeEndTime);
            });
    }

    /**
     * List years for which changefeed data exists.
     */
    private Flux<String> listYears(OffsetDateTime safeEndTime) {
        return client.listBlobsByHierarchy(SEGMENT_PREFIX)
            .map(BlobItem::getName)
            .filter(yearPath -> TimeUtils.validYear(yearPath, startTime, safeEndTime));
    }

    /**
     * List segments for years of interest.
     */
    private Flux<String> listSegmentsForYear(OffsetDateTime safeEndTime, String year) {
        return client.listBlobs(new ListBlobsOptions().setPrefix(year))
            .map(BlobItem::getName)
            .filter(segmentPath -> TimeUtils.validSegment(segmentPath, startTime, safeEndTime));
    }

    /**
     * Get events for segments of interest.
     */
    private Flux<BlobChangefeedEventWrapper> getEventsForSegment(String segment) {
        OffsetDateTime segmentTime = TimeUtils.convertPathToTime(segment);
        /* Only pass the user cursor in to the segment of interest. */
        if (userCursor != null && segmentTime.isEqual(startTime)) {
            return segmentFactory.getSegment(segment,
                changefeedCursor.toSegmentCursor(segment, userCursor.getCurrentSegmentCursor()),
                userCursor.getCurrentSegmentCursor()).getEvents();
        } else {
            return segmentFactory.getSegment(segment,
                changefeedCursor.toSegmentCursor(segment, null),
                null).getEvents();
        }
    }

}
