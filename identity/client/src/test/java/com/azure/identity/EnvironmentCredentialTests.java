package com.azure.identity;

import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.identity.credential.EnvironmentCredential;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static org.junit.Assert.fail;

public class EnvironmentCredentialTests {
    @Test
    public void testCreateEnvironmentCredential() {
        Configuration configuration = ConfigurationManager.getConfiguration();
        configuration.put(BaseConfigurations.AZURE_CLIENT_ID, "foo");
        configuration.put(BaseConfigurations.AZURE_CLIENT_SECRET, "bar");
        configuration.put(BaseConfigurations.AZURE_TENANT_ID, "baz");

        EnvironmentCredential credential = new EnvironmentCredential();

        // authentication will fail client-id=foo, but should be able to create ClientSecretCredential
        String token = credential.getToken("qux/.default")
            .doOnSuccess(s -> fail())
            .onErrorResume(t -> {
                String message = t.getMessage();
                Assert.assertFalse(message != null && message.contains("Cannot create any credentials with the current environment variables"));
                return Mono.just("token");
            }).block();
        Assert.assertEquals("token", token);
    }
}
