// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.spring.cloud.core.implementation.factory.credential.ManagedIdentityCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagedIdentityCredentialResolverTests {

    @Test
    void emptyPropertyShouldNotResolve() {
        ManagedIdentityCredentialResolver resolver = new ManagedIdentityCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithoutManagedIdentityEnabledShouldNotResolve() {
        ManagedIdentityCredentialResolver resolver = new ManagedIdentityCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setManagedIdentityEnabled(false);
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    @Test
    void propertyWithManagedIdentityEnabledShouldResolve() {
        ManagedIdentityCredentialResolver resolver = new ManagedIdentityCredentialResolver();
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setManagedIdentityEnabled(true);
        Assertions.assertTrue(resolver.isResolvable(properties));
        Assertions.assertEquals(ManagedIdentityCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void propertyWithManagedIdentityEnabledShouldResolveAndSet() {
        ManagedIdentityCredentialBuilderFactory mockBuilderFactory = mock(ManagedIdentityCredentialBuilderFactory.class);
        ManagedIdentityCredentialBuilder mockBuilder = mock(ManagedIdentityCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.clientId(any())).thenReturn(mockBuilder);

        ManagedIdentityCredentialResolver resolver = new ManagedIdentityCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setManagedIdentityEnabled(true);
        Assertions.assertTrue(resolver.isResolvable(properties));

        resolver.resolve(properties);

        verify(mockBuilder, times(0)).clientId(anyString());
    }

    @Test
    void propertyWithManagedIdentityEnabledAndClientIdShouldResolveAndSet() {
        ManagedIdentityCredentialBuilderFactory mockBuilderFactory = mock(ManagedIdentityCredentialBuilderFactory.class);
        ManagedIdentityCredentialBuilder mockBuilder = mock(ManagedIdentityCredentialBuilder.class);
        when(mockBuilderFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);

        ManagedIdentityCredentialResolver resolver = new ManagedIdentityCredentialResolver(mockBuilderFactory);
        AzureHttpSdkProperties properties = new AzureHttpClientProperties();
        properties.getCredential().setClientId("fake-client-id");
        properties.getCredential().setManagedIdentityEnabled(true);
        Assertions.assertTrue(resolver.isResolvable(properties));

        resolver.resolve(properties);

        verify(mockBuilder, times(1)).clientId("fake-client-id");
    }

}
