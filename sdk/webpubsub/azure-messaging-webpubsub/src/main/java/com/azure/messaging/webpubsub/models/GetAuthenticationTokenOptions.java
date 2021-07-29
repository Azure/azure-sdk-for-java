// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.webpubsub.models;

import com.azure.messaging.webpubsub.WebPubSubServiceAsyncClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Options class for configuring the
 * {@link WebPubSubServiceAsyncClient#getAuthenticationToken(GetAuthenticationTokenOptions)} and
 * {@link WebPubSubServiceClient#getAuthenticationToken(GetAuthenticationTokenOptions)} methods.
 */
public final class GetAuthenticationTokenOptions {
    private Duration expiresAfter;
    private String userId;
    private List<String> roles;

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
     * Adds a role to the requested authentication token.
     *
     * @param role The role to be added to the requested authentication token.
     * @return The same instance of this type, modified based on the value provided in this add method.
     */
    public GetAuthenticationTokenOptions addRole(String role) {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        roles.add(role);
        return this;
    }
    /**
     * Specifies the complete set of roles to be included when creating the authentication token, overwriting any other
     * roles previously set on this instance.
     *
     * @param roles The complete set of roles to be included when creating the authentication token.
     * @return The same instance of this type, modified based on the value provided in this set method.
     */
    public GetAuthenticationTokenOptions setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    /**
     * Returns the complete set of roles to be included when creating the authentication token.
     * @return The complete set of roles to be included when creating the authentication token
     */
    public List<String> getRoles() {
        return roles == null ? Collections.emptyList() : roles;
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
