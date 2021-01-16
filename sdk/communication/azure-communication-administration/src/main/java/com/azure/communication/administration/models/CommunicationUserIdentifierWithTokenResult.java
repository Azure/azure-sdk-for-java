// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration.models;

import com.azure.communication.common.CommunicationUserIdentifier;

/** The CommunicationUserIdentifierWithAccessTokenResult model. */
public final class CommunicationUserIdentifierWithTokenResult {

    private final CommunicationUserIdentifier communicationUser;
    private final CommunicationUserToken userToken;

    /**
     * Creates a CommunicationUserIdentifierWithTokenResult object
     * 
     * @param communicationUser the communication user identifier
     * @param userToken the user token of the communication user
     * @throws IllegalArgumentException thrown if id parameter fail the validation.
     */
    public CommunicationUserIdentifierWithTokenResult(CommunicationUserIdentifier communicationUser, CommunicationUserToken userToken)
    {
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
    public CommunicationUserToken getUserToken() {
        return this.userToken;
    }
}
