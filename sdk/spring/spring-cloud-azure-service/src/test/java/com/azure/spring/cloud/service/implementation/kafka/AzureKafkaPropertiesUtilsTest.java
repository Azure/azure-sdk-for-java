// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.Mapping.cloudType;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.Mapping.managedIdentityEnabled;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.convertAzurePropertiesToConfigMap;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.convertConfigMapToAzureProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureKafkaPropertiesUtilsTest {

    private Map<String, String> buildKafkaSourceConfigsFromAzureProperties() {
        Map<String, String> configs = new HashMap<>();
        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values()).forEach(mapping ->
            configs.put(mapping.propertyKey(), mapping.propertyKey() + ".test"));
        configs.put(managedIdentityEnabled.propertyKey(), "true");
        configs.put(cloudType.propertyKey(), "AZURE_CHINA");
        return configs;
    }
    @Test
    void testConvertConfigMapToAzureProperties() {
        AzureKafkaProperties properties = new AzureKafkaProperties();
        convertConfigMapToAzureProperties(buildKafkaSourceConfigsFromAzureProperties(), properties);

        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values()).forEach(mapping -> {
            if (mapping == managedIdentityEnabled) {
                assertTrue(Boolean.valueOf(mapping.getter().apply(properties)));
            } else if (mapping == AzureKafkaPropertiesUtils.Mapping.cloudType) {
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
        Map<String, String> sourceConfigs = buildKafkaSourceConfigsFromAzureProperties();
        Map<String, String> customKafkaConfigs = new HashMap<>();
        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values()).forEach(mapping ->
            customKafkaConfigs.put(mapping.propertyKey(), mapping.propertyKey() + ".override"));
        customKafkaConfigs.put(managedIdentityEnabled.propertyKey(), "false");
        customKafkaConfigs.put(cloudType.propertyKey(), "AZURE");
        convertConfigMapToAzureProperties(sourceConfigs, properties);
        convertAzurePropertiesToConfigMap(properties, customKafkaConfigs);

        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values()).forEach(mapping -> {
            if (mapping == managedIdentityEnabled) {
                assertFalse(Boolean.valueOf(customKafkaConfigs.get(mapping.propertyKey())));
            } else if (mapping == AzureKafkaPropertiesUtils.Mapping.cloudType) {
                assertEquals(AzureProfileOptionsProvider.CloudType.AZURE,
                    AzureProfileOptionsProvider.CloudType.get(customKafkaConfigs.get(mapping.propertyKey())));
            } else {
                assertEquals(mapping.propertyKey() + ".override", customKafkaConfigs.get(mapping.propertyKey()));
            }
        });
    }

    @Test
    void testConvertAzurePropertiesToConfigMapWithoutCustomValues() {
        AzureKafkaProperties properties = new AzureKafkaProperties();
        Map<String, String> sourceConfigs = buildKafkaSourceConfigsFromAzureProperties();
        Map<String, String> customKafkaConfigs = new HashMap<>();
        convertConfigMapToAzureProperties(sourceConfigs, properties);
        convertAzurePropertiesToConfigMap(properties, customKafkaConfigs);

        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values()).forEach(mapping -> {
            if (mapping == managedIdentityEnabled) {
                assertTrue(Boolean.valueOf(customKafkaConfigs.get(mapping.propertyKey())));
            } else if (mapping == AzureKafkaPropertiesUtils.Mapping.cloudType) {
                assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA,
                    AzureProfileOptionsProvider.CloudType.get(customKafkaConfigs.get(mapping.propertyKey())));
            } else {
                assertEquals(mapping.propertyKey() + ".test", customKafkaConfigs.get(mapping.propertyKey()));
            }
        });
    }

}
