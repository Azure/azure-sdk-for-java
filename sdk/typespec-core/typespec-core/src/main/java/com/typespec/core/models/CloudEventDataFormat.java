// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.models;

import com.typespec.core.util.BinaryData;
import com.typespec.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Representation of the data format for a {@link CloudEvent}.
 * <p>
 * When constructing a {@link CloudEvent} this is passed to determine the serialized format of the event's data.
 * If {@link #BYTES} is used the data will be stored as a Base64 encoded string,
 * otherwise it will be stored as a JSON serialized object.
 * @see CloudEvent#CloudEvent(String, String, BinaryData, com.typespec.core.models.CloudEventDataFormat, String)
 */
public final class CloudEventDataFormat extends ExpandableStringEnum<CloudEventDataFormat> {
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
    }

    /**
     * Bytes format.
     */
    public static final CloudEventDataFormat BYTES = fromString("BYTES", CloudEventDataFormat.class);

    /**
     * JSON format.
     */
    public static final CloudEventDataFormat JSON = fromString("JSON", CloudEventDataFormat.class);

    /**
     * Creates or gets a CloudEventDataFormat from its string representation.
     *
     * @param name Name of the CloudEventDataFormat.
     * @return The corresponding CloudEventDataFormat.
     */
    @JsonCreator
    public static CloudEventDataFormat fromString(String name) {
        return fromString(name, CloudEventDataFormat.class);
    }
}
