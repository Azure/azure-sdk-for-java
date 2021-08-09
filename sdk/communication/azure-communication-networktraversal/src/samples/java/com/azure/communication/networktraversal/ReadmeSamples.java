// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.networktraversal;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.CommunicationIceServer;
import java.util.List;

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
     * Sample code for creating a sync Communication Network Traversal Client.
     *
     * @return the Communication Relay Client.
     */
    public CommunicationRelayClient createCommunicationNetworkTraversalClient() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

        CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .buildClient();

        return communicationRelayClient;
    }

    /**
     * Sample code for creating a sync Communication Relay Client using connection string.
     *
     * @return the Communication Relay Client.
     */
    public CommunicationRelayClient createCommunicationRelayClientWithConnectionString() {
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "<connection_string>";

        CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        return communicationRelayClient;
    }

    /**
     * Sample code for creating a sync Communication Relay Client using AAD authentication.
     *
     * @return the Communication Relay Client.
     */
    public CommunicationRelayClient createCommunicationRelayClientWithAAD() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        return communicationRelayClient;
    }

    /**
     * Sample code for getting a relay configuration
     *
     * @return the created user
     */
    public CommunicationRelayConfiguration getRelayConfiguration() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        System.out.println("User id: " + user.getId());

        CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
        CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(user);
        
        System.out.println("Expires on:" + config.getExpiresOn());
        List<CommunicationIceServer> iceServers = config.getIceServers();

        for (CommunicationIceServer iceS : iceServers) {
            System.out.println("URLS: " + iceS.getUrls());
            System.out.println("Username: " + iceS.getUsername());
            System.out.println("credential: " + iceS.getCredential());
        } 
        return config;
    }

    /**
     * Sample code for troubleshooting
     */
    public void createUserTroubleshooting() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        try {
            CommunicationUserIdentifier user = communicationIdentityClient.createUser();
            CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
            CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(user);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
