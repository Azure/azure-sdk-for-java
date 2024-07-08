// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.credential;

import java.time.OffsetDateTime;

/**
 * <p>
 * Represents an immutable access token with a token string and an expiration time.
 * </p>
 *
 * <p>
 * An Access Token is a security token that is issued by an authentication source, such as
 * Azure Active Directory (AAD), and it represents the authorization to access a specific resource or service.
 * It is typically used to authenticate and authorize requests made to Azure services.
 * </p>
 *
 * <p>
 * Access Tokens are obtained through the authentication process, where the user or application presents valid
 * credentials (such as a client ID, client secret, username/password, or certificate) to the authentication source.
 * The authentication source then verifies the credentials and issues an Access Token, which is a time-limited token
 * that grants access to the requested resource.
 * </p>
 *
 * <p>
 * Once an Access Token is obtained, it can be included in the Authorization header of HTTP requests to
 * authenticate and authorize requests to Azure services.
 * </p>
 *
 * @see com.azure.core.credential
 * @see com.azure.core.v2.credential.TokenCredential
 */
public class AccessToken {
    private final String token;
    private final OffsetDateTime expiresAt;

    /**
     * Creates an access token instance.
     *
     * @param token the token string.
     * @param expiresAt the expiration time.
     */
    public AccessToken(String token, OffsetDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
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
     * Whether the token has expired.
     *
     * @return Whether the token has expired.
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}
