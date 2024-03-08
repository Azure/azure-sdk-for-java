// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * The data format of message.
 */
public final class WebPubSubDataFormat extends ExpandableStringEnum<WebPubSubDataFormat> {
    /**
     * the binary data.
     */
    public static final WebPubSubDataFormat BINARY = fromString("binary");

    /**
     * the JSON data.
     */
    public static final WebPubSubDataFormat JSON = fromString("json");

    /**
     * the text data.
     */
    public static final WebPubSubDataFormat TEXT = fromString("text");

    /**
     * the Protocol Buffers data.
     */
    public static final WebPubSubDataFormat PROTOBUF = fromString("protobuf");

    /**
     * Creates a new instance of WebPubSubDataFormat value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public WebPubSubDataFormat() {
    }

    /**
     * Creates or finds a WebPubSubDataFormat from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding WebPubSubDataFormat.
     */
    @JsonCreator
    public static WebPubSubDataFormat fromString(String name) {
        return fromString(name, WebPubSubDataFormat.class);
    }

    /**
     * Gets known WebPubSubDataFormat values.
     *
     * @return known WebPubSubDataFormat values.
     */
    public static Collection<WebPubSubDataFormat> values() {
        return values(WebPubSubDataFormat.class);
    }
}
