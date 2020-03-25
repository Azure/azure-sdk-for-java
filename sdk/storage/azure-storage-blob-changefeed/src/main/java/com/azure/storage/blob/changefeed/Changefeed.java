// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.util.TimeUtils;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.blob.models.ListBlobsOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

class Changefeed {

    private static ClientLogger logger = new ClientLogger(Changefeed.class);

    private static final String SEGMENT_PREFIX = "idx/segments/";

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;


    Changefeed(BlobContainerAsyncClient client, OffsetDateTime startTime, OffsetDateTime endTime) {
        this.client = client;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private Mono<Boolean> checkExistence() {
        return this.client.exists()
            .map(exists -> {
                if (exists == null || !exists) {
                    throw logger.logExceptionAsError(new RuntimeException("ChangeFeed has not been enabled for this "
                        + "account."));
                }
                return exists;
            });
    }

    Flux<BlobChangefeedEvent> getEvents() {
        /* TODO : First check existence. */
        return client.listBlobsByHierarchy(SEGMENT_PREFIX) /* Get all years. */
            .concatMap(year -> { /* Get segments for each year. */
                String yearPath = year.getName();
                if (validYear(yearPath)) {
                    return client.listBlobs(new ListBlobsOptions().setPrefix(yearPath));
                } else {
                    return Flux.empty();
                }
            }).concatMap(segment -> { /* Get events for each segment. */
                String segmentPath = segment.getName();
                if (validSegment(segmentPath)) {
                    return new Segment(client, segment.getName()).getEvents();
                } else {
                    return Flux.empty();
                }
            });
    }

    /**
     * Validates that the year lies within the start and end times.
     */
    private boolean validYear(String currentYearPath) {
        OffsetDateTime currentYear = TimeUtils.convertPathToTime(currentYearPath, logger);
        OffsetDateTime startYear = TimeUtils.roundDownToNearestYear(startTime);
        OffsetDateTime endYear = TimeUtils.roundDownToNearestYear(endTime);
        return validTimes(currentYear, startYear, endYear);
    }

    /**
     * Validates that the segment lies within the start and end times.
     */
    private boolean validSegment(String currentSegmentPath) {
        OffsetDateTime hour = TimeUtils.convertPathToTime(currentSegmentPath, logger);
        OffsetDateTime startHour = TimeUtils.roundDownToNearestHour(startTime);
        OffsetDateTime endHour = TimeUtils.roundUpToNearestHour(endTime);
        return validTimes(hour, startHour, endHour);
    }

    /**
     * Validates that start <= current <= end.
     */
    private static boolean validTimes(OffsetDateTime current, OffsetDateTime start, OffsetDateTime end) {
        return ((current.isEqual(start) || current.isAfter(start))
            && (current.isEqual(end) || current.isBefore(end)));
    }

}
