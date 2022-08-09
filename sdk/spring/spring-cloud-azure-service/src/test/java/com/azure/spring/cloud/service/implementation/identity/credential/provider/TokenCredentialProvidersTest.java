// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.credential.provider;

import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProviderOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TokenCredentialProvidersTest {
    private static final String SPRING_TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME
        = "com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider";

    @Test
    void shouldCreateDefaultTokenCredentialProvider() {
        TokenCredentialProvider credentialProvider = TokenCredentialProviders.createInstance(null);
        Assertions.assertTrue(credentialProvider instanceof DefaultTokenCredentialProvider);
    }

    @Test
    void testDefaultConstructor() {
        TokenCredentialProvider credentialProvider = TokenCredentialProviders.createInstance();
        Assertions.assertTrue(credentialProvider instanceof DefaultTokenCredentialProvider);
    }

    @Test
    void shouldCreateSpringTokenCredentialProvider() {
        TokenCredentialProviderOptions option = new TokenCredentialProviderOptions();
        option.setTokenCredentialProviderClassName(SPRING_TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME);
        TokenCredentialProvider credentialProvider = TokenCredentialProviders.createInstance(option);
        Assertions.assertTrue(credentialProvider instanceof SpringTokenCredentialProvider);
    }

}
