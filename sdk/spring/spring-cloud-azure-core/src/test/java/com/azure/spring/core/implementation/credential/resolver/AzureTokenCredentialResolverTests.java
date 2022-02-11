// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.credential.resolver;

import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.spring.core.properties.AzureAmqpSdkProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AzureTokenCredentialResolverTests {


    private final AzureTokenCredentialResolver resolver = new AzureTokenCredentialResolver();

    @Test
    void emptyPropertiesShouldNotResolve() {
        TestAzureProperties properties = new TestAzureProperties();
        Assertions.assertNull(resolver.resolve(properties));
    }

    @Test
    void shouldResolveClientSecretTokenCredential() {
        TestAzureProperties properties = new TestAzureProperties();
        properties.getCredential().setClientId("test-client-id");
        properties.getCredential().setClientSecret("test-client-secret");
        properties.getProfile().setTenantId("test-tenant-id");
        Assertions.assertEquals(ClientSecretCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void shouldResolveClientCertificateTokenCredential() {
        TestAzureProperties properties = new TestAzureProperties();
        properties.getCredential().setClientId("test-client-id");
        properties.getCredential().setClientCertificatePath("test-client-cert-path");
        properties.getProfile().setTenantId("test-tenant-id");
        Assertions.assertEquals(ClientCertificateCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void shouldResolveMITokenCredential() {
        TestAzureProperties properties = new TestAzureProperties();
        properties.getCredential().setManagedIdentityClientId("test-mi-client-id");
        Assertions.assertEquals(ManagedIdentityCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void shouldResolveUsernamePasswordTokenCredential() {
        TestAzureProperties properties = new TestAzureProperties();
        properties.getCredential().setUsername("test-username");
        properties.getCredential().setPassword("test-password");
        properties.getCredential().setClientId("test-client-id");

        Assertions.assertEquals(UsernamePasswordCredential.class, resolver.resolve(properties).getClass());
    }

    @Test
    void azurePropertiesShouldResolve() {
        TestAzureProperties properties = new TestAzureProperties();
        Assertions.assertTrue(resolver.isResolvable(properties));
    }

    @Test
    void nullAzurePropertiesShouldNotResolve() {
        Assertions.assertTrue(resolver.isResolvable(null));
    }

    private static class TestAzureProperties extends AzureAmqpSdkProperties {

    }
    
}
