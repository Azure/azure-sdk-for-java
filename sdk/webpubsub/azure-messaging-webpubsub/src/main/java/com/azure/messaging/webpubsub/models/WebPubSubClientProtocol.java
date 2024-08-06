// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.models;

/**
 * Defines values for WebPubSubClientProtocol
 */
public enum WebPubSubClientProtocol {
    /**
     * Default WebPubSub Client Endpoint. E.g: <code>wss://exampleHost.com/client/hubs/exampleHub</code>
     */
    DEFAULT("default"),

    /**
     * MQTT Client Endpoint. E.g: <code>wss://exampleHost.com/clients/mqtt/hubs/exampleHub</code>
     */
    MQTT("mqtt");

    /**
     * The actual serialized value for a WebPubSubClientProtocol.
     */
    private final String value;

    WebPubSubClientProtocol(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a WebPubSubClientProtocol instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed WebPubSubClientProtocol object, or null if unable to parse.
     */
    public static WebPubSubClientProtocol fromString(String value) {
        WebPubSubClientProtocol[] items = WebPubSubClientProtocol.values();
        for (WebPubSubClientProtocol item : items) {
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
