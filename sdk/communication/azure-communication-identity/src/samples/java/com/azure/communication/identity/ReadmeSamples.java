// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
import com.azure.communication.identity.models.GetTokenForTeamsUserOptions;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IPublicClientApplication;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;

public class ReadmeSamples {
    /**
     * Sample code for creating a sync Communication Identity Client.
     *
     * @return the Communication Identity Client.
     */
    public CommunicationIdentityClient createCommunicationIdentityClient() {
        // BEGIN: readme-sample-createCommunicationIdentityClient
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .buildClient();
        // END: readme-sample-createCommunicationIdentityClient

        return communicationIdentityClient;
    }

    /**
     * Sample code for creating an async Communication Identity Client.
     *
     * @return the Communication Identity Async Client.
     */
    public CommunicationIdentityAsyncClient createCommunicationIdentityAsyncClient() {
        // BEGIN: readme-sample-createCommunicationIdentityAsyncClient
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

        CommunicationIdentityAsyncClient communicationIdentityAsyncClient = new CommunicationIdentityClientBuilder()
                .endpoint(endpoint)
                .credential(keyCredential)
                .buildAsyncClient();
        // END: readme-sample-createCommunicationIdentityAsyncClient

        return communicationIdentityAsyncClient;
    }

    /**
     * Sample code for creating a sync Communication Identity Client using connection string.
     *
     * @return the Communication Identity Client.
     */
    public CommunicationIdentityClient createCommunicationIdentityClientWithConnectionString() {
        // BEGIN: readme-sample-createCommunicationIdentityClientWithConnectionString
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "<connection_string>";

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: readme-sample-createCommunicationIdentityClientWithConnectionString

        return communicationIdentityClient;
    }

    /**
     * Sample code for creating a sync Communication Identity Client using Azure AD authentication.
     *
     * @return the Communication Identity Client.
     */
    public CommunicationIdentityClient createCommunicationIdentityClientWithAAD() {
        // BEGIN: readme-sample-createCommunicationIdentityClientWithAAD
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createCommunicationIdentityClientWithAAD

        return communicationIdentityClient;
    }

    /**
     * Sample code for creating a user
     *
     * @return the created user
     */
    public CommunicationUserIdentifier createNewUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();

        // BEGIN: readme-sample-createNewUser
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        System.out.println("User id: " + user.getId());
        // END: readme-sample-createNewUser

