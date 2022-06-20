// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import java.util.Map;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.core.KafkaTemplate;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SASL_JAAS_CONFIG_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SECURITY_PROTOCOL_CONFIG_SASL;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractAzureKafkaOAuth2AutoConfigurationTests {
    protected static final String SPRING_BOOT_KAFKA_PROPERTIES_PREFIX = "spring.kafka.properties.";
    protected static final String SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX = "spring.kafka.producer.properties.";
    protected static final String SPRING_CLOUD_STREAM_KAFKA_PROPERTIES_PREFIX = "spring.cloud.stream.kafka.binder.configuration.";
    protected static final String SPRING_CLOUD_STREAM_KAFKA_CONSUMER_PROPERTIES_PREFIX = "spring.cloud.stream.kafka.binder.consumer-properties.";
    protected static final String CLIENT_ID = "azure.credential.client-id";
    protected static final String MANAGED_IDENTITY_ENABLED = "azure.credential.managed-identity-enabled";

    protected final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
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
    void shouldConfigureFactoryCustomizersAndBPP() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(DefaultKafkaConsumerFactoryCustomizer.class);
                    assertThat(context).hasSingleBean(DefaultKafkaProducerFactoryCustomizer.class);
                    assertThat(context).hasSingleBean(KafkaBinderConfigurationPropertiesBeanPostProcessor.class);
                });
    }

    @Test
    protected abstract void testBindSpringBootKafkaProperties();

    @Test
    protected abstract void testBindAzureGlobalProperties();


    protected void shouldConfigureOAuthProperties(Map<String, Object> configurationProperties) {
        assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, configurationProperties.get(SECURITY_PROTOCOL_CONFIG));
        assertEquals(SASL_MECHANISM_OAUTH, configurationProperties.get(SASL_MECHANISM));
        assertEquals(SASL_JAAS_CONFIG_OAUTH, configurationProperties.get(SASL_JAAS_CONFIG));
        assertEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH,
                configurationProperties.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }

    protected <T> void testOAuthKafkaPropertiesBind(Map<String, T> kafkaProperties) {
        assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, kafkaProperties.get(SECURITY_PROTOCOL_CONFIG));
        assertEquals(SASL_MECHANISM_OAUTH, kafkaProperties.get(SASL_MECHANISM));
        assertEquals(SASL_JAAS_CONFIG_OAUTH, kafkaProperties.get(SASL_JAAS_CONFIG));
        assertEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH, kafkaProperties.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }
}
