// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.passwordless;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.core.implementation.util.AzurePasswordlessPropertiesUtils;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MergeAzureCommonPropertiesTest {

    @Test
    void testGetPropertiesFromGlobalProperties() {
        AzureGlobalProperties globalProperties = new AzureGlobalProperties();
        globalProperties.getCredential().setClientId("global-client-id");
        globalProperties.getCredential().setClientSecret("global-client-secret");
        globalProperties.getCredential().setPassword("global-password");
        globalProperties.getCredential().setUsername("global-user-name");
        globalProperties.getCredential().setManagedIdentityEnabled(true);
        globalProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
        globalProperties.getProfile().setSubscriptionId("global-sub");
        globalProperties.getProfile().setTenantId("global-tenant-id");

        AzureServiceBusJmsProperties passwordlessProperties = new AzureServiceBusJmsProperties();

        AzureServiceBusJmsProperties result = new AzureServiceBusJmsProperties();
        AzurePasswordlessPropertiesUtils.mergeAzureCommonProperties(globalProperties, passwordlessProperties, result);

        assertEquals("https://servicebus.azure.net/.default", result.getScopes());
        assertEquals("global-client-id", result.getCredential().getClientId());
        assertEquals("global-client-secret", result.getCredential().getClientSecret());
        assertEquals("global-password", result.getCredential().getPassword());
        assertEquals("global-user-name", result.getCredential().getUsername());
        assertEquals(false, result.getCredential().isManagedIdentityEnabled());
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA, result.getProfile().getCloudType());
        assertEquals("global-sub", result.getProfile().getSubscriptionId());
        assertEquals("global-tenant-id", result.getProfile().getTenantId());
    }

    @Test
    void testGetPropertiesFromAzurePasswordlessProperties() {
        AzureGlobalProperties globalProperties = new AzureGlobalProperties();

        AzureServiceBusJmsProperties passwordlessProperties = new AzureServiceBusJmsProperties();
        passwordlessProperties.setScopes("scopes-us-gov");
        passwordlessProperties.getCredential().setClientId("client-id");
        passwordlessProperties.getCredential().setClientSecret("client-secret");
        passwordlessProperties.getCredential().setPassword("password");
        passwordlessProperties.getCredential().setUsername("user-name");
        passwordlessProperties.getCredential().setManagedIdentityEnabled(true);
        passwordlessProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        passwordlessProperties.getProfile().setSubscriptionId("sub");
        passwordlessProperties.getProfile().setTenantId("tenant-id");

        AzureServiceBusJmsProperties result = new AzureServiceBusJmsProperties();
        AzurePasswordlessPropertiesUtils.mergeAzureCommonProperties(globalProperties, passwordlessProperties, result);

        assertEquals("scopes-us-gov", result.getScopes());
        assertEquals("client-id", result.getCredential().getClientId());
        assertEquals("client-secret", result.getCredential().getClientSecret());
        assertEquals("password", result.getCredential().getPassword());
        assertEquals("user-name", result.getCredential().getUsername());
        assertEquals(true, result.getCredential().isManagedIdentityEnabled());
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, result.getProfile().getCloudType());
        assertEquals("sub", result.getProfile().getSubscriptionId());
        assertEquals("tenant-id", result.getProfile().getTenantId());

    }

    @Test
    void testGetPropertiesFromGlobalAndPasswordlessProperties() {

        AzureGlobalProperties globalProperties = new AzureGlobalProperties();
        globalProperties.getCredential().setClientId("global-client-id");
        globalProperties.getCredential().setClientSecret("global-client-secret");
        globalProperties.getCredential().setPassword("global-password");
        globalProperties.getCredential().setUsername("global-user-name");
        globalProperties.getCredential().setManagedIdentityEnabled(true);
        globalProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
        globalProperties.getProfile().setSubscriptionId("global-sub");
        globalProperties.getProfile().setTenantId("global-tenant-id");
        globalProperties.getClient().setApplicationId("global-application-id");
        globalProperties.getProxy().setUsername("global-proxy-username");
        globalProperties.getProxy().setPassword("global-proxy-password");
        globalProperties.getProxy().setHostname("global-proxy-hostname");
        globalProperties.getProxy().setPort(1111);

        AzureServiceBusJmsProperties passwordlessProperties = new AzureServiceBusJmsProperties();
        passwordlessProperties.setScopes("scope");
        passwordlessProperties.getCredential().setClientSecret("client-secret");
        passwordlessProperties.getCredential().setPassword("password");
        passwordlessProperties.getCredential().setManagedIdentityEnabled(true);
        passwordlessProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        passwordlessProperties.getProfile().setSubscriptionId("sub");

        AzureServiceBusJmsProperties result = new AzureServiceBusJmsProperties();
        AzurePasswordlessPropertiesUtils.mergeAzureCommonProperties(globalProperties, passwordlessProperties, result);

        assertEquals("scope", result.getScopes());
        assertEquals("global-client-id", result.getCredential().getClientId());
        assertEquals("client-secret", result.getCredential().getClientSecret());
        assertEquals("password", result.getCredential().getPassword());
        assertEquals("global-user-name", result.getCredential().getUsername());
        assertEquals(true, result.getCredential().isManagedIdentityEnabled());
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, result.getProfile().getCloudType());
        assertEquals("sub", result.getProfile().getSubscriptionId());
        assertEquals("global-tenant-id", result.getProfile().getTenantId());
    }
}
