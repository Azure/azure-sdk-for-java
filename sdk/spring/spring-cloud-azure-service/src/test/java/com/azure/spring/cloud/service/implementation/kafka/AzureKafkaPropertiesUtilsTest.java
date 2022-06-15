// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaConfigUtils.Mapping.cloudType;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaConfigUtils.Mapping.managedIdentityEnabled;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaConfigUtils.convertAzurePropertiesToConfigMap;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaConfigUtils.convertConfigMapToAzureProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureKafkaConfigUtilsTest {

    private final Map<String, Object> configs = new HashMap<>();

    @BeforeEach
    void setup() {
        configs.clear();
        Arrays.stream(AzureKafkaConfigUtils.Mapping.values()).forEach(mapping ->
            configs.put(mapping.propertyKey(), mapping.propertyKey() + ".test"));
        configs.put(managedIdentityEnabled.propertyKey(), "true");
        configs.put(cloudType.propertyKey(), "AZURE_CHINA");
    }

    @Test
    void testConvertConfigMapToAzureProperties() {
        AzureKafkaProperties properties = new AzureKafkaProperties();
        convertConfigMapToAzureProperties(configs, properties);

        Arrays.stream(AzureKafkaConfigUtils.Mapping.values()).forEach(mapping -> {
            if (mapping == managedIdentityEnabled) {
                assertTrue(Boolean.valueOf(mapping.getter().apply(properties)));
            } else if (mapping == AzureKafkaConfigUtils.Mapping.cloudType) {
                assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA,
                    AzureProfileOptionsProvider.CloudType.get(mapping.getter().apply(properties)));
            } else {
                assertEquals(mapping.propertyKey() + ".test", mapping.getter().apply(properties));
            }
        });
    }

    @Test
    void testConvertAzurePropertiesToConfigMapWithCustomValues() {
        AzureKafkaProperties properties = new AzureKafkaProperties();
        Map<String, String> configs = new HashMap<>();
        Arrays.stream(AzureKafkaConfigUtils.Mapping.values()).forEach(mapping ->
            configs.put(mapping.propertyKey(), mapping.propertyKey() + ".override"));
        configs.put(managedIdentityEnabled.propertyKey(), "false");
        configs.put(cloudType.propertyKey(), "AZURE");
        convertConfigMapToAzureProperties(this.configs, properties);
        convertAzurePropertiesToConfigMap(properties, configs);

        Arrays.stream(AzureKafkaConfigUtils.Mapping.values()).forEach(mapping -> {
            if (mapping == managedIdentityEnabled) {
                assertFalse(Boolean.valueOf(configs.get(mapping.propertyKey())));
            } else if (mapping == AzureKafkaConfigUtils.Mapping.cloudType) {
                assertEquals(AzureProfileOptionsProvider.CloudType.AZURE,
                    AzureProfileOptionsProvider.CloudType.get(configs.get(mapping.propertyKey())));
            } else {
                assertEquals(mapping.propertyKey() + ".override", configs.get(mapping.propertyKey()));
            }
        });
    }

    @Test
    void testConvertAzurePropertiesToConfigMapWithoutCustomValues() {
        AzureKafkaProperties properties = new AzureKafkaProperties();
        Map<String, String> configs = new HashMap<>();
        convertConfigMapToAzureProperties(this.configs, properties);
        convertAzurePropertiesToConfigMap(properties, configs);

        Arrays.stream(AzureKafkaConfigUtils.Mapping.values()).forEach(mapping -> {
            if (mapping == managedIdentityEnabled) {
                assertTrue(Boolean.valueOf(configs.get(mapping.propertyKey())));
            } else if (mapping == AzureKafkaConfigUtils.Mapping.cloudType) {
                assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA,
                    AzureProfileOptionsProvider.CloudType.get(configs.get(mapping.propertyKey())));
            } else {
                assertEquals(mapping.propertyKey() + ".test", configs.get(mapping.propertyKey()));
            }
        });
    }

}
