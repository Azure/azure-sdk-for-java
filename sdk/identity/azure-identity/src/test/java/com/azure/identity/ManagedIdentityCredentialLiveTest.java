package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import org.junit.Test;
import reactor.test.StepVerifier;

public class ManagedIdentityCredentialLiveTest {
    @Test
    public void testMSIEndpointOnAzure() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration();
        org.junit.Assume.assumeTrue("Skipping MSI tests on Azure", configuration.contains(Configuration.PROPERTY_MSI_ENDPOINT));

        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();
        StepVerifier.create(credential.getToken(new TokenRequestContext().addScopes("https://management.azure.com/.default")))
                .expectNextMatches(accessToken -> accessToken != null && accessToken.getToken() != null)
                .verifyComplete();
    }
}
