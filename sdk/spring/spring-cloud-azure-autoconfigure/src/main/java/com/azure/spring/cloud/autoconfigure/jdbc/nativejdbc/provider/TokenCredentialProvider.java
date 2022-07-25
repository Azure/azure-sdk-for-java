package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider;

import com.azure.core.credential.TokenCredential;

/**
 * Interface to be implemented by classes that wish to provide the token credential.
 */
public interface TokenCredentialProvider {

    TokenCredential getTokenCredential();

    TokenCredentialProvider addTokenCredentialFirst(TokenCredential tokenCredential);

    TokenCredentialProvider addTokenCredentialLast(TokenCredential tokenCredential);

}
