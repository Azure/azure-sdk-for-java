// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 *
 */
@Immutable
public final class QueryTimeSpan {
    private final Duration queryDuration;
    private final Duration endDuration;
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;

    /**
     * @param duration
     */
    public QueryTimeSpan(Duration duration) {
        this.queryDuration = duration;
        this.endDuration = null;
        this.startTime = null;
        this.endTime = null;
    }

    /**
     * @param startTime
     * @param endTime
     */
    public QueryTimeSpan(OffsetDateTime startTime, OffsetDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.queryDuration = null;
        this.endDuration = null;
    }

    /**
     * @param startTime
     * @param durationFromStartTime
     */
    public QueryTimeSpan(OffsetDateTime startTime, Duration durationFromStartTime) {
        this.queryDuration = null;
        this.endTime = null;
        this.startTime = startTime;
        this.endDuration = durationFromStartTime;
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
