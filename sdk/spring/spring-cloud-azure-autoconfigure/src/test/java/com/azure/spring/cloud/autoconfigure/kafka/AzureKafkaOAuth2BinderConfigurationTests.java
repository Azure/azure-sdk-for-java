// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    @SuppressWarnings("unchecked")
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

    @Override
    protected Map<String, Object> getConsumerProperties(ApplicationContext context) {
        return context.getBean(KafkaBinderConfigurationProperties.class).mergedConsumerConfiguration();
    }

    @Override
    protected Map<String, Object> getProducerProperties(ApplicationContext context) {
        return context.getBean(KafkaBinderConfigurationProperties.class).mergedProducerConfiguration();
    }

}
