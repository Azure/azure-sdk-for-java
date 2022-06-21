// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.kafka.provisioning.KafkaTopicProvisioner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AzureKafkaOAuth2BinderConfigurationTests extends AbstractAzureKafkaOAuth2AutoConfigurationTests {

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


    @Override
    protected void testBindSpringBootKafkaProperties() {
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


    @Override
    protected void testBindAzureGlobalProperties() {
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
    void testNotBindSpringCloudStreamKafkaBinderProperties() {
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
    void testBindSpringCloudStreamKafkaBinderProperties() {
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

}
