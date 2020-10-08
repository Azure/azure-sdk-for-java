// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

// This class manually copies the generated class of the same name, but also adds the property for timestamp
// since the swagger does not group it in with these options for us.

/** The DigitalTwinsSendComponentTelemetryOptions model. */
@Fluent
public final class DigitalTwinsSendComponentTelemetryOptions {
    /*
     * Identifies the request in a distributed tracing system.
     */
    @JsonProperty(value = "traceparent")
    private String traceparent;

    /*
     * Provides vendor-specific trace identification information and is a
     * companion to traceparent.
     */
    @JsonProperty(value = "tracestate")
    private String tracestate;

    /**
     * Get the traceparent property: Identifies the request in a distributed tracing system.
     *
     * @return the traceparent value.
     */
    public String getTraceparent() {
        return this.traceparent;
    }

    /**
     * Set the traceparent property: Identifies the request in a distributed tracing system.
     *
     * @param traceparent the traceparent value to set.
     * @return the DigitalTwinsSendComponentTelemetryOptions object itself.
     */
    public DigitalTwinsSendComponentTelemetryOptions setTraceparent(String traceparent) {
        this.traceparent = traceparent;
        return this;
    }

    /**
     * Get the tracestate property: Provides vendor-specific trace identification information and is a companion to
     * traceparent.
     *
     * @return the tracestate value.
     */
    public String getTracestate() {
        return this.tracestate;
    }

    /**
     * Set the tracestate property: Provides vendor-specific trace identification information and is a companion to
     * traceparent.
     *
     * @param tracestate the tracestate value to set.
     * @return the DigitalTwinsSendComponentTelemetryOptions object itself.
     */
    public DigitalTwinsSendComponentTelemetryOptions setTracestate(String tracestate) {
        this.tracestate = tracestate;
        return this;
    }

    /**
     * An RFC 3339 timestamp that identifies the time the telemetry was measured.
     * It defaults to the current date/time UTC.
     */
    private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

    /**
     * Gets the timestamp.
     * @return The timestamp that identifies the time the telemetry was measured.
     */
    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }

    /**
     * Set the timestamp
     * @param timestamp The timestamp that identifies the time the telemetry was measured.
     * @return The PublishTelemetryRequestOptions object itself.
     */
    public DigitalTwinsSendComponentTelemetryOptions setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
