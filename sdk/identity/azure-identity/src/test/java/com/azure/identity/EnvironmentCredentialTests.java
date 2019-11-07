// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static org.junit.Assert.fail;

public class EnvironmentCredentialTests {
    @Test
    public void testCreateEnvironmentCredential() {
        Configuration configuration = Configuration.getGlobalConfiguration();
        configuration.put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo");
        configuration.put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar");
        configuration.put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz");

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
}
