// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.identity.util.TestConfigurationSource;
import com.azure.v2.identity.util.TestUtils;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnvironmentCredentialTests {
    @Test
    public void testCreateEnvironmentClientSecretCredential() {
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(IdentityUtil.PROPERTY_AZURE_CLIENT_ID, "foo")
                .put(IdentityUtil.PROPERTY_AZURE_CLIENT_SECRET, "bar")
                .put(IdentityUtil.PROPERTY_AZURE_TENANT_ID, "baz"));

        EnvironmentCredential credential = new EnvironmentCredentialBuilder().configuration(configuration).build();

        Exception e = Assertions.assertThrows(CredentialAuthenticationException.class,
            () -> credential.getToken(new TokenRequestContext().addScopes("qux/.default")));

        String message = e.getMessage();
        Assertions.assertFalse(message != null
            && message.contains("Cannot create any credentials with the current environment variables"));
    }

    @Test
    public void testCreateEnvironmentClientCertificateCredential() {
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(IdentityUtil.PROPERTY_AZURE_CLIENT_ID, "foo")
                .put(IdentityUtil.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH, "bar")
                .put(IdentityUtil.PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD, "password")
                .put(IdentityUtil.PROPERTY_AZURE_TENANT_ID, "baz"));

        EnvironmentCredential credential = new EnvironmentCredentialBuilder().configuration(configuration).build();

        Exception e = Assertions.assertThrows(CredentialAuthenticationException.class,
            () -> credential.getToken(new TokenRequestContext().addScopes("qux/.default")));

        String message = e.getMessage();
        Assertions.assertFalse(message != null
            && message.contains("Cannot create any credentials with the current environment variables"));
    }

    @Test
    public void testInvalidAdditionalTenant() {
        // setup
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(IdentityUtil.PROPERTY_AZURE_CLIENT_ID, "foo")
                .put(IdentityUtil.PROPERTY_AZURE_CLIENT_SECRET, "bar")
                .put(IdentityUtil.PROPERTY_AZURE_TENANT_ID, "baz")
                .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, "RANDOM"));

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        EnvironmentCredential credential = new EnvironmentCredentialBuilder().configuration(configuration).build();

        Assertions.assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
    }

    @Test
    public void testInvalidMultiTenantAuth() {
        // setup
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(IdentityUtil.PROPERTY_AZURE_CLIENT_ID, "foo")
                .put(IdentityUtil.PROPERTY_AZURE_CLIENT_SECRET, "bar")
                .put(IdentityUtil.PROPERTY_AZURE_TENANT_ID, "baz"));

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        EnvironmentCredential credential = new EnvironmentCredentialBuilder().configuration(configuration).build();
        Assertions.assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
    }

    @Test
    public void testValidMultiTenantAuth() {
        // setup
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(IdentityUtil.PROPERTY_AZURE_CLIENT_ID, "foo")
                .put(IdentityUtil.PROPERTY_AZURE_CLIENT_SECRET, "bar")
                .put(IdentityUtil.PROPERTY_AZURE_TENANT_ID, "baz")
                .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, "*"));

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        EnvironmentCredential credential = new EnvironmentCredentialBuilder().configuration(configuration).build();
        Assertions.assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
    }
}
