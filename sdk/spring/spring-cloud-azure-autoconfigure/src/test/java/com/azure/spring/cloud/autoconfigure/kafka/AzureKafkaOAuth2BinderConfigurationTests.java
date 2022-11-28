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

    private static final String SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX = "spring.cloud.stream.kafka.binder.configuration.";
    private static final String SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX = "spring.cloud.stream.kafka.binder.consumer-properties.";

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
                SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX + MANAGED_IDENTITY_ENABLED + "=true"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertFalse(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                KafkaBinderConfigurationProperties kafkaSpringProperties = getKafkaSpringProperties(context);
                Map<String, Object> consumerProperties = getConsumerProperties(kafkaSpringProperties);
                assertFalse(consumerProperties.containsKey(MANAGED_IDENTITY_ENABLED));
                String adminJaasProperties = getAdminJaasProperties(kafkaSpringProperties);
                assertNull(adminJaasProperties);
            });
    }

    @Test
    void testBindSpringBootKafkaProperties() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + MANAGED_IDENTITY_ENABLED + "=true",
                "spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertFalse(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                KafkaBinderConfigurationProperties kafkaSpringProperties = getKafkaSpringProperties(context);
                assertConsumerPropsConfigured(kafkaSpringProperties, MANAGED_IDENTITY_ENABLED, "true");
                assertProducerPropsConfigured(kafkaSpringProperties, MANAGED_IDENTITY_ENABLED, "true");
                assertAdminPropsConfigured(kafkaSpringProperties, MANAGED_IDENTITY_ENABLED, "true");
            });
    }

    @Test
    void testBindKafkaBinderProperties() {
        getContextRunnerWithEventHubsURL()
                .withPropertyValues(
                        SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-client-id",
                        SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-consumer-client-id",
                        SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-client-id",
                        SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-producer-client-id",
                        "spring.cloud.azure.credential.client-id=azure-client-id",
                        "spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093"
                )
                .run(context -> {
                    KafkaBinderConfigurationProperties kafkaSpringProperties = getKafkaSpringProperties(context);
                    assertConsumerPropsConfigured(kafkaSpringProperties, CLIENT_ID, "cloud-consumer-client-id");
                    assertProducerPropsConfigured(kafkaSpringProperties, CLIENT_ID, "cloud-client-id");
                    assertAdminPropsConfigured(kafkaSpringProperties, CLIENT_ID, "cloud-client-id");
                });
    }

    @Test
    void testNotBindBinderPropertiesOnBoot() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-client-id",
                SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-consumer-client-id",
                "spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093"
            )
            .run(context -> {
                KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                assertFalse(kafkaProperties.getProperties().containsKey(CLIENT_ID));
                assertFalse(kafkaProperties.buildConsumerProperties().containsKey(CLIENT_ID));
                assertFalse(kafkaProperties.getProducer().getProperties().get(SASL_JAAS_CONFIG).contains(CLIENT_ID));
                assertFalse(kafkaProperties.buildProducerProperties().containsKey(CLIENT_ID));
                assertFalse(kafkaProperties.getConsumer().getProperties().get(SASL_JAAS_CONFIG).contains(CLIENT_ID));
                assertFalse(kafkaProperties.buildAdminProperties().containsKey(CLIENT_ID));
                assertFalse(kafkaProperties.getAdmin().getProperties().get(SASL_JAAS_CONFIG).contains(CLIENT_ID));
            });
    }

    @Test
    void testOAuth2ConfiguredToCallbackHandlerWithKafkaBinderProperties() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX + MANAGED_IDENTITY_ENABLED + "=true"
            )
            .run(context -> {
                KafkaBinderConfigurationProperties kafkaProperties = context.getBean(KafkaBinderConfigurationProperties.class);
                HashMap<String, Object> modifiedConfigs = new HashMap<>(getConsumerProperties(kafkaProperties));
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
                modifiedConfigs.putAll(getProducerProperties(kafkaProperties));
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
