package com.azure.core.client;

import com.azure.core.credential.TokenCredential;

/**
 * The interface for client builders that support a {@link TokenCredential}.
 *
 * @param <TBuilder> the type of client builder.
 */
public interface TokenCredentialSupport<TBuilder extends TokenCredentialSupport<TBuilder>> {

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@code TBuilder} object.
     * @throws NullPointerException Might be thrown if {@code tokenCredential} is null.
     */
    TBuilder credential(TokenCredential tokenCredential);
}
