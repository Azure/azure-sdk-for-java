// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers.siprouting;

import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
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

        PagedIterable<SipTrunk> trunks = client.listTrunks();

        assertNotNull(trunks);
        assertTrue(getAsList(trunks).size() == 0);
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

        PagedIterable<SipTrunkRoute> routes = client.listRoutes();

        assertNotNull(routes);
        assertTrue(getAsList(routes).size() == 0);
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
        List<SipTrunk> trunksAsList = getAsList(client.listTrunks());
        assertTrue(trunksAsList.size() == 0);

        client.setTrunk(SET_TRUNK);

        SipTrunk storedTrunk = client.getTrunk(SET_TRUNK_FQDN);
        assertNotNull(storedTrunk);
        assertEquals(SET_TRUNK_PORT, storedTrunk.getSipSignalingPort());

        assertEquals(1, getAsList(client.listTrunks()).size());
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
        assertTrue(getAsList(client.listTrunks()).size() == 0);

        client.setTrunks(EXPECTED_TRUNKS);

        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksEmptyBeforeWithResponseSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(getAsList(client.listTrunks()).size() == 0);

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
        assertTrue(getAsList(client.listTrunks()).size() == 0);

        client.setTrunks(EXPECTED_TRUNKS);

        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksNotEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksNotEmptyBeforeSync");
        client.setTrunks(UPDATED_TRUNKS);
        List<SipTrunk> trunksAsList = getAsList(client.listTrunks());
        assertEquals(UPDATED_TRUNKS.size(), trunksAsList.size());

        client.setTrunks(EXPECTED_TRUNKS);

        validateTrunks(EXPECTED_TRUNKS, client.listTrunks());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksNotEmptyBeforeWithResponseSync");
        client.setTrunks(UPDATED_TRUNKS);
        List<SipTrunk> trunksAsList = getAsList(client.listTrunks());
        assertEquals(UPDATED_TRUNKS.size(), trunksAsList.size());

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

        assertTrue(getAsList(client.listTrunks()).size() == 0);
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
        assertTrue(getAsList(client.listTrunks()).size() == 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyTrunksEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyTrunksEmptyBeforeSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(getAsList(client.listTrunks()).size() == 0);

        client.setTrunks(new ArrayList<>());

        assertTrue(getAsList(client.listTrunks()).size() == 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyTrunksEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyTrunksEmptyBeforeWithResponseSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(getAsList(client.listTrunks()).size() == 0);

        Response<Void> response = client.setTrunksWithResponse(new ArrayList<>(), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(getAsList(client.listTrunks()).size() == 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksInvalidFqdn(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksInvalidFqdnSync");

        SipTrunk invalidTrunk = new SipTrunk(SET_TRUNK_INVALID_FQDN, SET_TRUNK_PORT);
        assertThrows(HttpResponseException.class,
            () -> client.setTrunk(invalidTrunk));

        assertThrows(HttpResponseException.class,
            () -> client.setTrunks(asList(invalidTrunk)));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksInvalidPort(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksInvalidPortSync");
        client.setTrunk(SET_TRUNK);

        SipTrunk invalidTrunk = new SipTrunk(SET_TRUNK_FQDN, SET_TRUNK_INVALID_PORT);
        assertThrows(HttpResponseException.class,
            () -> client.setTrunk(invalidTrunk));

        assertThrows(HttpResponseException.class,
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

        assertThrows(HttpResponseException.class, () -> client.setTrunks(EXPECTED_TRUNKS));
        assertThrows(HttpResponseException.class, () -> client.deleteTrunk(SET_TRUNK_FQDN));

        client.setRoutes(new ArrayList<>());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksRemoveRequiredTrunkWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setTrunksRemoveRequiredTrunkWithResponseSync");
        client.setTrunk(SET_TRUNK);
        client.setRoutes(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK);

        assertThrows(HttpResponseException.class,
            () -> client.setTrunksWithResponse(EXPECTED_TRUNKS, Context.NONE));
        assertThrows(HttpResponseException.class,
            () -> client.deleteTrunkWithResponse(SET_TRUNK_FQDN, Context.NONE));

        client.setRoutes(new ArrayList<>());
    }

    // set routes
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesEmptyBeforeSync");
        client.setRoutes(new ArrayList<>());
        assertTrue(getAsList(client.listRoutes()).size() == 0);

        client.setRoutes(EXPECTED_ROUTES);

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesEmptyBeforeWithResponseSync");
        client.setRoutes(new ArrayList<>());
        assertTrue(getAsList(client.listRoutes()).size() == 0);

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
        assertTrue(getAsList(client.listRoutes()).size() == 0);

        client.setRoutes(EXPECTED_ROUTES);

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesNotEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesNotEmptyBeforeSync");
        client.setRoutes(UPDATED_ROUTES);
        List<SipTrunkRoute> routesAsList = getAsList(client.listRoutes());

        assertEquals(UPDATED_ROUTES.size(), routesAsList.size());
        client.setRoutes(EXPECTED_ROUTES);

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesNotEmptyBeforeWithResponseSync");
        client.setRoutes(UPDATED_ROUTES);
        List<SipTrunkRoute> routesAsList = getAsList(client.listRoutes());

        assertEquals(UPDATED_ROUTES.size(), routesAsList.size());

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

        assertTrue(getAsList(client.listRoutes()).size() == 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesNotEmptyBeforeWithResponseSync");
        client.setRoutes(EXPECTED_ROUTES);
        
        PagedIterable<SipTrunkRoute> iter = client.listRoutes();
        validateExpectedRoutes(iter);

        Response<Void> response = client.setRoutesWithResponse(new ArrayList<>(), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(getAsList(client.listRoutes()).size() == 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesEmptyBefore(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesEmptyBeforeSync");
        client.setRoutes(new ArrayList<>());
        assertTrue(getAsList(client.listRoutes()).size() == 0);

        client.setRoutes(new ArrayList<>());

        assertTrue(getAsList(client.listRoutes()).size() == 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesEmptyBeforeWithResponseSync");
        client.setRoutes(new ArrayList<>());
        assertTrue(getAsList(client.listRoutes()).size() == 0);

        Response<Void> response = client.setRoutesWithResponse(new ArrayList<>(), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(getAsList(client.listRoutes()).size() == 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidName(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNameSync");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(null, SET_TRUNK_ROUTE_NUMBER_PATTERN);

        Throwable exception = assertThrows(HttpResponseException.class, () -> client.setRoutes(asList(invalidRoute)));
        assertEquals(MESSAGE_INVALID_ROUTE_NAME, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidNameWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNameWithResponseSync");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(null, SET_TRUNK_ROUTE_NUMBER_PATTERN);

        Throwable exception = assertThrows(HttpResponseException.class, () -> client.setRoutesWithResponse(asList(invalidRoute), Context.NONE));
        assertEquals(MESSAGE_INVALID_ROUTE_NAME, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidNumberPattern(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNumberPatternSync");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, null);

        Throwable exception = assertThrows(HttpResponseException.class, () -> client.setRoutes(asList(invalidRoute)));
        assertEquals(MESSAGE_INVALID_NUMBER_PATTERN, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidNumberPatternWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNumberPatternWithResponseSync");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, null);

        Throwable exception = assertThrows(HttpResponseException.class, () -> client.setRoutesWithResponse(asList(invalidRoute), Context.NONE));
        assertEquals(MESSAGE_INVALID_NUMBER_PATTERN, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutes(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutesSync");
        List<SipTrunkRoute> invalidRoutes = asList(SET_TRUNK_ROUTE, SET_TRUNK_ROUTE);

        Throwable exception = assertThrows(HttpResponseException.class, () -> client.setRoutes(invalidRoutes));
        assertEquals(MESSAGE_DUPLICATE_ROUTES, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutesWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutesWithResponseSync");
        List<SipTrunkRoute> invalidRoutes = asList(SET_TRUNK_ROUTE, SET_TRUNK_ROUTE);

        Throwable exception = assertThrows(HttpResponseException.class, () -> client.setRoutesWithResponse(invalidRoutes, Context.NONE));
        assertEquals(MESSAGE_DUPLICATE_ROUTES, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutingTrunks(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutingTrunksSync");
        client.setTrunk(SET_TRUNK);
        SipTrunkRoute routeWithDuplicatedTrunks = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, SET_TRUNK_ROUTE_NUMBER_PATTERN)
            .setTrunks(asList(SET_TRUNK_FQDN, SET_TRUNK_FQDN));

        Throwable exception = assertThrows(HttpResponseException.class, () -> client.setRoutes(asList(routeWithDuplicatedTrunks)));
        assertEquals(MESSAGE_DUPLICATE_TRUNKS, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutingTrunksWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutingTrunksWithResponseSync");
        client.setTrunk(SET_TRUNK);
        SipTrunkRoute routeWithDuplicatedTrunks = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, SET_TRUNK_ROUTE_NUMBER_PATTERN)
            .setTrunks(asList(SET_TRUNK_FQDN, SET_TRUNK_FQDN));

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutesWithResponse(asList(routeWithDuplicatedTrunks), Context.NONE));
        assertEquals(MESSAGE_DUPLICATE_TRUNKS, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesMissingTrunk(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesMissingTrunkSync");
        client.setTrunks(new ArrayList<>());

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutes(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK));
        assertEquals(MESSAGE_MISSING_TRUNK, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesMissingTrunkWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "setRoutesMissingTrunkWithResponseSync");
        client.setTrunks(new ArrayList<>());

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutesWithResponse(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK, Context.NONE));
        assertEquals(MESSAGE_MISSING_TRUNK, exception.getMessage());
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
        assertTrue(getAsList(client.listTrunks()).size() == 0);
        assertNull(client.getTrunk(DELETE_FQDN));

        client.deleteTrunk(DELETE_FQDN);

        assertNull(client.getTrunk(DELETE_FQDN));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkNotExistingWithResponse(HttpClient httpClient) {
        SipRoutingClient client = getClientWithConnectionString(httpClient, "deleteTrunkNotExistingWithResponseSync");
        client.setTrunks(new ArrayList<>());
        assertTrue(getAsList(client.listTrunks()).size() == 0);
        assertNull(client.getTrunk(DELETE_FQDN));

        Response<Void> response = client.deleteTrunkWithResponse(DELETE_FQDN, Context.NONE);

        assertNotNull(response);
        assertNull(response.getValue());
        assertEquals(200, response.getStatusCode());
        assertNull(client.getTrunk(DELETE_FQDN));
    }

    private void validateTrunks(List<SipTrunk> expected, PagedIterable<SipTrunk> actual) {
        assertNotNull(actual);
        List<SipTrunk> trunksList = getAsList(actual);
        assertEquals(expected.size(), trunksList.size());
        for (SipTrunk expectedTrunk : expected) {
            Optional<SipTrunk> actualTrunk = trunksList.stream()
                .filter(value -> Objects.equals(expectedTrunk.getFqdn(), value.getFqdn())).findAny();
            assertTrue(actualTrunk.isPresent());
            assertEquals(expectedTrunk.getSipSignalingPort(), actualTrunk.get().getSipSignalingPort());
        }
    }

    private void validateExpectedRoutes(PagedIterable<SipTrunkRoute> routes) {
        assertNotNull(routes);
        List<SipTrunkRoute> routesList = getAsList(routes);
        assertEquals(3, routesList.size());
        for (int i = 0; i < routesList.size(); i++) {
            SipTrunkRoute route = routesList.get(i);
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

    private <T> List<T> getAsList(PagedIterable<T> listIterable) {
        List<T> list = new ArrayList<T>();

        listIterable.streamByPage().forEach(resp -> {
            resp.getElements().forEach(value -> list.add(value));
        });

        return list;
    }
}
