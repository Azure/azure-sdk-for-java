/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.keyvault.authentication;

/**
 * Stores authentication token and client key used for proof of possession authentication.
 */
public class AuthenticationResult{
    private String authToken;
    private String popKey;

    /**
     * Constructor
     *
     * @param authToken
     *      authentication token string.
     * @param popKey
     *      serialized json web key used for pop authentication.
     */
    public AuthenticationResult(String authToken, String popKey){
        this.authToken = authToken;
        this.popKey = popKey;
    }

    /**
     * Retrieve stored authentication token.
     *
     * @return authentication token.
     */
    public String getAuthToken(){
        return authToken;
    }

    /**
     * Retrieve stored PoP key.
     *
     * @return proof of possession key.
     */
    public String getPopKey(){
        return popKey;
    }
}