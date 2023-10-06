// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class AzureCliCredentialNegativeTest {

    @Test
    public void testInvalidScopeFromRequest() {
        TokenRequestContext request = new TokenRequestContext().addScopes("scope" + invalidCharacter);


        AzureCliCredential credential = new AzureCliCredentialBuilder().build();

        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof IllegalArgumentException)
            .verify();
    }
    @Test
    public void testInvalidTenantFromRequest() {
        TokenRequestContext request = new TokenRequestContext().addScopes("scope").setTenantId("tenant" + invalidCharacter);
        AzureCliCredential credential = new AzureCliCredentialBuilder().build();

        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof IllegalArgumentException)
            .verify();
    }

    @Test
    public void testInvalidScopeFromRequestSync() {
        TokenRequestContext request = new TokenRequestContext().addScopes("scope" + invalidCharacter);

        AzureCliCredential credential = new AzureCliCredentialBuilder().build();
        try {
            credential.getTokenSync(request);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testInvalidTenantFromRequestSync() {
        TokenRequestContext request = new TokenRequestContext().addScopes("scope").setTenantId("tenant" + invalidCharacter);
        AzureCliCredential credential = new AzureCliCredentialBuilder().build();
        try {
            credential.getTokenSync(request);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Parameterized.Parameter
    public String invalidCharacter;

    @Parameterized.Parameters(name = "invalid character: {0}")
    public static Object[] getInvalidCharacters() {
        return new Object[] { "|", "&", ";" };
    }
}
