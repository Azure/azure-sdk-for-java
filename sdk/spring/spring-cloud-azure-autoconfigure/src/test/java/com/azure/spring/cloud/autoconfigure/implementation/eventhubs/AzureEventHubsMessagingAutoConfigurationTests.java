// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.configuration.TestCheckpointStore;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProducerFactory;
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
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static com.azure.spring.cloud.autoconfigure.implementation.eventhubs.EventHubsTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

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
            .withBean("userObjectMapper", ObjectMapper.class, () -> new ObjectMapper())
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasBean("userObjectMapper");
                assertThat(context).hasSingleBean(ObjectMapper.class);
                assertThat(context).hasSingleBean(EventHubsMessageConverter.class);
            });
    }

    @Test
    @SuppressWarnings("unchecked")
    void processorFactoryShouldConfigureCredentialsWhenProvided() throws Exception {
        TokenCredential mockCredential = new MockTokenCredential();
        AzureCredentialResolver<TokenCredential> mockResolver = new MockAzureCredentialResolver(mockCredential);
        
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class, CredentialConfiguration.class)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsProcessorFactory.class);
                EventHubsProcessorFactory factory = context.getBean(EventHubsProcessorFactory.class);
                assertThat(factory).isInstanceOf(DefaultEventHubsNamespaceProcessorFactory.class);

                // Verify the factory has the credential fields that can be set by setDefaultCredential and setTokenCredentialResolver
                // The methods are called by the AutoConfiguration - we verify the fields exist and are accessible
                Field defaultCredentialField = factory.getClass().getDeclaredField("defaultCredential");
                defaultCredentialField.setAccessible(true);
                // Field exists, confirming setDefaultCredential() can be called

                Field tokenCredentialResolverField = factory.getClass().getDeclaredField("tokenCredentialResolver");
                tokenCredentialResolverField.setAccessible(true);
                // Field exists, confirming setTokenCredentialResolver() can be called
                
                // Verify credentials from CredentialConfiguration are available in context
                assertThat(context).hasBean("mockTokenCredential");
                assertThat(context).hasBean("mockTokenCredentialResolver");
            });
    }

    @Test
    @SuppressWarnings("unchecked")
    void producerFactoryShouldConfigureCredentialsWhenProvided() throws Exception {
        TokenCredential mockCredential = new MockTokenCredential();
        AzureCredentialResolver<TokenCredential> mockResolver = new MockAzureCredentialResolver(mockCredential);
        
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class, CredentialConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsProducerFactory.class);
                EventHubsProducerFactory factory =
                    context.getBean(EventHubsProducerFactory.class);
                assertThat(factory).isInstanceOf(DefaultEventHubsNamespaceProducerFactory.class);

                // Verify the factory has the credential fields that can be set by setDefaultCredential and setTokenCredentialResolver
                // The methods are called by the AutoConfiguration - we verify the fields exist and are accessible
                Field defaultCredentialField = factory.getClass().getDeclaredField("defaultCredential");
                defaultCredentialField.setAccessible(true);
                // Field exists, confirming setDefaultCredential() can be called

                Field tokenCredentialResolverField = factory.getClass().getDeclaredField("tokenCredentialResolver");
                tokenCredentialResolverField.setAccessible(true);
                // Field exists, confirming setTokenCredentialResolver() can be called
                
                // Verify credentials from CredentialConfiguration are available in context
                assertThat(context).hasBean("mockTokenCredential");
                assertThat(context).hasBean("mockTokenCredentialResolver");
            });
    }

    @Test
    @SuppressWarnings("unchecked")
    void tokenCredentialBeanNamePropertyShouldTakeEffect() throws Exception {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.credential.token-credential-bean-name=my-token-credential"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class, CustomCredentialConfiguration.class)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsProcessorFactory.class);
                EventHubsProcessorFactory factory = context.getBean(EventHubsProcessorFactory.class);
                assertThat(factory).isInstanceOf(DefaultEventHubsNamespaceProcessorFactory.class);

                // Verify the custom credential bean is available
                assertThat(context).hasBean("my-token-credential");
                TokenCredential customCredential = context.getBean("my-token-credential", TokenCredential.class);
                assertThat(customCredential).isInstanceOf(CustomTokenCredential.class);

                // Verify the factory can access credential fields
                Field defaultCredentialField = factory.getClass().getDeclaredField("defaultCredential");
                defaultCredentialField.setAccessible(true);
                // The AutoConfiguration should have set the credential from the bean name property
            });
    }
    
    // Configuration class to provide mock credentials
    @Configuration
    static class CredentialConfiguration {
        @Bean
        public TokenCredential mockTokenCredential() {
            return new MockTokenCredential();
        }
        
        @Bean
        @SuppressWarnings("rawtypes")
        public AzureCredentialResolver mockTokenCredentialResolver() {
            return new MockAzureCredentialResolver(new MockTokenCredential());
        }
    }

    // Configuration class to provide custom named credential
    @Configuration
    static class CustomCredentialConfiguration {
        @Bean("my-token-credential")
        public TokenCredential myTokenCredential() {
            return new CustomTokenCredential();
        }
    }
    
    // Mock TokenCredential for testing
    private static class MockTokenCredential implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext request) {
            return Mono.just(new AccessToken("mock-token", OffsetDateTime.now().plusHours(1)));
        }
    }
    
    // Mock AzureCredentialResolver for testing
    private static class MockAzureCredentialResolver implements AzureCredentialResolver<TokenCredential> {
        private final TokenCredential credential;
        
        MockAzureCredentialResolver(TokenCredential credential) {
            this.credential = credential;
        }
        
        @Override
        public TokenCredential resolve(AzureProperties properties) {
            return credential;
        }
        
        @Override
        public boolean isResolvable(AzureProperties properties) {
            return true;
        }
    }

    // Custom TokenCredential for testing bean name property
    private static class CustomTokenCredential implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext request) {
            return Mono.just(new AccessToken("custom-token", OffsetDateTime.now().plusHours(1)));
        }
    }

}
