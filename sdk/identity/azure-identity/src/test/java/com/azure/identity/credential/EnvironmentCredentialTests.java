// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.util.configuration.BaseConfigurations;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.junit.Assert.fail;

public class EnvironmentCredentialTests {
    @Test
    public void testCreateEnvironmentCredential() {
        Configuration configuration = ConfigurationManager.getConfiguration();
        configuration.put(BaseConfigurations.AZURE_CLIENT_ID, "foo");
        configuration.put(BaseConfigurations.AZURE_CLIENT_SECRET, "bar");
        configuration.put(BaseConfigurations.AZURE_TENANT_ID, "baz");

        EnvironmentCredential credential = new EnvironmentCredentialBuilder().build();

        // authentication will fail client-id=foo, but should be able to create ClientSecretCredential
        AccessToken token = credential.getToken("qux/.default")
            .doOnSuccess(s -> fail())
            .onErrorResume(t -> {
                String message = t.getMessage();
                Assert.assertFalse(message != null && message.contains("Cannot create any credentials with the current environment variables"));
                return Mono.just(new AccessToken("token", OffsetDateTime.MAX));
            }).block();
        Assert.assertEquals("token", token.token());
    }
}
