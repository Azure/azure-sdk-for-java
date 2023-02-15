// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting;

import com.azure.communication.phonenumbers.SipRoutingAsyncClient;
import com.azure.communication.phonenumbers.SipRoutingClient;
import com.azure.communication.phonenumbers.SipRoutingClientBuilder;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.List;

import static java.util.Arrays.asList;

public class ReadmeSamples {

    /**
     * Sample code for creating a sync SIP Routing Client.
     *
     * @return the SIP Routing Client.
     */
    public SipRoutingClient createSipRoutingClient() {
        // BEGIN: readme-sample-createSipRoutingClient
        // You can find your endpoint and access token from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("SECRET");

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        SipRoutingClient sipRoutingClient = new SipRoutingClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .httpClient(httpClient)
            .buildClient();
        // END: readme-sample-createSipRoutingClient

        return sipRoutingClient;
    }

    /**
     * Sample code for creating an async SIP Routing Client.
     *
     * @return the SIP Routing Client.
     */
    public SipRoutingAsyncClient createSipRoutingAsyncClient() {
        // BEGIN: readme-sample-createSipRoutingAsyncClient
        // You can find your endpoint and access token from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("SECRET");

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        SipRoutingAsyncClient sipRoutingClient = new SipRoutingClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .httpClient(httpClient)
            .buildAsyncClient();
        // END: readme-sample-createSipRoutingAsyncClient

        return sipRoutingClient;
    }

    /**
     * Sample code for creating a sync SIP Routing Client using AAD authentication.
     *
     * @return the SIP Routing Client.
     */
    public SipRoutingClient createSipRoutingClientWithAAD() {
        // BEGIN: readme-sample-createSipRoutingClientWithAAD
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        SipRoutingClient sipRoutingClient = new SipRoutingClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpClient(httpClient)
            .buildClient();
        // END: readme-sample-createSipRoutingClientWithAAD

        return sipRoutingClient;
    }

    /**
     * Sample code for listing SIP trunks and routes.
     */
    public void listTrunksAndRoutes() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: readme-sample-listTrunksAndRoutes
        List<SipTrunk> trunks = sipRoutingClient.listTrunks();
        List<SipTrunkRoute> routes = sipRoutingClient.listRoutes();
        for (SipTrunk trunk : trunks) {
            System.out.println("Trunk " + trunk.getFqdn() + ":" + trunk.getSipSignalingPort());
        }
        for (SipTrunkRoute route : routes) {
            System.out.println("Route name: " + route.getName());
            System.out.println("Route description: " + route.getDescription());
            System.out.println("Route number pattern: " + route.getNumberPattern());
            System.out.println("Route trunks: " + String.join(",", route.getTrunks()));
        }
        // END: readme-sample-listTrunksAndRoutes
    }

    /**
     * Sample code for getting a SIP trunk based on its FQDN.
     */
    public void getTrunk() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: readme-sample-getTrunk
        String fqdn = "<trunk fqdn>";
        SipTrunk trunk = sipRoutingClient.getTrunk(fqdn);
        if (trunk != null) {
            System.out.println("Trunk " + trunk.getFqdn() + ":" + trunk.getSipSignalingPort());
        } else {
            System.out.println("Trunk not found. " + fqdn);
        }
        // END: readme-sample-getTrunk
    }

    /**
     * Sample code for setting SIP trunks and routes.
     */
    public void setTrunksAndRoutes() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: readme-sample-setTrunksAndRoutes
        sipRoutingClient.setTrunks(asList(
            new SipTrunk("<first trunk fqdn>", 12345),
            new SipTrunk("<second trunk fqdn>", 23456)
        ));
        sipRoutingClient.setRoutes(asList(
            new SipTrunkRoute("route name1", ".*9").setTrunks(asList("<first trunk fqdn>", "<second trunk fqdn>")),
            new SipTrunkRoute("route name2", ".*").setTrunks(asList("<second trunk fqdn>"))
        ));
        // END: readme-sample-setTrunksAndRoutes
    }

    /**
     * Sample code for setting a SIP trunk.
     */
    public void setTrunk() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: readme-sample-setTrunk
        sipRoutingClient.setTrunk(new SipTrunk("<trunk fqdn>", 12345));
        // END: readme-sample-setTrunk
    }

    /**
     * Sample code for deleting a SIP trunk.
     */
    public void deleteTrunk() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: readme-sample-deleteTrunk
        sipRoutingClient.deleteTrunk("<trunk fqdn>");
        // END: readme-sample-deleteTrunk
    }
}
