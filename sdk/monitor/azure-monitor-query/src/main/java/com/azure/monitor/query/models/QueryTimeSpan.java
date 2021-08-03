// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Represents the time interval for which a query is performed. The interval is represented as ISO 8601 format.
 */
@Immutable
public final class QueryTimeSpan {
    private final Duration queryDuration;
    private final Duration endDuration;
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;

    /**
     * Creates an instance of {@link QueryTimeSpan} using the provided duration.
     * @param duration the duration for this query time span.
     */
    public QueryTimeSpan(Duration duration) {
        this.queryDuration = Objects.requireNonNull(duration, "'duration' cannot be null");
        this.endDuration = null;
        this.startTime = null;
        this.endTime = null;
    }

    /**
     * Creates an instance of {@link QueryTimeSpan} using the start and end {@link OffsetDateTime OffsetDateTimes}.
     * @param startTime The start time of the interval.
     * @param endTime The end time of the interval.
     */
    public QueryTimeSpan(OffsetDateTime startTime, OffsetDateTime endTime) {
        this.startTime = Objects.requireNonNull(startTime, "'startTime' cannot be null");
        this.endTime = Objects.requireNonNull(endTime, "'endTime' cannot be null");
        this.queryDuration = null;
        this.endDuration = null;
    }

    /**
     * Creates an instance of {@link QueryTimeSpan} using the start and end duration of the interval.
     * @param startTime The start time of the interval.
     * @param durationFromStartTime The end duration of the interval.
     */
    public QueryTimeSpan(OffsetDateTime startTime, Duration durationFromStartTime) {
        this.queryDuration = null;
        this.endTime = null;
        this.startTime = Objects.requireNonNull(startTime, "'startTime' cannot be null");
        this.endDuration = Objects.requireNonNull(durationFromStartTime, "'duration' cannot be null");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (startTime != null) {
            sb.append(startTime);
            sb.append("/");
            if (endTime != null) {
                sb.append(endTime);
            }
            if (endDuration != null) {
                sb.append(endDuration);
            }
        }

        if (queryDuration != null) {
            sb.append(queryDuration);
        }
        return sb.toString();
    }
}
