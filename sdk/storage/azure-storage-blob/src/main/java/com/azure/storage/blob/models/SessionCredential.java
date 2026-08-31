// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Represents a session credential used to authenticate blob storage requests via the lightweight,
 * per-container session authentication scheme.
 * <p>
 * This is modeled after {@code com.azure.core.credential.AccessToken}: a small, immutable holder for the
 * session token, session key, and expiration returned by the storage service's CreateSession operation (or
 * by a customer-supplied {@link SessionProvider}). Actual request signing is performed internally using the
 * fixed HMAC scheme the service defines for session authentication; this type only carries the data needed
 * to do so.
 *
 * @see SessionProvider
 */
public final class SessionCredential {

    private final String sessionToken;
    private final String sessionKey;
    private final OffsetDateTime expiresAt;
    private final String accountName;

    /**
     * Creates a new {@link SessionCredential}.
     *
     * @param sessionToken the session token issued by the service (or a custom {@link SessionProvider}).
     * @param sessionKey the Base64-encoded session key used to sign requests.
     * @param expiresAt the instant at which this session credential expires.
     * @param accountName the storage account name this session credential is scoped to.
     * @throws NullPointerException if {@code sessionToken}, {@code sessionKey}, or {@code accountName} is
     * {@code null}.
     */
    public SessionCredential(String sessionToken, String sessionKey, OffsetDateTime expiresAt, String accountName) {
        this.sessionToken = Objects.requireNonNull(sessionToken, "'sessionToken' cannot be null.");
        this.sessionKey = Objects.requireNonNull(sessionKey, "'sessionKey' cannot be null.");
        this.expiresAt = Objects.requireNonNull(expiresAt, "'expiresAt' cannot be null.");
        this.accountName = Objects.requireNonNull(accountName, "'accountName' cannot be null.");
    }

    /**
     * Gets the session token.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Gets the Base64-encoded session key used to sign requests.
     *
     * @return the session key.
     */
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * Gets the instant at which this session credential expires.
     *
     * @return the expiration instant.
     */
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Gets the storage account name this session credential is scoped to.
     *
     * @return the account name.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Gets whether this session credential is expired.
     *
     * @return {@code true} if the current time is after {@link #getExpiresAt()}; {@code false} otherwise.
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}
