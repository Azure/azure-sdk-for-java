// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;

class SpringTokenCredentialProviderTest implements TokenCredentialProvider {

    SpringTokenCredentialProviderTest(TokenCredentialProviderOptions options) {
    }

    @Override
    public TokenCredential get() {
        return null;
    }
}
