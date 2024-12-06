// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization.xml;

public enum NamespaceType {
    MESSAGING("Messaging"), NOTIFICATION_HUB("NotificationHub"), MIXED("Mixed"), EVENT_HUB("EventHub"), RELAY("Relay");

    private final String type;

    NamespaceType(String type) {
        this.type = type;
    }

    public static NamespaceType fromString(String type) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case "Messaging":
                return MESSAGING;

            case "NotificationHub":
                return NOTIFICATION_HUB;

            case "Mixed":
                return MIXED;

            case "EventHub":
                return EVENT_HUB;

            case "Relay":
                return RELAY;

            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return type;
    }
}
