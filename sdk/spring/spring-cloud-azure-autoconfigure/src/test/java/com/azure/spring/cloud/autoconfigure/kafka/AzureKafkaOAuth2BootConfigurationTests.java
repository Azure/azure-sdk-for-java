// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.service.implementation.kafka.KafkaOAuth2AuthenticateCallbackHandler;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AzureKafkaOAuth2BootConfigurationTests extends AbstractAzureKafkaOAuth2AutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093")
            .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaOAuth2AutoConfiguration.class,
                    AzureGlobalPropertiesAutoConfiguration.class, AzureTokenCredentialAutoConfiguration.class,
                    KafkaAutoConfiguration.class));
    @Override
    protected ApplicationContextRunner getContextRunner() {
        return this.contextRunner;
    }

    @Test
    void shouldNotConfigureWithoutKafkaTemplate() {
        getContextRunner()
                .withClassLoader(new FilteredClassLoader(KafkaTemplate.class))
                .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class));
    }

    @Test
    void shouldNotConfigureWhenKafkaDisabled() {
        getContextRunner()
                .withPropertyValues("spring.cloud.azure.eventhubs.kafka.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                });
    }

    @Test
    void testFactoryConfigureOAuthAndTokenCredential() {
        this.contextRunner
            .withPropertyValues(
                "spring.kafka.producer.properties.azure.credential.managed-identity-enabled=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                assertThat(context).hasSingleBean(DefaultKafkaConsumerFactoryCustomizer.class);
                assertThat(context).hasSingleBean(DefaultKafkaProducerFactoryCustomizer.class);
                assertThat(context).hasBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME);
                assertThat(context).hasSingleBean(ConsumerFactory.class);
                assertThat(context).hasSingleBean(ProducerFactory.class);

                DefaultKafkaConsumerFactory<?, ?> consumerFactory =
                    (DefaultKafkaConsumerFactory<?, ?>) context.getBean(ConsumerFactory.class);
                Map<String, Object> consumerProperties = consumerFactory.getConfigurationProperties();
                TokenCredential defaultAzureCredential =
                    (TokenCredential) context.getBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME);
                assertEquals(defaultAzureCredential, consumerProperties.get(AZURE_TOKEN_CREDENTIAL));
                shouldConfigureOAuthProperties(consumerProperties);

                DefaultKafkaProducerFactory<?, ?> producerFactory =
                    (DefaultKafkaProducerFactory<?, ?>) context.getBean(ProducerFactory.class);
                Map<String, Object> producerProperties = producerFactory.getConfigurationProperties();
                assertNotEquals(defaultAzureCredential, producerProperties.get(AZURE_TOKEN_CREDENTIAL));
                assertTrue(producerProperties.get(AZURE_TOKEN_CREDENTIAL) instanceof ManagedIdentityCredential);
                shouldConfigureOAuthProperties(producerProperties);
            });
    }

    @Test
    @SuppressWarnings("unchecked")
    void testOAuthConfiguredToCallbackHandler() {
        this.contextRunner
            .withPropertyValues(
                "spring.kafka.bootstrap-servers=test:9093",
                "spring.kafka.producer.properties.azure.credential.managed-identity-enabled=true"
            )
            .run(context -> {
                DefaultKafkaProducerFactory<?, ?> producerFactory =
                    (DefaultKafkaProducerFactory<?, ?>) context.getBean(ProducerFactory.class);
                Map<String, Object> producerProperties = producerFactory.getConfigurationProperties();

                KafkaOAuth2AuthenticateCallbackHandler callbackHandler = new KafkaOAuth2AuthenticateCallbackHandler();
                callbackHandler.configure(producerProperties, null, null);

                AzurePasswordlessProperties properties = (AzurePasswordlessProperties) ReflectionTestUtils
                    .getField(callbackHandler, "properties");
                AzureCredentialResolver<TokenCredential> azureTokenCredentialResolver =
                    (AzureCredentialResolver<TokenCredential>) ReflectionTestUtils.getField(callbackHandler, "tokenCredentialResolver");
                assertNotNull(azureTokenCredentialResolver);
                assertTrue(azureTokenCredentialResolver.resolve(properties) instanceof ManagedIdentityCredential);

                DefaultKafkaConsumerFactory<?, ?> consumerFactory =
                    (DefaultKafkaConsumerFactory<?, ?>) context.getBean(ConsumerFactory.class);
                Map<String, Object> consumerProperties = consumerFactory.getConfigurationProperties();
                callbackHandler.configure(consumerProperties, null, null);
                properties = (AzurePasswordlessProperties) ReflectionTestUtils.getField(callbackHandler, "properties");
                azureTokenCredentialResolver = (AzureCredentialResolver<TokenCredential>) ReflectionTestUtils.getField(callbackHandler, "tokenCredentialResolver");
                assertNotNull(azureTokenCredentialResolver);
                assertTrue(azureTokenCredentialResolver.resolve(properties) instanceof DefaultAzureCredential);
            });
    }

    @Override
    protected Map<String, Object> getConsumerProperties(ApplicationContext context) {
        return context.getBean(KafkaProperties.class).buildConsumerProperties();
    }

    @Override
    protected Map<String, Object> getProducerProperties(ApplicationContext context) {
        return context.getBean(KafkaProperties.class).buildProducerProperties();
    }

}
