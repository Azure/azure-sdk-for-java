// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties;

import com.azure.spring.service.eventhubs.properties.EventHubNamespaceDescriptor;

/**
 * An event hub namespace related properties.
 */
public class NamespaceProperties extends CommonProperties implements EventHubNamespaceDescriptor {

    private Boolean sharedConnection;

    @Override
    public Boolean getSharedConnection() {
        return sharedConnection;
    }

    public void setSharedConnection(Boolean sharedConnection) {
        this.sharedConnection = sharedConnection;
    }
}
