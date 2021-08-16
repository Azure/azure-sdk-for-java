// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.networktraversal;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.CommunicationIceServer;
import java.util.List;

/**
 * Shows how to get a CommunicationUserIdentifier using CommunicationIdentityClient
 * to later return a relay configuration using CommunicationRelayConfiguration
 * 
 * It iterates over the lis of CommunicationIceServer to print the urls, username and credential
 */
public class CreateAndIssueRelayCredentialsExample {
    public static void main(String[] args) {
        String connectionString = System.getenv("COMMUNICATION_SAMPLES_CONNECTION_STRING");
        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        
        CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        System.out.println("User id: " + user.getId());

        // Define a list of communication token scopes
        CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(user);
        
        System.out.println("Expires on:" + config.getExpiresOn());
        List<CommunicationIceServer> iceServers = config.getIceServers();

        for (CommunicationIceServer iceS : iceServers) {
            System.out.println("URLS: " + iceS.getUrls());
            System.out.println("Username: " + iceS.getUsername());
            System.out.println("credential: " + iceS.getCredential());
        }
    }
}
