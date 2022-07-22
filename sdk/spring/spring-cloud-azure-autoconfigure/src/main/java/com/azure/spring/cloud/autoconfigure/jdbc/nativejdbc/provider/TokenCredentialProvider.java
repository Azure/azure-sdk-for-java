package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider;

import com.azure.core.credential.TokenCredential;

public interface TokenCredentialProvider {
    TokenCredential getTokenCredential();

    TokenCredentialProvider addTokenCredentialFirst(TokenCredential tokenCredential);

    TokenCredentialProvider addTokenCredentialLast(TokenCredential tokenCredential);

}
