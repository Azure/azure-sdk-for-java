// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

public class ConnectionProperties {
    private final String connectionId;
    private final String host;

    ConnectionProperties(String connectionId, String host) {
        this.connectionId = connectionId;
        this.host = host;
    }

    public String connectionId() {
        return connectionId;
    }

    public String host() {
        return host;
    }
}
