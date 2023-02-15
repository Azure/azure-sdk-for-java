// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting;

import com.azure.communication.phonenumbers.SipRoutingAsyncClient;
import com.azure.communication.phonenumbers.SipRoutingClientBuilder;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;

import static java.util.Arrays.asList;

public class AsyncClientJavaDocCodeSnippets {

    /**
     * Sample code for creating an async SIP Routing Client using connection string.
     *
     * @return the SIP Routing Client.
     */
    public SipRoutingAsyncClient createSipRoutingAsyncClient() {
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "endpoint=https://RESOURCE_NAME.communication.azure.com/;accesskey=SECRET";

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.instantiation
        SipRoutingAsyncClient sipRoutingAsyncClient = new SipRoutingClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.instantiation

        return sipRoutingAsyncClient;
    }

    /**
     * Sample code for listing SIP trunks.
     */
    public void listTrunks() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.listTrunks
        sipRoutingAsyncClient.listTrunks()
            .subscribe(trunks -> trunks.forEach(trunk ->
                System.out.println("Trunk " + trunk.getFqdn() + ":" + trunk.getSipSignalingPort())));
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.listTrunks
    }

    /**
     * Sample code for listing SIP trunks with response.
     */
    public void listTrunksWithResponse() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.listTrunksWithResponse
        sipRoutingAsyncClient.listTrunksWithResponse()
            .subscribe(response -> response.getValue().forEach(trunk ->
                System.out.println("Trunk " + trunk.getFqdn() + ":" + trunk.getSipSignalingPort())));
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.listTrunksWithResponse
    }

    /**
     * Sample code for listing SIP routing routes.
     */
    public void listRoutes() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.listRoutes
        sipRoutingAsyncClient.listRoutes().subscribe(routes -> routes.forEach(route -> {
            System.out.println("Route name: " + route.getName());
            System.out.println("Route description: " + route.getDescription());
            System.out.println("Route number pattern: " + route.getNumberPattern());
            System.out.println("Route trunks: " + String.join(",", route.getTrunks()));
        }));
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.listRoutes
    }

    /**
     * Sample code for listing SIP routing routes with response.
     */
    public void listRoutesWithResponse() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.listRoutesWithResponse
        sipRoutingAsyncClient.listRoutesWithResponse()
            .subscribe(response -> response.getValue().forEach(route -> {
                System.out.println("Route name: " + route.getName());
                System.out.println("Route description: " + route.getDescription());
                System.out.println("Route number pattern: " + route.getNumberPattern());
                System.out.println("Route trunks: " + String.join(",", route.getTrunks()));
            }));
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.listRoutesWithResponse
    }

    /**
     * Sample code for getting a SIP trunk based on its FQDN.
     */
    public void getTrunk() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.getTrunk
        sipRoutingAsyncClient.getTrunk("<trunk fqdn>").subscribe(trunk ->
            System.out.println("Trunk " + trunk.getFqdn() + ":" + trunk.getSipSignalingPort()));
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.getTrunk
    }

    /**
     * Sample code for getting a SIP trunk with response based on its FQDN.
     */
    public void getTrunkWithResponse() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.getTrunkWithResponse
        sipRoutingAsyncClient.getTrunkWithResponse("<trunk fqdn>")
            .subscribe(response -> {
                SipTrunk trunk = response.getValue();
                System.out.println("Trunk " + trunk.getFqdn() + ":" + trunk.getSipSignalingPort());
            });
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.getTrunkWithResponse
    }

    /**
     * Sample code for setting SIP trunks.
     */
    public void setTrunks() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunks
        sipRoutingAsyncClient.setTrunks(asList(
            new SipTrunk("<first trunk fqdn>", 12345),
            new SipTrunk("<second trunk fqdn>", 23456)
        )).block();
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunks
    }

    /**
     * Sample code for setting SIP trunks with response.
     */
    public void setTrunksWithResponse() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunksWithResponse
        sipRoutingAsyncClient.setTrunksWithResponse(asList(
            new SipTrunk("<first trunk fqdn>", 12345),
            new SipTrunk("<second trunk fqdn>", 23456)
        )).subscribe(response -> {
            System.out.println("Response status " + response.getStatusCode());
        });
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunksWithResponse
    }

    /**
     * Sample code for setting SIP routing routes.
     */
    public void setRoutes() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.setRoutes
        sipRoutingAsyncClient.setRoutes(asList(
            new SipTrunkRoute("route name1", ".*9").setTrunks(asList("<first trunk fqdn>", "<second trunk fqdn>")),
            new SipTrunkRoute("route name2", ".*").setTrunks(asList("<second trunk fqdn>"))
        )).block();
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.setRoutes
    }

    /**
     * Sample code for setting SIP routing routes with response.
     */
    public void setRoutesWithResponse() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.setRoutesWithResponse
        sipRoutingAsyncClient.setRoutesWithResponse(asList(
            new SipTrunkRoute("route name1", ".*9").setTrunks(asList("<first trunk fqdn>", "<second trunk fqdn>")),
            new SipTrunkRoute("route name2", ".*").setTrunks(asList("<second trunk fqdn>"))
        )).subscribe(response -> {
            System.out.println("Response status " + response.getStatusCode());
        });
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.setRoutesWithResponse
    }

    /**
     * Sample code for setting a SIP trunk.
     */
    public void setTrunk() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunk
        sipRoutingAsyncClient.setTrunk(new SipTrunk("<trunk fqdn>", 12345)).block();
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunk
    }

    /**
     * Sample code for deleting a SIP trunk.
     */
    public void deleteTrunk() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.deleteTrunk
        sipRoutingAsyncClient.deleteTrunk("<trunk fqdn>").block();
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.deleteTrunk
    }

    /**
     * Sample code for deleting a SIP trunk with response.
     */
    public void deleteTrunkWithResponse() {
        SipRoutingAsyncClient sipRoutingAsyncClient = createSipRoutingAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.asyncclient.deleteTrunkWithResponse
        sipRoutingAsyncClient.deleteTrunkWithResponse("<trunk fqdn>").subscribe(response -> {
            System.out.println("Response status " + response.getStatusCode());
        });
        // END: com.azure.communication.phonenumbers.siprouting.asyncclient.deleteTrunkWithResponse
    }

}
