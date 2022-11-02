// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.eventhubs.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.kafka.AzureEventHubsKafkaOAuth2AutoConfiguration;
import com.azure.spring.cloud.autoconfigure.kafka.AzureKafkaSpringCloudStreamConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Collections;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.eventhubs.kafka.AzureEventHubsKafkaAutoConfigurationTests.CONNECTION_STRING_FORMAT;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubsKafkaConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaOAuth2AutoConfiguration.class, AzureEventHubsKafkaAutoConfiguration.class,
                    AzureGlobalPropertiesAutoConfiguration.class, AzureTokenCredentialAutoConfiguration.class,
                    KafkaAutoConfiguration.class, AzureKafkaSpringCloudStreamConfiguration.class, KafkaBinderConfiguration.class));


    @Test
    void shouldConfigureSaslPlainWhenGivenConnectionString() {
        contextRunner
                .withPropertyValues("spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test"))
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(AzureGlobalProperties.class);
                    assertThat(context).hasSingleBean(KafkaPropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);
                    assertThat(context).hasSingleBean(ConsumerFactory.class);
                    assertThat(context).hasSingleBean(ProducerFactory.class);
                    assertThat(context).hasSingleBean(KafkaBinderConfigurationProperties.class);

                    KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
                    assertSaslPlainConfigured(kafkaProperties.buildProducerProperties());
                    DefaultKafkaConsumerFactory<?, ?> consumerFactory = (DefaultKafkaConsumerFactory<?, ?>) context.getBean(ConsumerFactory.class);
                    assertSaslPlainConfigured(consumerFactory.getConfigurationProperties());
                    DefaultKafkaProducerFactory<?, ?> producerFactory = (DefaultKafkaProducerFactory<?, ?>) context.getBean(ProducerFactory.class);
                    assertSaslPlainConfigured(producerFactory.getConfigurationProperties());
                    KafkaBinderConfigurationProperties binderConfigurationProperties = context.getBean(KafkaBinderConfigurationProperties.class);
                    assertSaslPlainConfigured(binderConfigurationProperties.mergedConsumerConfiguration());

                });
    }

    private void assertSaslPlainConfigured(Map<String, ?> configurationProperties) {
        assertThat(configurationProperties
                .get(BOOTSTRAP_SERVERS_CONFIG)).isEqualTo(Collections.singletonList("test.servicebus.windows.net:9093"));
        assertThat(configurationProperties
                .get(SECURITY_PROTOCOL_CONFIG)).isEqualTo(SASL_SSL.name());
        assertThat(configurationProperties
                .get(SASL_MECHANISM)).isEqualTo("PLAIN");
    }

}
