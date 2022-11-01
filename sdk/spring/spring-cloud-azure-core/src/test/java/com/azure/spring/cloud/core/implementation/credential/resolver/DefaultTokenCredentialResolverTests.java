// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.core.implementation.credential.resolver.DefaultTokenCredentialResolver.buildCredentialKey;
import static org.mockito.Mockito.mock;

class DefaultTokenCredentialResolverTests {

    @Test
    void emptyPropertyShouldResolve() {
        TokenCredential mockCredential = mock(TokenCredential.class);
        DefaultTokenCredentialResolver resolver = new DefaultTokenCredentialResolver(mockCredential, null);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        Assertions.assertTrue(resolver.isResolvable(properties));
    }

    @Test
    void normalPropertyShouldResolve() {
        AzureHttpSdkProperties defaultProperties = new AzureHttpClientProperties();
        defaultProperties.getCredential().setClientId("fake-client-id");
        defaultProperties.getProfile().setTenantId("fake-tenant-id");

        TokenCredential mockCredential = mock(TokenCredential.class);
        DefaultTokenCredentialResolver resolver = new DefaultTokenCredentialResolver(mockCredential, defaultProperties);

        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setClientId("fake-client-id");
        properties.getProfile().setTenantId("fake-tenant-id");
        Assertions.assertTrue(resolver.isResolvable(properties));
        Assertions.assertEquals(mockCredential, resolver.resolve(properties));
    }

    @Test
    void nullPropertyShouldBuildNullKey() {
        String key = buildCredentialKey(null);
        Assertions.assertEquals("null,null,null", key);
    }

    @Test
    void propertyWithClientIdShouldBuildPartialNullKey() {
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setClientId("fake-client-id");
        String key = buildCredentialKey(properties);
        Assertions.assertEquals("fake-client-id,null,null", key);
    }

    @Test
    void propertyWithTenantIdShouldBuildPartialNullKey() {
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        String key = buildCredentialKey(properties);
        Assertions.assertEquals("null,fake-tenant-id,null", key);
    }

    @Test
    void propertyWithAuthorityHostShouldBuildPartialNullKey() {
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().getEnvironment().setActiveDirectoryEndpoint("fake-authority-host");
        String key = buildCredentialKey(properties);
        Assertions.assertEquals("null,null,fake-authority-host", key);
    }



}
