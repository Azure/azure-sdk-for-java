// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.Utils;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusSenderClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.CONFIGURATION_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * Configuration for a {@link ServiceBusSenderClient} and a {@link ServiceBusSenderAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-name", "producer.entity-name" })
class AzureServiceBusProducerClientConfiguration {

    private final Environment env;
    AzureServiceBusProducerClientConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    @ConditionalOnMissingBean
    ServiceBusClientBuilder.ServiceBusSenderClientBuilder serviceBusReceiverClientBuilder(
        @Qualifier(CONFIGURATION_BUILDER_BEAN_NAME) ConfigurationBuilder configurationBuilder,
                                                                                          @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                                                                          Optional<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> builderCustomizer) {

        com.azure.core.util.Configuration producerSection = null;
        // there maybe some room for improvement on SDK configuration here, but this is also not bad
        if (env.containsProperty("spring.cloud.azure.servicebus.producer.entity-name")) {
            producerSection = configurationBuilder.buildSection("servicebus.producer");
        } else {
            producerSection = configurationBuilder.buildSection("servicebus");
        }

        return Utils.configureBuilder(
            new ServiceBusClientBuilder().sender(),
            producerSection,
            defaultTokenCredential,
            builderCustomizer);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class)
    public ServiceBusSenderAsyncClient serviceBusSenderAsyncClient(
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder) {
        return senderClientBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class)
    public ServiceBusSenderClient serviceBusSenderClient(
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder) {
        return senderClientBuilder.buildClient();
    }
}
