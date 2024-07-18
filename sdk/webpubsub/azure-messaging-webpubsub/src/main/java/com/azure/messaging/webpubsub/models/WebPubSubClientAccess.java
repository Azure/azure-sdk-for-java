// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.models;

/**
 * Defines values for WebPubSubClientAccess
 */
public enum WebPubSubClientAccess {
    /**
     * Default WebPubSub Client Endpoint. E.g: <code>wss://exampleHost.com/client/hubs/exampleHub</code>
     */
    DEFAULT("default"),

    /**
     * MQTT Client Endpoint. E.g: <code>wss://exampleHost.com/clients/mqtt/hubs/exampleHub</code>
     */
    MQTT("mqtt");

    /**
     * The actual serialized value for a WebPubSubClientAccess.
     */
    private final String value;

    WebPubSubClientAccess(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a WebPubSubClientAccess instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed WebPubSubClientAccess object, or null if unable to parse.
     */
    public static WebPubSubClientAccess fromString(String value) {
        WebPubSubClientAccess[] items = WebPubSubClientAccess.values();
        for (WebPubSubClientAccess item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
