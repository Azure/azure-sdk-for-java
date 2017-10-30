/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Simple wrapper over joda.time.DateTime used for specifying unix seconds format during serialization/deserialization.
 */
public final class UnixTime {
    /**
     * The actual datetime object.
     */
    private final DateTime dateTime;

    /**
     * Creates a new UnixTime object with the specified DateTime.
     * @param dateTime The DateTime object to wrap.
     */
    public UnixTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Creates a new UnixTime object with the specified DateTime.
     * @param unixSeconds The Unix seconds value.
     */
    public UnixTime(long unixSeconds) {
        this.dateTime = new DateTime(unixSeconds * 1000L, DateTimeZone.UTC);
    }

    /**
     * Returns the underlying DateTime.
     * @return The underlying DateTime.
     */
    public DateTime dateTime() {
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
        return this.dateTime.equals(rhs.dateTime());
    }
}
