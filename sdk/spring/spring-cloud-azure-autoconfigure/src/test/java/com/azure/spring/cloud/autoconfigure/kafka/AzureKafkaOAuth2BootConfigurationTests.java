// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.service.implementation.kafka.KafkaOAuth2AuthenticateCallbackHandler;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.apache.kafka.common.config.types.Password;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
class AzureKafkaOAuth2BootConfigurationTests extends AbstractAzureKafkaOAuth2AutoConfigurationTests<KafkaProperties, KafkaPropertiesBeanPostProcessor> {

    AzureKafkaOAuth2BootConfigurationTests() {
        super(new KafkaPropertiesBeanPostProcessor(new AzureGlobalProperties()));
    }

    private final ApplicationContextRunner contextRunnerWithoutEventHubsURL = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaOAuth2AutoConfiguration.class,
            AzureGlobalPropertiesAutoConfiguration.class, AzureTokenCredentialAutoConfiguration.class,
            KafkaAutoConfiguration.class));

    private final ApplicationContextRunner contextRunnerWithEventHubsURL = contextRunnerWithoutEventHubsURL
        .withPropertyValues("spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093");

    @Override
    protected ApplicationContextRunner getContextRunnerWithEventHubsURL() {
        return this.contextRunnerWithEventHubsURL;
    }

    @Override
    protected ApplicationContextRunner getContextRunnerWithoutEventHubsURL() {
        return this.contextRunnerWithoutEventHubsURL;
    }

    @Test
    void shouldNotConfigureWithoutKafkaTemplate() {
        getContextRunnerWithEventHubsURL()
                .withClassLoader(new FilteredClassLoader(KafkaTemplate.class))
                .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class));
    }

    @Test
    void shouldNotConfigureWhenKafkaDisabled() {
        getContextRunnerWithEventHubsURL()
                .withPropertyValues("spring.cloud.azure.eventhubs.kafka.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                });
    }

    @Test
    void shouldNotConfigureWhenKafkaBoot() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues("spring.cloud.azure.eventhubs.kafka.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
            });
    }

    @Test
    void testBindSpringBootKafkaProperties() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + MANAGED_IDENTITY_ENABLED + "=true",
                SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX + MANAGED_IDENTITY_ENABLED + "=false"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertFalse(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                KafkaProperties kafkaSpringProperties = getKafkaSpringProperties(context);
                assertConsumerPropsConfigured(kafkaSpringProperties, MANAGED_IDENTITY_ENABLED, "true");
                assertProducerPropsConfigured(kafkaSpringProperties, MANAGED_IDENTITY_ENABLED, "false");
                assertAdminPropsConfigured(kafkaSpringProperties, MANAGED_IDENTITY_ENABLED, "true");
            });
    }

    @Test
    void testSpringBootCommonKafkaShouldOverrideAzureGlobal() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                "spring.cloud.azure." + CLIENT_ID + "=global",
                SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=common"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertFalse(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                KafkaProperties kafkaSpringProperties = getKafkaSpringProperties(context);
                assertConsumerPropsConfigured(kafkaSpringProperties, CLIENT_ID, "common");
                assertProducerPropsConfigured(kafkaSpringProperties, CLIENT_ID, "common");
                assertAdminPropsConfigured(kafkaSpringProperties, CLIENT_ID, "common");
            });
    }

    @Test
    void testSpringBootClientKafkaShouldOverrideAzureGlobal() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                "spring.cloud." + CLIENT_ID + "=global",
                SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX + CLIENT_ID + "=client"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertFalse(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                KafkaProperties kafkaSpringProperties = getKafkaSpringProperties(context);
                assertConsumerPropsConfigured(kafkaSpringProperties, CLIENT_ID, "global");
                assertProducerPropsConfigured(kafkaSpringProperties, CLIENT_ID, "client");
                assertAdminPropsConfigured(kafkaSpringProperties, CLIENT_ID, "global");
            });
    }

    @Test
    void testOAuthConfiguredToCallbackHandler() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX + MANAGED_IDENTITY_ENABLED + "=true"
            )
            .run(context -> {
                HashMap<String, Object> modifiedConfigs = new HashMap<>(getProducerProperties(getKafkaSpringProperties(context)));
                modifiedConfigs.put(SASL_JAAS_CONFIG, new Password((String) modifiedConfigs.get(SASL_JAAS_CONFIG)));
                KafkaOAuth2AuthenticateCallbackHandler callbackHandler = new KafkaOAuth2AuthenticateCallbackHandler();
                callbackHandler.configure(modifiedConfigs, null, null);

                AzurePasswordlessProperties properties = (AzurePasswordlessProperties) ReflectionTestUtils
                    .getField(callbackHandler, "properties");
                AzureCredentialResolver<TokenCredential> azureTokenCredentialResolver =
                    (AzureCredentialResolver<TokenCredential>) ReflectionTestUtils.getField(callbackHandler, "tokenCredentialResolver");
                assertNotNull(azureTokenCredentialResolver);
                assertTrue(azureTokenCredentialResolver.resolve(properties) instanceof ManagedIdentityCredential);

                Map<String, Object> consumerProperties = getConsumerProperties(getKafkaSpringProperties(context));
                modifiedConfigs.clear();
                modifiedConfigs.putAll(consumerProperties);
                modifiedConfigs.put(SASL_JAAS_CONFIG, new Password((String) modifiedConfigs.get(SASL_JAAS_CONFIG)));
                callbackHandler.configure(modifiedConfigs, null, null);
                properties = (AzurePasswordlessProperties) ReflectionTestUtils.getField(callbackHandler, "properties");
                azureTokenCredentialResolver = (AzureCredentialResolver<TokenCredential>) ReflectionTestUtils.getField(callbackHandler, "tokenCredentialResolver");
                assertNotNull(azureTokenCredentialResolver);
                assertTrue(azureTokenCredentialResolver.resolve(properties) instanceof DefaultAzureCredential);
            });
    }

    @Test
    void testOAuthConfiguredToCallbackHandlerWithAzureProperties() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                "spring.kafka.bootstrap-servers=test:9093",
                "spring.cloud.azure.credential.managed-identity-enabled=true"
            )
            .run(context -> {
                HashMap<String, Object> modifiedConfigs = new HashMap<>(getProducerProperties(getKafkaSpringProperties(context)));
                modifiedConfigs.put(SASL_JAAS_CONFIG, new Password((String) modifiedConfigs.get(SASL_JAAS_CONFIG)));
                KafkaOAuth2AuthenticateCallbackHandler callbackHandler = new KafkaOAuth2AuthenticateCallbackHandler();
                callbackHandler.configure(modifiedConfigs, null, null);

                AzurePasswordlessProperties properties = (AzurePasswordlessProperties) ReflectionTestUtils
                    .getField(callbackHandler, "properties");
                AzureCredentialResolver<TokenCredential> azureTokenCredentialResolver =
                    (AzureCredentialResolver<TokenCredential>) ReflectionTestUtils.getField(callbackHandler, "tokenCredentialResolver");
                assertNotNull(azureTokenCredentialResolver);
                assertTrue(azureTokenCredentialResolver.resolve(properties) instanceof ManagedIdentityCredential);
            });
    }

    @Override
    protected KafkaProperties getKafkaSpringProperties(ApplicationContext context) {
        return context.getBean(KafkaProperties.class);
    }
}
