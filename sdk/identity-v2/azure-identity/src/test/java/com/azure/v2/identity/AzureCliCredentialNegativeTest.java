// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
        assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
    }

    @ParameterizedTest
    @MethodSource("invalidCharacters")
    public void testInvalidTenantFromRequest(String invalidCharacter) {
        TokenRequestContext request
            = new TokenRequestContext().addScopes("scope").setTenantId("tenant" + invalidCharacter);
        AzureCliCredential credential = new AzureCliCredentialBuilder().build();
        assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
    }
}
