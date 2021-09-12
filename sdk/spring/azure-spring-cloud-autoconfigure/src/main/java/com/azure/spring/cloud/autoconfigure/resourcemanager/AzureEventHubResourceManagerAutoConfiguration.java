package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import com.azure.spring.cloud.resourcemanager.connectionstring.EventHubArmConnectionStringProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 *
 */
public class AzureEventHubResourceManagerAutoConfiguration extends AzureServiceResourceManagerConfigurationBase {

    private final AzureEventHubProperties eventHubProperties;

    public AzureEventHubResourceManagerAutoConfiguration(AzureResourceManager azureResourceManager,
                                                         AzureEventHubProperties eventHubProperties) {
        super(azureResourceManager);
        this.eventHubProperties = eventHubProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureEventHubProperties.PREFIX, value = "namespace")
    // TODO(xiada) conditional on missing connection-string property
    @Order
    public EventHubArmConnectionStringProvider eventHubArmConnectionStringProvider() {

        return new EventHubArmConnectionStringProvider(this.azureResourceManager,
                                                       this.eventHubProperties.getResource(),
                                                       this.eventHubProperties.getNamespace());
    }

}
