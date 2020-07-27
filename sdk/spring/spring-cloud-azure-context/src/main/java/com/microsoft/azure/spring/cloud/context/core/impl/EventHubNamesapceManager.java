/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;

public class EventHubNamesapceManager extends AzureManager<EventHubNamespace, String> {

    public EventHubNamesapceManager(Azure azure, AzureProperties azureProperties) {
        super(azure, azureProperties);
    }

    @Override
    String getResourceName(String key) {
        return key;
    }

    @Override
    String getResourceType() {
        return EventHubNamespace.class.getSimpleName();
    }

    @Override
    public EventHubNamespace internalGet(String namespace) {
        try {
            return azure.eventHubNamespaces().getByResourceGroup(azureProperties.getResourceGroup(), namespace);
        } catch (NullPointerException e) {
            // azure management api has no way to determine whether an eventhub namespace exists
            // Workaround for this is by catching NPE
            return null;
        }
    }

    @Override
    public EventHubNamespace internalCreate(String namespace) {
        return azure.eventHubNamespaces().define(namespace).withRegion(azureProperties.getRegion())
                    .withExistingResourceGroup(azureProperties.getResourceGroup()).create();
    }
}
