// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ManagedIdentityCredential;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpringCredentialBuilderBaseTest extends SpringCredentialTestBase {

    @Test
    public void testPropertyBinder() {

        final String prefix = "azure.activedirectory.";

        final Properties properties = new PropertiesBuilder()
            .prefix(prefix)
            .property("clientId", "fake-client-id") // camel case
            .property("client-secret", "fake-secret") // kebab case
            .property("tenant_id", "fake-tenant-id") // underscore notation
            .property("CLIENT_CERTIFICATE_PATH", "fake-cert-path")// upper case format
            .build();

        final TestSpringCredentialBuilder builder = new TestSpringCredentialBuilder()
            .environment(buildEnvironment(properties));

        assertEquals("fake-client-id", builder.getPropertyValue(prefix, "client-id"));
        assertEquals("fake-secret", builder.getPropertyValue(prefix, "client-secret"));
        assertEquals("fake-tenant-id", builder.getPropertyValue(prefix, "tenant-id"));
        assertEquals("fake-cert-path", builder.getPropertyValue(prefix, "client-certificate-path"));
    }

    @Test
    public void testClientSecretCredentialCreated() {
        final Properties properties = new PropertiesBuilder()
            .property("client-id", "1")
            .property("client-secret", "2")
            .property("tenant-id", "3")
            .build();

        final TokenCredential tokenCredential = new TestSpringCredentialBuilder()
            .environment(buildEnvironment(properties))
            .populateTokenCredential("");

        assertTrue(tokenCredential instanceof ClientSecretCredential);
    }

    @Test
    public void testClientCertCredentialCreated() {
        final Properties properties = new PropertiesBuilder()
            .property("client-id", "1")
            .property("client-certificate-path", "2")
            .property("tenant-id", "3")
            .build();

        final TokenCredential tokenCredential = new TestSpringCredentialBuilder()
            .environment(buildEnvironment(properties))
            .populateTokenCredential("");

        assertTrue(tokenCredential instanceof ClientCertificateCredential);
    }

    @Test
    public void testManagedIdentityCredentialCreated() {
        final Properties properties = new PropertiesBuilder()
            .property("client-id", "1")
            .build();

        final TokenCredential tokenCredential = new TestSpringCredentialBuilder()
            .environment(buildEnvironment(properties))
            .populateTokenCredential("");

        assertTrue(tokenCredential instanceof ManagedIdentityCredential);
    }

    @Test
    public void testManagedIdentityCredentialCreatedByDefault() {
        final TokenCredential tokenCredential = new TestSpringCredentialBuilder()
            .environment(buildEnvironment(new Properties()))
            .populateTokenCredential("");

        assertTrue(tokenCredential instanceof ManagedIdentityCredential);
    }

    @Test
    public void testNoCredentialCreatedByDefaultWhenRequireClientId() {
        final TokenCredential tokenCredential = new TestSpringCredentialBuilder()
            .environment(buildEnvironment(new Properties()))
            .populateTokenCredentialBasedOnClientId("");

        assertNull(tokenCredential);
    }

    static class TestSpringCredentialBuilder extends SpringCredentialBuilderBase<TestSpringCredentialBuilder> {

    }


}
