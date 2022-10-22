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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.kafka.provisioning.KafkaTopicProvisioner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AzureKafkaOAuth2BinderConfigurationTests extends AbstractAzureKafkaOAuth2AutoConfigurationTests {

    private static final String SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX = "spring.cloud.stream.kafka.binder.configuration.";
    private static final String SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX = "spring.cloud.stream.kafka.binder.consumer-properties.";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.cloud.stream.kafka.binder.brokers=myehnamespace.servicebus.windows.net:9093")
            .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaOAuth2AutoConfiguration.class,
                    AzureGlobalPropertiesAutoConfiguration.class, AzureTokenCredentialAutoConfiguration.class,
                    KafkaAutoConfiguration.class, AzureKafkaSpringCloudStreamConfiguration.class, KafkaBinderConfiguration.class));

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return this.contextRunner;
    }

    @Test
    void shouldNotConfigureBPPWithoutKafkaMessageChannelBinder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(KafkaMessageChannelBinder.class))
            .run(context -> {
                assertThat(context)
                        .doesNotHaveBean(AzureKafkaSpringCloudStreamConfiguration.class);
                assertThat(context).doesNotHaveBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
            });
    }

    @Test
    @Disabled
    void testNotBindKafkaBinderProperties() {
        this.contextRunner
                .withPropertyValues(
                        SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-client-id",
                        SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-consumer-client-id"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureKafkaSpringCloudStreamConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(KafkaBinderConfigurationProperties.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                    assertNull(kafkaProperties.getProperties().get(CLIENT_ID));
                    assertNull(kafkaProperties.buildConsumerProperties().get(CLIENT_ID));
                    assertNull(kafkaProperties.buildProducerProperties().get(CLIENT_ID));
                });
    }

    @Test
    void testKafkaBinderPropertiesNotBindOnBoot() {
        this.contextRunner
            .withPropertyValues(
                String.format(JAAS_PROPERTY_FORMAT, SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX, CLIENT_ID, "cloud-client-id"),
                String.format(JAAS_PROPERTY_FORMAT, SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX, CLIENT_ID, "cloud-consumer-client-id")
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKafkaSpringCloudStreamConfiguration.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationProperties.class);
                assertThat(context).hasSingleBean(KafkaProperties.class);

                KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                assertNull(kafkaProperties.getProperties().get(SASL_JAAS_CONFIG));
                assertNull(kafkaProperties.buildConsumerProperties().get(SASL_JAAS_CONFIG));
                assertNull(kafkaProperties.buildProducerProperties().get(SASL_JAAS_CONFIG));
            });
    }

    @Test
    @SuppressWarnings("unchecked")
    @Disabled
    void testBindKafkaBinderProperties() {
        this.contextRunner
                .withPropertyValues(
                        SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-client-id",
                        SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-consumer-client-id",
                        SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-client-id",
                        SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-producer-client-id",
                        "spring.cloud.azure.credential.client-id=azure-client-id"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureKafkaSpringCloudStreamConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(KafkaBinderConfigurationProperties.class);
                    assertThat(context).hasSingleBean(KafkaTopicProvisioner.class);

                    KafkaBinderConfigurationProperties kafkaProperties = context.getBean(KafkaBinderConfigurationProperties.class);

                    Map<String, String> kafkaConfiguration = kafkaProperties.getConfiguration();
                    assertEquals("cloud-client-id", kafkaConfiguration.get(CLIENT_ID));

                    KafkaTopicProvisioner kafkaTopicProvisioner = context.getBean(KafkaTopicProvisioner.class);
                    Map<String, Object> adminClientProperties = (Map<String, Object>) ReflectionTestUtils.getField(kafkaTopicProvisioner,
                            "adminClientProperties");
                    shouldConfigureOAuthProperties(adminClientProperties);

                    Map<String, Object> consumerConfiguration = kafkaProperties.mergedConsumerConfiguration();
                    assertEquals("cloud-consumer-client-id", consumerConfiguration.get(CLIENT_ID));
                    shouldConfigureOAuthProperties(consumerConfiguration);

                    Map<String, Object> producerConfiguration = kafkaProperties.mergedProducerConfiguration();
                    assertEquals("kafka-producer-client-id", producerConfiguration.get(CLIENT_ID));
                    shouldConfigureOAuthProperties(producerConfiguration);
                });
    }

    @Test
    @SuppressWarnings("unchecked")
    void testBindKafkaBinderJaasProperties() {
        this.contextRunner
            .withPropertyValues(
                String.format(JAAS_PROPERTY_FORMAT, SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX, CLIENT_ID, "cloud-client-id"),
                String.format(JAAS_PROPERTY_FORMAT, SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX, CLIENT_ID, "cloud-consumer-client-id"),
                String.format(JAAS_PROPERTY_FORMAT, SPRING_BOOT_KAFKA_PROPERTIES_PREFIX, CLIENT_ID, "kafka-client-id"),
                String.format(JAAS_PROPERTY_FORMAT, SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX, CLIENT_ID, "kafka-producer-client-id"),
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKafkaSpringCloudStreamConfiguration.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationProperties.class);
                assertThat(context).hasSingleBean(KafkaTopicProvisioner.class);

                KafkaBinderConfigurationProperties kafkaProperties = context.getBean(KafkaBinderConfigurationProperties.class);

                Map<String, String> kafkaConfiguration = kafkaProperties.getConfiguration();
                assertTrue(kafkaConfiguration.get(SASL_JAAS_CONFIG).contains("cloud-client-id"));

                KafkaTopicProvisioner kafkaTopicProvisioner = context.getBean(KafkaTopicProvisioner.class);
                Map<String, Object> adminClientProperties = (Map<String, Object>) ReflectionTestUtils.getField(kafkaTopicProvisioner,
                    "adminClientProperties");
                assertTrue(((String) adminClientProperties.get(SASL_JAAS_CONFIG)).contains("cloud-client-id"));
                shouldConfigureOAuthProperties(adminClientProperties);

                Map<String, Object> consumerConfiguration = kafkaProperties.mergedConsumerConfiguration();
                assertTrue(((String) consumerConfiguration.get(SASL_JAAS_CONFIG)).contains("cloud-consumer-client-id"));
                shouldConfigureOAuthProperties(consumerConfiguration);

                Map<String, Object> producerConfiguration = kafkaProperties.mergedProducerConfiguration();
                assertTrue(((String) producerConfiguration.get(SASL_JAAS_CONFIG)).contains("cloud-client-id"));
                shouldConfigureOAuthProperties(producerConfiguration);
            });
    }

    @Test
    @SuppressWarnings("unchecked")
    void testBindKafkaBootJaasProperties() {
        this.contextRunner
            .withPropertyValues(
                String.format(JAAS_PROPERTY_FORMAT, SPRING_BOOT_KAFKA_PROPERTIES_PREFIX, CLIENT_ID, "kafka-client-id"),
                String.format(JAAS_PROPERTY_FORMAT, SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX, CLIENT_ID, "kafka-producer-client-id"),
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                KafkaBinderConfigurationProperties kafkaProperties = context.getBean(KafkaBinderConfigurationProperties.class);

                Map<String, String> kafkaConfiguration = kafkaProperties.getConfiguration();
                assertTrue(kafkaConfiguration.get(SASL_JAAS_CONFIG).contains("kafka-client-id"));

                KafkaTopicProvisioner kafkaTopicProvisioner = context.getBean(KafkaTopicProvisioner.class);
                Map<String, Object> adminClientProperties = (Map<String, Object>) ReflectionTestUtils.getField(kafkaTopicProvisioner,
                    "adminClientProperties");
                assertTrue(((String) adminClientProperties.get(SASL_JAAS_CONFIG)).contains("kafka-client-id"));
                shouldConfigureOAuthProperties(adminClientProperties);

                Map<String, Object> consumerConfiguration = kafkaProperties.mergedConsumerConfiguration();
                assertTrue(((String) consumerConfiguration.get(SASL_JAAS_CONFIG)).contains("kafka-client-id"));
                shouldConfigureOAuthProperties(consumerConfiguration);

                Map<String, Object> producerConfiguration = kafkaProperties.mergedProducerConfiguration();
                assertTrue(((String) producerConfiguration.get(SASL_JAAS_CONFIG)).contains("kafka-producer-client-id"));
                shouldConfigureOAuthProperties(producerConfiguration);
            });
    }

    @Test
    @SuppressWarnings("unchecked")
    void testOAuthConfiguredToCallbackHandlerWithKafkaBinderProperties() {
        this.contextRunner
            .withPropertyValues(
                String.format(JAAS_PROPERTY_FORMAT, SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX, MANAGED_IDENTITY_ENABLED, "true")
            )
            .run(context -> {
                KafkaBinderConfigurationProperties kafkaProperties = context.getBean(KafkaBinderConfigurationProperties.class);
                Map<String, Object> consumerConfiguration = kafkaProperties.mergedConsumerConfiguration();
                HashMap<String, Object> modifiedConfigs = new HashMap<>(consumerConfiguration);
                modifiedConfigs.put(BOOTSTRAP_SERVERS_CONFIG, List.of("myehnamespace.servicebus.windows.net:9093"));
                KafkaOAuth2AuthenticateCallbackHandler callbackHandler = new KafkaOAuth2AuthenticateCallbackHandler();
                callbackHandler.configure(modifiedConfigs, null, null);

                AzurePasswordlessProperties properties = (AzurePasswordlessProperties) ReflectionTestUtils
                    .getField(callbackHandler, "properties");
                AzureCredentialResolver<TokenCredential> azureTokenCredentialResolver =
                    (AzureCredentialResolver<TokenCredential>) ReflectionTestUtils.getField(callbackHandler, "tokenCredentialResolver");
                assertTrue(azureTokenCredentialResolver.resolve(properties) instanceof ManagedIdentityCredential);

                Map<String, Object> producerConfiguration = kafkaProperties.mergedProducerConfiguration();
                modifiedConfigs.clear();
                modifiedConfigs.putAll(producerConfiguration);
                modifiedConfigs.put(BOOTSTRAP_SERVERS_CONFIG, List.of("myehnamespace.servicebus.windows.net:9093"));
                callbackHandler.configure(modifiedConfigs, null, null);
                properties = (AzurePasswordlessProperties) ReflectionTestUtils.getField(callbackHandler, "properties");
                azureTokenCredentialResolver =
                    (AzureCredentialResolver<TokenCredential>) ReflectionTestUtils.getField(callbackHandler, "tokenCredentialResolver");
                assertTrue(azureTokenCredentialResolver.resolve(properties) instanceof DefaultAzureCredential);
            });
    }

    @Override
    protected Map<String, Object> getConsumerProperties(ApplicationContext context) {
        return context.getBean(KafkaBinderConfigurationProperties.class).mergedConsumerConfiguration();
    }

    @Override
    protected Map<String, Object> getProducerProperties(ApplicationContext context) {
        return context.getBean(KafkaBinderConfigurationProperties.class).mergedProducerConfiguration();
    }

}
