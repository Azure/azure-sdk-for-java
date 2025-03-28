package io.clientcore.core.traits;

import io.clientcore.core.credentials.oauth.OAuthTokenCredential;

public interface OAuthTokenCredentialTrait<T extends OAuthTokenCredentialTrait<T>>{
    /**
     * Sets the {@link OAuthTokenCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link OAuthTokenCredential} used to authorize requests sent to the service.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T credential(OAuthTokenCredential credential);
}
