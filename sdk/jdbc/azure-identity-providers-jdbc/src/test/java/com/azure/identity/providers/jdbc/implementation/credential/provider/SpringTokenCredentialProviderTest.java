package com.azure.identity.providers.jdbc.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.providers.jdbc.api.credential.provider.TokenCredentialProvider;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;

class SpringTokenCredentialProviderTest implements TokenCredentialProvider {

    public SpringTokenCredentialProviderTest(TokenCredentialProviderOptions options) {
    }

    @Override
    public TokenCredential get() {
        return null;
    }
}
