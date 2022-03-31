package com.azure.communication.identity.util;

import com.azure.communication.identity.implementation.models.TeamsUserExchangeTokenRequest;

/** This is a helper class, which provides availability to create required request models */
public class RequestModelCreator {

    /**
     * Creates a request model of type TeamsUserExchangeTokenRequest
     *
     * @param teamsUserAadToken AAD access token of a Teams User to acquire Communication Identity access token.
     * @param appId Client ID of an Azure AD application to be verified against the appId claim in the Azure AD access token.
     * @param userId Object ID of an Azure AD user (Teams User) to be verified against the OID claim in the Azure AD access token.
     * @return TeamsUserExchangeTokenRequest object.
     */
    public static TeamsUserExchangeTokenRequest createTeamsUserExchangeTokenRequest(String teamsUserAadToken, String appId, String userId){
        TeamsUserExchangeTokenRequest requestBody = new TeamsUserExchangeTokenRequest();
        requestBody.setToken(teamsUserAadToken);
        requestBody.setAppId(appId);
        requestBody.setUserId(userId);
        return requestBody;
    }

}
