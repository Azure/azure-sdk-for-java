// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.spring.cloud.core.implementation.factory.credential.UsernamePasswordCredentialBuilderFactory;
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

class UsernamePasswordCredentialResolverTests {

    @Test
    void emptyPropertyShouldNotResolve() {
        UsernamePasswordCredentialResolver resolver = new UsernamePasswordCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithoutClientIdShouldNotResolve() {
        UsernamePasswordCredentialResolver resolver = new UsernamePasswordCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setUsername("fake-username");
        properties.getCredential().setPassword("fake-password");
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithoutUsernameShouldNotResolve() {
        UsernamePasswordCredentialResolver resolver = new UsernamePasswordCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setPassword("fake-password");
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithoutPasswordShouldNotResolve() {
        UsernamePasswordCredentialResolver resolver = new UsernamePasswordCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setUsername("fake-username");
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithClientIdAndUsernameAndPasswordShouldResolve() {
        UsernamePasswordCredentialResolver resolver = new UsernamePasswordCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setUsername("fake-username");
        properties.getCredential().setPassword("fake-password");
        Assertions.assertTrue(resolver.isResolvable(properties));
        Assertions.assertEquals(UsernamePasswordCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void propertyWithClientIdAndUsernameAndPasswordShouldResolveAndSet() {
        UsernamePasswordCredentialBuilderFactory mockBuilderFactory = mock(UsernamePasswordCredentialBuilderFactory.class);
        UsernamePasswordCredentialBuilder mockBuilder = mock(UsernamePasswordCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.password(anyString())).thenReturn(mockBuilder);

        UsernamePasswordCredentialResolver resolver = new UsernamePasswordCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setUsername("fake-username");
        properties.getCredential().setPassword("fake-password");
        Assertions.assertTrue(resolver.isResolvable(properties));

        resolver.resolve(properties);

        verify(mockBuilder, times(1)).authorityHost(anyString());
        verify(mockBuilder, times(1)).tenantId("fake-tenant-id");
        verify(mockBuilder, times(1)).clientId("fake-client-id");
        verify(mockBuilder, times(1)).username("fake-username");
        verify(mockBuilder, times(1)).password("fake-password");
    }

    @Test
    void propertyWithoutAuthorityHostShouldResolveAzureGlobal() {
        UsernamePasswordCredentialBuilderFactory mockBuilderFactory = mock(UsernamePasswordCredentialBuilderFactory.class);
        UsernamePasswordCredentialBuilder mockBuilder = mock(UsernamePasswordCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.password(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(any())).thenReturn(mockBuilder);

        UsernamePasswordCredentialResolver resolver = new UsernamePasswordCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setUsername("fake-username");
        properties.getCredential().setPassword("fake-password");
        resolver.resolve(properties);

        Assertions.assertNull(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        verify(mockBuilder, times(1)).authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);
    }

    @Test
    void propertyWithAuthorityHostShouldSet() {
        UsernamePasswordCredentialBuilderFactory mockBuilderFactory = mock(UsernamePasswordCredentialBuilderFactory.class);
        UsernamePasswordCredentialBuilder mockBuilder = mock(UsernamePasswordCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.username(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.password(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(any())).thenReturn(mockBuilder);

        UsernamePasswordCredentialResolver resolver = new UsernamePasswordCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setUsername("fake-username");
        properties.getCredential().setPassword("fake-password");

        resolver.resolve(properties);

        Assertions.assertEquals(AzureAuthorityHosts.AZURE_CHINA, properties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        verify(mockBuilder, times(1)).authorityHost(AzureAuthorityHosts.AZURE_CHINA);
    }

}
