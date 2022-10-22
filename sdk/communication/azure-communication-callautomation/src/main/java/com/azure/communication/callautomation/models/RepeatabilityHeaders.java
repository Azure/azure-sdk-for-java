// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * The Repeatability Headers.
 */
@Fluent
public final class RepeatabilityHeaders {
    /**
     * The value of the Repeatability-Request-Id is an opaque string representing a client-generated unique identifier for the request.
     * It is a version 4 (random) UUID.
     */
    private final UUID repeatabilityRequestId;

    /**
     * The value should be the date and time at which the request was first created.
     */
    private final ZonedDateTime repeatabilityFirstSent;

    /**
     * Constructor
     *
     * @param repeatabilityRequestId The value of the Repeatability-Request-Id is an opaque string representing a client-generated unique identifier for the request.
     *                               It is a version 4 (random) UUID.
     * @param repeatabilityFirstSent The value should be the date and time at which the request was first created.
     */
    public RepeatabilityHeaders(UUID repeatabilityRequestId, Instant repeatabilityFirstSent) {
        this.repeatabilityRequestId = repeatabilityRequestId;
        this.repeatabilityFirstSent = repeatabilityFirstSent.atZone(ZoneId.of("UTC"));
    }

    /**
     * Get the repeatabilityRequestId : The value of the Repeatability-Request-Id is an opaque string representing a client-generated unique identifier for the request.
     *                                It is a version 4 (random) UUID.
     * @return the repeatabilityRequestId.
     */
    public UUID getRepeatabilityRequestId() {
        return repeatabilityRequestId;
    }

    /**
     * Get the repeatabilityFirstSent in IMF-fixdate form of HTTP-date format.
     * @return the repeatabilityFirstSent in a string with IMF-fixdate form of HTTP-date format.
     */
    public String getRepeatabilityFirstSentInHttpDateFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));
        return repeatabilityFirstSent.format(formatter);
    }

    /**
     * Get the repeatabilityFirstSent : The value should be the date and time at which the request was first created.
     * @return the repeatabilityFirstSent.
     */
    public ZonedDateTime getRepeatabilityFirstSent() {
        return repeatabilityFirstSent;
    }
}
