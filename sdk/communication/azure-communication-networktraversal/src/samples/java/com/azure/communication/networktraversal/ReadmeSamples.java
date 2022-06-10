// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.networktraversal;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.RouteType;
import com.azure.communication.networktraversal.models.CommunicationIceServer;
import com.azure.communication.networktraversal.models.GetRelayConfigurationOptions;
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
        // BEGIN: readme-sample-createCommunicationNetworkTraversalClient
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

        CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .buildClient();
        // END: readme-sample-createCommunicationNetworkTraversalClient

        return communicationRelayClient;
    }

    /**
     * Sample code for creating a Relay Client Builder
     *
     * @return the Communication Relay Client Builder
     */
    public CommunicationRelayClientBuilder createCommunicationNetworkTraversalClientBuilder() {
        // BEGIN: readme-sample-createCommunicationNetworkTraversalClientBuilder
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

        CommunicationRelayClientBuilder communicationRelayClientBuilder = new CommunicationRelayClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential);
        // END: readme-sample-createCommunicationNetworkTraversalClientBuilder

        return communicationRelayClientBuilder;
    }

    /**
     * Sample code for creating a sync Communication Network Traversal Client.
     *
     * @return the Communication Relay Async Client.
     */
    public CommunicationRelayAsyncClient createCommunicationNetworkTraversalAsyncClient() {
        // BEGIN: readme-sample-createCommunicationNetworkTraversalAsyncClient
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

        CommunicationRelayAsyncClient communicationRelayClient = new CommunicationRelayClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .buildAsyncClient();
        // END: readme-sample-createCommunicationNetworkTraversalAsyncClient

        return communicationRelayClient;
    }

    /**
     * Sample code for creating a sync Communication Relay Client using connection string.
     *
     * @return the Communication Relay Client.
     */
    public CommunicationRelayClient createCommunicationRelayClientWithConnectionString() {
        // BEGIN: readme-sample-createCommunicationRelayClientWithConnectionString
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "<connection_string>";

        CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: readme-sample-createCommunicationRelayClientWithConnectionString

        return communicationRelayClient;
    }

    /**
     * Sample code for creating a sync Communication Relay Client using AAD authentication.
     *
     * @return the Communication Relay Client.
     */
    public CommunicationRelayClient createCommunicationRelayClientWithAAD() {
        // BEGIN: readme-sample-createCommunicationRelayClientWithAAD
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createCommunicationRelayClientWithAAD

        return communicationRelayClient;
    }

    /**
     * Sample code for getting a relay configuration
     *
     * @return the created user
     */
    public CommunicationRelayConfiguration getRelayConfiguration() {
        // BEGIN: readme-sample-getRelayConfiguration
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();

        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        System.out.println("User id: " + user.getId());

        GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
        options.setCommunicationUserIdentifier(user);

        CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
        CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(options);

        System.out.println("Expires on:" + config.getExpiresOn());
        List<CommunicationIceServer> iceServers = config.getIceServers();

        for (CommunicationIceServer iceS : iceServers) {
            System.out.println("URLS: " + iceS.getUrls());
            System.out.println("Username: " + iceS.getUsername());
            System.out.println("Credential: " + iceS.getCredential());
            System.out.println("RouteType: " + iceS.getRouteType());
        }
        // END: readme-sample-getRelayConfiguration
        return config;
    }

    /**
     * Sample code for getting a relay configuration without identity
     *
     * @return the created user
     */
    public CommunicationRelayConfiguration getRelayConfigurationWithoutIdentity() {
        // BEGIN: readme-sample-getRelayConfigurationWithoutIdentity
        CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
        CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration();

        System.out.println("Expires on:" + config.getExpiresOn());
        List<CommunicationIceServer> iceServers = config.getIceServers();

        for (CommunicationIceServer iceS : iceServers) {
            System.out.println("URLS: " + iceS.getUrls());
            System.out.println("Username: " + iceS.getUsername());
            System.out.println("Credential: " + iceS.getCredential());
            System.out.println("RouteType: " + iceS.getRouteType());
        }
        // END: readme-sample-getRelayConfigurationWithoutIdentity
        return config;
    }

    /**
     * Sample code for getting a relay configuration providing RouteType
     *
     * @return the created user
     */
    public CommunicationRelayConfiguration getRelayConfigurationWithRouteType() {
        // BEGIN: readme-sample-getRelayConfigurationWithRouteType

        GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
        options.setRouteType(RouteType.ANY);

        CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
        CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(options);

        System.out.println("Expires on:" + config.getExpiresOn());
        List<CommunicationIceServer> iceServers = config.getIceServers();

        for (CommunicationIceServer iceS : iceServers) {
            System.out.println("URLS: " + iceS.getUrls());
            System.out.println("Username: " + iceS.getUsername());
            System.out.println("Credential: " + iceS.getCredential());
            System.out.println("RouteType: " + iceS.getRouteType());
        }
        // END: readme-sample-getRelayConfigurationWithRouteType
        return config;
    }

    /**
     * Sample code for troubleshooting
     */
    public void createUserTroubleshooting() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();

        // BEGIN: readme-sample-createUserTroubleshooting
        try {
            CommunicationUserIdentifier user = communicationIdentityClient.createUser();
            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            options.setCommunicationUserIdentifier(user);

            CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
            CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(options);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
        // END: readme-sample-createUserTroubleshooting
    }
}
