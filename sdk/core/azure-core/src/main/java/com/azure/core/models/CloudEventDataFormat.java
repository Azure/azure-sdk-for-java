// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.util.BinaryData;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.core.util.SimpleCache;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Representation of the data format for a {@link CloudEvent}.
 * <p>
 * When constructing a {@link CloudEvent} this is passed to determine the serialized format of the event's data.
 * If {@link #BYTES} is used the data will be stored as a Base64 encoded string,
 * otherwise it will be stored as a JSON serialized object.
 * @see CloudEvent#CloudEvent(String, String, BinaryData, com.azure.core.models.CloudEventDataFormat, String)
 */
public final class CloudEventDataFormat extends ExpandableStringEnum<CloudEventDataFormat> {
    private static final Map<String, CloudEventDataFormat> CONSTANTS = new ConcurrentHashMap<>();

    // Should there be two caches? One for the constant values and one for the runtime values?
    // Or should any value not in the constants be a new instance and not cached?
    private static final SimpleCache<String, CloudEventDataFormat> RUNTIME = new SimpleCache<>();

    private final String value;

    /**
     * Creates a new instance of {@link CloudEventDataFormat} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link CloudEventDataFormat} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public CloudEventDataFormat() {
        this.value = null;
    }

    @Deprecated
    private CloudEventDataFormat(String value) {
        this.value = value;
    }

    /**
     * Bytes format.
     */
    public static final CloudEventDataFormat BYTES = fromStringConstant("BYTES");

    /**
     * JSON format.
     */
    public static final CloudEventDataFormat JSON = fromStringConstant("JSON");

    /**
     * Creates or gets a CloudEventDataFormat from its string representation.
     *
     * @param name Name of the CloudEventDataFormat.
     * @return The corresponding CloudEventDataFormat.
     */
    @JsonCreator
    public static CloudEventDataFormat fromString(String name) {
        if (name == null) {
            return null;
        }

        CloudEventDataFormat format = CONSTANTS.get(name);
        if (format != null) {
            return format;
        }

        format = RUNTIME.get(name);
        if (format != null) {
            return format;
        }

        return RUNTIME.computeIfAbsent(name, CloudEventDataFormat::new);
    }

    // This method should only be called by constants.
    private static CloudEventDataFormat fromStringConstant(String name) {
        return CONSTANTS.computeIfAbsent(name, CloudEventDataFormat::new);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(CloudEventDataFormat.class, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CloudEventDataFormat)) {
            return false;
        }

        CloudEventDataFormat other = (CloudEventDataFormat) obj;
        return Objects.equals(value, other.value);
    }
}
