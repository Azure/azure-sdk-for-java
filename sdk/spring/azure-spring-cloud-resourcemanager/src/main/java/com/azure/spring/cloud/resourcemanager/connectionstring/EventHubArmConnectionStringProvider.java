package com.azure.spring.cloud.resourcemanager.connectionstring;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.AuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationKey;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.crud.EventHubNamespaceCrud;
import com.azure.spring.core.service.AzureServiceType;

/**
 * @author Xiaolu Dai, 2021/9/9.
 */
public class EventHubArmConnectionStringProvider extends AbstractArmConnectionStringProvider<AzureServiceType.EventHub> {

    private final String namespace;
    private final EventHubNamespaceCrud eventHubNamespaceCrud;

    public EventHubArmConnectionStringProvider(AzureResourceManager resourceManager,
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
    public AzureServiceType.EventHub getServiceType() {
        return AzureServiceType.EVENT_HUB;
    }
}
