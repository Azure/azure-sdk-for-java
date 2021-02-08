// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import com.azure.core.annotation.Fluent;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * Custom CommunicationUserToken class used to make CommunicationIdentityToken
 * object include a CommunicationUser as a property. 
 */
@Fluent
public final class CommunicationUserToken {
    /*
     * Identifier of the identity owning the token.
     */
    @JsonProperty(value = "user", required = true)
    private CommunicationUserIdentifier user;

    /*
     * The token issued for the identity.
     */
    @JsonProperty(value = "token", required = true)
    private String token;

    /*
     * The expiry time of the token.
     */
    @JsonProperty(value = "expiresOn", required = true)
    private OffsetDateTime expiresOn;

    /**
     * Get the associated CommunicationUserIdentifier object.
     *
     * @return the CommunicationUserIdentifier object.
     */
    public CommunicationUserIdentifier getUser() {
        return this.user;
    }

    /**
     * Set the CommunicationUserIdentifier object.
     *
     * @param user the id value to set.
     * @return the CommunicationIdentityToken object itself.
     */
    public CommunicationUserToken setUser(CommunicationUserIdentifier user) {
        this.user = user;
        return this;
    }

    /**
     * Get the token property: The token issued for the identity.
     *
     * @return the token value.
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Set the token property: The token issued for the identity.
     *
     * @param token the token value to set.
     * @return the CommunicationIdentityToken object itself.
     */
    public CommunicationUserToken setToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * Get the expiresOn property: The expiry time of the token.
     *
     * @return the expiresOn value.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }

    /**
     * Set the expiresOn property: The expiry time of the token.
     *
     * @param expiresOn the expiresOn value to set.
     * @return the CommunicationIdentityToken object itself.
     */
    public CommunicationUserToken setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }
}
