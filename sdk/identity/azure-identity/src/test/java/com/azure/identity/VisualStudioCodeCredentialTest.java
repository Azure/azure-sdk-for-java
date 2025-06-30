// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class VisualStudioCodeCredentialTest {
    @Test
    public void testInValidStateVsCode() {
        // setup
        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        VisualStudioCodeCredential credential = new VisualStudioCodeCredentialBuilder().tenantId("tenant").build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof CredentialUnavailableException
                && (e.getMessage().startsWith("Visual Studio Code Authentication is not available. Ensure you have")))
            .verify();
    }

}
