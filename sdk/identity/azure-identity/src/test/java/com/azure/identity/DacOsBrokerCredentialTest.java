// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class DacOsBrokerCredentialTest {
    @Test
    public void testBrokerUnavailable() {
        // setup
        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        // Simulate broker unavailable by using a test double or by ensuring the environment does not support broker
        BrokerCredential credential = new BrokerCredential("tenant");
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof CredentialUnavailableException
                && e.getMessage().contains("azure-identity-broker dependency is not available")
                && e.getMessage()
                    .contains(
                        "To mitigate this issue, refer to http://aka.ms/azsdk/java/identity/dacbrokerauth/troubleshoot"))
            .verify();
    }
}
