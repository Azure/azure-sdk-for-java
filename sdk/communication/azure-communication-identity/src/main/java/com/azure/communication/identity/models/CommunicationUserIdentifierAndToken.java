// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity.models;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.credential.AccessToken;

/** The CommunicationUserIdentifierWithAccessTokenResult model. */
public final class CommunicationUserIdentifierAndToken {

    private final CommunicationUserIdentifier communicationUser;
    private final AccessToken userToken;

    /**
     * Creates a CommunicationUserIdentifierAndToken object
     * 
     * @param communicationUser the communication user identifier
     * @param userToken the user token of the communication user
     * @throws IllegalArgumentException thrown if id parameter fail the validation.
     */
    public CommunicationUserIdentifierAndToken(CommunicationUserIdentifier communicationUser,
        AccessToken userToken) {
        this.communicationUser = communicationUser;
        this.userToken = userToken;
    }

    /**
     * Get the communicationUser property: A Communication User Identifier.
     *
     * @return the communicationUser value.
     */
    public CommunicationUserIdentifier getUser() {
        return this.communicationUser;
    }

    /**
     * Get the userToken property: A user token.
     *
     * @return the userToken value.
     */
    public AccessToken getUserToken() {
        return this.userToken;
    }
}
