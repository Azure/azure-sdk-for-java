// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ContentType.
 */
public enum WebPubSubContentType {
    /** Enum value application/json. */
    APPLICATION_JSON("application/json"),

    /** Enum value application/octet-stream. */
    APPLICATION_OCTET_STREAM("application/octet-stream"),

    /** Enum value text/plain. */
    TEXT_PLAIN("text/plain");

    /** The actual serialized value for a ContentType instance. */
    private final String value;

    WebPubSubContentType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ContentType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ContentType object, or null if unable to parse.
     */
    @JsonCreator
    public static WebPubSubContentType fromString(String value) {
        WebPubSubContentType[] items = WebPubSubContentType.values();
        for (WebPubSubContentType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
