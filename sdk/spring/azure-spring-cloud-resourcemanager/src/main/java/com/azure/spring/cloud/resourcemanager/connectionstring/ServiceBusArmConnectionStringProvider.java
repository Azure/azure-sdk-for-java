// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.connectionstring;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.AuthorizationKeys;
import com.azure.resourcemanager.servicebus.models.AuthorizationRule;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusNamespaceCrud;
import com.azure.spring.core.service.AzureServiceType;

/**
 * A connection string provider reads Service Bus connection string from Azure Resource Manager.
 */
public class ServiceBusArmConnectionStringProvider extends AbstractArmConnectionStringProvider<AzureServiceType.ServiceBus> {

    private final String namespace;
    private final ServiceBusNamespaceCrud serviceBusNamespaceCrud;
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
