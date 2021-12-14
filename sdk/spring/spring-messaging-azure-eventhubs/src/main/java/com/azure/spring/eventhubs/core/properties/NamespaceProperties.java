// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties;

import com.azure.spring.service.eventhubs.properties.EventHubsNamespaceDescriptor;

/**
 * An event hub namespace related properties.
 */
public class NamespaceProperties extends CommonProperties implements EventHubsNamespaceDescriptor {

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
