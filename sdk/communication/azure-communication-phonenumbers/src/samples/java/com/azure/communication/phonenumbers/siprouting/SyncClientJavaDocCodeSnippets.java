// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting;

import com.azure.communication.phonenumbers.SipRoutingClient;
import com.azure.communication.phonenumbers.SipRoutingClientBuilder;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.List;

import static java.util.Arrays.asList;

public class SyncClientJavaDocCodeSnippets {

    /**
     * Sample code for creating a sync SIP Routing Client using connection string.
     *
     * @return the SIP Routing Client.
     */
    public SipRoutingClient createSipRoutingClient() {
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "endpoint=https://RESOURCE_NAME.communication.azure.com/;accesskey=SECRET";

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.instantiation
        SipRoutingClient sipRoutingClient = new SipRoutingClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.communication.phonenumbers.siprouting.client.instantiation

        return sipRoutingClient;
    }

    /**
     * Sample code for listing SIP trunks.
     * @return the SIP trunks.
     */
    public List<SipTrunk> listTrunks() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.listTrunks
        List<SipTrunk> trunks = sipRoutingClient.listTrunks();
        for (SipTrunk trunk : trunks) {
            System.out.println("Trunk " + trunk.getFqdn() + ":" + trunk.getSipSignalingPort());
        }
        // END: com.azure.communication.phonenumbers.siprouting.client.listTrunks

        return trunks;
    }

    /**
     * Sample code for listing SIP trunks with response.
     * @return the SIP trunks.
     */
    public List<SipTrunk> listTrunksWithResponse() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.listTrunksWithResponse
        Response<List<SipTrunk>> response = sipRoutingClient.listTrunksWithResponse(Context.NONE);
        List<SipTrunk> trunks = response.getValue();
        for (SipTrunk trunk : trunks) {
            System.out.println("Trunk " + trunk.getFqdn() + ":" + trunk.getSipSignalingPort());
        }
        // END: com.azure.communication.phonenumbers.siprouting.client.listTrunksWithResponse

        return trunks;
    }

    /**
     * Sample code for listing SIP routing routes.
     * @return the SIP routes.
     */
    public List<SipTrunkRoute> listRoutes() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.listRoutes
        List<SipTrunkRoute> routes = sipRoutingClient.listRoutes();
        for (SipTrunkRoute route : routes) {
            System.out.println("Route name: " + route.getName());
            System.out.println("Route description: " + route.getDescription());
            System.out.println("Route number pattern: " + route.getNumberPattern());
            System.out.println("Route trunks: " + String.join(",", route.getTrunks()));
        }
        // END: com.azure.communication.phonenumbers.siprouting.client.listRoutes

        return routes;
    }

    /**
     * Sample code for listing SIP routing routes with response.
     * @return the SIP routes.
     */
    public List<SipTrunkRoute> listRoutesWithResponse() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.listRoutesWithResponse
        Response<List<SipTrunkRoute>> response = sipRoutingClient.listRoutesWithResponse(Context.NONE);
        List<SipTrunkRoute> routes = response.getValue();
        for (SipTrunkRoute route : routes) {
            System.out.println("Route name: " + route.getName());
            System.out.println("Route description: " + route.getDescription());
            System.out.println("Route number pattern: " + route.getNumberPattern());
            System.out.println("Route trunks: " + String.join(",", route.getTrunks()));
        }
        // END: com.azure.communication.phonenumbers.siprouting.client.listRoutesWithResponse

        return routes;
    }

    /**
     * Sample code for getting a SIP trunk based on its FQDN.
     * @return the SIP trunk.
     */
    public SipTrunk getTrunk() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.getTrunk
        SipTrunk trunk = sipRoutingClient.getTrunk("<trunk fqdn>");
        System.out.println("Trunk " + trunk.getFqdn() + ":" + trunk.getSipSignalingPort());
        // END: com.azure.communication.phonenumbers.siprouting.client.getTrunk

        return trunk;
    }

    /**
     * Sample code for getting a SIP trunk with response based on its FQDN.
     * @return the SIP trunk.
     */
    public SipTrunk getTrunkWithResponse() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.getTrunkWithResponse
        Response<SipTrunk> response = sipRoutingClient.getTrunkWithResponse("<trunk fqdn>", Context.NONE);
        SipTrunk trunk = response.getValue();
        System.out.println("Trunk " + trunk.getFqdn() + ":" + trunk.getSipSignalingPort());
        // END: com.azure.communication.phonenumbers.siprouting.client.getTrunkWithResponse

        return trunk;
    }

    /**
     * Sample code for setting SIP trunks.
     */
    public void setTrunks() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.setTrunks
        sipRoutingClient.setTrunks(asList(
            new SipTrunk("<first trunk fqdn>", 12345),
            new SipTrunk("<second trunk fqdn>", 23456)
        ));
        // END: com.azure.communication.phonenumbers.siprouting.client.setTrunks
    }

    /**
     * Sample code for setting SIP trunks with response.
     */
    public void setTrunksWithResponse() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.setTrunksWithResponse
        Response<Void> response = sipRoutingClient.setTrunksWithResponse(asList(
            new SipTrunk("<first trunk fqdn>", 12345),
            new SipTrunk("<second trunk fqdn>", 23456)
        ), Context.NONE);
        // END: com.azure.communication.phonenumbers.siprouting.client.setTrunksWithResponse
    }

    /**
     * Sample code for setting SIP routing routes.
     */
    public void setRoutes() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.setRoutes
        sipRoutingClient.setRoutes(asList(
            new SipTrunkRoute("route name1", ".*9").setTrunks(asList("<first trunk fqdn>", "<second trunk fqdn>")),
            new SipTrunkRoute("route name2", ".*").setTrunks(asList("<second trunk fqdn>"))
        ));
        // END: com.azure.communication.phonenumbers.siprouting.client.setRoutes
    }

    /**
     * Sample code for setting SIP routing routes with response.
     */
    public void setRoutesWithResponse() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.setRoutesWithResponse
        Response<Void> response = sipRoutingClient.setRoutesWithResponse(asList(
            new SipTrunkRoute("route name1", ".*9").setTrunks(asList("<first trunk fqdn>", "<second trunk fqdn>")),
            new SipTrunkRoute("route name2", ".*").setTrunks(asList("<second trunk fqdn>"))
        ), Context.NONE);
        // END: com.azure.communication.phonenumbers.siprouting.client.setRoutesWithResponse
    }

    /**
     * Sample code for setting a SIP trunk.
     */
    public void setTrunk() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.setTrunk
        sipRoutingClient.setTrunk(new SipTrunk("<trunk fqdn>", 12345));
        // END: com.azure.communication.phonenumbers.siprouting.client.setTrunk
    }

    /**
     * Sample code for deleting a SIP trunk.
     */
    public void deleteTrunk() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.deleteTrunk
        sipRoutingClient.deleteTrunk("<trunk fqdn>");
        // END: com.azure.communication.phonenumbers.siprouting.client.deleteTrunk
    }

    /**
     * Sample code for deleting a SIP trunk with response.
     */
    public void deleteTrunkWithResponse() {
        SipRoutingClient sipRoutingClient = createSipRoutingClient();

        // BEGIN: com.azure.communication.phonenumbers.siprouting.client.deleteTrunkWithResponse
        Response<Void> response = sipRoutingClient.deleteTrunkWithResponse("<trunk fqdn>", Context.NONE);
        // END: com.azure.communication.phonenumbers.siprouting.client.deleteTrunkWithResponse
    }
}
