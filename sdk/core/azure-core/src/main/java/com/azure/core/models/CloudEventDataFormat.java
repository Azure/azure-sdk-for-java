// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.util.BinaryData;
import com.azure.core.util.ExpandableStringEnum;

/**
 * Representation of the data format for a {@link CloudEvent}.
 * <p>
 * When constructing a {@link CloudEvent} this is passed to determine the serialized format of the event's data.
 * If {@link #BYTES} is used the data will be stored as a Base64 encoded string,
 * otherwise it will be stored as a JSON serialized object.
 * @see CloudEvent#CloudEvent(String, String, BinaryData, com.azure.core.models.CloudEventDataFormat, String)
 */
public final class CloudEventDataFormat extends ExpandableStringEnum<CloudEventDataFormat> {
    /**
     * Creates a new instance of {@link CloudEventDataFormat} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link CloudEventDataFormat} which doesn't
     * have a String enum value.
     */
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
}
