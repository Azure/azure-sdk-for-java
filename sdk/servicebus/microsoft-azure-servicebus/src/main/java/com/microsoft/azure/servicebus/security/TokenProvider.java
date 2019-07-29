// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.security;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import com.microsoft.azure.servicebus.security.AzureActiveDirectoryTokenProvider.AuthenticationCallback;

/**
 * This abstract class defines the contract of a token provider. All token providers should inherit from this class.
 * An instance of token provider is used to obtain a security token for a given audience.
 * @since 1.2.0
 *
 */
public abstract class TokenProvider {
    /**
     * Asynchronously gets a security token for the given audience. Implementations of this method may choose to create a new token for every call
     * or return a cached token. But the token returned must be valid.
     * @param audience path of the entity for which this security token is to be presented
     * @return an instance of CompletableFuture which returns a {@link SecurityToken} on completion.
     */
    public abstract CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience);
    
    /**
     * Creates a Shared Access Signature token provider with the given key name and key value. Returned token provider creates tokens
     * with validity of 20 minutes. This is a utility method.
     * @param sasKeyName SAS key name
     * @param sasKey SAS key value
     * @return an instance of Shared Access Signature token provider with the given key name, key value.
     */
    public static TokenProvider createSharedAccessSignatureTokenProvider(String sasKeyName, String sasKey) {
        return new SharedAccessSignatureTokenProvider(sasKeyName, sasKey, SecurityConstants.DEFAULT_SAS_TOKEN_VALIDITY_IN_SECONDS);
    }
    
    /**
     * Creates a Shared Access Signature token provider that always returns an already created token. This is a utility method.
     * @param sasToken Already created Shared Access Signature token to be returned by {@link #getSecurityTokenAsync(String)} method.
     * @param sasTokenValidUntil Instant when the token expires
     * @return an instance of Shared Access Signature token provider that always returns an already created token.
     */
    public static TokenProvider createSharedAccessSignatureTokenProvider(String sasToken, Instant sasTokenValidUntil) {
        return new SharedAccessSignatureTokenProvider(sasToken, sasTokenValidUntil);
    }

    /**
     * Creates a Azure Active Directory token provider that creates a token with the user defined AuthenticationCallback. This is a utility method.
     * @param callback A custom AuthenticationCallback that takes in the target resource and address of the authority 
     *            to issue token and provides a security token for the target url
     * @param authority URL of the Azure Active Directory instance
     * @param callbackState Custom parameter that may be provided to the AuthenticationCallback
     * @return an instance of Azure Active Directory token provider
     */
    public static TokenProvider createAzureActiveDirectoryTokenProvider(AuthenticationCallback callback, String authority, Object callbackState) {
        if (callback == null) {
            throw new IllegalArgumentException("The callback provided cannot be null.");
        }
        if (authority == null) {
            throw new IllegalArgumentException("The authority provided cannot be null.");
        }
        return new AzureActiveDirectoryTokenProvider(callback, authority, callbackState);
    }

    /**
     * Creates a Managed Identity token provider. This is a utility method.
     * @return an instance of Managed Identity token provider
     */
    public static TokenProvider createManagedIdentityTokenProvider() {
        return new ManagedIdentityTokenProvider();
    }
}
