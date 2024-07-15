// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.models;

/**
 * Defines values for ClientType
 */
public enum ClientType {
    /**
     * Default WebPubSub Client Endpoint. E.g: <code>wss://exampleHost.com/client/hubs/exampleHub</code>
     */
    DEFAULT("default"),

    /**
     * MQTT Client Endpoint. E.g: <code>wss://exampleHost.com/clients/mqtt/hubs/exampleHub</code>
     */
    MQTT("mqtt");

    /**
     * The actual serialized value for a ClientType.
     */
    private final String value;

    ClientType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ClientType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ClientType object, or null if unable to parse.
     */
    public static ClientType fromString(String value) {
        ClientType[] items = ClientType.values();
        for (ClientType item : items) {
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
