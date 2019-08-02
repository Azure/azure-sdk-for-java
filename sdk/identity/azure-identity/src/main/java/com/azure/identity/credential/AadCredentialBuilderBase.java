// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

/**
 * The base class for credential builders that allow specifying a client id and tenant ID for an Azure Active Directory.
 * @param <T> the type of the credential builder
 */
public abstract class AadCredentialBuilderBase<T extends AadCredentialBuilderBase<T>> extends CredentialBuilderBase<T> {
    String clientId;

    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return {@link <T>} itself
     */
    @SuppressWarnings("unchecked")
    public T authorityHost(String authorityHost) {
        this.identityClientOptions.authorityHost(authorityHost);
        return (T) this;
    }

    /**
     * Sets the client ID of the application.
     * @param clientId the client ID of the application.
     * @return {@link <T>} itself
     */
    @SuppressWarnings("unchecked")
    public T clientId(String clientId) {
        this.clientId = clientId;
        return (T) this;
    }
}
