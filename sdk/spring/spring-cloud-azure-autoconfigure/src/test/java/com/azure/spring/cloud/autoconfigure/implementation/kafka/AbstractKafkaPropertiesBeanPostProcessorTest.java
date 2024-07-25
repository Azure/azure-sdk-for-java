// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import org.apache.kafka.common.message.ApiVersionsRequestData;
import org.apache.kafka.common.requests.ApiVersionsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AbstractKafkaPropertiesBeanPostProcessor.SECURITY_PROTOCOL_CONFIG_SASL;
import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH;
import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.VERSION;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.DEFAULT_SECURITY_PROTOCOL;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.DEFAULT_SASL_MECHANISM;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractKafkaPropertiesBeanPostProcessorTest<P extends AbstractKafkaPropertiesBeanPostProcessor<T>, T> {

    private final P processor;

    AbstractKafkaPropertiesBeanPostProcessorTest(P processor) {
        this.processor = processor;
    }

    private final String eventHubsBootStrapServer = "mynamespace.servicebus.windows.net:9093";
    private final String kafkaBootStrapServer = "localhost:9092";

    @Test
    void testWhenSecurityProtocolNotConfigured() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        sourceConfigs.put(BOOTSTRAP_SERVERS_CONFIG, eventHubsBootStrapServer);
        assertTrue(processor.needConfigureSaslOAuth(sourceConfigs));
    }

    @Test
    void testWhenSecurityProtocolConfiguredOthers() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        sourceConfigs.put(BOOTSTRAP_SERVERS_CONFIG, eventHubsBootStrapServer);
        sourceConfigs.put(SECURITY_PROTOCOL_CONFIG, DEFAULT_SECURITY_PROTOCOL);
        assertFalse(processor.needConfigureSaslOAuth(sourceConfigs));
    }

    @Test
    void testWhenSaslMechanismNotConfigured() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        Map<String, String> targetConfigs = new HashMap<>();
        sourceConfigs.put(BOOTSTRAP_SERVERS_CONFIG, eventHubsBootStrapServer);
        sourceConfigs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        targetConfigs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        assertTrue(processor.needConfigureSaslOAuth(sourceConfigs));
    }

    @Test
    void testWhenSaslMechanismConfiguredOthers() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        sourceConfigs.put(BOOTSTRAP_SERVERS_CONFIG, eventHubsBootStrapServer);
        sourceConfigs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        sourceConfigs.put(SASL_MECHANISM, DEFAULT_SASL_MECHANISM);
        assertFalse(processor.needConfigureSaslOAuth(sourceConfigs));
    }

    @Test
    void testWhenSaslMechanismConfiguredOAUTH() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        Map<String, String> targetConfigs = new HashMap<>();
        sourceConfigs.put(BOOTSTRAP_SERVERS_CONFIG, eventHubsBootStrapServer);
        sourceConfigs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        sourceConfigs.put(SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        targetConfigs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        targetConfigs.put(SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        targetConfigs.put(SASL_JAAS_CONFIG, "fake-value");
        targetConfigs.put(SASL_LOGIN_CALLBACK_HANDLER_CLASS, "fake-value");
        assertTrue(processor.needConfigureSaslOAuth(sourceConfigs));
    }

    @Test
    void testWhenBootstrapServersNotConfigured() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        assertFalse(processor.needConfigureSaslOAuth(sourceConfigs));
    }

    @Test
    void testWhenKafkaBootstrapServersConfigured() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        sourceConfigs.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBootStrapServer);
        assertFalse(processor.needConfigureSaslOAuth(sourceConfigs));
    }

    @Test
    void testWhenMultipleBootstrapServersStringConfigured() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        sourceConfigs.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBootStrapServer + "," + eventHubsBootStrapServer);
        assertFalse(processor.needConfigureSaslOAuth(sourceConfigs));

        sourceConfigs.put(BOOTSTRAP_SERVERS_CONFIG, Arrays.asList(kafkaBootStrapServer, eventHubsBootStrapServer));
        assertFalse(processor.needConfigureSaslOAuth(sourceConfigs));
    }

    @Test
    void testConfigureKafkaUserAgent() {
        getApiVersionsRequestData().ifPresent(method -> {
            AbstractKafkaPropertiesBeanPostProcessor.configureKafkaUserAgent();
            ApiVersionsRequest apiVersionsRequest = new ApiVersionsRequest.Builder().build();
            ApiVersionsRequestData apiVersionsRequestData = (ApiVersionsRequestData) ReflectionUtils.invokeMethod(method, apiVersionsRequest);
            assertTrue(apiVersionsRequestData.clientSoftwareName()
                .contains(AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH));
            assertEquals(VERSION, apiVersionsRequestData.clientSoftwareVersion());
            assertTrue(apiVersionsRequest.isValid());
        });
    }

    @Test
    void testConfigureKafkaUserAgentMultipleTimes() {
        getApiVersionsRequestData().ifPresent(method -> {
            AbstractKafkaPropertiesBeanPostProcessor.configureKafkaUserAgent();
            AbstractKafkaPropertiesBeanPostProcessor.configureKafkaUserAgent();
            ApiVersionsRequest apiVersionsRequest = new ApiVersionsRequest.Builder().build();
            ApiVersionsRequestData apiVersionsRequestData = (ApiVersionsRequestData) ReflectionUtils.invokeMethod(method, apiVersionsRequest);
            assertTrue(apiVersionsRequestData.clientSoftwareName()
                .contains(AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH));
            assertEquals(1, apiVersionsRequestData.clientSoftwareName()
                .split(AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH, -1).length - 1);
            assertEquals(VERSION, apiVersionsRequestData.clientSoftwareVersion());
            assertTrue(apiVersionsRequest.isValid());
        });
    }

    private Optional<Method> getApiVersionsRequestData() {
        return Optional.ofNullable(ReflectionUtils.findMethod(ApiVersionsRequest.class, "data"));
    }


}
