// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessPropertiesUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessPropertiesUtils.Mapping.managedIdentityEnabled;
import static com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessPropertiesUtils.convertAzurePropertiesToConfigMap;
import static com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessPropertiesUtils.convertConfigMapToAzureProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzurePasswordlessPropertiesUtilsTest {

    private Map<String, String> buildKafkaSourceConfigsFromAzureProperties() {
        Map<String, String> configs = new HashMap<>();
        Arrays.stream(AzurePasswordlessPropertiesUtils.Mapping.values()).forEach(mapping ->
            configs.put(mapping.getAuthProperty().getPropertyKey(), mapping.getAuthProperty().getPropertyKey() + ".test"));
        configs.put(managedIdentityEnabled.getAuthProperty().getPropertyKey(), "true");
        return configs;
    }

    @Test
    void testConvertConfigMapToAzureProperties() {
        AzurePasswordlessProperties properties = new AzurePasswordlessProperties();
        convertConfigMapToAzureProperties(buildKafkaSourceConfigsFromAzureProperties(), properties);

        Arrays.stream(AzurePasswordlessPropertiesUtils.Mapping.values()).forEach(mapping -> {
            if (mapping == managedIdentityEnabled) {
                assertTrue(Boolean.valueOf(mapping.getGetter().apply(properties)));
            } else {
                assertEquals(mapping.getAuthProperty().getPropertyKey() + ".test", mapping.getGetter().apply(properties));
            }
        });
    }

    @Test
    void testConvertAzurePropertiesToConfigMapWithCustomValues() {
        AzurePasswordlessProperties properties = new AzurePasswordlessProperties();
        Map<String, String> sourceConfigs = buildKafkaSourceConfigsFromAzureProperties();
        Map<String, String> customKafkaConfigs = new HashMap<>();
        Arrays.stream(AzurePasswordlessPropertiesUtils.Mapping.values()).forEach(mapping ->
            customKafkaConfigs.put(mapping.getAuthProperty().getPropertyKey(), mapping.getAuthProperty().getPropertyKey() + ".override"));
        customKafkaConfigs.put(managedIdentityEnabled.getAuthProperty().getPropertyKey(), "false");
        convertConfigMapToAzureProperties(sourceConfigs, properties);
        convertAzurePropertiesToConfigMap(properties, customKafkaConfigs);

        Arrays.stream(AzurePasswordlessPropertiesUtils.Mapping.values()).forEach(mapping -> {
            if (mapping == managedIdentityEnabled) {
                assertFalse(Boolean.valueOf(customKafkaConfigs.get(mapping.getAuthProperty().getPropertyKey())));
            } else {
                assertEquals(mapping.getAuthProperty().getPropertyKey() + ".override", customKafkaConfigs.get(mapping.getAuthProperty().getPropertyKey()));
            }
        });
    }

    @Test
    void testConvertAzurePropertiesToConfigMapWithoutCustomValues() {
        AzurePasswordlessProperties properties = new AzurePasswordlessProperties();
        Map<String, String> sourceConfigs = buildKafkaSourceConfigsFromAzureProperties();
        Map<String, String> customKafkaConfigs = new HashMap<>();
        convertConfigMapToAzureProperties(sourceConfigs, properties);
        convertAzurePropertiesToConfigMap(properties, customKafkaConfigs);

        Arrays.stream(AzurePasswordlessPropertiesUtils.Mapping.values()).forEach(mapping -> {
            if (mapping == managedIdentityEnabled) {
                assertTrue(Boolean.valueOf(customKafkaConfigs.get(mapping.getAuthProperty().getPropertyKey())));
            } else {
                assertEquals(mapping.getAuthProperty().getPropertyKey() + ".test", customKafkaConfigs.get(mapping.getAuthProperty().getPropertyKey()));
            }
        });
    }

}
