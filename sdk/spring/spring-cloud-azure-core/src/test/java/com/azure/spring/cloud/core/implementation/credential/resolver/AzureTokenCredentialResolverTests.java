// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AzureTokenCredentialResolverTests {


    private final AzureTokenCredentialResolver resolver = new AzureTokenCredentialResolver();

    @Test
    void emptyPropertiesShouldNotResolve() {
        AzureTestProperties properties = new AzureTestProperties();
        Assertions.assertNull(resolver.resolve(properties));
    }

    @Test
    void shouldResolveClientSecretTokenCredential() {
        AzureTestProperties properties = new AzureTestProperties();
        properties.getCredential().setClientId("test-client-id");
        properties.getCredential().setClientSecret("test-client-secret");
        properties.getProfile().setTenantId("test-tenant-id");
        Assertions.assertEquals(ClientSecretCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void shouldResolveClientCertificateTokenCredential() {
        AzureTestProperties properties = new AzureTestProperties();
        properties.getCredential().setClientId("test-client-id");
        properties.getCredential().setClientCertificatePath("test-client-cert-path");
        properties.getProfile().setTenantId("test-tenant-id");
        Assertions.assertEquals(ClientCertificateCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void shouldResolveUserAssignedMITokenCredential() {
        AzureTestProperties properties = new AzureTestProperties();
        properties.getCredential().setClientId("test-mi-client-id");
        properties.getCredential().setManagedIdentityEnabled(true);
        Assertions.assertEquals(ManagedIdentityCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void shouldResolveSystemAssignedMITokenCredential() {
        AzureTestProperties properties = new AzureTestProperties();
        properties.getCredential().setManagedIdentityEnabled(true);
        Assertions.assertEquals(ManagedIdentityCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void shouldResolveUsernamePasswordTokenCredential() {
        AzureTestProperties properties = new AzureTestProperties();
        properties.getCredential().setUsername("test-username");
        properties.getCredential().setPassword("test-password");
        properties.getCredential().setClientId("test-client-id");

        Assertions.assertEquals(UsernamePasswordCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void azurePropertiesShouldResolve() {
        AzureTestProperties properties = new AzureTestProperties();
        Assertions.assertTrue(resolver.isResolvable(properties));
    }

    @Test
    void nullAzurePropertiesShouldNotResolve() {
        Assertions.assertTrue(resolver.isResolvable(null));
    }

    private static class AzureTestProperties extends AzureAmqpSdkProperties {

    }
    
}
