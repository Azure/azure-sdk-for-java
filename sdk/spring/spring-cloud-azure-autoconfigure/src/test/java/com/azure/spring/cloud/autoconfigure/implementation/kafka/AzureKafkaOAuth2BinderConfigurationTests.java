// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
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
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
class AzureKafkaOAuth2BinderConfigurationTests extends AbstractAzureKafkaOAuth2AutoConfigurationTests<KafkaBinderConfigurationProperties, KafkaBinderConfigurationPropertiesBeanPostProcessor> {

    AzureKafkaOAuth2BinderConfigurationTests() {
        super(new KafkaBinderConfigurationPropertiesBeanPostProcessor(new AzureGlobalProperties()));
    }

    private final ApplicationContextRunner contextRunnerWithoutEventHubsURL = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaOAuth2AutoConfiguration.class,
            AzureGlobalPropertiesAutoConfiguration.class, AzureTokenCredentialAutoConfiguration.class,
            KafkaAutoConfiguration.class, AzureKafkaSpringCloudStreamConfiguration.class, KafkaBinderConfiguration.class));

    private final ApplicationContextRunner contextRunnerWithEventHubsURL = contextRunnerWithoutEventHubsURL
            .withPropertyValues("spring.cloud.stream.kafka.binder.brokers=myehnamespace.servicebus.windows.net:9093");


    @Override
    protected ApplicationContextRunner getContextRunnerWithEventHubsURL() {
        return this.contextRunnerWithEventHubsURL;
    }

    @Override
    protected ApplicationContextRunner getContextRunnerWithoutEventHubsURL() {
        return this.contextRunnerWithoutEventHubsURL;
    }

    @Test
    void shouldNotConfigureBPPWithoutKafkaMessageChannelBinder() {
        getContextRunnerWithEventHubsURL()
            .withClassLoader(new FilteredClassLoader(KafkaMessageChannelBinder.class))
            .run(context -> {
                assertThat(context)
                        .doesNotHaveBean(AzureKafkaSpringCloudStreamConfiguration.class);
                assertThat(context).doesNotHaveBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
            });
    }

    @Test
    void testNotBindBinderKafkaProperties() {
        getContextRunnerWithoutEventHubsURL()
            .withPropertyValues(
                    "spring.cloud.stream.kafka.binder.configuration.azure.credential.managed-identity-enabled=true"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertFalse(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                KafkaBinderConfigurationProperties kafkaSpringProperties = getKafkaSpringProperties(context);
                Map<String, Object> consumerProperties = processor.getMergedConsumerProperties(kafkaSpringProperties);
                assertPropertyRemoved(consumerProperties, "azure.credential.managed-identity-enabled");
                assertNull(processor.getMergedAdminProperties(kafkaSpringProperties).get(SASL_JAAS_CONFIG));
            });
    }

    @Test
    void testBindSpringBootKafkaProperties() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                    "spring.kafka.properties.azure.credential.managed-identity-enabled=true",
                "spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertFalse(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                KafkaBinderConfigurationProperties kafkaSpringProperties = getKafkaSpringProperties(context);
                Map<String, Object> mergedConsumerProperties = processor.getMergedConsumerProperties(kafkaSpringProperties);
                assertOAuthPropertiesConfigure(mergedConsumerProperties);
                assertPropertyRemoved(mergedConsumerProperties, "azure.credential.managed-identity-enabled");
                assertJaasPropertiesConfigured(mergedConsumerProperties, "azure.credential.managed-identity-enabled", "true");

                Map<String, Object> mergedProducerProperties = processor.getMergedProducerProperties(kafkaSpringProperties);
                assertOAuthPropertiesConfigure(mergedProducerProperties);
                assertPropertyRemoved(mergedProducerProperties, "azure.credential.managed-identity-enabled");
                assertJaasPropertiesConfigured(mergedProducerProperties, "azure.credential.managed-identity-enabled", "true");

                Map<String, Object> mergedAdminProperties = processor.getMergedAdminProperties(kafkaSpringProperties);
                assertOAuthPropertiesConfigure(mergedAdminProperties);
                assertPropertyRemoved(mergedAdminProperties, "azure.credential.managed-identity-enabled");
                assertJaasPropertiesConfigured(mergedAdminProperties, "azure.credential.managed-identity-enabled", "true");
            });
    }

    @Test
    void testBindKafkaBinderProperties() {
        getContextRunnerWithEventHubsURL()
                .withPropertyValues(
                        "spring.cloud.stream.kafka.binder.configuration.azure.credential.client-id=cloud-client-id",
                        "spring.cloud.stream.kafka.binder.consumer-properties.azure.credential.client-id=cloud-consumer-client-id",
                        "spring.kafka.properties.azure.credential.client-id=kafka-client-id",
                        "spring.kafka.producer.properties.azure.credential.client-id=kafka-producer-client-id",
                        "spring.cloud.azure.credential.client-id=azure-client-id",
                        "spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093"
                )
                .run(context -> {
                    KafkaBinderConfigurationProperties kafkaSpringProperties = getKafkaSpringProperties(context);
                    Map<String, Object> mergedConsumerProperties = processor.getMergedConsumerProperties(kafkaSpringProperties);
                    assertOAuthPropertiesConfigure(mergedConsumerProperties);
                    assertPropertyRemoved(mergedConsumerProperties, "azure.credential.client-id");
                    assertJaasPropertiesConfigured(mergedConsumerProperties, "azure.credential.client-id", "cloud-consumer-client-id");

                    Map<String, Object> mergedProducerProperties = processor.getMergedProducerProperties(kafkaSpringProperties);
                    assertOAuthPropertiesConfigure(mergedProducerProperties);
                    assertPropertyRemoved(mergedProducerProperties, "azure.credential.client-id");
                    assertJaasPropertiesConfigured(mergedProducerProperties, "azure.credential.client-id", "cloud-client-id");

                    Map<String, Object> mergedAdminProperties = processor.getMergedAdminProperties(kafkaSpringProperties);
                    assertOAuthPropertiesConfigure(mergedAdminProperties);
                    assertPropertyRemoved(mergedAdminProperties, "azure.credential.client-id");
                    assertJaasPropertiesConfigured(mergedAdminProperties, "azure.credential.client-id", "cloud-client-id");
                });
    }

    @SuppressWarnings("removal")
    @Test
    void testNotBindBinderPropertiesOnBoot() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                "spring.cloud.stream.kafka.binder.configuration.azure.credential.client-id=cloud-client-id",
                "spring.cloud.stream.kafka.binder.consumer-properties.azure.credential.client-id=cloud-consumer-client-id",
                "spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093"
            )
            .run(context -> {
                KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                assertFalse(kafkaProperties.getProperties().containsKey("azure.credential.client-id"));
                assertFalse(kafkaProperties.buildConsumerProperties().containsKey("azure.credential.client-id"));
                assertFalse(kafkaProperties.getProducer().getProperties().get(SASL_JAAS_CONFIG).contains("azure.credential.client-id"));
                assertFalse(kafkaProperties.buildProducerProperties().containsKey("azure.credential.client-id"));
                assertFalse(kafkaProperties.getConsumer().getProperties().get(SASL_JAAS_CONFIG).contains("azure.credential.client-id"));
                assertFalse(kafkaProperties.buildAdminProperties().containsKey("azure.credential.client-id"));
                assertFalse(kafkaProperties.getAdmin().getProperties().get(SASL_JAAS_CONFIG).contains("azure.credential.client-id"));
            });
    }

    @Test
    void testOAuth2ConfiguredToCallbackHandlerWithKafkaBinderProperties() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                "spring.cloud.stream.kafka.binder.consumer-properties.azure.credential.managed-identity-enabled=true"
            )
            .run(context -> {
                KafkaBinderConfigurationProperties kafkaProperties = context.getBean(KafkaBinderConfigurationProperties.class);
                HashMap<String, Object> modifiedConfigs = new HashMap<>(processor.getMergedConsumerProperties(kafkaProperties));
                modifiedConfigs.put(BOOTSTRAP_SERVERS_CONFIG, Arrays.asList("myehnamespace.servicebus.windows.net:9093"));
                modifiedConfigs.put(SASL_JAAS_CONFIG, new Password((String) modifiedConfigs.get(SASL_JAAS_CONFIG)));
                KafkaOAuth2AuthenticateCallbackHandler callbackHandler = new KafkaOAuth2AuthenticateCallbackHandler();
                callbackHandler.configure(modifiedConfigs, null, null);

                AzurePasswordlessProperties properties = (AzurePasswordlessProperties) ReflectionTestUtils
                    .getField(callbackHandler, "properties");
                AzureCredentialResolver<TokenCredential> azureTokenCredentialResolver =
                    (AzureCredentialResolver<TokenCredential>) ReflectionTestUtils.getField(callbackHandler, "tokenCredentialResolver");
                assertTrue(azureTokenCredentialResolver.resolve(properties) instanceof ManagedIdentityCredential);

                modifiedConfigs.clear();
                modifiedConfigs.putAll(processor.getMergedProducerProperties(kafkaProperties));
                modifiedConfigs.put(BOOTSTRAP_SERVERS_CONFIG, Arrays.asList("myehnamespace.servicebus.windows.net:9093"));
                modifiedConfigs.put(SASL_JAAS_CONFIG, new Password((String) modifiedConfigs.get(SASL_JAAS_CONFIG)));
                callbackHandler.configure(modifiedConfigs, null, null);
                properties = (AzurePasswordlessProperties) ReflectionTestUtils.getField(callbackHandler, "properties");
                azureTokenCredentialResolver =
                    (AzureCredentialResolver<TokenCredential>) ReflectionTestUtils.getField(callbackHandler, "tokenCredentialResolver");
                assertTrue(azureTokenCredentialResolver.resolve(properties) instanceof DefaultAzureCredential);
            });
    }

    @Override
    protected KafkaBinderConfigurationProperties getKafkaSpringProperties(ApplicationContext context) {
        return context.getBean(KafkaBinderConfigurationProperties.class);
    }
}
