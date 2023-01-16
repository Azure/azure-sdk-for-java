// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.passwordless;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

class AzureRedisPasswordlessPropertiesTest {

    private static final String REDIS_SCOPE_GLOBAL = "https://*.cacheinfra.windows.net:10225/appid/.default";
    private static final String REDIS_SCOPE_CHINA = "https://*.cacheinfra.windows.net.china:10225/appid/.default";
    private static final String REDIS_SCOPE_GERMANY = "https://*.cacheinfra.windows.net.germany:10225/appid/.default";
    private static final String REDIS_SCOPE_US_GOVERNMENT = "https://*.cacheinfra.windows.us.government.net:10225/appid/.default";

    @Test
    void testGetScopes() {
        AzureProfileProperties profile = new AzureProfileProperties();

        AzureRedisPasswordlessProperties properties = new AzureRedisPasswordlessProperties();
        properties.setScopes("fake-scopes");
        Assertions.assertEquals("fake-scopes", properties.getScopes());

        properties.setScopes(null);
        String scopes = properties.getScopes();
        Assertions.assertEquals(REDIS_SCOPE_GLOBAL, scopes);

        profile.setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_GERMANY);
        properties.setProfile(profile);
        scopes = properties.getScopes();
        Assertions.assertEquals(REDIS_SCOPE_GERMANY, scopes);

        profile.setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
        properties.setProfile(profile);
        scopes = properties.getScopes();
        Assertions.assertEquals(REDIS_SCOPE_CHINA, scopes);

        profile.setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        properties.setProfile(profile);
        scopes = properties.getScopes();
        Assertions.assertEquals(REDIS_SCOPE_US_GOVERNMENT, scopes);

        properties.setScopes("fake-scopes");
        scopes = properties.getScopes();
        Assertions.assertEquals("fake-scopes", scopes);

    }

    @Test
    void testToProperties() {
        TokenCredentialProperties credential = new TokenCredentialProperties();
        credential.setClientSecret("fake-client-secret");
        credential.setClientId("fake-client-id");
        credential.setUsername("fake-username");
        credential.setPassword("fake-password");
        credential.setClientCertificatePath("fake-client-certificate-path");
        credential.setClientCertificatePassword("fake-client-certificate-password");

        AzureProfileProperties profile = new AzureProfileProperties();
        profile.setTenantId("fake-tenantId");
        profile.setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_GERMANY);

        AzureRedisPasswordlessProperties azureRedisPasswordlessProperties = new AzureRedisPasswordlessProperties();
        azureRedisPasswordlessProperties.setScopes("fake-scopes");
        azureRedisPasswordlessProperties.setCredential(credential);
        azureRedisPasswordlessProperties.setProfile(profile);

        Properties properties = azureRedisPasswordlessProperties.toProperties();


        Assertions.assertEquals("fake-client-id", properties.getProperty(AuthProperty.CLIENT_ID.getPropertyKey()));
        Assertions.assertEquals("fake-client-secret", properties.getProperty(AuthProperty.CLIENT_SECRET.getPropertyKey()));
        Assertions.assertEquals("fake-username", properties.getProperty(AuthProperty.USERNAME.getPropertyKey()));
        Assertions.assertEquals("fake-password", properties.getProperty(AuthProperty.PASSWORD.getPropertyKey()));
        Assertions.assertEquals("fake-client-certificate-path", properties.getProperty(AuthProperty.CLIENT_CERTIFICATE_PATH.getPropertyKey()));
        Assertions.assertEquals("fake-client-certificate-password", properties.getProperty(AuthProperty.CLIENT_CERTIFICATE_PASSWORD.getPropertyKey()));
        Assertions.assertEquals("fake-tenantId", properties.getProperty(AuthProperty.TENANT_ID.getPropertyKey()));
        Assertions.assertEquals("fake-scopes", properties.getProperty(AuthProperty.SCOPES.getPropertyKey()));
    }
}
