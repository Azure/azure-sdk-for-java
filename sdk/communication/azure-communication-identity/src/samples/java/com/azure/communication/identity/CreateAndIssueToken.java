// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;

public class CreateAndIssueToken {
    public static void main(String[] args) {
        String endpoint = System.getenv("AZURE_COMMUNICATION_ENDPOINT");
        String accessKey = System.getenv("AZURE_COMMUNICATION_KEY");
        AzureKeyCredential keyCredential = new AzureKeyCredential(accessKey);

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .buildClient();
        System.out.println(" builden Cilent ");
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        System.out.println("User id: " + user.getId());

        // Define a list of communication token scopes
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        AccessToken userToken = communicationIdentityClient.getToken(user, scopes);
        System.out.println("User token value: " + userToken.getToken());
        System.out.println("Expires at: " + userToken.getExpiresAt());


    }
}
