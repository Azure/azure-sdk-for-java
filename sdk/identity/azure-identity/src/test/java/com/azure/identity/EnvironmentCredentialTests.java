// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;


public class EnvironmentCredentialTests {
    @Test
    public void testCreateEnvironmentClientSecretCredential() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz"));

        EnvironmentCredential credential = new EnvironmentCredentialBuilder()
            .configuration(configuration)
            .build();

        // authentication will fail client-id=foo, but should be able to create ClientSecretCredential
        StepVerifier.create(credential.getToken(new TokenRequestContext().addScopes("qux/.default")))
            .verifyErrorSatisfies(t -> {
                String message = t.getMessage();
                Assertions.assertFalse(message != null
                    && message.contains("Cannot create any credentials with the current environment variables"));
            });


        // Validate Sync flow.
        Exception e = Assertions.assertThrows(Exception.class,
            () -> credential.getTokenSync(new TokenRequestContext().addScopes("qux/.default")));

        String message = e.getMessage();
        Assertions.assertFalse(message != null
            && message.contains("Cannot create any credentials with the current environment variables"));
    }

    @Test
    public void testCreateEnvironmentClientCertificateCredential() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH, "bar")
            .put(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD, "password")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz"));

        EnvironmentCredential credential = new EnvironmentCredentialBuilder()
            .configuration(configuration)
            .build();

        // authentication will fail client-id=foo, but should be able to create ClientCertificateCredential
        StepVerifier.create(credential.getToken(new TokenRequestContext().addScopes("qux/.default")))
            .verifyErrorSatisfies(t -> {
                String message = t.getMessage();
                Assertions.assertFalse(message != null
                    && message.contains("Cannot create any credentials with the current environment variables"));
            });

        // Validate Sync flow.
        Exception e = Assertions.assertThrows(Exception.class,
            () -> credential.getTokenSync(new TokenRequestContext().addScopes("qux/.default")));

        String message = e.getMessage();
        Assertions.assertFalse(message != null
            && message.contains("Cannot create any credentials with the current environment variables"));
    }

    @Test
    public void testCreateEnvironmentUserPasswordCredential() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_USERNAME, "bar")
            .put(Configuration.PROPERTY_AZURE_PASSWORD, "baz"));

        EnvironmentCredential credential = new EnvironmentCredentialBuilder()
            .configuration(configuration)
            .build();

        // authentication will fail client-id=foo, but should be able to create UsernamePasswordCredential
        StepVerifier.create(credential.getToken(new TokenRequestContext().addScopes("qux/.default")))
            .verifyErrorSatisfies(t -> {
                String message = t.getMessage();
                Assertions.assertFalse(message != null
                    && message.contains("Cannot create any credentials with the current environment variables"));
            });

        // Validate Sync flow.
        Exception e = Assertions.assertThrows(Exception.class,
            () -> credential.getTokenSync(new TokenRequestContext().addScopes("qux/.default")));

        String message = e.getMessage();
        Assertions.assertFalse(message != null
            && message.contains("Cannot create any credentials with the current environment variables"));
    }

    @Test
    public void testInvalidAdditionalTenant() {
        // setup
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz")
            .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, "RANDOM"));

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        EnvironmentCredential credential = new EnvironmentCredentialBuilder()
            .configuration(configuration)
            .build();

        StepVerifier.create(credential.getToken(request))
            .verifyErrorMatches(e -> e instanceof ClientAuthenticationException
                && (e.getMessage().startsWith("The current credential is not configured to")));
    }

    @Test
    public void testInvalidMultiTenantAuth() {
        // setup
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz"));

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        EnvironmentCredential credential = new EnvironmentCredentialBuilder()
            .configuration(configuration)
            .build();
        StepVerifier.create(credential.getToken(request))
            .verifyErrorMatches(e -> e instanceof ClientAuthenticationException
                && (e.getMessage().startsWith("The current credential is not configured to")));
    }

    @Test
    public void testValidMultiTenantAuth() {
        // setup
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz")
            .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, "*"));

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        EnvironmentCredential credential = new EnvironmentCredentialBuilder()
            .configuration(configuration)
            .build();
        StepVerifier.create(credential.getToken(request))
            .verifyError(MsalServiceException.class);
    }
}
