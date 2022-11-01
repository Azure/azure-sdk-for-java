// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientCertificateCredentialBuilderFactory;
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

class ClientCertificateCredentialResolverTests {

    @Test
    void emptyPropertyShouldNotResolve() {
        ClientCertificateCredentialResolver resolver = new ClientCertificateCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithoutTenantIdShouldNotResolve() {
        ClientCertificateCredentialResolver resolver = new ClientCertificateCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientCertificatePath("fake-client-cert-path");
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithoutClientIdShouldNotResolve() {
        ClientCertificateCredentialResolver resolver = new ClientCertificateCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientCertificatePath("fake-client-cert-path");
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithoutCertPathShouldNotResolve() {
        ClientCertificateCredentialResolver resolver = new ClientCertificateCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithTenantIdAndClientIdAndCertPathShouldResolve() {
        ClientCertificateCredentialResolver resolver = new ClientCertificateCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientCertificatePath("fake-client-cert-path");
        Assertions.assertTrue(resolver.isResolvable(properties));
        Assertions.assertEquals(ClientCertificateCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void propertyWithTenantIdAndClientIdAndCertPathShouldResolveAndSet() {
        ClientCertificateCredentialBuilderFactory mockBuilderFactory = mock(ClientCertificateCredentialBuilderFactory.class);
        ClientCertificateCredentialBuilder mockBuilder = mock(ClientCertificateCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(anyString())).thenReturn(mockBuilder);

        ClientCertificateCredentialResolver resolver = new ClientCertificateCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientCertificatePath("fake-client-cert-path");
        Assertions.assertTrue(resolver.isResolvable(properties));

        resolver.resolve(properties);

        verify(mockBuilder, times(1)).authorityHost(anyString());
        verify(mockBuilder, times(1)).clientId("fake-client-id");
        verify(mockBuilder, times(1)).tenantId("fake-tenant-id");
    }

    @Test
    void propertyWithCertPasswordShouldResolvePfx() {
        ClientCertificateCredentialBuilderFactory mockBuilderFactory = mock(ClientCertificateCredentialBuilderFactory.class);
        ClientCertificateCredentialBuilder mockBuilder = mock(ClientCertificateCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(anyString())).thenReturn(mockBuilder);

        ClientCertificateCredentialResolver resolver = new ClientCertificateCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientCertificatePath("fake-client-cert-path");
        properties.getCredential().setClientCertificatePassword("fake-client-cert-password");
        resolver.resolve(properties);
        verify(mockBuilder, times(1)).pfxCertificate("fake-client-cert-path", "fake-client-cert-password");
        verify(mockBuilder, times(0)).pemCertificate(any());
    }

    @Test
    void propertyWithoutCertPasswordShouldResolvePem() {
        ClientCertificateCredentialBuilderFactory mockBuilderFactory = mock(ClientCertificateCredentialBuilderFactory.class);
        ClientCertificateCredentialBuilder mockBuilder = mock(ClientCertificateCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(anyString())).thenReturn(mockBuilder);

        ClientCertificateCredentialResolver resolver = new ClientCertificateCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientCertificatePath("fake-client-cert-path");
        resolver.resolve(properties);
        verify(mockBuilder, times(0)).pfxCertificate(anyString(), anyString());
        verify(mockBuilder, times(1)).pemCertificate("fake-client-cert-path");
    }

    @Test
    void propertyWithoutAuthorityHostShouldResolveAzureGlobal() {
        ClientCertificateCredentialBuilderFactory mockBuilderFactory = mock(ClientCertificateCredentialBuilderFactory.class);
        ClientCertificateCredentialBuilder mockBuilder = mock(ClientCertificateCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(anyString())).thenReturn(mockBuilder);

        ClientCertificateCredentialResolver resolver = new ClientCertificateCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientCertificatePath("fake-client-cert-path");
        resolver.resolve(properties);

        Assertions.assertNull(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        verify(mockBuilder, times(1)).authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);
    }

    @Test
    void propertyWithAuthorityHostShouldSet() {
        ClientCertificateCredentialBuilderFactory mockBuilderFactory = mock(ClientCertificateCredentialBuilderFactory.class);
        ClientCertificateCredentialBuilder mockBuilder = mock(ClientCertificateCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.authorityHost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.tenantId(anyString())).thenReturn(mockBuilder);

        ClientCertificateCredentialResolver resolver = new ClientCertificateCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getProfile().setTenantId("fake-tenant-id");
        properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setClientCertificatePath("fake-client-cert-path");
        resolver.resolve(properties);

        Assertions.assertEquals(AzureAuthorityHosts.AZURE_CHINA, properties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        verify(mockBuilder, times(1)).authorityHost(AzureAuthorityHosts.AZURE_CHINA);
    }

}
