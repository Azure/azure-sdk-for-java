// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.Utils;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusProcessorClientBuilderFactory;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
 * Configuration for a {@link ServiceBusProcessorClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(MessageProcessingListener.class)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-name", "processor.entity-name" })
@Import({
    AzureServiceBusProcessorClientConfiguration.SessionProcessorClientConfiguration.class,
    AzureServiceBusProcessorClientConfiguration.NoneSessionProcessorClientConfiguration.class
})
class AzureServiceBusProcessorClientConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.session-enabled", havingValue = "false",
        matchIfMissing = true)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-type", "processor.entity-type" })
    static class NoneSessionProcessorClientConfiguration {

        private final Environment env;
        NoneSessionProcessorClientConfiguration(Environment env) {
            this.env = env;
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder serviceBusProcessorClientBuilder(ConfigurationBuilder configurationBuilder,
                                                                                              @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                                                                              ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilder,
                                                                                              Optional<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> builderCustomizer) {

            com.azure.core.util.Configuration processorSection = null;
            // there maybe some room for improvement on SDK configuration here, but this is also not bad
            if (env.containsProperty("spring.cloud.azure.servicebus.processor.entity-type")) {
                processorSection = configurationBuilder.buildSection("servicebus.processor");
            } else {
                processorSection = configurationBuilder.buildSection("servicebus");
            }

            return Utils.configureBuilder(
                isDedicatedConnection(processorSection) ?  new ServiceBusClientBuilder().processor() : serviceBusClientBuilder.getIfAvailable().processor(),
                processorSection,
                defaultTokenCredential,
                builderCustomizer);
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProcessorClient serviceBusProcessorClient(
            ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorClientBuilder) {
            return processorClientBuilder.buildProcessorClient();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.session-enabled", havingValue = "true")
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-type", "processor.entity-type" })
    static class SessionProcessorClientConfiguration {

        private final Environment env;
        SessionProcessorClientConfiguration(Environment env) {
            this.env = env;
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder serviceBusSessionProcessorClientBuilder(ConfigurationBuilder configurationBuilder,
                                                                                                  @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                                                                                  ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilder,
                                                                                                  Optional<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> builderCustomizer) {

            com.azure.core.util.Configuration processorSection = null;
            // there maybe some room for improvement on SDK configuration here, but this is also not bad
            if (env.containsProperty("spring.cloud.azure.servicebus.processor.entity-type")) {
                processorSection = configurationBuilder.buildSection("servicebus.processor");
            } else {
                processorSection = configurationBuilder.buildSection("servicebus");
            }

            return Utils.configureBuilder(
                isDedicatedConnection(processorSection) ?  new ServiceBusClientBuilder().sessionProcessor() : serviceBusClientBuilder.getIfAvailable().sessionProcessor(),
                processorSection,
                defaultTokenCredential,
                builderCustomizer);
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProcessorClient serviceBusProcessorClient(
            ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder processorClientBuilder) {
            return processorClientBuilder.buildProcessorClient();
        }
    }

    private static boolean isDedicatedConnection(com.azure.core.util.Configuration configuration) {
        return configuration.contains("namespace") || configuration.contains("connection-string");
    }
}
