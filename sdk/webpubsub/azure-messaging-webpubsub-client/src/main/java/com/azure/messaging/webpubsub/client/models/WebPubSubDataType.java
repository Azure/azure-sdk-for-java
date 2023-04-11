// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * The data type of message.
 */
public final class WebPubSubDataType extends ExpandableStringEnum<WebPubSubDataType> {
    /**
     * the binary data.
     */
    public static final WebPubSubDataType BINARY = fromString("binary");

    /**
     * the JSON data.
     */
    public static final WebPubSubDataType JSON = fromString("json");

    /**
     * the text data.
     */
    public static final WebPubSubDataType TEXT = fromString("text");

    /**
     * the Protocol Buffers data.
     */
    public static final WebPubSubDataType PROTOBUF = fromString("protobuf");

    /**
     * Creates a new instance of WebPubSubDataType value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public WebPubSubDataType() {
    }

    /**
     * Creates or finds a WebPubSubDataType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding WebPubSubDataType.
     */
    @JsonCreator
    public static WebPubSubDataType fromString(String name) {
        return fromString(name, WebPubSubDataType.class);
    }

    /**
     * Gets known WebPubSubDataType values.
     *
     * @return known WebPubSubDataType values.
     */
    public static Collection<WebPubSubDataType> values() {
        return values(WebPubSubDataType.class);
    }
}
