//  Copyright (c) Microsoft Corporation. All rights reserved.
//  Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import java.util.Map;

import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class KafkaPropertiesBeanPostProcessorTest {

//	@Test
//	void testConvertAzurePropertiesToMap() {
//		AzurePasswordlessProperties properties = new AzurePasswordlessProperties();
//		properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE);
//		properties.getProfile().setTenantId("fake-tenant-id");
//		Map<String, String> configs = convertAzurePropertiesToMap(properties);
//		assertFalse(Boolean.valueOf(configs.get(AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.managedIdentityEnabled.propertyKey())));
//		assertEquals(AzureProfileOptionsProvider.CloudType.AZURE,
//			AzureProfileOptionsProvider.CloudType.fromString(configs.get(AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.cloudType.propertyKey())));
//		assertEquals("fake-tenant-id", configs.get(AzureKafkaPropertiesUtils.AzureKafkaPasswordlessPropertiesMapping.tenantId.propertyKey()));
//	}

}
