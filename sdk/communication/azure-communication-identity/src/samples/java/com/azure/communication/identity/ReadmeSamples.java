// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
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
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .buildClient();

        return communicationIdentityClient;
    }

    /**
     * Sample code for creating a sync Communication Identity Client using connection string.
     *
     * @return the Communication Identity Client.
     */
    public CommunicationIdentityClient createCommunicationIdentityClientWithConnectionString() {
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "<connection_string>";

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        return communicationIdentityClient;
    }

    /**
     * Sample code for creating a sync Communication Identity Client using AAD authentication.
     *
     * @return the Communication Identity Client.
     */
    public CommunicationIdentityClient createCommunicationIdentityClientWithAAD() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        return communicationIdentityClient;
    }

    /**
     * Sample code for creating a user
     *
     * @return the created user
     */
    public CommunicationUserIdentifier createNewUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        System.out.println("User id: " + user.getId());
        return user;
    }

    /**
     * Sample code for creating a user with token
     *
     * @return the result with the created user and token
     */
    public CommunicationUserIdentifierAndToken createNewUserAndToken() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        // Define a list of communication token scopes
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        CommunicationUserIdentifierAndToken result = communicationIdentityClient.createUserAndToken(scopes);
        System.out.println("User id: " + result.getUser().getId());
        System.out.println("User token value: " + result.getUserToken().getToken());
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
         // Define a list of communication token scopes
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        AccessToken userToken = communicationIdentityClient.getToken(user, scopes);
        System.out.println("User token value: " + userToken.getToken());
        System.out.println("Expires at: " + userToken.getExpiresAt());
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
        // revoke tokens issued for the specified user
        communicationIdentityClient.revokeTokens(user);
    }

    /**
     * Sample code for deleting user
     */
    public void deleteUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        // delete a previously created user
        communicationIdentityClient.deleteUser(user);
    }

    /**
     * Sample code for exchanging an AAD access token of a Teams User for a new Communication Identity access token.
     */
    public void getTokenForTeamsUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        try {
            String teamsUserAadToken = generateTeamsUserAadToken();
            AccessToken accessToken = communicationIdentityClient.getTokenForTeamsUser(teamsUserAadToken);
            System.out.println("User token value: " + accessToken.getToken());
            System.out.println("Expires at: " + accessToken.getExpiresAt());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Sample code for generating an AAD access token of a Teams User
     */
    private static String generateTeamsUserAadToken() throws MalformedURLException, ExecutionException, InterruptedException {
        String teamsUserAadToken = "";
        try {
            IPublicClientApplication publicClientApplication = PublicClientApplication.builder("<M365_APP_ID>")
                .authority("<M365_AAD_AUTHORITY>" + "/" + "<M365_AAD_TENANT>")
                .build();
            //M365 scopes
            Set<String> scopes = Collections.singleton("https://auth.msft.communication.azure.com/VoIP");
            char[] password = "<MSAL_PASSWORD>".toCharArray();
            UserNamePasswordParameters userNamePasswordParameters =  UserNamePasswordParameters.builder(scopes, "<MSAL_USERNAME>", password)
                .build();
            Arrays.fill(password, '0');
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
        try {
            CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
