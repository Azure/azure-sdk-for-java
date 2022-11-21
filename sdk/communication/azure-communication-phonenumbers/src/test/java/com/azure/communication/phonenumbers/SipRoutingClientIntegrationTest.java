// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.siprouting.models.SipRoutingResponseException;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@Execution(value = ExecutionMode.SAME_THREAD)
public class SipRoutingClientIntegrationTest extends SipRoutingIntegrationTestBase {

    // get trunk
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTrunkNotExisting(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "getTrunkNotExistingSync");
        SipTrunk trunk = client.getTrunk(NOT_EXISTING_FQDN);

        assertNull(trunk);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTrunkNotExistingWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "getTrunkNotExistingWithResponseSync");

        Response<SipTrunk> response = client.getTrunkWithResponse(NOT_EXISTING_FQDN, Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNull(response.getValue());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTrunkExisting(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "getTrunkExistingSync");
        client.setTrunk(SET_TRUNK);

        SipTrunk trunk = client.getTrunk(SET_TRUNK_FQDN);

        assertNotNull(trunk);
        assertEquals(SET_TRUNK_PORT, trunk.getSipSignalingPort());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTrunkExistingWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "getTrunkExistingWithResponseSync");
        client.setTrunk(SET_TRUNK);

        Response<SipTrunk> response = client.getTrunkWithResponse(SET_TRUNK_FQDN, Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        SipTrunk trunk = response.getValue();
        assertNotNull(trunk);
        assertEquals(SET_TRUNK_PORT, trunk.getSipSignalingPort());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTrunkExistingWithAAD(HttpClient httpClient) {
        SipRoutingClient client = getClientWithManagedIdentity(httpClient, "getTrunkExistingWithAADSync");
        client.setTrunk(SET_TRUNK);

        SipTrunk trunk = client.getTrunk(SET_TRUNK_FQDN);

        assertNotNull(trunk);
        assertEquals(SET_TRUNK_PORT, trunk.getSipSignalingPort());
    }

    // list trunks
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listTrunksEmpty(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "listTrunksEmptySync");
        client.setTrunks(new ArrayList<>());

        List<SipTrunk> trunks = client.listTrunks();

        assertNotNull(trunks);
        assertTrue(trunks.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listTrunksEmptyWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "listTrunksEmptyWithResponseSync");
        client.setTrunks(new ArrayList<>());

        Response<List<SipTrunk>> response = client.listTrunksWithResponse(Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        List<SipTrunk> trunks = response.getValue();
        assertNotNull(trunks);
        assertTrue(trunks.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listTrunksNotEmpty(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "listTrunksNotEmptySync");
        client.setTrunks(EXPECTED_TRUNKS);

        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listTrunksNotEmptyWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "listTrunksNotEmptyWithResponseSync");
        client.setTrunks(EXPECTED_TRUNKS);

        Response<List<SipTrunk>> response = client.listTrunksWithResponse(Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        List<SipTrunk> trunks = response.getValue();
        assertNotNull(trunks);
        validateTrunks(EXPECTED_TRUNKS, trunks);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listTrunksNotEmptyWithAAD(HttpClient httpClient) {
        SipRoutingClient client = getClientWithManagedIdentity(httpClient, "listTrunksNotEmptyWithAADSync");
        client.setTrunks(EXPECTED_TRUNKS);

        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    // list routes
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listRoutesEmpty(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "listRoutesEmptySync");
        client.setRoutes(new ArrayList<>());

        List<SipTrunkRoute> routes = client.listRoutes();

        assertNotNull(routes);
        assertTrue(routes.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listRoutesEmptyWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "listRoutesEmptyWithResponseSync");
        client.setRoutes(new ArrayList<>());

        Response<List<SipTrunkRoute>> response = client.listRoutesWithResponse(Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        List<SipTrunkRoute> routes = response.getValue();
        assertNotNull(routes);
        assertTrue(routes.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listRoutesNotEmpty(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "listRoutesNotEmptySync");
        client.setRoutes(EXPECTED_ROUTES);

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listRoutesNotEmptyWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "listRoutesNotEmptyWithResponseSync");
        client.setRoutes(EXPECTED_ROUTES);

        Response<List<SipTrunkRoute>> response = client.listRoutesWithResponse(Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        List<SipTrunkRoute> routes = response.getValue();
        assertNotNull(routes);
        validateExpectedRoutes(routes);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listRoutesNotEmptyWithAAD(HttpClient httpClient) {
        SipRoutingClient client = getClientWithManagedIdentity(httpClient, "listRoutesNotEmptyWithAADSync");
        client.setRoutes(EXPECTED_ROUTES);

        validateExpectedRoutes(client.listRoutes());
    }

    // set trunk
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunkNotExistingEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunkNotExistingEmptyBeforeSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(client.listTrunks().isEmpty());

        client.setTrunk(SET_TRUNK);

        SipTrunk storedTrunk = client.getTrunk(SET_TRUNK_FQDN);
        assertNotNull(storedTrunk);
        assertEquals(SET_TRUNK_PORT, storedTrunk.getSipSignalingPort());
        assertEquals(1, client.listTrunks().size());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunkNotExistingNotEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunkNotExistingNotEmptyBeforeSync");
        List<SipTrunk> initialTrunks = EXPECTED_TRUNKS;
        client.setTrunks(initialTrunks);
        validateTrunks(initialTrunks, client.listTrunks());

        client.setTrunk(SET_TRUNK);

        SipTrunk storedTrunk = client.getTrunk(SET_TRUNK_FQDN);
        assertNotNull(storedTrunk);
        assertEquals(SET_TRUNK_PORT, storedTrunk.getSipSignalingPort());

        ArrayList<SipTrunk> expectedTrunks = new ArrayList<>(initialTrunks);
        expectedTrunks.add(SET_TRUNK);
        validateTrunks(expectedTrunks, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunkExisting(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunkExistingSync");
        List<SipTrunk> expectedTrunks = new ArrayList<>(EXPECTED_TRUNKS);
        expectedTrunks.add(SET_TRUNK);
        client.setTrunks(expectedTrunks);
        validateTrunks(expectedTrunks, client.listTrunks());

        client.setTrunk(SET_UPDATED_TRUNK);

        SipTrunk storedTrunk = client.getTrunk(SET_TRUNK_FQDN);
        assertNotNull(storedTrunk);
        assertEquals(SET_TRUNK_UPDATED_PORT, storedTrunk.getSipSignalingPort());
        expectedTrunks = new ArrayList<>(EXPECTED_TRUNKS);
        expectedTrunks.add(SET_UPDATED_TRUNK);
        validateTrunks(expectedTrunks, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunkExistingWithAAD(HttpClient httpClient) {
        SipRoutingClient client = getClientWithManagedIdentity(httpClient, "setTrunkExistingWithAADSync");
        List<SipTrunk> expectedTrunks = new ArrayList<>(EXPECTED_TRUNKS);
        expectedTrunks.add(SET_TRUNK);
        client.setTrunks(expectedTrunks);
        validateTrunks(expectedTrunks, client.listTrunks());

        client.setTrunk(SET_UPDATED_TRUNK);

        SipTrunk storedTrunk = client.getTrunk(SET_TRUNK_FQDN);
        assertNotNull(storedTrunk);
        assertEquals(SET_TRUNK_UPDATED_PORT, storedTrunk.getSipSignalingPort());
        expectedTrunks = new ArrayList<>(EXPECTED_TRUNKS);
        expectedTrunks.add(SET_UPDATED_TRUNK);
        validateTrunks(expectedTrunks, client.listTrunks());
    }

    // set trunks
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksEmptyBeforeSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(client.listTrunks().isEmpty());

        client.setTrunks(EXPECTED_TRUNKS);

        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksEmptyBeforeWithResponseSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(client.listTrunks().isEmpty());

        Response<Void> response = client.setTrunksWithResponse(EXPECTED_TRUNKS, Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksEmptyBeforeWithAAD(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksEmptyBeforeWithAADSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(client.listTrunks().isEmpty());

        client.setTrunks(EXPECTED_TRUNKS);

        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksNotEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksNotEmptyBeforeSync");
        client.setTrunks(UPDATED_TRUNKS);
        assertEquals(UPDATED_TRUNKS.size(), client.listTrunks().size());

        client.setTrunks(EXPECTED_TRUNKS);

        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksNotEmptyBeforeWithResponseSync");
        client.setTrunks(UPDATED_TRUNKS);
        assertEquals(UPDATED_TRUNKS.size(), client.listTrunks().size());

        Response<Void> response = client.setTrunksWithResponse(EXPECTED_TRUNKS, Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksWithoutAffectingRoutes(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksWithoutAffectingRoutesSync");
        client.setRoutes(EXPECTED_ROUTES);

        client.setTrunks(EXPECTED_TRUNKS);

        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksWithoutAffectingRoutesWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksWithoutAffectingRoutesWithResponseSync");
        client.setRoutes(EXPECTED_ROUTES);

        Response<Void> response = client.setTrunksWithResponse(EXPECTED_TRUNKS, Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyTrunksNotEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyTrunksNotEmptyBeforeSync");
        client.setTrunks(EXPECTED_TRUNKS);
        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());

        client.setTrunks(new ArrayList<>());

        assertTrue(client.listTrunks().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyTrunksNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyTrunksNotEmptyBeforeWithResponseSync");
        client.setTrunks(EXPECTED_TRUNKS);
        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());

        Response<Void> response = client.setTrunksWithResponse(new ArrayList<>(), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(client.listTrunks().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyTrunksEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyTrunksEmptyBeforeSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(client.listTrunks().isEmpty());

        client.setTrunks(new ArrayList<>());

        assertTrue(client.listTrunks().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyTrunksEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyTrunksEmptyBeforeWithResponseSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(client.listTrunks().isEmpty());

        Response<Void> response = client.setTrunksWithResponse(new ArrayList<>(), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(client.listTrunks().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksInvalidFqdn(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksInvalidFqdnSync");

        SipTrunk invalidTrunk = new SipTrunk(SET_TRUNK_INVALID_FQDN, SET_TRUNK_PORT);
        assertThrows(SipRoutingResponseException.class,
            () -> client.setTrunk(invalidTrunk));

        assertThrows(SipRoutingResponseException.class,
            () -> client.setTrunks(asList(invalidTrunk)));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksInvalidPort(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksInvalidPortSync");
        client.setTrunk(SET_TRUNK);

        SipTrunk invalidTrunk = new SipTrunk(SET_TRUNK_FQDN, SET_TRUNK_INVALID_PORT);
        assertThrows(SipRoutingResponseException.class,
            () -> client.setTrunk(invalidTrunk));

        assertThrows(SipRoutingResponseException.class,
            () -> client.setTrunks(asList(invalidTrunk)));

        SipTrunk storedTrunk = client.getTrunk(SET_TRUNK_FQDN);
        assertNotNull(storedTrunk);
        assertEquals(SET_TRUNK_PORT, storedTrunk.getSipSignalingPort());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksRemoveRequiredTrunk(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksRemoveRequiredTrunkSync");
        client.setTrunk(SET_TRUNK);
        client.setRoutes(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK);

        assertThrows(SipRoutingResponseException.class, () -> client.setTrunks(EXPECTED_TRUNKS));
        assertThrows(SipRoutingResponseException.class, () -> client.deleteTrunk(SET_TRUNK_FQDN));

        client.setRoutes(new ArrayList<>());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksRemoveRequiredTrunkWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksRemoveRequiredTrunkWithResponseSync");
        client.setTrunk(SET_TRUNK);
        client.setRoutes(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK);

        assertThrows(SipRoutingResponseException.class,
            () -> client.setTrunksWithResponse(EXPECTED_TRUNKS, Context.NONE));
        assertThrows(SipRoutingResponseException.class,
            () -> client.deleteTrunkWithResponse(SET_TRUNK_FQDN, Context.NONE));

        client.setRoutes(new ArrayList<>());
    }

    // set routes
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesEmptyBeforeSync");
        client.setRoutes(new ArrayList<>());
        assertTrue(client.listRoutes().isEmpty());

        client.setRoutes(EXPECTED_ROUTES);

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesEmptyBeforeWithResponseSync");
        client.setRoutes(new ArrayList<>());
        assertTrue(client.listRoutes().isEmpty());

        Response<Void> response = client.setRoutesWithResponse(EXPECTED_ROUTES, Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesEmptyBeforeWithAAD(HttpClient httpClient) {
        SipRoutingClient client = getClientWithManagedIdentity(httpClient, "setRoutesEmptyBeforeWithAADSync");
        client.setRoutes(new ArrayList<>());
        assertTrue(client.listRoutes().isEmpty());

        client.setRoutes(EXPECTED_ROUTES);

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesNotEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesNotEmptyBeforeSync");
        client.setRoutes(UPDATED_ROUTES);
        assertEquals(UPDATED_ROUTES.size(), client.listRoutes().size());

        client.setRoutes(EXPECTED_ROUTES);

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesNotEmptyBeforeWithResponseSync");
        client.setRoutes(UPDATED_ROUTES);
        assertEquals(UPDATED_ROUTES.size(), client.listRoutes().size());

        Response<Void> response = client.setRoutesWithResponse(EXPECTED_ROUTES, Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesWithoutAffectingTrunks(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesWithoutAffectingTrunksSync");
        client.setTrunks(EXPECTED_TRUNKS);

        client.setRoutes(EXPECTED_ROUTES);

        validateExpectedRoutes(client.listRoutes());
        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesWithoutAffectingTrunksWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesWithoutAffectingTrunksWithResponseSync");
        client.setTrunks(EXPECTED_TRUNKS);

        Response<Void> response = client.setRoutesWithResponse(EXPECTED_ROUTES, Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateExpectedRoutes(client.listRoutes());
        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesNotEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesNotEmptyBeforeSync");
        client.setRoutes(EXPECTED_ROUTES);
        validateExpectedRoutes(client.listRoutes());

        client.setRoutes(new ArrayList<>());

        assertTrue(client.listRoutes().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesNotEmptyBeforeWithResponseSync");
        client.setRoutes(EXPECTED_ROUTES);
        validateExpectedRoutes(client.listRoutes());

        Response<Void> response = client.setRoutesWithResponse(new ArrayList<>(), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(client.listRoutes().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesEmptyBeforeSync");
        client.setRoutes(new ArrayList<>());
        assertTrue(client.listRoutes().isEmpty());

        client.setRoutes(new ArrayList<>());

        assertTrue(client.listRoutes().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesEmptyBeforeWithResponseSync");
        client.setRoutes(new ArrayList<>());
        assertTrue(client.listRoutes().isEmpty());

        Response<Void> response = client.setRoutesWithResponse(new ArrayList<>(), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(client.listRoutes().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidName(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNameSync");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(null, SET_TRUNK_ROUTE_NUMBER_PATTERN);

        assertThrows(SipRoutingResponseException.class,
            () -> client.setRoutes(asList(invalidRoute)));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidNameWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNameWithResponseSync");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(null, SET_TRUNK_ROUTE_NUMBER_PATTERN);

        assertThrows(SipRoutingResponseException.class,
            () -> client.setRoutesWithResponse(asList(invalidRoute), Context.NONE));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidNumberPattern(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNumberPatternSync");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, null);

        assertThrows(SipRoutingResponseException.class,
            () -> client.setRoutes(asList(invalidRoute)));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidNumberPatternWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNumberPatternWithResponseSync");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, null);

        assertThrows(SipRoutingResponseException.class,
            () -> client.setRoutesWithResponse(asList(invalidRoute), Context.NONE));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutes(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutesSync");
        List<SipTrunkRoute> invalidRoutes = asList(SET_TRUNK_ROUTE, SET_TRUNK_ROUTE);

        assertThrows(SipRoutingResponseException.class, () -> client.setRoutes(invalidRoutes));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutesWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutesWithResponseSync");
        List<SipTrunkRoute> invalidRoutes = asList(SET_TRUNK_ROUTE, SET_TRUNK_ROUTE);

        assertThrows(SipRoutingResponseException.class,
            () -> client.setRoutesWithResponse(invalidRoutes, Context.NONE));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutingTrunks(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutingTrunksSync");
        client.setTrunk(SET_TRUNK);
        SipTrunkRoute routeWithDuplicatedTrunks = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, SET_TRUNK_ROUTE_NUMBER_PATTERN)
            .setTrunks(asList(SET_TRUNK_FQDN, SET_TRUNK_FQDN));

        assertThrows(SipRoutingResponseException.class, () -> client.setRoutes(asList(routeWithDuplicatedTrunks)));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutingTrunksWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutingTrunksWithResponseSync");
        client.setTrunk(SET_TRUNK);
        SipTrunkRoute routeWithDuplicatedTrunks = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, SET_TRUNK_ROUTE_NUMBER_PATTERN)
            .setTrunks(asList(SET_TRUNK_FQDN, SET_TRUNK_FQDN));

        assertThrows(SipRoutingResponseException.class,
            () -> client.setRoutesWithResponse(asList(routeWithDuplicatedTrunks), Context.NONE));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesMissingTrunk(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesMissingTrunkSync");
        client.setTrunks(new ArrayList<>());

        assertThrows(SipRoutingResponseException.class,
            () -> client.setRoutes(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesMissingTrunkWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesMissingTrunkWithResponseSync");
        client.setTrunks(new ArrayList<>());

        assertThrows(SipRoutingResponseException.class,
            () -> client.setRoutesWithResponse(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK, Context.NONE));
    }

    // delete trunk
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkExisting(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "deleteTrunkExistingSync");
        client.setTrunk(DELETE_TRUNK);
        assertNotNull(client.getTrunk(DELETE_FQDN));

        client.deleteTrunk(DELETE_FQDN);

        assertNull(client.getTrunk(DELETE_FQDN));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkExistingWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "deleteTrunkExistingWithResponseSync");
        client.setTrunk(DELETE_TRUNK);
        assertNotNull(client.getTrunk(DELETE_FQDN));

        Response<Void> response = client.deleteTrunkWithResponse(DELETE_FQDN, Context.NONE);

        assertNotNull(response);
        assertNull(response.getValue());
        assertEquals(200, response.getStatusCode());
        assertNull(client.getTrunk(DELETE_FQDN));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkExistingWithAAD(HttpClient httpClient) {
        SipRoutingClient client = getClientWithManagedIdentity(httpClient, "deleteTrunkExistingWithAADSync");
        client.setTrunk(DELETE_TRUNK);
        assertNotNull(client.getTrunk(DELETE_FQDN));

        client.deleteTrunk(DELETE_FQDN);

        assertNull(client.getTrunk(DELETE_FQDN));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkNotExisting(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "deleteTrunkNotExistingSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(client.listTrunks().isEmpty());
        assertNull(client.getTrunk(DELETE_FQDN));

        client.deleteTrunk(DELETE_FQDN);

        assertNull(client.getTrunk(DELETE_FQDN));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkNotExistingWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "deleteTrunkNotExistingWithResponseSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(client.listTrunks().isEmpty());
        assertNull(client.getTrunk(DELETE_FQDN));

        Response<Void> response = client.deleteTrunkWithResponse(DELETE_FQDN, Context.NONE);

        assertNotNull(response);
        assertNull(response.getValue());
        assertEquals(200, response.getStatusCode());
        assertNull(client.getTrunk(DELETE_FQDN));
    }

    private void validateTrunks(List<SipTrunk> expected, List<SipTrunk> actual) {
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        for (SipTrunk expectedTrunk : expected) {
            Optional<SipTrunk> actualTrunk = actual.stream()
                .filter(value -> Objects.equals(expectedTrunk.getFqdn(), value.getFqdn())).findAny();
            assertTrue(actualTrunk.isPresent());
            assertEquals(expectedTrunk.getSipSignalingPort(), actualTrunk.get().getSipSignalingPort());
        }
    }

    private void validateExpectedRoutes(List<SipTrunkRoute> routes) {
        assertNotNull(routes);
        assertEquals(3, routes.size());
        for (int i = 0; i < routes.size(); i++) {
            SipTrunkRoute route = routes.get(i);
            assertEquals("route" + i, route.getName());
            assertEquals(i + ".*", route.getNumberPattern());
            assertEquals("desc" + i, route.getDescription());
            assertTrue(route.getTrunks().isEmpty());
        }
    }

    private SipRoutingClient getClientWithConnectionString(HttpClient httpClient, String testName) {
        SipRoutingClientBuilder builder = super.getClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }

    private SipRoutingClient getClientWithManagedIdentity(HttpClient httpClient, String testName) {
        SipRoutingClientBuilder builder = super.getClientBuilderUsingManagedIdentity(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
