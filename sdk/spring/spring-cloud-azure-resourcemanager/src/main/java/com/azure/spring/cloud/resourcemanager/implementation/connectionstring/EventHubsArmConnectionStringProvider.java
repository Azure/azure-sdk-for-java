// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.connectionstring;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.AuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationKey;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.resourcemanager.implementation.crud.EventHubNamespaceCrud;

/**
 * A connection string provider reads Event Hub connection string from Azure Resource Manager.
 */
public class EventHubsArmConnectionStringProvider extends ArmConnectionStringProvider<AzureServiceType.EventHubs> {

    private final String namespace;
    private final EventHubNamespaceCrud eventHubNamespaceCrud;

    /**
     * Creates a new instance of {@link EventHubsArmConnectionStringProvider}.
     * @param resourceManager the azure resource manager
     * @param resourceMetadata the azure resource metadata
     * @param namespace the namespace
     */
    public EventHubsArmConnectionStringProvider(AzureResourceManager resourceManager,
                                                AzureResourceMetadata resourceMetadata,
                                                String namespace) {
        super(resourceManager, resourceMetadata);
        this.namespace = namespace;
        this.eventHubNamespaceCrud = new EventHubNamespaceCrud(resourceManager, resourceMetadata);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String getConnectionString() {
        return this.eventHubNamespaceCrud
            .get(this.namespace)
            .listAuthorizationRules()
            .stream()
            .findFirst()
            .map(AuthorizationRule::getKeys)
            .map(EventHubAuthorizationKey::primaryConnectionString)
            .orElseThrow(() -> new RuntimeException(
                String.format("Failed to fetch connection string of namespace '%s'", this.namespace), null));
    }

    @Override
    public AzureServiceType.EventHubs getServiceType() {
        return AzureServiceType.EVENT_HUBS;
    }
}
