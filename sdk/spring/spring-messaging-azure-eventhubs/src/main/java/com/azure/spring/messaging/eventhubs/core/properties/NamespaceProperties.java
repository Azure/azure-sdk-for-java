// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.properties;

import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubsNamespaceProperties;

/**
 * An event hub namespace related properties.
 */
public class NamespaceProperties extends CommonProperties implements EventHubsNamespaceProperties {

    private Boolean sharedConnection;

    @Override
    public Boolean getSharedConnection() {
        return sharedConnection;
    }

    /**
     * Set if to enable shared connection.
     * @param sharedConnection if to enable shared connection.
     */
    public void setSharedConnection(Boolean sharedConnection) {
        this.sharedConnection = sharedConnection;
    }
}
