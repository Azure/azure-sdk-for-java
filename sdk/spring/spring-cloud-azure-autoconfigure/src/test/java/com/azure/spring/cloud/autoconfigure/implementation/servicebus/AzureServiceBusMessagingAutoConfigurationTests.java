// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.servicebus.core.ServiceBusConsumerFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusProducerFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson2.autoconfigure.Jackson2AutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

import static com.azure.spring.cloud.autoconfigure.implementation.util.TestServiceBusUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;

class AzureServiceBusMessagingAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusMessagingAutoConfiguration.class));

    @Test
    void disableServiceBusShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.enabled=false",
                "spring.cloud.azure.servicebus.namespace=test-namespace"
            )
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ConsumerContainerConfiguration.class);
            });
    }

    @Test
    void withoutServiceBusTemplateShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ServiceBusTemplate.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.namespace=test-namespace"
            )
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ConsumerContainerConfiguration.class);
            });
    }

    @Test
    void withoutServiceBusConnectionShouldNotConfigure() {
        this.contextRunner
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ConsumerContainerConfiguration.class);
            });
    }

    @Test
    void connectionInfoProvidedShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusProcessorFactory.class);
                assertThat(context).hasSingleBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusMessagingAutoConfiguration.ConsumerContainerConfiguration.class);
            });
    }

    @Test
    @SuppressWarnings("removal")
    void withoutObjectMapperShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ObjectMapper.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withConfiguration(AutoConfigurations.of(Jackson2AutoConfiguration.class))
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> assertThatIllegalStateException());
    }

    @Test
    @SuppressWarnings("removal")
    void withIsolatedObjectMapper() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"))
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withConfiguration(AutoConfigurations.of(Jackson2AutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasBean("defaultServiceBusMessageConverter");
                assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                assertThat(context).doesNotHaveBean("serviceBusMessageConverter");
            });
    }

    @Test
    @SuppressWarnings("removal")
    void withNonIsolatedObjectMapper() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.message-converter.isolated-object-mapper=false")
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withConfiguration(AutoConfigurations.of(Jackson2AutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasBean("serviceBusMessageConverter");
                assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                assertThat(context).doesNotHaveBean("defaultServiceBusMessageConverter");
            });
    }

    @Test
    @SuppressWarnings("removal")
    void withUserProvidedObjectMapper() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.message-converter.isolated-object-mapper=false")
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean("userObjectMapper", ObjectMapper.class, () -> new ObjectMapper())
            .withConfiguration(AutoConfigurations.of(Jackson2AutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasBean("userObjectMapper");
                assertThat(context).hasSingleBean(ObjectMapper.class);
                assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
            });
    }

    @Test
    void testCustomTokenCredentialConfiguration() {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(CustomTokenCredentialConfiguration.class,
                AzureTokenCredentialAutoConfiguration.class,
                AzureGlobalPropertiesAutoConfiguration.class))
            .withBean(ServiceBusMessageConverter.class, () -> mock(ServiceBusMessageConverter.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.servicebus.credential.token-credential-bean-name=customTokenCredential"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                TokenCredential customCredential = context.getBean("customTokenCredential", TokenCredential.class);
                AzureServiceBusProperties serviceBusProperties = context.getBean(AzureServiceBusProperties.class);

                assertThat(serviceBusProperties.getCredential().getTokenCredentialBeanName())
                    .isEqualTo("customTokenCredential");

                ServiceBusProducerFactory producerFactory = context.getBean(ServiceBusProducerFactory.class);
                ServiceBusProcessorFactory processorFactory = context.getBean(ServiceBusProcessorFactory.class);
                ServiceBusConsumerFactory consumerFactory = context.getBean(ServiceBusConsumerFactory.class);

                assertThat(resolveCredential(producerFactory, serviceBusProperties))
                    .isSameAs(customCredential);
                assertThat(resolveCredential(processorFactory, serviceBusProperties))
                    .isSameAs(customCredential);
                assertThat(resolveCredential(consumerFactory, serviceBusProperties))
                    .isSameAs(customCredential);

                // Validate runtime producer creation
                producerFactory.createProducer("test-queue", ServiceBusEntityType.QUEUE).close();
            });
    }

    @SuppressWarnings("unchecked")
    private TokenCredential resolveCredential(Object factory, AzureServiceBusProperties properties) throws Exception {
        Field field = factory.getClass().getDeclaredField("tokenCredentialResolver");
        field.setAccessible(true);
        AzureCredentialResolver<TokenCredential> resolver = (AzureCredentialResolver<TokenCredential>) field.get(factory);
        return resolver.resolve(properties);
    }

    @Configuration
    public static class CustomTokenCredentialConfiguration {
        @Bean
        public TokenCredential customTokenCredential() {
            return mock(TokenCredential.class);
        }
    }

}
