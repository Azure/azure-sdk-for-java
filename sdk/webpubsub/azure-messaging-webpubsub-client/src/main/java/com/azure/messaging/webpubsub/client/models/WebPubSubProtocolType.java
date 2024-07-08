// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The type of Web Pubsub protocol.
 */
public final class WebPubSubProtocolType extends ExpandableStringEnum<WebPubSubProtocolType> {
    /**
     * the json.reliable.webpubsub.azure.v1 protocol.
     */
    public static final WebPubSubProtocolType JSON_RELIABLE_PROTOCOL = fromString("json.reliable.webpubsub.azure.v1");

    /**
     * the json.webpubsub.azure.v1 protocol.
     */
    public static final WebPubSubProtocolType JSON_PROTOCOL = fromString("json.webpubsub.azure.v1");

    /**
     * Creates a new instance of WebPubSubProtocolType value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public WebPubSubProtocolType() {
    }

    /**
     * Creates or finds a WebPubSubProtocolType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding WebPubSubProtocolType.
     */
    public static WebPubSubProtocolType fromString(String name) {
        return fromString(name, WebPubSubProtocolType.class);
    }

    /**
     * Gets known WebPubSubProtocolType values.
     *
     * @return known WebPubSubProtocolType values.
     */
    public static Collection<WebPubSubProtocolType> values() {
        return values(WebPubSubProtocolType.class);
    }
}
