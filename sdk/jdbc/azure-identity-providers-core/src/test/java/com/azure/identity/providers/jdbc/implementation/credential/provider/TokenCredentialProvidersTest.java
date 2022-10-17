// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.provider;

import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TokenCredentialProvidersTest {

    private static final String SPRING_TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME = SpringTokenCredentialProviderTest.class.getName();

    @Test
    void testOptionsIsNull() {
        TokenCredentialProvider credentialProvider = TokenCredentialProviders.createInstance(null);
        Assertions.assertTrue(credentialProvider instanceof DefaultTokenCredentialProvider);
    }

    @Test
    void testDefaultConstructor() {
        TokenCredentialProvider credentialProvider = TokenCredentialProviders.createInstance();
        Assertions.assertTrue(credentialProvider instanceof DefaultTokenCredentialProvider);
    }

    @Test
    void testCreateSpringTokenCredentialProvider() {
        TokenCredentialProviderOptions option = new TokenCredentialProviderOptions();
        option.setTokenCredentialProviderClassName(SPRING_TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME);
        TokenCredentialProvider credentialProvider = TokenCredentialProviders.createInstance(option);
        Assertions.assertTrue(credentialProvider instanceof SpringTokenCredentialProviderTest);
    }

}
