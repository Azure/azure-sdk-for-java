// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.ExpandableEnum;

/**
 * Representation of the data format for a {@link CloudEvent}.
 * <p>
 * When constructing a {@link CloudEvent} this is passed to determine the serialized format of the event's data.
 * If {@link #BYTES} is used the data will be stored as a Base64 encoded string,
 * otherwise it will be stored as a JSON serialized object.
 *
 * @see CloudEvent#CloudEvent(String, String, BinaryData, CloudEventDataFormat, String)
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class CloudEventDataFormat implements ExpandableEnum<String> {
    private final String value;

    private CloudEventDataFormat(String value) {
        this.value = value;
    }

    /**
     * Bytes format.
     */
    public static final CloudEventDataFormat BYTES = new CloudEventDataFormat("BYTES");

    /**
     * JSON format.
     */
    public static final CloudEventDataFormat JSON = new CloudEventDataFormat("JSON");

    @Override
    public String getValue() {
        return value;
    }
}
