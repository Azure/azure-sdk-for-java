// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.identity.implementation.util.ScopeUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScopeUtilTests {

    @ParameterizedTest
    @MethodSource("validScopes")
    public void validScopes(String scope) {
        assertDoesNotThrow(() -> ScopeUtil.validateScope(scope));

    }

    @ParameterizedTest
    @MethodSource("invalidScopes")
    public void validInvalidScopes(String scope) {
        assertThrows(IllegalArgumentException.class, () -> ScopeUtil.validateScope(scope));

    }

    static Stream<String> validScopes() {
        return Stream.of("https://vaults.azure.net/.default", "https://management.core.windows.net//.default",
            "https://graph.microsoft.com/User.Read", "api://app-id-has-hyphens-and-1234567890s/user_impersonation");
    }

    static Stream<String> invalidScopes() {
        return Stream.of("api://app-id-has-hyphens-and-1234567890s/invalid scope",
            "api://app-id-has-hyphens-and-1234567890s/invalid\"scope",
            "api://app-id-has-hyphens-and-1234567890s/invalid\\scope");
    }
}
