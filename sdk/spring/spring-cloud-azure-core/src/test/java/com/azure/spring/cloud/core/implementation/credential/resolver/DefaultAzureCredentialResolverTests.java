// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultAzureCredentialResolverTests {

    @Test
    void emptyPropertyShouldResolve() {
        DefaultAzureCredentialResolver resolver = new DefaultAzureCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        Assertions.assertTrue(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithClientIdShouldResolve() {
        DefaultAzureCredentialResolver resolver = new DefaultAzureCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setClientId("fake-client-id");
        Assertions.assertTrue(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithTenantIdShouldResolve() {
        DefaultAzureCredentialResolver resolver = new DefaultAzureCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        Assertions.assertTrue(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithClientIdAndTenantIdShouldResolveAndSet() {
        DefaultAzureCredentialBuilderFactory mockBuilderFactory = mock(DefaultAzureCredentialBuilderFactory.class);
        DefaultAzureCredentialBuilder mockBuilder = mock(DefaultAzureCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.managedIdentityClientId(anyString())).thenReturn(mockBuilder);

        DefaultAzureCredentialResolver resolver = new DefaultAzureCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        Assertions.assertTrue(resolver.isResolvable(properties));

        resolver.resolve(properties);

        verify(mockBuilder, times(1)).authorityHost(anyString());
        verify(mockBuilder, times(1)).tenantId("fake-tenant-id");
        verify(mockBuilder, times(1)).managedIdentityClientId("fake-client-id");
    }

    @Test
    void propertyWithoutAuthorityHostShouldResolveAzureGlobal() {
        DefaultAzureCredentialBuilderFactory mockBuilderFactory = mock(DefaultAzureCredentialBuilderFactory.class);
        DefaultAzureCredentialBuilder mockBuilder = mock(DefaultAzureCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(any())).thenReturn(mockBuilder);
        when(mockBuilder.managedIdentityClientId(any())).thenReturn(mockBuilder);

        DefaultAzureCredentialResolver resolver = new DefaultAzureCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        resolver.resolve(properties);

        Assertions.assertNull(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        verify(mockBuilder, times(1)).authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);
    }

    @Test
    void propertyWithAuthorityHostShouldSet() {
        DefaultAzureCredentialBuilderFactory mockBuilderFactory = mock(DefaultAzureCredentialBuilderFactory.class);
        DefaultAzureCredentialBuilder mockBuilder = mock(DefaultAzureCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(any())).thenReturn(mockBuilder);
        when(mockBuilder.managedIdentityClientId(any())).thenReturn(mockBuilder);

        DefaultAzureCredentialResolver resolver = new DefaultAzureCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);

        resolver.resolve(properties);

        Assertions.assertEquals(AzureAuthorityHosts.AZURE_CHINA, properties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        verify(mockBuilder, times(1)).authorityHost(AzureAuthorityHosts.AZURE_CHINA);
    }

}
