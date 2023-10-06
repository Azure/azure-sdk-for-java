// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import reactor.test.StepVerifier;

@RunWith(Parameterized.class)
public class AzurePowerShellCredentialNegativeTest {
    @Test
    public void testInvalidScopeFromRequest() {
        TokenRequestContext request = new TokenRequestContext().addScopes("scope" + invalidCharacter);


        AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();

        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof IllegalArgumentException)
            .verify();
    }

    @Parameterized.Parameter
    public String invalidCharacter;

    @Parameterized.Parameters(name = "invalid character: {0}")
    public static Object[] getInvalidCharacters() {
        return new Object[] { "|", "&", "'", ";" };
    }
}
