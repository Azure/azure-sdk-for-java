// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.webpubsub.models;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.azure.messaging.webpubsub.WebPubSubAsyncServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;

/**
 * Options class for configuring the
 * {@link WebPubSubAsyncServiceClient#getAuthenticationToken(GetAuthenticationTokenOptions)} and
 * {@link WebPubSubServiceClient#getAuthenticationToken(GetAuthenticationTokenOptions)} methods.
 */
public final class GetAuthenticationTokenOptions {
    private Duration expiresAfter;
    private Map<String, Object> claims;
    private String userId;

    /**
     * Specifies when the duration after which the requested authentication token will expire.
     *
     * @param expiresAfter The duration after which the requested authentication token will expire.
     * @return The same instance of this type, modified based on the value provided in this set method.
     */
    public GetAuthenticationTokenOptions setExpiresAfter(final Duration expiresAfter) {
        this.expiresAfter = expiresAfter;
        return this;
    }

    /**
     * Returns the duration after which the requested authentication token will expire.
     * @return The duration after which the requested authentication token will expire.
     */
    public Duration getExpiresAfter() {
        return expiresAfter;
    }

    /**
     * Adds a new claim to any existing claims set on this instance. Previously set claims will not be removed, however,
     * if a claim exists with the given key, it will be replaced by the provided value.
     * @param key The claim key.
     * @param value The claim value for the given key.
     * @return The same instance of this type, modified based on the value provided in this set method.
     */
    public GetAuthenticationTokenOptions addClaim(String key, Object value) {
        if (claims == null) {
            this.claims = new HashMap<>();
        }
        this.claims.put(key, value);
        return this;
    }

    /**
     * Specifies the complete set of claims to be included when creating the authentication token, overwriting any other
     * claims previously set on this instance.
     *
     * @param claims The complete set of claims to be included when creating the authentication token.
     * @return The same instance of this type, modified based on the value provided in this set method.
     */
    public GetAuthenticationTokenOptions setClaims(final Map<String, Object> claims) {
        this.claims = claims;
        return this;
    }

    /**
     * Returns the complete set of claims to be included when creating the authentication token.
     * @return The complete set of claims to be included when creating the authentication token
     */
    public Map<String, Object> getClaims() {
        return claims == null ? Collections.emptyMap() : claims;
    }

    /**
     * Specifies the user ID to be used when creating the authentication token.
     *
     * @param userId The user ID to be used when creating the authentication token.
     * @return The same instance of this type, modified based on the value provided in this set method.
     */
    public GetAuthenticationTokenOptions setUserId(final String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Returns the user ID to be used when creating the authentication token.
     * @return The user ID to be used when creating the authentication token.
     */
    public String getUserId() {
        return userId;
    }
}
