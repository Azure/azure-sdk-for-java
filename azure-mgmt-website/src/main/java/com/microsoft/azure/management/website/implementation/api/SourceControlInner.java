/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Describes the Source Control OAuth Token.
 */
@JsonFlatten
public class SourceControlInner extends Resource {
    /**
     * Name or Source Control Type.
     */
    @JsonProperty(value = "properties.name")
    private String sourceControlName;

    /**
     * OAuth Access Token.
     */
    @JsonProperty(value = "properties.token")
    private String token;

    /**
     * OAuth Access Token Secret.
     */
    @JsonProperty(value = "properties.tokenSecret")
    private String tokenSecret;

    /**
     * OAuth Refresh Token.
     */
    @JsonProperty(value = "properties.refreshToken")
    private String refreshToken;

    /**
     * OAuth Token Expiration.
     */
    @JsonProperty(value = "properties.expirationTime")
    private DateTime expirationTime;

    /**
     * Get the sourceControlName value.
     *
     * @return the sourceControlName value
     */
    public String sourceControlName() {
        return this.sourceControlName;
    }

    /**
     * Set the sourceControlName value.
     *
     * @param sourceControlName the sourceControlName value to set
     * @return the SourceControlInner object itself.
     */
    public SourceControlInner withSourceControlName(String sourceControlName) {
        this.sourceControlName = sourceControlName;
        return this;
    }

    /**
     * Get the token value.
     *
     * @return the token value
     */
    public String token() {
        return this.token;
    }

    /**
     * Set the token value.
     *
     * @param token the token value to set
     * @return the SourceControlInner object itself.
     */
    public SourceControlInner withToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * Get the tokenSecret value.
     *
     * @return the tokenSecret value
     */
    public String tokenSecret() {
        return this.tokenSecret;
    }

    /**
     * Set the tokenSecret value.
     *
     * @param tokenSecret the tokenSecret value to set
     * @return the SourceControlInner object itself.
     */
    public SourceControlInner withTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
        return this;
    }

    /**
     * Get the refreshToken value.
     *
     * @return the refreshToken value
     */
    public String refreshToken() {
        return this.refreshToken;
    }

    /**
     * Set the refreshToken value.
     *
     * @param refreshToken the refreshToken value to set
     * @return the SourceControlInner object itself.
     */
    public SourceControlInner withRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    /**
     * Get the expirationTime value.
     *
     * @return the expirationTime value
     */
    public DateTime expirationTime() {
        return this.expirationTime;
    }

    /**
     * Set the expirationTime value.
     *
     * @param expirationTime the expirationTime value to set
     * @return the SourceControlInner object itself.
     */
    public SourceControlInner withExpirationTime(DateTime expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

}
