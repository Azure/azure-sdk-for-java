// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.credentials.oauth;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;

import java.time.OffsetDateTime;

/**
 * <p>
 * Represents an immutable access token with a token string and an expiration time.
 * </p>
 *
 * <p>
 * Access Tokens are obtained through the authentication process, where the user or application presents valid
 * credentials (either a secret or a managed identity) to the authentication source.
 * The authentication source then verifies the credentials and issues an Access Token, which is a time-limited token
 * that grants access to the requested resource.
 * </p>
 *
 * <p>
 * Once an Access Token is obtained, it can be included in the Authorization header of HTTP requests to
 * authenticate and authorize requests.
 * </p>
 *
 * @see io.clientcore.core.credentials
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public class AccessToken {
    private final String token;
    private final OffsetDateTime expiresAt;
    private final OffsetDateTime refreshAt;
    private final AccessTokenType tokenType;

    /**
     * Creates an access token instance.
     * Defaults to {@link AccessTokenType#BEARER} for {@code tokenType}.
     *
     * @param token the token string.
     * @param expiresAt the expiration time.
     */
    public AccessToken(String token, OffsetDateTime expiresAt) {
        this(token, expiresAt, null, AccessTokenType.BEARER);
    }

    /**
     * Creates an access token instance.
     * Defaults to {@link AccessTokenType#BEARER} for {@code tokenType}.
     *
     * @param token the token string.
     * @param expiresAt the expiration time.
     * @param refreshAt the next token refresh time.
     */
    public AccessToken(String token, OffsetDateTime expiresAt, OffsetDateTime refreshAt) {
        this(token, expiresAt, refreshAt, AccessTokenType.BEARER);
    }

    /**
     * Creates an access token instance.
     *
     * @param token the token string.
     * @param expiresAt the expiration time.
     * @param refreshAt the next token refresh time.
     * @param tokenType the type of token.
     */
    public AccessToken(String token, OffsetDateTime expiresAt, OffsetDateTime refreshAt, AccessTokenType tokenType) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.refreshAt = refreshAt;
        this.tokenType = tokenType;
    }

    /**
     * Gets the token.
     *
     * @return The token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the time when the token expires, in UTC.
     *
     * @return The time when the token expires, in UTC.
     */
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Gets the time when the token should refresh next, in UTC.
     * @return The time when the token should refresh next, in UTC.
     */
    public OffsetDateTime getRefreshAt() {
        return refreshAt;
    }

    /**
     * Whether the token has expired.
     *
     * @return Whether the token has expired.
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    /**
     * Gets the token type.
     *
     * @return The {@link AccessTokenType} representing the Token Type.
     */
    public AccessTokenType getTokenType() {
        return tokenType;
    }
}
