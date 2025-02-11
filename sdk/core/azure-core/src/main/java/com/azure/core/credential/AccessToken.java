// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import java.time.Duration;
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
 * @see com.azure.core.credential.TokenCredential
 */
public class AccessToken {
    private final String token;
    private final OffsetDateTime expiresAt;
    private final OffsetDateTime refreshAt;
    private final String tokenType;

    /**
     * Creates an access token instance.
     *
     * @param token the token string.
     * @param expiresAt the expiration time.
     */
    public AccessToken(String token, OffsetDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.refreshAt = null;
        this.tokenType = "Bearer";
    }

    /**
     * Creates an access token instance.
     *
     * @param token the token string.
     * @param expiresAt the expiration time.
     * @param refreshAt the next token refresh time.
     */
    public AccessToken(String token, OffsetDateTime expiresAt, OffsetDateTime refreshAt) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.refreshAt = refreshAt;
        this.tokenType = "Bearer";
    }

    /**
     * Creates an access token instance.
     *
     * @param token the token string.
     * @param expiresAt the expiration time.
     * @param refreshAt the next token refresh time.
     * @param tokenType the type of token.
     */
    public AccessToken(String token, OffsetDateTime expiresAt, OffsetDateTime refreshAt, String tokenType) {
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
     *
     * <p>Note: This value can be null as it is not always provided by the service. When it is provided,
     * it overrides the default refresh offset used by the
     * {@link com.azure.core.http.policy.BearerTokenAuthenticationPolicy} to proactively refresh the token.</p>
     *
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
     * @return A string representing the token type. It can be "Bearer" or "Pop".
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Gets the {@link Duration} until the {@link AccessToken} expires.
     * <p>
     * The {@link Duration} is based on the {@link OffsetDateTime#now() current time} and may return a negative
     * {@link Duration}, indicating that the {@link AccessToken} has expired.
     *
     * @return The {@link Duration} until the {@link AccessToken} expires.
     */
    public Duration getDurationUntilExpiration() {
        // Call Duration.between with the 'cache.getExpiresAt' as the start Temporal and 'now' as the end Temporal as
        // some TokenCredential implementations may use 'OffsetDateTime.MAX' as the expiration time. When comparing the
        // time between now and 'OffsetDateTime.MAX', depending on the Java version, it may attempt to change the end
        // Temporal's time zone to match the start Temporal's time zone. Since 'OffsetDateTime.MAX' uses the most
        // minimal time zone offset, if the now time is using anything before that it will result in
        // 'OffsetDateTime.MAX' needing to roll over its time to the next day which results in the 'year' value being
        // incremented to a value outside the 'year' bounds allowed by OffsetDateTime.
        //
        // Changing to having the 'cache.getExpiresAt' means it is impossible for this rollover to occur as the start
        // Temporal doesn't have its time zone modified. But it now means the time between value is inverted, so the
        // result is 'negated()' to maintain current behaviors.
        return Duration.between(expiresAt, OffsetDateTime.now()).negated();
    }
}
