package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider;


import com.azure.core.credential.AccessToken;

/**
 * Interface to be implemented by classes that wish to provide accessToken
 */
interface AccessTokenProvider {

    AccessToken getAccessToken();
}
