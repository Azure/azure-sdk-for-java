// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

/**
 * Represents a Service Bus session.
 */
public final class ServiceBusSession {
    private final String id;

    ServiceBusSession(String id) {
        this.id = id;
    }

    /**
     * Gets the session id.
     *
     * @return the session id.
     */
    public String getId() {
        return id;
    }
}
