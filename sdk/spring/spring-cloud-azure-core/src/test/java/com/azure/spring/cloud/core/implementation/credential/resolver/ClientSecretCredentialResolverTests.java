// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientSecretCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClientSecretCredentialResolverTests {

    @Test
    void emptyPropertyShouldNotResolve() {
        ClientSecretCredentialResolver resolver = new ClientSecretCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithoutTenantIdShouldNotResolve() {
        ClientSecretCredentialResolver resolver = new ClientSecretCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientSecret("fake-client-secret");
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithoutClientIdShouldNotResolve() {
        ClientSecretCredentialResolver resolver = new ClientSecretCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientSecret("fake-client-secret");
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithoutClientSecretShouldNotResolve() {
        ClientSecretCredentialResolver resolver = new ClientSecretCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithTenantIdAndClientIdAndClientSecretShouldResolve() {
        ClientSecretCredentialResolver resolver = new ClientSecretCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientSecret("fake-client-secret");
        Assertions.assertTrue(resolver.isResolvable(properties));
        Assertions.assertEquals(ClientSecretCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void propertyWithTenantIdAndClientIdAndClientSecretShouldResolveAndSet() {
        ClientSecretCredentialBuilderFactory mockBuilderFactory = mock(ClientSecretCredentialBuilderFactory.class);
        ClientSecretCredentialBuilder mockBuilder = mock(ClientSecretCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(anyString())).thenReturn(mockBuilder);

        ClientSecretCredentialResolver resolver = new ClientSecretCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientSecret("fake-client-secret");
        Assertions.assertTrue(resolver.isResolvable(properties));

        resolver.resolve(properties);

        verify(mockBuilder, times(1)).authorityHost(anyString());
        verify(mockBuilder, times(1)).tenantId("fake-tenant-id");
        verify(mockBuilder, times(1)).clientId("fake-client-id");
        verify(mockBuilder, times(1)).clientSecret("fake-client-secret");
    }

    @Test
    void propertyWithoutAuthorityHostShouldResolveAzureGlobal() {
        ClientSecretCredentialBuilderFactory mockBuilderFactory = mock(ClientSecretCredentialBuilderFactory.class);
        ClientSecretCredentialBuilder mockBuilder = mock(ClientSecretCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(anyString())).thenReturn(mockBuilder);

        ClientSecretCredentialResolver resolver = new ClientSecretCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientSecret("fake-client-secret");
        resolver.resolve(properties);

        Assertions.assertNull(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        verify(mockBuilder, times(1)).authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);
    }

    @Test
    void propertyWithAuthorityHostShouldSet() {
        ClientSecretCredentialBuilderFactory mockBuilderFactory = mock(ClientSecretCredentialBuilderFactory.class);
        ClientSecretCredentialBuilder mockBuilder = mock(ClientSecretCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(anyString())).thenReturn(mockBuilder);

        ClientSecretCredentialResolver resolver = new ClientSecretCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientSecret("fake-client-secret");
        resolver.resolve(properties);

        Assertions.assertEquals(AzureAuthorityHosts.AZURE_CHINA, properties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        verify(mockBuilder, times(1)).authorityHost(AzureAuthorityHosts.AZURE_CHINA);
    }

}
