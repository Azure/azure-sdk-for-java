// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

public class AzurePowerShellCredentialNegativeTest {
    static Stream<String> invalidCharacters() {
        return Stream.of("|", "&", "'", ";");
    }

    @ParameterizedTest
    @MethodSource("invalidCharacters")
    public void testInvalidScopeFromRequest(String invalidCharacter) {
        TokenRequestContext request = new TokenRequestContext().addScopes("scope" + invalidCharacter);

        AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();

        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof IllegalArgumentException)
            .verify();
    }
}
