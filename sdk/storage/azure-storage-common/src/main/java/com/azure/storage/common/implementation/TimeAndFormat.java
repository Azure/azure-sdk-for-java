// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A class to hold an {@code OffsetDateTime} object and the formatter from which it was parsed for effective
 * roundtripping.
 * Without this, we always format date-times the same way, but if we parsed a value from a sas token in on an endpoint
 * or in a connection string, we have to preserve the formatting, especially the precision, so the signature still
 * matches.
 */
public final class TimeAndFormat {
    private final OffsetDateTime dateTime;
    private final DateTimeFormatter format;

    /**
     * Constructs a new {@code TimeAndFormat object}.
     *
     * @param dateTime The date-time object.
     * @param formatter The format it was parsed from. Null if it was not parsed from a {@code DateTimeFormatter}.
     */
    public TimeAndFormat(OffsetDateTime dateTime, DateTimeFormatter formatter) {
        // If the format is null, it means we didn't parse it and we can just use our default formatter
        this.dateTime = dateTime;
        this.format = formatter;
    }

    /**
     * Gets the date-time.
     *
     * @return The date-time
     */
    public OffsetDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Gets the format the time was parsed from.
     *
     * @return The format. May be null if time was not parsed.
     */
    public DateTimeFormatter getFormatter() {
        return format;
    }
}
