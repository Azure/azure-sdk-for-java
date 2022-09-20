// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.util.IdentityUtil;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static org.junit.Assert.fail;

public class EnvironmentCredentialTests {
    @Test
    public void testCreateEnvironmentClientSecretCredential() {
        Configuration.getGlobalConfiguration()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz");

        EnvironmentCredential credential = new EnvironmentCredentialBuilder().build();

        // authentication will fail client-id=foo, but should be able to create ClientSecretCredential
        StepVerifier.create(credential.getToken(new TokenRequestContext().addScopes("qux/.default"))
            .doOnSuccess(s -> fail())
            .onErrorResume(t -> {
                String message = t.getMessage();
                Assert.assertFalse(message != null && message.contains("Cannot create any credentials with the current environment variables"));
                return Mono.just(new AccessToken("token", OffsetDateTime.MAX));
            }))
            .expectNextMatches(token -> "token".equals(token.getToken()))
            .verifyComplete();
    }

    @Test
    public void testCreateEnvironmentClientCertificateCredential() {
        Configuration.getGlobalConfiguration()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH, "bar")
            .put(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD, "password")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz");

        EnvironmentCredential credential = new EnvironmentCredentialBuilder().build();

        // authentication will fail client-id=foo, but should be able to create ClientCertificateCredential
        StepVerifier.create(credential.getToken(new TokenRequestContext().addScopes("qux/.default"))
            .doOnSuccess(s -> fail())
            .onErrorResume(t -> {
                String message = t.getMessage();
                Assert.assertFalse(message != null && message.contains("Cannot create any credentials with the current environment variables"));
                return Mono.just(new AccessToken("token", OffsetDateTime.MAX));
            }))
            .expectNextMatches(token -> "token".equals(token.getToken()))
            .verifyComplete();
    }

    @Test
    public void testCreateEnvironmentUserPasswordCredential() {
        Configuration.getGlobalConfiguration()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_USERNAME, "bar")
            .put(Configuration.PROPERTY_AZURE_PASSWORD, "baz");

        EnvironmentCredential credential = new EnvironmentCredentialBuilder().build();

        // authentication will fail client-id=foo, but should be able to create UsernamePasswordCredential
        StepVerifier.create(credential.getToken(new TokenRequestContext().addScopes("qux/.default"))
            .doOnSuccess(s -> fail())
            .onErrorResume(t -> {
                String message = t.getMessage();
                Assert.assertFalse(message != null && message.contains("Cannot create any credentials with the current environment variables"));
                return Mono.just(new AccessToken("token", OffsetDateTime.MAX));
            }))
            .expectNextMatches(token -> "token".equals(token.getToken()))
            .verifyComplete();
    }

    @Test
    public void testInvalidAdditionalTenant() throws Exception {
        // setup
        Configuration.getGlobalConfiguration()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz")
            .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, "RANDOM");

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        EnvironmentCredential credential =
            new EnvironmentCredentialBuilder().build();

        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testInvalidMultiTenantAuth() throws Exception {
        // setup
        Configuration.getGlobalConfiguration()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz");

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        EnvironmentCredential credential =
            new EnvironmentCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testValidMultiTenantAuth() throws Exception {
        // setup
        Configuration.getGlobalConfiguration()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz")
            .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, "*");

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        EnvironmentCredential credential =
            new EnvironmentCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof MsalServiceException)
            .verify();
    }
}
