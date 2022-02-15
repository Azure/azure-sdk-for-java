// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureDefaultTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.Utils;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
import com.azure.spring.servicebus.core.producer.implementation.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.util.Optional;
import java.util.function.Supplier;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.CONFIGURATION_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;


/**
 * An auto-configuration for Service Bus Queue.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ServiceBusTemplate.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "connection-string", "namespace" })
@Import({
    AzureDefaultTokenCredentialAutoConfiguration.class,
    AzureServiceBusMessagingAutoConfiguration.ServiceBusTemplateConfiguration.class,
})
public class AzureServiceBusMessagingAutoConfiguration {

    /**
     * Configure the {@link ServiceBusTemplate}
     */
    @Configuration(proxyBeanMethods = false)
    public static class ServiceBusTemplateConfiguration {

        private final Environment env;
        ServiceBusTemplateConfiguration(Environment env) {
            this.env = env;
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProducerFactory defaultServiceBusNamespaceProducerFactory(
            @Qualifier(CONFIGURATION_BUILDER_BEAN_NAME) ConfigurationBuilder configurationBuilder,
                                                                                   @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                                                                   Optional<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> builderCustomizer) {
            Supplier<ServiceBusClientBuilder.ServiceBusSenderClientBuilder> builderSupplier = () -> {
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
            };

            return new DefaultServiceBusNamespaceProducerFactory(builderSupplier);
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusMessageConverter messageConverter() {
            return new ServiceBusMessageConverter();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusProducerFactory.class)
        public ServiceBusTemplate serviceBusTemplate(ServiceBusProducerFactory producerCache,
                                                     ServiceBusMessageConverter messageConverter) {
            ServiceBusTemplate serviceBusTemplate = new ServiceBusTemplate(producerCache);
            serviceBusTemplate.setMessageConverter(messageConverter);
            return serviceBusTemplate;
        }
    }
}
