// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.service.implementation.passwordless.AzureKafkaPasswordlessProperties;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.copyJaasPropertyToAzureProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureKafkaPropertiesUtilsTest {

    @Test
    void testCopyJaasPropertyToAzureProperties() {
        String jaasConfig = "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required azure.configured=\"true\";";
        AzureKafkaPasswordlessProperties properties = new AzureKafkaPasswordlessProperties();

        copyJaasPropertyToAzureProperties(jaasConfig, properties);
        assertFalse(properties.getCredential().isManagedIdentityEnabled());
        assertNull(properties.getCredential().getClientId());
        assertNull(properties.getProfile().getCloudType());
    }

    @Test
    void testCopyJaasPropertyWithCustomizedValuesToAzureProperties() {
        String jaasConfig = "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required azure.configured=\"true\" "
            + "azure.credential.managed-identity-enabled=\"true\" azure.credential.client-id=\"test\" azure.profile.cloud-type=\"azure\";";
        AzureKafkaPasswordlessProperties properties = new AzureKafkaPasswordlessProperties();

        copyJaasPropertyToAzureProperties(jaasConfig, properties);
        assertTrue(properties.getCredential().isManagedIdentityEnabled());
        assertEquals("test", properties.getCredential().getClientId());
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE, properties.getProfile().getCloudType());

    }

    @Test
    void testClearAzureProperties() {
        AzureKafkaPasswordlessProperties properties = new AzureKafkaPasswordlessProperties();
        properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE);
        properties.getProfile().setTenantId("fake-tenant-id");
    }

}
