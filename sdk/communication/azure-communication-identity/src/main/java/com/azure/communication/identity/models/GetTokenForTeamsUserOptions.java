// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity.models;

import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import  com.azure.communication.identity.CommunicationIdentityClient;

/**
 * Options class for configuring the
 * {@link CommunicationIdentityAsyncClient#getTokenForTeamsUser(GetTokenForTeamsUserOptions)} and
 * {@link CommunicationIdentityClient#getTokenForTeamsUser(GetTokenForTeamsUserOptions)} methods.
 */
public final class GetTokenForTeamsUserOptions {

    private String teamsUserAadToken;
    private String clientId;
    private String userObjectId;

    /**
     * Creates a GetTokenForTeamsUserOptions object
     *
     * @param teamsUserAadToken AAD access token of a Teams User.
     * @param clientId Client ID of an Azure AD application to be verified
     *                 against the appId claim in the Azure AD access token.
     * @param userObjectId Object ID of an Azure AD user (Teams User)
     *                     to be verified against the OID claim in the Azure AD access token.
     */
    public GetTokenForTeamsUserOptions(String teamsUserAadToken, String clientId, String userObjectId) {
        this.teamsUserAadToken = teamsUserAadToken;
        this.clientId = clientId;
        this.userObjectId = userObjectId;
    }

    /**
     * Gets the AAD access token of a Teams User.
     *
     * @return the AAD access token of a Teams User.
     */
    public String getTeamsUserAadToken() {
        return teamsUserAadToken;
    }

    /**
     * Sets the AAD access token of a Teams User.
     *
     * @param teamsUserAadToken the AAD access token of a Teams User.
     * @return the {@link GetTokenForTeamsUserOptions}.
     */
    public GetTokenForTeamsUserOptions setTeamsUserAadToken(String teamsUserAadToken) {
        this.teamsUserAadToken = teamsUserAadToken;
        return this;
    }

    /**
     * Gets the Client ID of an Azure AD application.
     *
     * @return the Client ID of an Azure AD application.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the Client ID of an Azure AD application to be verified
     * against the appId claim in the Azure AD access token.
     *
     * @param clientId the Client ID of an Azure AD application.
     * @return the {@link GetTokenForTeamsUserOptions}.
     */
    public GetTokenForTeamsUserOptions setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Gets the Object ID of an Azure AD user (Teams User).
     *
     * @return the Object ID of an Azure AD user (Teams User).
     */
    public String getUserObjectId() {
        return userObjectId;
    }

    /**
     * Sets the Object ID of an Azure AD user (Teams User)
     * to be verified against the OID claim in the Azure AD access token
     *
     * @param userObjectId the Object ID of an Azure AD user (Teams User).
     * @return the {@link GetTokenForTeamsUserOptions}.
     */
    public GetTokenForTeamsUserOptions setUserObjectId(String userObjectId) {
        this.userObjectId = userObjectId;
        return this;
    }
}
