// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.models;

/**
 * Defines values for ClientEndpointType
 */
public enum ClientEndpointType {
    /**
     * Default WebPubSub Client Endpoint. E.g: <code>wss://exampleHost.com/client/hubs/exampleHub</code>
     */
    DEFAULT("default"),

    /**
     * MQTT Client Endpoint. E.g: <code>wss://exampleHost.com/clients/mqtt/hubs/exampleHub</code>
     */
    MQTT("mqtt");

    /**
     * The actual serialized value for a ClientEndpointType.
     */
    private final String value;

    ClientEndpointType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ClientEndpointType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ClientEndpointType object, or null if unable to parse.
     */
    public static ClientEndpointType fromString(String value) {
        ClientEndpointType[] items = ClientEndpointType.values();
        for (ClientEndpointType item : items) {
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
