// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * A wrapper over java.time.OffsetDateTime used for specifying unix seconds format during serialization and
 * deserialization.
 */
public final class UnixTime {
    /**
     * The actual datetime object.
     */
    private final OffsetDateTime dateTime;

    /**
     * Creates aUnixTime object with the specified DateTime.
     *
     * @param dateTime The DateTime object to wrap
     */
    public UnixTime(OffsetDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Creates a UnixTime object with the specified DateTime.
     *
     * @param unixSeconds The Unix seconds value
     */
    public UnixTime(long unixSeconds) {
        this.dateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(unixSeconds), ZoneOffset.UTC);
    }

    /**
     * Get the underlying DateTime.
     *
     * @return The underlying DateTime
     */
    public OffsetDateTime getDateTime() {
        if (this.dateTime == null) {
            return null;
        }
        return this.dateTime;
    }

    @Override
    public String toString() {
        return dateTime.toString();
    }

    @Override
    public int hashCode() {
        return this.dateTime.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof UnixTime)) {
            return false;
        }

        UnixTime rhs = (UnixTime) obj;
        return this.dateTime.equals(rhs.getDateTime());
    }
}
