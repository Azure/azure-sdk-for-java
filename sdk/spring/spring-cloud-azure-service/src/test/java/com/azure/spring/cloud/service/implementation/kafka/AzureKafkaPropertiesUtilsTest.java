// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.cloudType;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.managedIdentityEnabled;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.tenantId;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.copyJaasPropertyToAzureProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureKafkaPropertiesUtilsTest {

//    private Map<String, String> buildKafkaSourceConfigsFromAzureProperties() {
//        Map<String, String> configs = new HashMap<>();
//        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values()).forEach(mapping ->
//            configs.put(mapping.propertyKey(), mapping.propertyKey() + ".test"));
//        configs.put(managedIdentityEnabled.propertyKey(), "true");
//        configs.put(cloudType.propertyKey(), "AZURE_CHINA");
//        return configs;
//    }

//    @Test
//    void testConvertConfigMapToAzureProperties() {
//        AzurePasswordlessProperties properties = new AzurePasswordlessProperties();
//        convertConfigMapToAzureProperties(buildKafkaSourceConfigsFromAzureProperties(), properties);
//
//        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values()).forEach(mapping -> {
//            if (mapping == managedIdentityEnabled) {
//                assertTrue(Boolean.valueOf(mapping.getter().apply(properties)));
//            } else if (mapping == AzureKafkaPropertiesUtils.Mapping.cloudType) {
//                assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA,
//                    AzureProfileOptionsProvider.CloudType.fromString(mapping.getter().apply(properties)));
//            } else {
//                assertEquals(mapping.propertyKey() + ".test", mapping.getter().apply(properties));
//            }
//        });
//    }



//    @Test
//    void testConvertAzurePropertiesToConfigMapWithoutCustomValues() {
//        AzurePasswordlessProperties properties = new AzurePasswordlessProperties();
////        Map<String, String> sourceConfigs = buildKafkaSourceConfigsFromAzureProperties();
////        convertConfigMapToAzureProperties(sourceConfigs, properties);
//        Map<String, String> configs = convertAzurePropertiesToConfigMap(properties);
//
//        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values()).forEach(mapping -> {
//            if (mapping == managedIdentityEnabled) {
//                assertTrue(Boolean.valueOf(configs.get(mapping.propertyKey())));
//            } else if (mapping == AzureKafkaPropertiesUtils.Mapping.cloudType) {
//                assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA,
//                    AzureProfileOptionsProvider.CloudType.fromString(configs.get(mapping.propertyKey())));
//            } else {
//                assertEquals(mapping.propertyKey() + ".test", configs.get(mapping.propertyKey()));
//            }
//        });
//    }

