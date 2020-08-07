// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.management.servicebus.AuthorizationKeys;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManager;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * An auto-configuration for Service Bus
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(IMessage.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureServiceBusProperties.class)
public class AzureServiceBusAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(AzureServiceBusAutoConfiguration.class);
    private static final String SERVICE_BUS = "ServiceBus";

    @SuppressWarnings("rawtypes")
    private static String buildConnectionString(ResourceManager<ServiceBusNamespace, String> serviceBusNamespaceManager,
                                                String namespace) {
        return serviceBusNamespaceManager.getOrCreate(namespace).authorizationRules().list().stream().findFirst()
            .map(com.microsoft.azure.management.servicebus.AuthorizationRule::getKeys)
            .map(AuthorizationKeys::primaryConnectionString)
            .map(s -> new ConnectionStringBuilder(s, namespace).toString()).orElseThrow(
                () -> new RuntimeException(
                    String.format("Service bus namespace '%s' key is empty", namespace), null));
    }

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS);
    }

    @Bean
    @ConditionalOnBean(ResourceManagerProvider.class)
    public AzureServiceBusProperties serviceBusProperties(ResourceManagerProvider resourceManagerProvider,
                                                          AzureServiceBusProperties serviceBusProperties) {
        if (!StringUtils.hasText(serviceBusProperties.getConnectionString())) {
            ResourceManager<ServiceBusNamespace, String> serviceBusNamespaceManager =
                resourceManagerProvider.getServiceBusNamespaceManager();
            serviceBusNamespaceManager.getOrCreate(serviceBusProperties.getNamespace());
            serviceBusProperties.setConnectionString(
                buildConnectionString(resourceManagerProvider.getServiceBusNamespaceManager(),
                    serviceBusProperties.getNamespace()));
            LOG.info("'spring.cloud.azure.servicebus.connection-string' auto configured");
        }

        return serviceBusProperties;
    }
}
