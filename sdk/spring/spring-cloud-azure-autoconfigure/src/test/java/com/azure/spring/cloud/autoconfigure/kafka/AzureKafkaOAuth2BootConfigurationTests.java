// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.kafka;

import java.util.Map;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaProperties;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.buildAzureProperties;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AzureKafkaOAuth2BootConfigurationTests extends AbstractAzureKafkaOAuth2AutoConfigurationTests {

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


    @Override
    protected void assertBootPropertiesConfigureCorrectly(AssertableApplicationContext context) {
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
    }

    @Override
    protected void assertGlobalPropertiesConfigureCorrectly(AssertableApplicationContext context) {
        assertThat(context).hasSingleBean(AzureEventHubsKafkaOAuth2AutoConfiguration.class);
        assertThat(context).hasSingleBean(AzureGlobalProperties.class);
        assertThat(context).hasSingleBean(KafkaProperties.class);

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
    }
}
