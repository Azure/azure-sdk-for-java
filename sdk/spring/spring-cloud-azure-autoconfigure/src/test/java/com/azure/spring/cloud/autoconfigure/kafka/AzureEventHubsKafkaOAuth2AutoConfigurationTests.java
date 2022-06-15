// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.kafka.provisioning.KafkaTopicProvisioner;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SASL_JAAS_CONFIG_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SECURITY_PROTOCOL_CONFIG_SASL;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.buildAzureProperties;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AzureEventHubsKafkaOAuth2AutoConfigurationTests {

    private static final String SPRING_BOOT_KAFKA_PROPERTIES_PREFIX = "spring.kafka.properties.";
    private static final String SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX = "spring.kafka.producer.properties.";
    private static final String SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX = "spring.cloud.stream.kafka.binder.configuration.";
    private static final String SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX = "spring.cloud.stream.kafka.binder.consumer-properties.";
    private static final String CLIENT_ID = "azure.credential.client-id";
    private static final String MANAGED_IDENTITY_ENABLED = "azure.credential.managed-identity-enabled";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaOAuth2AutoConfiguration.class,
            AzureGlobalPropertiesAutoConfiguration.class, AzureTokenCredentialAutoConfiguration.class,
            KafkaAutoConfiguration.class));

    @Test
    void shouldNotConfigureWithoutKafkaTemplate() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(KafkaTemplate.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class));
    }

    @Test
    void shouldNotConfigureBPPWithoutKafkaMessageChannelBinder() {
        this.contextRunner
            .withUserConfiguration(KafkaAutoConfiguration.class)
            .withClassLoader(new FilteredClassLoader(KafkaMessageChannelBinder.class))
            .run(context -> {
                assertThat(context)
                    .doesNotHaveBean(AzureEventHubsKafkaOAuth2AutoConfiguration.AzureKafkaSpringCloudStreamConfiguration.class);
                assertThat(context).doesNotHaveBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
            });
    }

    @Test
    void shouldConfigureFactoryCustomizersAndBPP() {
        this.contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(DefaultKafkaConsumerFactoryCustomizer.class);
                assertThat(context).hasSingleBean(DefaultKafkaProducerFactoryCustomizer.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
            });
    }

    @Test
    void testKafkaBootPropertiesShouldBindOnBoot() {
        this.contextRunner
            .withPropertyValues(
                SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-client-id",
                SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-producer-client-id",
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureGlobalProperties.class);
                assertThat(context).hasSingleBean(KafkaProperties.class);

                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertEquals("azure-client-id", azureGlobalProperties.getCredential().getClientId());
                KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                assertEquals("kafka-client-id", kafkaProperties.getProperties().get(CLIENT_ID));
                assertEquals("kafka-client-id", kafkaProperties.buildConsumerProperties().get(CLIENT_ID));
                assertEquals("kafka-producer-client-id", kafkaProperties.buildProducerProperties().get(CLIENT_ID));

                AzureKafkaProperties azureBuiltKafkaConsumerProp = buildAzureProperties(
                    kafkaProperties.buildConsumerProperties(), azureGlobalProperties);
                assertEquals("kafka-client-id", azureBuiltKafkaConsumerProp.getCredential().getClientId());
                AzureKafkaProperties azureBuiltKafkaProducerProp = buildAzureProperties(
                    kafkaProperties.buildProducerProperties(), azureGlobalProperties);
                assertEquals("kafka-producer-client-id", azureBuiltKafkaProducerProp.getCredential().getClientId());
            });
    }

    @Test
    void testKafkaBootPropertiesShouldBindOnBinder() {
        this.contextRunner
            .withUserConfiguration(KafkaBinderConfiguration.class)
            .withPropertyValues(
                SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-client-id",
                SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-producer-client-id",
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationProperties.class);

                KafkaBinderConfigurationProperties kafkaProperties = context.getBean(KafkaBinderConfigurationProperties.class);

                Map<String, Object> consumerConfiguration = kafkaProperties.mergedConsumerConfiguration();
                assertEquals("kafka-client-id", consumerConfiguration.get(CLIENT_ID));
                testOAuthKafkaPropertiesBind(consumerConfiguration);

                Map<String, Object> producerConfiguration = kafkaProperties.mergedProducerConfiguration();
                assertEquals("kafka-producer-client-id", producerConfiguration.get(CLIENT_ID));
                testOAuthKafkaPropertiesBind(producerConfiguration);
            });
    }

    @Test
    void testAzureGlobalPropertiesShouldBindOnBoot() {
        this.contextRunner
            .withUserConfiguration(KafkaBinderConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureGlobalProperties.class);
                assertThat(context).hasSingleBean(KafkaProperties.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationProperties.class);

                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertEquals("azure-client-id", azureGlobalProperties.getCredential().getClientId());
                KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                assertNull(kafkaProperties.getProperties().get(CLIENT_ID));
                assertNull(kafkaProperties.buildConsumerProperties().get(CLIENT_ID));
                assertNull(kafkaProperties.buildProducerProperties().get(CLIENT_ID));

                AzureKafkaProperties azureBuiltKafkaConsumerProp = buildAzureProperties(
                    kafkaProperties.buildConsumerProperties(), azureGlobalProperties);
                assertEquals("azure-client-id", azureBuiltKafkaConsumerProp.getCredential().getClientId());
                AzureKafkaProperties azureBuiltKafkaProducerProp = buildAzureProperties(
                    kafkaProperties.buildProducerProperties(), azureGlobalProperties);
                assertEquals("azure-client-id", azureBuiltKafkaProducerProp.getCredential().getClientId());
            });
    }

    @Test
    void testAzureGlobalPropertiesShouldBindOnBinder() {
        this.contextRunner
            .withUserConfiguration(KafkaBinderConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationProperties.class);

                KafkaBinderConfigurationProperties kafkaProperties = context.getBean(KafkaBinderConfigurationProperties.class);

                Map<String, Object> consumerConfiguration = kafkaProperties.mergedConsumerConfiguration();
                assertEquals("azure-client-id", consumerConfiguration.get(CLIENT_ID));
                testOAuthKafkaPropertiesBind(consumerConfiguration);

                Map<String, Object> producerConfiguration = kafkaProperties.mergedProducerConfiguration();
                assertEquals("azure-client-id", producerConfiguration.get(CLIENT_ID));
                testOAuthKafkaPropertiesBind(producerConfiguration);
            });
    }

    @Test
    void testKafkaBinderPropertiesShouldNotBindOnBoot() {
        this.contextRunner
            .withUserConfiguration(KafkaBinderConfiguration.class)
            .withPropertyValues(
                SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-client-id",
                SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-consumer-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
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
    @SuppressWarnings("unchecked")
    void testKafkaBinderPropertiesShouldBindOnBinder() {
        this.contextRunner
            .withUserConfiguration(KafkaBinderConfiguration.class)
            .withPropertyValues(
                SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-client-id",
                SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX + CLIENT_ID + "=cloud-consumer-client-id",
                SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-client-id",
                SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-producer-client-id",
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(KafkaBinderConfigurationProperties.class);
                assertThat(context).hasSingleBean(KafkaTopicProvisioner.class);

                KafkaBinderConfigurationProperties kafkaProperties = context.getBean(KafkaBinderConfigurationProperties.class);

                Map<String, String> kafkaConfiguration = kafkaProperties.getConfiguration();
                assertEquals("cloud-client-id", kafkaConfiguration.get(CLIENT_ID));
                
                KafkaTopicProvisioner kafkaTopicProvisioner = context.getBean(KafkaTopicProvisioner.class);
                Map<String, Object> adminClientProperties = (Map<String, Object>) ReflectionTestUtils.getField(kafkaTopicProvisioner,
                    "adminClientProperties");
                testOAuthKafkaPropertiesBind(adminClientProperties);

                Map<String, Object> consumerConfiguration = kafkaProperties.mergedConsumerConfiguration();
                assertEquals("cloud-consumer-client-id", consumerConfiguration.get(CLIENT_ID));
                testOAuthKafkaPropertiesBind(consumerConfiguration);

                Map<String, Object> producerConfiguration = kafkaProperties.mergedProducerConfiguration();
                assertEquals("kafka-producer-client-id", producerConfiguration.get(CLIENT_ID));
                testOAuthKafkaPropertiesBind(producerConfiguration);
            });
    }

    @Test
    void testFactoryConfigureOAuthAndTokenCredential() {
        this.contextRunner
            .withPropertyValues(
                "spring.kafka.producer.properties." + MANAGED_IDENTITY_ENABLED + "=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
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

    private void shouldConfigureOAuthProperties(Map<String, Object> configurationProperties) {
        assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, configurationProperties.get(SECURITY_PROTOCOL_CONFIG));
        assertEquals(SASL_MECHANISM_OAUTH, configurationProperties.get(SASL_MECHANISM));
        assertEquals(SASL_JAAS_CONFIG_OAUTH, configurationProperties.get(SASL_JAAS_CONFIG));
        assertEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH,
            configurationProperties.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }

    private <T> void testOAuthKafkaPropertiesBind(Map<String, T> kafkaProperties) {
        assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, kafkaProperties.get(SECURITY_PROTOCOL_CONFIG));
        assertEquals(SASL_MECHANISM_OAUTH, kafkaProperties.get(SASL_MECHANISM));
        assertEquals(SASL_JAAS_CONFIG_OAUTH, kafkaProperties.get(SASL_JAAS_CONFIG));
        assertEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH, kafkaProperties.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }

}
