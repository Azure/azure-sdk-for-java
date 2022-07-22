package com.azure.spring.cloud.autoconfigure.jdbc.provider;

import com.azure.core.credential.TokenCredential;

public interface TokenCredentialProvider {
    TokenCredential getTokenCredential();
}
