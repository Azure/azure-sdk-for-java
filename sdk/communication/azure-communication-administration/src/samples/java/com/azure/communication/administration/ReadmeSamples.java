// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.communication.common.CommunicationUser;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

public class ReadmeSamples {

    /**
     * Sample code for creating a sync Communication Identity Client.
     *
     * @return the Communication Identity Client.
     * @throws NoSuchAlgorithmException if Communcication Client Credential HMAC not available
     * @throws InvalidKeyException if Communcication Client Credential access key is not valid
     */
    public CommunicationIdentityClient createCommunicationIdentityClient() {
        // Your can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        String accessKey = "SECRET";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .endpoint(endpoint)
            .accessKey(accessKey)
            .httpClient(httpClient)
            .buildClient();

        return communicationIdentityClient;
    }

    /**
     * Sample code for creating a user
     *
     * @return the created user
     */
    public CommunicationUser createNewUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUser user = communicationIdentityClient.createUser();
        System.out.println("User id: " + user.getId());
        return user;
    }

    /**
     * Sample code for issuing a user token
     *
     * @return the issued user token
     */
    public CommunicationUserToken issueUserToken() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUser user = communicationIdentityClient.createUser();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken userToken = communicationIdentityClient.issueToken(user, scopes);
        System.out.println("Token: " + userToken.getToken());
        System.out.println("Expires On: " + userToken.getExpiresOn());
        return userToken;
    }

     /**
      * Sample code for revoking user token
      */
    public void revokeUserToken() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUser user = createNewUser();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        communicationIdentityClient.issueToken(user, scopes);
        // revoke tokens issued for the user prior to now
        communicationIdentityClient.revokeTokens(user, OffsetDateTime.now());
    }

    /**
     * Sample code for deleting user
     */
    public void deleteUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUser user = communicationIdentityClient.createUser();
        // delete a previously created user
        communicationIdentityClient.deleteUser(user);
    }
}