//    @Test
//    void testCovertAzurePropertiesToJaasProperty() {
//        String jaasConfig = "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;";
//        AzurePasswordlessProperties properties = new AzurePasswordlessProperties();
////        convertConfigMapToAzureProperties(buildKafkaSourceConfigsFromAzureProperties(), properties);
//        String converted = convertAzurePropertiesToJaasProperty(properties, jaasConfig);
//
//        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values())
//            .filter(mapping -> Objects.nonNull(mapping.getter().apply(properties)))
//            .forEach(mapping -> {
//                if (mapping == managedIdentityEnabled) {
//                    assertTrue(converted.contains(String.format(JAAS_OPTIONS_PATTERN, mapping.propertyKey(), "true")));
//                } else if (mapping == AzureKafkaPropertiesUtils.Mapping.cloudType) {
//                    assertTrue(converted.contains(String.format(JAAS_OPTIONS_PATTERN, mapping.propertyKey(), AzureProfileOptionsProvider.CloudType.AZURE_CHINA.name())));
//                } else {
//                    assertTrue(converted.contains(String.format(JAAS_OPTIONS_PATTERN, mapping.propertyKey(), mapping.propertyKey() + ".test")));
//                }
//            });
//        assertTrue(converted.startsWith("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required "));
//        assertTrue(converted.endsWith(";"));
//    }

    @Test
    void testConvertJaasStringToMapWithoutAzureProperties() {
        Map<String, String> target = AzureKafkaPropertiesUtils.convertJaasStringToMap("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required azure.configured=\"true\";");
        assertTrue(target.isEmpty());
    }

    @Test
    void testConvertJaasStringToMapWithAzureProperties() {
        Map<String, String> target = AzureKafkaPropertiesUtils.convertJaasStringToMap("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required azure.configured=\"true\" "
            + "azure.credential.managed-identity-enabled=\"true\" azure.credential.client-id=\"test\" azure.profile.cloud-type=\"azure\";");
        assertEquals(3, target.size());
        assertEquals("true", target.get("azure.credential.managed-identity-enabled"));
        assertEquals("test", target.get("azure.credential.client-id"));
        assertEquals("azure", target.get("azure.profile.cloud-type"));
    }

    @Test
    void testCopyJaasPropertyToAzureProperties() {
        String jaasConfig = "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required azure.configured=\"true\";";
        AzurePasswordlessProperties properties = new AzurePasswordlessProperties();
//        convertConfigMapToAzureProperties(buildKafkaSourceConfigsFromAzureProperties(), properties);
//        String converted = convertAzurePropertiesToJaasProperty(properties, jaasConfig);

        copyJaasPropertyToAzureProperties(jaasConfig, properties);
        assertFalse(properties.getCredential().isManagedIdentityEnabled());
        assertNull(properties.getCredential().getClientId());
        assertNull(properties.getProfile().getCloudType());

//        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values())
//            .filter(mapping -> Objects.nonNull(mapping.getter().apply(properties)))
//            .forEach(mapping -> {
//                if (mapping == managedIdentityEnabled) {
//                    assertEquals(mapping.getter().apply(properties), "false");
//                } else if (mapping == cloudType) {
//                    assertEquals(mapping.getter().apply(properties), AzureProfileOptionsProvider.CloudType.AZURE_CHINA.name());
//                } else {
//                    assertEquals(mapping.getter().apply(properties), mapping.propertyKey() + ".test");
//                }
//            });
    }

    @Test
    void testCopyJaasPropertyWithCustomizedValuesToAzureProperties() {
        String jaasConfig = "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required azure.configured=\"true\" "
            + "azure.credential.managed-identity-enabled=\"true\" azure.credential.client-id=\"test\" azure.profile.cloud-type=\"azure\";";
        AzurePasswordlessProperties properties = new AzurePasswordlessProperties();
//        convertConfigMapToAzureProperties(buildKafkaSourceConfigsFromAzureProperties(), properties);
//        String converted = convertAzurePropertiesToJaasProperty(properties, jaasConfig);

        copyJaasPropertyToAzureProperties(jaasConfig, properties);
        assertTrue(properties.getCredential().isManagedIdentityEnabled());
        assertEquals("test", properties.getCredential().getClientId());
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE, properties.getProfile().getCloudType());

//        Arrays.stream(AzureKafkaPropertiesUtils.Mapping.values())
//            .filter(mapping -> Objects.nonNull(mapping.getter().apply(properties)))
//            .forEach(mapping -> {
//                if (mapping == managedIdentityEnabled) {
//                    assertEquals(mapping.getter().apply(properties), "false");
//                } else if (mapping == cloudType) {
//                    assertEquals(mapping.getter().apply(properties), AzureProfileOptionsProvider.CloudType.AZURE_CHINA.name());
//                } else {
//                    assertEquals(mapping.getter().apply(properties), mapping.propertyKey() + ".test");
//                }
//            });
    }

    @Test
    void testClearAzureProperties() {
        AzurePasswordlessProperties properties = new AzurePasswordlessProperties();
        properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE);
        properties.getProfile().setTenantId("fake-tenant-id");
    }

}
