// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.Utils;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusReceiverClientBuilderFactory;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusSessionReceiverClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * Configuration for a {@link ServiceBusReceiverClient} and a {@link ServiceBusReceiverAsyncClient} or a
 * {@link ServiceBusSessionReceiverAsyncClient} and a {@link ServiceBusSessionReceiverClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-name", "consumer.entity-name" })
@Import({
    AzureServiceBusConsumerClientConfiguration.SessionConsumerClientConfiguration.class,
    AzureServiceBusConsumerClientConfiguration.NoneSessionConsumerClientConfiguration.class
})
class AzureServiceBusConsumerClientConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.consumer.session-enabled", havingValue = "false",
        matchIfMissing = true)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-type", "consumer.entity-type" })
    static class NoneSessionConsumerClientConfiguration {

        private final Environment env;
        NoneSessionConsumerClientConfiguration(Environment env) {
            this.env = env;
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder serviceBusReceiverClientBuilder(ConfigurationBuilder configurationBuilder,
                                                              @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                                              Optional<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusReceiverClientBuilder>> builderCustomizer) {

            com.azure.core.util.Configuration consumerSection = null;
            // there maybe some room for improvement on SDK configuration here, but this is also not bad
            if (env.containsProperty("spring.cloud.azure.servicebus.consumer.entity-type")) {
                consumerSection = configurationBuilder.buildSection("servicebus.consumer");
            } else {
                consumerSection = configurationBuilder.buildSection("servicebus");
            }

            return Utils.configureBuilder(
                new ServiceBusClientBuilder().receiver(),
                consumerSection,
                defaultTokenCredential,
                builderCustomizer);
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusReceiverAsyncClient serviceBusReceiverAsyncClient(
            ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusReceiverClient serviceBusReceiverClient(
            ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildClient();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.consumer.session-enabled", havingValue = "true")
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-type", "consumer.entity-type" })
    static class SessionConsumerClientConfiguration {

        private final Environment env;
        SessionConsumerClientConfiguration(Environment env) {
            this.env = env;
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverAsyncClient(ConfigurationBuilder configurationBuilder,
                                                                                                @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                                                                                Optional<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder>> builderCustomizer) {
            com.azure.core.util.Configuration consumerSection = null;
            // there maybe some room for improvement on SDK configuration here, but this is also not bad
            if (env.containsProperty("spring.cloud.azure.servicebus.consumer.entity-type")) {
                consumerSection = configurationBuilder.buildSection("servicebus.consumer");
            } else {
                consumerSection = configurationBuilder.buildSection("servicebus");
            }

            return Utils.configureBuilder(
                new ServiceBusClientBuilder().sessionReceiver(),
                consumerSection,
                defaultTokenCredential,
                builderCustomizer);
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusSessionReceiverAsyncClient serviceBusSessionReceiverAsyncClient(
            ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusSessionReceiverClient serviceBusSessionReceiverClient(
            ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildClient();
        }

    }
}
