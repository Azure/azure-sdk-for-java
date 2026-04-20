// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.time.OffsetDateTime;

/**
 * Contains the results of a Create Session operation on a blob container.
 * <p>
 * A session provides temporary credentials (a session token and session key) scoped to a container
 * that can be used to sign subsequent requests using the HMAC Shared Key protocol, amortizing
 * authentication and authorization cost across many requests.
 */
public final class BlobContainerSessionInfo {

    private final String sessionId;
    private final OffsetDateTime expiration;
    private final String sessionToken;
    private final String sessionKey;

    /**
     * Creates a new {@link BlobContainerSessionInfo}.
     *
     * @param sessionId A unique identifier for the created session.
     * @param expiration The time when the session will expire.
     * @param sessionToken An opaque token used to authorize subsequent requests in the session.
     * @param sessionKey A symmetric key used to sign requests in the session using the Shared Key protocol.
     */
    public BlobContainerSessionInfo(String sessionId, OffsetDateTime expiration, String sessionToken,
        String sessionKey) {
        this.sessionId = sessionId;
        this.expiration = expiration;
        this.sessionToken = sessionToken;
        this.sessionKey = sessionKey;
    }

    /**
     * Gets the unique identifier for the session.
     *
     * @return the session ID.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the time when the session will expire.
     *
     * @return the expiration time.
     */
    public OffsetDateTime getExpiration() {
        return expiration;
    }

    /**
     * Gets the opaque token used to authorize subsequent requests in the session.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Gets the symmetric key used to sign requests in the session using the Shared Key protocol.
     *
     * @return the session key.
     */
    public String getSessionKey() {
        return sessionKey;
    }
}
