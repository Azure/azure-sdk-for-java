/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.mdesrc.azurekeyvaultprovider;

/**
 * Provides a callback delegate which is to be implemented by the client code
 *
 */
public interface KeyVaultAuthenticationCallback {

    /**
     * Returns the acesss token of the authentication request
     *
     * @param authority
     *        - Identifier of the authority, a URL.
     * @param resource
     *        - Identifier of the target resource that is the recipient of the requested token, a URL.
     * @param scope
     *        - The scope of the authentication request.
     * @return access token
     */
    String getAccessToken(String authority, String resource, String scope);
}
