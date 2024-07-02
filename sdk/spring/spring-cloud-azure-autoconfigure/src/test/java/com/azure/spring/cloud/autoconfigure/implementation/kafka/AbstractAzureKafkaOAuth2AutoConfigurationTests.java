// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.AZURE_CONFIGURED_JAAS_OPTIONS_KEY;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.AZURE_CONFIGURED_JAAS_OPTIONS_VALUE;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.SECURITY_PROTOCOL_CONFIG_SASL;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractAzureKafkaOAuth2AutoConfigurationTests<P, B extends AbstractKafkaPropertiesBeanPostProcessor<P>> {

    protected abstract ApplicationContextRunner getContextRunnerWithEventHubsURL();
    protected abstract ApplicationContextRunner getContextRunnerWithoutEventHubsURL();
    protected abstract P getKafkaSpringProperties(ApplicationContext context);

    protected final B processor;

    AbstractAzureKafkaOAuth2AutoConfigurationTests(B processor) {
        this.processor = processor;
    }

    @Test
    void testBindAzureGlobalProperties() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                "spring.cloud.azure.credential.managed-identity-enabled=true"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertTrue(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                P kafkaSpringProperties = getKafkaSpringProperties(context);
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
    void testNotBindSpringBootKafkaProperties() {
        getContextRunnerWithoutEventHubsURL()
            .withPropertyValues(
                    "spring.kafka.properties.azure.credential.managed-identity-enabled=true"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertFalse(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                P kafkaSpringProperties = getKafkaSpringProperties(context);
                Map<String, Object> consumerProperties = processor.getMergedConsumerProperties(kafkaSpringProperties);
                assertPropertyRemoved(consumerProperties, "azure.credential.managed-identity-enabled");
                assertNull(processor.getMergedAdminProperties(kafkaSpringProperties).get(SASL_JAAS_CONFIG));
            });
    }

    protected void assertOAuthPropertiesConfigure(Map<String, Object> configs) {
        assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, configs.get(SECURITY_PROTOCOL_CONFIG));
        assertEquals(SASL_MECHANISM_OAUTH, configs.get(SASL_MECHANISM));
        assertTrue(((String) configs.get(SASL_JAAS_CONFIG)).startsWith("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required"));
        assertTrue(((String) configs.get(SASL_JAAS_CONFIG)).contains(AZURE_CONFIGURED_JAAS_OPTIONS_KEY + "=\"" + AZURE_CONFIGURED_JAAS_OPTIONS_VALUE + "\""));
        assertEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH, configs.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }

    protected void assertPropertyRemoved(Map<String, Object> configs, String key) {
        assertFalse(configs.containsKey(key));
    }

    protected void assertJaasPropertiesConfigured(Map<String, Object> configs, String key, String value) {
        String jaas = (String) configs.get(SASL_JAAS_CONFIG);
        assertTrue(jaas.contains(key.concat("=\"").concat(value).concat("\"")));
    }

}
