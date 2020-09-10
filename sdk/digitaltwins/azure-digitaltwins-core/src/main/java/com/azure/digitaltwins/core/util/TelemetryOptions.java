package com.azure.digitaltwins.core.util;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * The additional information to be used when processing a telemetry request.
 */
@Fluent
public final class TelemetryOptions {

    /**
     * A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages.
     * Defaults to a random guid.
     */
    private String messageId = UUID.randomUUID().toString();

    /**
     * An RFC 3339 timestamp that identifies the time the telemetry was measured.
     * It defaults to the current date/time UTC.
     */
    private OffsetDateTime timeStamp = OffsetDateTime.now(ZoneOffset.UTC);

    /**
     * Gets the message Id.
     * @return A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages.
     */
    public String getMessageId() {
        return this.messageId;
    }

    /**
     * Gets the timestamp.
     * @return The timestamp that identifies the time the telemetry was measured.
     */
    public OffsetDateTime getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * Set the message Id
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages.
     * @return The TelemetryOption object itself.
     */
    public TelemetryOptions setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Set the timestamp
     * @param timeStamp The timestamp that identifies the time the telemetry was measured.
     * @return The TelemetryOption object itself.
     */
    public TelemetryOptions setTimeStamp(OffsetDateTime timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }
}
