// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 *
 * @author Warren Zhu
 */

@ConditionalOnClass(EventHubClientBuilder.class)
@AzureEventHubAutoConfiguration.ConditionalOnEventHub
@Import({
    AzureEventHubClientConfiguration.class,
    AzureBlobCheckpointStoreConfiguration.class,
    AzureEventProcessorClientConfiguration.class
})
public class AzureEventHubAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureEventHubAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureEventHubProperties.PREFIX)
    public AzureEventHubProperties azureEventHubProperties() {
        return loadProperties(this.azureGlobalProperties, new AzureEventHubProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    @ConditionalOnProperty("spring.cloud.azure.eventhub.connection-string")
    public StaticConnectionStringProvider<AzureServiceType.EventHub> eventHubStaticConnectionStringProvider(
        AzureEventHubProperties eventHubProperties) {
        return new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUB,
                                                    eventHubProperties.getConnectionString());
    }

    /**
     * Condition indicates when event hub should be auto-configured.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression("${spring.cloud.azure.eventhub.enabled:true} and "
                                 + "(!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhub.connection-string:}') or "
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhub.namespace:}'))")
    public @interface ConditionalOnEventHub {
    }

}
