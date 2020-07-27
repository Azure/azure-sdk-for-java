/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;

public class EventHubManager extends AzureManager<EventHub, Tuple<EventHubNamespace, String>> {

    public EventHubManager(Azure azure, AzureProperties azureProperties) {
        super(azure, azureProperties);
    }

    @Override
    String getResourceName(Tuple<EventHubNamespace, String> key) {
        return key.getSecond();
    }

    @Override
    String getResourceType() {
        return EventHub.class.getSimpleName();
    }

    @Override
    public EventHub internalGet(Tuple<EventHubNamespace, String> namespaceAndName) {
        return azure.eventHubs().getByName(azureProperties.getResourceGroup(), namespaceAndName.getFirst().name(),
                namespaceAndName.getSecond());
    }

    @Override
    public EventHub internalCreate(Tuple<EventHubNamespace, String> namespaceAndName) {
        return azure.eventHubs().define(namespaceAndName.getSecond()).withExistingNamespace(namespaceAndName.getFirst())
                    .create();
    }
}
