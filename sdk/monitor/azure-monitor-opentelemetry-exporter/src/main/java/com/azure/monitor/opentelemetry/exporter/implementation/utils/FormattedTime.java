// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public final class FormattedTime {

    public static OffsetDateTime offSetDateTimeFromNow() {
        return offSetDateTimeFromEpochMillis(System.currentTimeMillis());
    }

    public static OffsetDateTime offSetDateTimeFromEpochNanos(long epochNanos) {
        return Instant.ofEpochMilli(NANOSECONDS.toMillis(epochNanos)).atOffset(ZoneOffset.UTC);
    }

    public static OffsetDateTime offSetDateTimeFromEpochMillis(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atOffset(ZoneOffset.UTC);
    }

    private FormattedTime() {
    }
}
