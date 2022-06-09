// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.connectionstring;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.AuthorizationKeys;
import com.azure.resourcemanager.servicebus.models.AuthorizationRule;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusNamespaceCrud;

/**
 * A connection string provider reads Service Bus connection string from Azure Resource Manager.
 */
public class ServiceBusArmConnectionStringProvider extends ArmConnectionStringProvider<AzureServiceType.ServiceBus> {

    private final String namespace;
    private final ServiceBusNamespaceCrud serviceBusNamespaceCrud;

    /**
     * Creates a new instance of {@link ServiceBusArmConnectionStringProvider}.
     * @param azureResourceManager the azure resource manager
     * @param azureResourceMetadata the azure resource metadata
     * @param namespace the namespace
     */
    public ServiceBusArmConnectionStringProvider(AzureResourceManager azureResourceManager,
                                                 AzureResourceMetadata azureResourceMetadata,
                                                 String namespace) {
        super(azureResourceManager, azureResourceMetadata);
        this.namespace = namespace;
        this.serviceBusNamespaceCrud = new ServiceBusNamespaceCrud(azureResourceManager, azureResourceMetadata);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String getConnectionString() {
        return this.serviceBusNamespaceCrud
            .get(this.namespace)
            .authorizationRules()
            .list()
            .stream()
            .findFirst()
            .map(AuthorizationRule::getKeys)
            .map(AuthorizationKeys::primaryConnectionString)
            .orElseThrow(
                () -> new RuntimeException(String.format("Service bus namespace '%s' key is empty", this.namespace),
                                           null));
    }

    @Override
    public AzureServiceType.ServiceBus getServiceType() {
        return AzureServiceType.SERVICE_BUS;
    }
}