        return user;
    }

    /**
     * Sample code for creating a user together with token with custom expiration
     *
     * @return the result with the created user and token with custom expiration
     */
    public CommunicationUserIdentifierAndToken createNewUserAndTokenWithCustomExpiration() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();

        // BEGIN: readme-sample-createNewUserAndTokenWithCustomExpiration
        // Define a list of communication token scopes
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        // Set custom validity period of the Communication Identity access token within [1,24]
        // hours range. If not provided, the default value of 24 hours will be used.
        Duration tokenExpiresIn = Duration.ofHours(1);
        CommunicationUserIdentifierAndToken result = communicationIdentityClient.createUserAndToken(scopes, tokenExpiresIn);
        System.out.println("User id: " + result.getUser().getId());
        System.out.println("User token value: " + result.getUserToken().getToken());
        // END: readme-sample-createNewUserAndTokenWithCustomExpiration

        return result;
    }

    /**
     * Sample code for creating a user with token
     *
     * @return the result with the created user and token
     */
    public CommunicationUserIdentifierAndToken createNewUserAndToken() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();

        // BEGIN: readme-sample-createNewUserAndToken
        // Define a list of communication token scopes
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        CommunicationUserIdentifierAndToken result = communicationIdentityClient.createUserAndToken(scopes);
        System.out.println("User id: " + result.getUser().getId());
        System.out.println("User token value: " + result.getUserToken().getToken());
        // END: readme-sample-createNewUserAndToken

        return result;
    }

    /**
     * Sample code for issuing a user token
     *
     * @return the issued user token
     */
    public AccessToken issueUserToken() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();

        // BEGIN: readme-sample-issueUserToken
         // Define a list of communication token scopes
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        AccessToken userToken = communicationIdentityClient.getToken(user, scopes);
        System.out.println("User token value: " + userToken.getToken());
        System.out.println("Expires at: " + userToken.getExpiresAt());
        // END: readme-sample-issueUserToken

        return userToken;
    }

    /**
     * Sample code for issuing a Communication Identity access token with custom expiration
     *
     * @return the Communication Identity access token
     */
    public AccessToken issueTokenWithCustomExpiration() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();

        // BEGIN: readme-sample-issueTokenWithCustomExpiration
        // Define a list of Communication Identity access token scopes
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        // Set custom validity period of the Communication Identity access token within [1,24]
        // hours range. If not provided, the default value of 24 hours will be used.
        Duration tokenExpiresIn = Duration.ofHours(1);
        AccessToken userToken = communicationIdentityClient.getToken(user, scopes, tokenExpiresIn);
        System.out.println("User token value: " + userToken.getToken());
        System.out.println("Expires at: " + userToken.getExpiresAt());
        // END: readme-sample-issueTokenWithCustomExpiration

        return userToken;
    }

     /**
      * Sample code for revoking user token
      */
    public void revokeUserToken() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUserIdentifier user = createNewUser();
        // Define a list of communication token scopes
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        communicationIdentityClient.getToken(user, scopes);

        // BEGIN: readme-sample-revokeUserToken
        // revoke tokens issued for the specified user
        communicationIdentityClient.revokeTokens(user);
        // END: readme-sample-revokeUserToken
    }

    /**
     * Sample code for deleting user
     */
    public void deleteUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();

        // BEGIN: readme-sample-deleteUser
        // delete a previously created user
        communicationIdentityClient.deleteUser(user);
        // END: readme-sample-deleteUser
    }

    /**
     * Sample code for exchanging an Azure AD access token of a Teams User for a new Communication Identity access token.
     */
    public void getTokenForTeamsUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        try {
            String teamsUserAadToken = getTeamsUserAadToken();
            // BEGIN: readme-sample-getTokenForTeamsUser
            String clientId = "<Client ID of an Azure AD application>";
            String userObjectId = "<Object ID of an Azure AD user (Teams User)>";
            GetTokenForTeamsUserOptions options = new GetTokenForTeamsUserOptions(teamsUserAadToken, clientId, userObjectId);
            AccessToken accessToken = communicationIdentityClient.getTokenForTeamsUser(options);
            System.out.println("User token value: " + accessToken.getToken());
            System.out.println("Expires at: " + accessToken.getExpiresAt());
            // END: readme-sample-getTokenForTeamsUser
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Sample code for getting an Azure AD access token of a Teams User
     */
    private static String getTeamsUserAadToken() throws MalformedURLException, ExecutionException, InterruptedException {
        String teamsUserAadToken = "";
        try {
            IPublicClientApplication publicClientApplication = PublicClientApplication.builder("<M365_APP_ID>")
                .authority("<M365_AAD_AUTHORITY>" + "/" + "<M365_AAD_TENANT>")
                .build();

            // Create request parameters object for acquiring the AAD token and object ID of a Teams user
            Set<String> scopes = new HashSet<String>(Arrays.asList(
                    "https://auth.msft.communication.azure.com/Teams.ManageCalls",
                    "https://auth.msft.communication.azure.com/Teams.ManageChats"
            ));
            char[] password = "<MSAL_PASSWORD>".toCharArray();
            UserNamePasswordParameters userNamePasswordParameters =  UserNamePasswordParameters.builder(scopes, "<MSAL_USERNAME>", password)
                .build();
            Arrays.fill(password, '0');
            // Retrieve the AAD token and object ID of a Teams user
            IAuthenticationResult result = publicClientApplication.acquireToken(userNamePasswordParameters).get();
            teamsUserAadToken = result.accessToken();
        } catch (Exception e) {
            throw e;
        }
        return teamsUserAadToken;
    }

    /**
     * Sample code for troubleshooting
     */
    public void createUserTroubleshooting() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();

        // BEGIN: readme-sample-createUserTroubleshooting
        try {
            CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
        // END: readme-sample-createUserTroubleshooting
    }
}
