// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzureCliCredentialNegativeTest {

    static Stream<String> invalidCharacters() {
        return Stream.of("|", "&", ";");
    }

    @ParameterizedTest
    @MethodSource("invalidCharacters")
    public void testInvalidScopeFromRequest(String invalidCharacter) {
        TokenRequestContext request = new TokenRequestContext().addScopes("scope" + invalidCharacter);

        AzureCliCredential credential = new AzureCliCredentialBuilder().build();

        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof IllegalArgumentException)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("invalidCharacters")
    public void testInvalidTenantFromRequest(String invalidCharacter) {
        TokenRequestContext request
            = new TokenRequestContext().addScopes("scope").setTenantId("tenant" + invalidCharacter);
        AzureCliCredential credential = new AzureCliCredentialBuilder().build();

        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof IllegalArgumentException)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("invalidCharacters")
    public void testInvalidScopeFromRequestSync(String invalidCharacter) {
        TokenRequestContext request = new TokenRequestContext().addScopes("scope" + invalidCharacter);

        AzureCliCredential credential = new AzureCliCredentialBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> credential.getTokenSync(request));
    }

    @ParameterizedTest
    @MethodSource("invalidCharacters")
    public void testInvalidTenantFromRequestSync(String invalidCharacter) {
        TokenRequestContext request
            = new TokenRequestContext().addScopes("scope").setTenantId("tenant" + invalidCharacter);
        AzureCliCredential credential = new AzureCliCredentialBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> credential.getTokenSync(request));
    }
}
