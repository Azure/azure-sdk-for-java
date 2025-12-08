// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.configuration.TestCheckpointStore;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsProducerFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsTemplate;
import com.azure.spring.messaging.eventhubs.implementation.support.converter.EventHubsMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

import static com.azure.spring.cloud.autoconfigure.implementation.eventhubs.EventHubsTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;

class AzureEventHubsMessagingAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsMessagingAutoConfiguration.class));

    @Test
    void disableEventHubsShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.enabled=false",
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void withoutEventHubsTemplateShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubsTemplate.class))
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void withoutEventHubConnectionShouldNotConfigure() {
        this.contextRunner
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void withoutCheckpointStoreShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void connectionInfoAndCheckpointStoreProvidedShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsProcessorFactory.class);
                assertThat(context).hasSingleBean(AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class);
            });
    }

    @Test
    void withoutObjectMapperShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ObjectMapper.class))
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withPropertyValues(
            "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
        )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .run(context -> assertThatIllegalStateException());
    }

    @Test
    void withIsolatedObjectMapper() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"))
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasBean("defaultEventHubsMessageConverter");
                assertThat(context).hasSingleBean(EventHubsMessageConverter.class);
                assertThat(context).doesNotHaveBean("eventHubsMessageConverter");
            });
    }

    @Test
    void withNonIsolatedObjectMapper() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.message-converter.isolated-object-mapper=false")
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasBean("eventHubsMessageConverter");
                assertThat(context).hasSingleBean(EventHubsMessageConverter.class);
                assertThat(context).doesNotHaveBean("defaultEventHubsMessageConverter");
            });
    }

    @Test
    void withUserProvidedObjectMapper() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.message-converter.isolated-object-mapper=false")
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean("userObjectMapper", ObjectMapper.class, ObjectMapper::new)
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasBean("userObjectMapper");
                assertThat(context).hasSingleBean(ObjectMapper.class);
                assertThat(context).hasSingleBean(EventHubsMessageConverter.class);
            });
    }

    @Test
    void testCustomTokenCredentialConfiguration() {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(CustomTokenCredentialConfiguration.class,
                AzureTokenCredentialAutoConfiguration.class,
                AzureGlobalPropertiesAutoConfiguration.class))
            .withBean(EventHubsMessageConverter.class, EventHubsMessageConverter::new)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.eventhubs.credential.token-credential-bean-name=customTokenCredential"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .run(context -> {

                // Verify that the properties contain the correct credential bean name
                AzureEventHubsProperties eventHubsProperties = context.getBean(AzureEventHubsProperties.class);
                assertThat(eventHubsProperties).isNotNull();
                assertThat(eventHubsProperties.getCredential()).isNotNull();
                assertThat(eventHubsProperties.getCredential().getTokenCredentialBeanName())
                    .as("The token-credential-bean-name property should be set to customTokenCredential")
                    .isEqualTo("customTokenCredential");

                // Verify that the custom token credential bean exists
                assertThat(context).hasBean("customTokenCredential");
                TokenCredential customCredential = context.getBean("customTokenCredential", TokenCredential.class);
                assertThat(customCredential).isNotNull();

                // Verify the EventHubsProducerFactory has the tokenCredentialResolver configured
                assertThat(context).hasSingleBean(EventHubsProducerFactory.class);
                EventHubsProducerFactory producerFactory = context.getBean(EventHubsProducerFactory.class);
                assertThat(producerFactory).isNotNull();

                // Verify tokenCredentialResolver resolves to the custom credential
                Field tokenCredentialResolverField = producerFactory.getClass().getDeclaredField("tokenCredentialResolver");
                tokenCredentialResolverField.setAccessible(true);
                Object tokenCredentialResolver = tokenCredentialResolverField.get(producerFactory);
                assertThat(tokenCredentialResolver).as("TokenCredentialResolver should be configured").isNotNull();

                // Cast to AzureCredentialResolver and invoke resolve() to verify it returns customTokenCredential
                @SuppressWarnings("unchecked")
                AzureCredentialResolver<TokenCredential> resolver =
                    (AzureCredentialResolver<TokenCredential>) tokenCredentialResolver;
                TokenCredential resolvedCredential = resolver.resolve(eventHubsProperties);
                assertThat(resolvedCredential)
                    .as("The resolved credential should be the customTokenCredential bean")
                    .isSameAs(customCredential);
            });
    }

    @Configuration
    public static class CustomTokenCredentialConfiguration {
        @Bean
        public TokenCredential customTokenCredential() {
            return mock(TokenCredential.class);
        }
    }

}
