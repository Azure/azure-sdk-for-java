// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.redis;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzureRedisPasswordlessProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AzureJedisPasswordlessUtilTest {

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
        globalProperties.getClient().setApplicationId("global-application-id");
        globalProperties.getProxy().setUsername("global-proxy-username");
        globalProperties.getProxy().setPassword("global-proxy-password");
        globalProperties.getProxy().setHostname("global-proxy-hostname");
        globalProperties.getProxy().setPort(1111);

        AzureRedisPasswordlessProperties passwordlessProperties = new AzureRedisPasswordlessProperties();

        AzurePasswordlessProperties result = AzureJedisPasswordlessUtil.mergeAzureProperties(globalProperties, passwordlessProperties);

        assertEquals("https://*.cacheinfra.windows.net:10225/appid/.default", result.getScopes());
        assertEquals("global-client-id", result.getCredential().getClientId());
        assertEquals("global-client-secret", result.getCredential().getClientSecret());
        assertEquals("global-password", result.getCredential().getPassword());
        assertEquals("global-user-name", result.getCredential().getUsername());
        assertEquals(false, result.getCredential().isManagedIdentityEnabled());
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA, result.getProfile().getCloudType());
        assertEquals("global-sub", result.getProfile().getSubscriptionId());
        assertEquals("global-tenant-id", result.getProfile().getTenantId());
        assertEquals("global-application-id", result.getClient().getApplicationId());
        assertEquals("global-proxy-username", result.getProxy().getUsername());
        assertEquals("global-proxy-password", result.getProxy().getPassword());
        assertEquals("global-proxy-hostname", result.getProxy().getHostname());
        assertEquals(1111, result.getProxy().getPort());
    }

    @Test
    void testGetPropertiesFromAzurePasswordlessProperties() {
        AzureGlobalProperties globalProperties = new AzureGlobalProperties();

        AzureRedisPasswordlessProperties passwordlessProperties = new AzureRedisPasswordlessProperties();
        passwordlessProperties.setScopes("scopes-us-gov");
        passwordlessProperties.getCredential().setClientId("client-id");
        passwordlessProperties.getCredential().setClientSecret("client-secret");
        passwordlessProperties.getCredential().setPassword("password");
        passwordlessProperties.getCredential().setUsername("user-name");
        passwordlessProperties.getCredential().setManagedIdentityEnabled(true);
        passwordlessProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        passwordlessProperties.getProfile().setSubscriptionId("sub");
        passwordlessProperties.getProfile().setTenantId("tenant-id");
        ((ClientProperties) passwordlessProperties.getClient()).setApplicationId("passwordless-application-id");
        ((ProxyProperties) passwordlessProperties.getProxy()).setUsername("proxy-username");
        ((ProxyProperties) passwordlessProperties.getProxy()).setPassword("proxy-password");
        ((ProxyProperties) passwordlessProperties.getProxy()).setHostname("proxy-hostname");
        ((ProxyProperties) passwordlessProperties.getProxy()).setPort(2222);

        AzurePasswordlessProperties result = AzureJedisPasswordlessUtil.mergeAzureProperties(globalProperties, passwordlessProperties);

        assertEquals("scopes-us-gov", result.getScopes());
        assertEquals("client-id", result.getCredential().getClientId());
        assertEquals("client-secret", result.getCredential().getClientSecret());
        assertEquals("password", result.getCredential().getPassword());
        assertEquals("user-name", result.getCredential().getUsername());
        assertEquals(true, result.getCredential().isManagedIdentityEnabled());
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, result.getProfile().getCloudType());
        assertEquals("sub", result.getProfile().getSubscriptionId());
        assertEquals("tenant-id", result.getProfile().getTenantId());
        assertEquals("passwordless-application-id", result.getClient().getApplicationId());
        assertEquals("proxy-username", result.getProxy().getUsername());
        assertEquals("proxy-password", result.getProxy().getPassword());
        assertEquals("proxy-hostname", result.getProxy().getHostname());
        assertEquals(2222, result.getProxy().getPort());

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

        AzureRedisPasswordlessProperties passwordlessProperties = new AzureRedisPasswordlessProperties();
        passwordlessProperties.setScopes("scope");
        passwordlessProperties.getCredential().setClientSecret("client-secret");
        passwordlessProperties.getCredential().setPassword("password");
        passwordlessProperties.getCredential().setManagedIdentityEnabled(true);
        passwordlessProperties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        passwordlessProperties.getProfile().setSubscriptionId("sub");
        ((ClientProperties) passwordlessProperties.getClient()).setApplicationId("passwordless-application-id");
        ((ProxyProperties) passwordlessProperties.getProxy()).setUsername("proxy-username");
        ((ProxyProperties) passwordlessProperties.getProxy()).setHostname("proxy-hostname");
        ((ProxyProperties) passwordlessProperties.getProxy()).setPort(2222);

        AzurePasswordlessProperties result = AzureJedisPasswordlessUtil.mergeAzureProperties(globalProperties, passwordlessProperties);

        assertEquals("scope", result.getScopes());
        assertEquals("global-client-id", result.getCredential().getClientId());
        assertEquals("client-secret", result.getCredential().getClientSecret());
        assertEquals("password", result.getCredential().getPassword());
        assertEquals("global-user-name", result.getCredential().getUsername());
        assertEquals(true, result.getCredential().isManagedIdentityEnabled());
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, result.getProfile().getCloudType());
        assertEquals("sub", result.getProfile().getSubscriptionId());
        assertEquals("global-tenant-id", result.getProfile().getTenantId());
        assertEquals("passwordless-application-id", result.getClient().getApplicationId());
        assertEquals("proxy-username", result.getProxy().getUsername());
        assertEquals("global-proxy-password", result.getProxy().getPassword());
        assertEquals("proxy-hostname", result.getProxy().getHostname());
        assertEquals(2222, result.getProxy().getPort());
    }
}
