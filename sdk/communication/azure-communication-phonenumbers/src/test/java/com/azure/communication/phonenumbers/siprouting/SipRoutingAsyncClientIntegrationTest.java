// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers.siprouting;

import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@Execution(value = ExecutionMode.SAME_THREAD)
public class SipRoutingAsyncClientIntegrationTest extends SipRoutingIntegrationTestBase {

    // get trunk
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTrunkNotExisting(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "getTrunkNotExisting");
        StepVerifier.create(client.getTrunk(NOT_EXISTING_FQDN)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTrunkNotExistingWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "getTrunkNotExistingWithResponse");
        StepVerifier.create(client.getTrunkWithResponse(NOT_EXISTING_FQDN)).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNull(response.getValue());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTrunkExisting(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "getTrunkExisting");
        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();

        StepVerifier.create(client.getTrunk(SET_TRUNK_FQDN)).assertNext(trunk -> {
            assertNotNull(trunk);
            assertEquals(SET_TRUNK_PORT, trunk.getSipSignalingPort());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTrunkExistingWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "getTrunkExistingWithResponse");
        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();

        StepVerifier.create(client.getTrunkWithResponse(SET_TRUNK_FQDN)).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            SipTrunk trunk = response.getValue();
            assertNotNull(trunk);
            assertEquals(SET_TRUNK_PORT, trunk.getSipSignalingPort());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTrunkExistingWithAAD(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithManagedIdentity(httpClient, "getTrunkExistingWithAAD");
        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();

        StepVerifier.create(client.getTrunk(SET_TRUNK_FQDN)).assertNext(trunk -> {
            assertNotNull(trunk);
            assertEquals(SET_TRUNK_PORT, trunk.getSipSignalingPort());
        }).verifyComplete();
    }

    // list trunks
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listTrunksEmpty(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "listTrunksEmpty");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();
        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        
        assertNotNull(trunksList);
        assertTrue(trunksList.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listTrunksNotEmpty(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "listTrunksNotEmpty");
        StepVerifier.create(client.setTrunks(EXPECTED_TRUNKS)).verifyComplete();

        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listTrunksNotEmptyWithAAD(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithManagedIdentity(httpClient, "listTrunksNotEmptyWithAAD");
        StepVerifier.create(client.setTrunks(EXPECTED_TRUNKS)).verifyComplete();

        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
    }

    //list routes
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listRoutesEmpty(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "listRoutesEmpty");
        StepVerifier.create(client.setRoutes(new ArrayList<>())).verifyComplete();

        List<SipTrunkRoute> routesList = getAsList(client.listRoutes());
        assertNotNull(routesList);
        assertTrue(routesList.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listRoutesNotEmpty(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "listRoutesNotEmpty");
        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES)).verifyComplete();
        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listRoutesNotEmptyWithAAD(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithManagedIdentity(httpClient, "listRoutesNotEmptyWithAAD");
        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES)).verifyComplete();

        validateExpectedRoutes(client.listRoutes());
    }

    // set trunk
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunkNotExistingEmptyBefore(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunkNotExistingEmptyBefore");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listTrunks()).verifyComplete();

        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();

        StepVerifier.create(client.getTrunk(SET_TRUNK_FQDN)).assertNext(trunk -> {
            assertNotNull(trunk);
            assertEquals(SET_TRUNK_PORT, trunk.getSipSignalingPort());
        }).verifyComplete();
        StepVerifier.create(client.listTrunks()).assertNext(trunk -> trunk.getFqdn().equals(SET_TRUNK_FQDN)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunkNotExistingNotEmptyBefore(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunkNotExistingNotEmptyBefore");
        List<SipTrunk> initialTrunks = EXPECTED_TRUNKS;
        StepVerifier.create(client.setTrunks(initialTrunks)).verifyComplete();
        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(initialTrunks, trunksList);

        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();

        StepVerifier.create(client.getTrunk(SET_TRUNK_FQDN)).assertNext(trunk -> {
            assertNotNull(trunk);
            assertEquals(SET_TRUNK_PORT, trunk.getSipSignalingPort());
        }).verifyComplete();

        List<SipTrunk> expectedTrunks = new ArrayList<>(initialTrunks);
        expectedTrunks.add(SET_TRUNK);
        trunksList = getAsList(client.listTrunks());
        validateTrunks(expectedTrunks, trunksList);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunkExisting(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunkExisting");
        List<SipTrunk> initialTrunks = new ArrayList<>(EXPECTED_TRUNKS);
        initialTrunks.add(SET_TRUNK);
        StepVerifier.create(client.setTrunks(initialTrunks)).verifyComplete();
        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(initialTrunks, trunksList);

        StepVerifier.create(client.setTrunk(SET_UPDATED_TRUNK)).verifyComplete();

        StepVerifier.create(client.getTrunk(SET_TRUNK_FQDN)).assertNext(trunk -> {
            assertNotNull(trunk);
            assertEquals(SET_TRUNK_UPDATED_PORT, trunk.getSipSignalingPort());
        }).verifyComplete();
        List<SipTrunk> expectedTrunks = new ArrayList<>(EXPECTED_TRUNKS);
        expectedTrunks.add(SET_UPDATED_TRUNK);
        trunksList = getAsList(client.listTrunks());
        validateTrunks(expectedTrunks, trunksList);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunkExistingWithAAD(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithManagedIdentity(httpClient, "setTrunkExistingWithAAD");
        List<SipTrunk> initialTrunks = new ArrayList<>(EXPECTED_TRUNKS);
        initialTrunks.add(SET_TRUNK);
        StepVerifier.create(client.setTrunks(initialTrunks)).verifyComplete();
        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(initialTrunks, trunksList);

        StepVerifier.create(client.setTrunk(SET_UPDATED_TRUNK)).verifyComplete();

        StepVerifier.create(client.getTrunk(SET_TRUNK_FQDN)).assertNext(trunk -> {
            assertNotNull(trunk);
            assertEquals(SET_TRUNK_UPDATED_PORT, trunk.getSipSignalingPort());
        }).verifyComplete();
        List<SipTrunk> expectedTrunks = new ArrayList<>(EXPECTED_TRUNKS);
        expectedTrunks.add(SET_UPDATED_TRUNK);
        trunksList = getAsList(client.listTrunks());
        validateTrunks(expectedTrunks, trunksList);
    }

    // set trunks
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksEmptyBefore(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksEmptyBefore");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listTrunks()).verifyComplete();

        StepVerifier.create(client.setTrunks(EXPECTED_TRUNKS)).verifyComplete();

        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksEmptyBeforeWithResponse");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listTrunks()).verifyComplete();

        StepVerifier.create(client.setTrunksWithResponse(EXPECTED_TRUNKS)).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).verifyComplete();

        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksEmptyBeforeWithAAD(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksEmptyBeforeWithAAD");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listTrunks()).verifyComplete();

        StepVerifier.create(client.setTrunks(EXPECTED_TRUNKS)).verifyComplete();

        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksNotEmptyBefore(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksNotEmptyBefore");
        StepVerifier.create(client.setTrunks(UPDATED_TRUNKS)).verifyComplete();
        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        assertEquals(UPDATED_TRUNKS.size(), trunksList.size());

        StepVerifier.create(client.setTrunks(EXPECTED_TRUNKS)).verifyComplete();

        trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksNotEmptyBeforeWithResponse");
        StepVerifier.create(client.setTrunks(UPDATED_TRUNKS)).verifyComplete();
        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        assertEquals(UPDATED_TRUNKS.size(), trunksList.size());
        StepVerifier.create(client.setTrunksWithResponse(EXPECTED_TRUNKS)).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).verifyComplete();

        trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksWithoutAffectingRoutes(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksWithoutAffectingRoutes");
        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES)).verifyComplete();

        StepVerifier.create(client.setTrunks(EXPECTED_TRUNKS)).verifyComplete();

        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksWithoutAffectingRoutesWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksWithoutAffectingRoutesWithResponse");
        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES)).verifyComplete();

        StepVerifier.create(client.setTrunksWithResponse(EXPECTED_TRUNKS)).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).verifyComplete();

        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyTrunksNotEmptyBefore(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setEmptyTrunksNotEmptyBefore");
        StepVerifier.create(client.setTrunks(EXPECTED_TRUNKS)).verifyComplete();
        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);

        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();

        StepVerifier.create(client.listTrunks()).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyTrunksNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setEmptyTrunksNotEmptyBeforeWithResponse");
        StepVerifier.create(client.setTrunks(EXPECTED_TRUNKS)).verifyComplete();
        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);

        StepVerifier.create(client.setTrunksWithResponse(new ArrayList<>())).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).verifyComplete();

        StepVerifier.create(client.listTrunks()).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyTrunksEmptyBefore(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setEmptyTrunksEmptyBefore");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listTrunks()).verifyComplete();

        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();

        StepVerifier.create(client.listTrunks()).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyTrunksEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setEmptyTrunksEmptyBeforeWithResponse");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listTrunks()).verifyComplete();

        StepVerifier.create(client.setTrunksWithResponse(new ArrayList<>())).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).verifyComplete();

        StepVerifier.create(client.listTrunks()).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksInvalidFqdn(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksInvalidFqdn");
        SipTrunk invalidTrunk = new SipTrunk(SET_TRUNK_INVALID_FQDN, SET_TRUNK_PORT);

        assertThrows(HttpResponseException.class, () -> client.setTrunk(invalidTrunk).block());
        assertThrows(HttpResponseException.class, () -> client.setTrunks(asList(invalidTrunk)).block());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksInvalidFqdnWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksInvalidFqdnWithResponse");
        SipTrunk invalidTrunk = new SipTrunk(SET_TRUNK_INVALID_FQDN, SET_TRUNK_PORT);

        assertThrows(HttpResponseException.class, () -> client.setTrunksWithResponse(asList(invalidTrunk)).block());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksInvalidPort(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksInvalidPort");
        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();
        SipTrunk invalidTrunk = new SipTrunk(SET_TRUNK_FQDN, SET_TRUNK_INVALID_PORT);

        assertThrows(HttpResponseException.class, () -> client.setTrunk(invalidTrunk).block());
        assertThrows(HttpResponseException.class, () -> client.setTrunks(asList(invalidTrunk)).block());

        StepVerifier.create(client.getTrunk(SET_TRUNK_FQDN))
            .assertNext(storedTrunk -> {
                assertNotNull(storedTrunk);
                assertEquals(SET_TRUNK_PORT, storedTrunk.getSipSignalingPort());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksInvalidPortWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksInvalidPortWithResponse");
        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();
        SipTrunk invalidTrunk = new SipTrunk(SET_TRUNK_FQDN, SET_TRUNK_INVALID_PORT);

        assertThrows(HttpResponseException.class, () -> client.setTrunksWithResponse(asList(invalidTrunk)).block());

        StepVerifier.create(client.getTrunk(SET_TRUNK_FQDN))
            .assertNext(storedTrunk -> {
                assertNotNull(storedTrunk);
                assertEquals(SET_TRUNK_PORT, storedTrunk.getSipSignalingPort());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksRemoveRequiredTrunk(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksRemoveRequiredTrunk");
        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();
        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK)).verifyComplete();

        assertThrows(HttpResponseException.class, () -> client.setTrunks(EXPECTED_TRUNKS).block());
        assertThrows(HttpResponseException.class, () -> client.deleteTrunk(SET_TRUNK_FQDN).block());

        StepVerifier.create(client.setRoutes(new ArrayList<>())).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setTrunksRemoveRequiredTrunkWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setTrunksRemoveRequiredTrunkWithResponse");
        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();
        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK)).verifyComplete();

        assertThrows(HttpResponseException.class, () -> client.setTrunksWithResponse(EXPECTED_TRUNKS).block());
        assertThrows(HttpResponseException.class, () -> client.deleteTrunkWithResponse(SET_TRUNK_FQDN).block());

        StepVerifier.create(client.setRoutes(new ArrayList<>())).verifyComplete();
    }

    // set routes
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesEmptyBefore(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesEmptyBefore");
        StepVerifier.create(client.setRoutes(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listRoutes()).verifyComplete();

        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES)).verifyComplete();

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesEmptyBeforeWithResponse");
        StepVerifier.create(client.setRoutes(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listRoutes()).verifyComplete();

        StepVerifier.create(client.setRoutesWithResponse(EXPECTED_ROUTES)).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).verifyComplete();

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesEmptyBeforeWithAAD(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithManagedIdentity(httpClient, "setRoutesEmptyBeforeWithAAD");
        StepVerifier.create(client.setRoutes(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listRoutes()).verifyComplete();

        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES)).verifyComplete();

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesNotEmptyBefore(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesNotEmptyBefore");
        StepVerifier.create(client.setRoutes(UPDATED_ROUTES)).verifyComplete();
        List<SipTrunkRoute> routesList = getAsList(client.listRoutes());
        assertEquals(UPDATED_ROUTES.size(), routesList.size());

        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES)).verifyComplete();

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesNotEmptyBeforeWithResponse");
        StepVerifier.create(client.setRoutes(UPDATED_ROUTES)).verifyComplete();
        List<SipTrunkRoute> routesList = getAsList(client.listRoutes());
        assertEquals(UPDATED_ROUTES.size(), routesList.size());

        StepVerifier.create(client.setRoutesWithResponse(EXPECTED_ROUTES)).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).verifyComplete();

        validateExpectedRoutes(client.listRoutes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesWithoutAffectingTrunks(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesWithoutAffectingTrunks");
        StepVerifier.create(client.setTrunks(EXPECTED_TRUNKS)).verifyComplete();

        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES)).verifyComplete();

        validateExpectedRoutes(client.listRoutes());
        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesWithoutAffectingTrunksWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesWithoutAffectingTrunksWithResponse");
        StepVerifier.create(client.setTrunks(EXPECTED_TRUNKS)).verifyComplete();

        StepVerifier.create(client.setRoutesWithResponse(EXPECTED_ROUTES)).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).verifyComplete();

        validateExpectedRoutes(client.listRoutes());
        List<SipTrunk> trunksList = getAsList(client.listTrunks());
        validateTrunks(EXPECTED_TRUNKS, trunksList);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesNotEmptyBefore(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesNotEmptyBefore");
        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES)).verifyComplete();
        validateExpectedRoutes(client.listRoutes());

        StepVerifier.create(client.setRoutes(new ArrayList<>())).verifyComplete();

        StepVerifier.create(client.listRoutes()).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesNotEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesNotEmptyBeforeWithResponse");
        StepVerifier.create(client.setRoutes(EXPECTED_ROUTES)).verifyComplete();
        validateExpectedRoutes(client.listRoutes());

        StepVerifier.create(client.setRoutesWithResponse(new ArrayList<>())).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).verifyComplete();

        StepVerifier.create(client.listRoutes()).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesEmptyBefore(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesEmptyBefore");
        StepVerifier.create(client.setRoutes(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listRoutes()).verifyComplete();

        StepVerifier.create(client.setRoutes(new ArrayList<>())).verifyComplete();

        StepVerifier.create(client.listRoutes()).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setEmptyRoutesEmptyBeforeWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setEmptyRoutesEmptyBeforeWithResponse");
        StepVerifier.create(client.setRoutes(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listRoutes()).verifyComplete();

        StepVerifier.create(client.setRoutesWithResponse(new ArrayList<>())).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).verifyComplete();

        StepVerifier.create(client.listRoutes()).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidName(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidName");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(null, SET_TRUNK_ROUTE_NUMBER_PATTERN);

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutes(asList(invalidRoute)).block());
        assertEquals(MESSAGE_INVALID_ROUTE_NAME, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidNameWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNameWithResponse");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(null, SET_TRUNK_ROUTE_NUMBER_PATTERN);

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutesWithResponse(asList(invalidRoute)).block());
        assertEquals(MESSAGE_INVALID_ROUTE_NAME, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidNumberPattern(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNumberPattern");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, null);

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutes(asList(invalidRoute)).block());
        assertEquals(MESSAGE_INVALID_NUMBER_PATTERN, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesInvalidNumberPatternWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesInvalidNumberPatternWithResponse");
        SipTrunkRoute invalidRoute = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, null);

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutesWithResponse(asList(invalidRoute)).block());
        assertEquals(MESSAGE_INVALID_NUMBER_PATTERN, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutes(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutes");
        List<SipTrunkRoute> invalidRoutes = asList(SET_TRUNK_ROUTE, SET_TRUNK_ROUTE);

        Throwable exception = assertThrows(HttpResponseException.class, () -> client.setRoutes(invalidRoutes).block());
        assertEquals(MESSAGE_DUPLICATE_ROUTES, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutesWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutesWithResponse");
        List<SipTrunkRoute> invalidRoutes = asList(SET_TRUNK_ROUTE, SET_TRUNK_ROUTE);

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutesWithResponse(invalidRoutes).block());
        assertEquals(MESSAGE_DUPLICATE_ROUTES, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutingTrunks(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutingTrunks");
        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();
        SipTrunkRoute routeWithDuplicatedTrunks = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, SET_TRUNK_ROUTE_NUMBER_PATTERN)
            .setTrunks(asList(SET_TRUNK_FQDN, SET_TRUNK_FQDN));

        Throwable exception = assertThrows(HttpResponseException.class, () -> client.setRoutes(asList(routeWithDuplicatedTrunks)).block());
        assertEquals(MESSAGE_DUPLICATE_TRUNKS, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesDuplicatedRoutingTrunksWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesDuplicatedRoutingTrunksWithResponse");
        StepVerifier.create(client.setTrunk(SET_TRUNK)).verifyComplete();
        SipTrunkRoute routeWithDuplicatedTrunks = new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, SET_TRUNK_ROUTE_NUMBER_PATTERN)
            .setTrunks(asList(SET_TRUNK_FQDN, SET_TRUNK_FQDN));

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutesWithResponse(asList(routeWithDuplicatedTrunks)).block());
        assertEquals(MESSAGE_DUPLICATE_TRUNKS, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesMissingTrunk(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesMissingTrunk");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutes(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK).block());
        assertEquals(MESSAGE_MISSING_TRUNK, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void setRoutesMissingTrunkWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "setRoutesMissingTrunkWithResponse");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();

        Throwable exception = assertThrows(HttpResponseException.class,
            () -> client.setRoutesWithResponse(EXPECTED_ROUTES_WITH_REFERENCED_TRUNK).block());
        assertEquals(MESSAGE_MISSING_TRUNK, exception.getMessage());

    }

    // delete trunk
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkExisting(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "deleteTrunkExisting");
        StepVerifier.create(client.setTrunk(DELETE_TRUNK)).verifyComplete();
        StepVerifier.create(client.getTrunk(DELETE_FQDN)).assertNext(Assertions::assertNotNull).verifyComplete();

        StepVerifier.create(client.deleteTrunk(DELETE_FQDN)).verifyComplete();

        StepVerifier.create(client.getTrunk(DELETE_FQDN)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkExistingWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "deleteTrunkExistingWithResponse");
        StepVerifier.create(client.setTrunk(DELETE_TRUNK)).verifyComplete();
        StepVerifier.create(client.getTrunk(DELETE_FQDN)).assertNext(Assertions::assertNotNull).verifyComplete();

        StepVerifier.create(client.deleteTrunkWithResponse(DELETE_FQDN)).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNull(response.getValue());
        }).verifyComplete();

        StepVerifier.create(client.getTrunk(DELETE_FQDN)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkExistingWithAAD(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithManagedIdentity(httpClient, "deleteTrunkExistingWithAAD");
        StepVerifier.create(client.setTrunk(DELETE_TRUNK)).verifyComplete();
        StepVerifier.create(client.getTrunk(DELETE_FQDN)).assertNext(Assertions::assertNotNull).verifyComplete();

        StepVerifier.create(client.deleteTrunk(DELETE_FQDN)).verifyComplete();

        StepVerifier.create(client.getTrunk(DELETE_FQDN)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkNotExisting(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "deleteTrunkNotExisting");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listTrunks()).verifyComplete();
        StepVerifier.create(client.getTrunk(DELETE_FQDN)).verifyComplete();

        StepVerifier.create(client.deleteTrunk(DELETE_FQDN)).verifyComplete();

        StepVerifier.create(client.getTrunk(DELETE_FQDN)).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteTrunkNotExistingWithResponse(HttpClient httpClient) {
        SipRoutingAsyncClient client = getClientWithConnectionString(httpClient, "deleteTrunkNotExistingWithResponse");
        StepVerifier.create(client.setTrunks(new ArrayList<>())).verifyComplete();
        StepVerifier.create(client.listTrunks()).verifyComplete();
        StepVerifier.create(client.getTrunk(DELETE_FQDN)).verifyComplete();

        StepVerifier.create(client.deleteTrunkWithResponse(DELETE_FQDN)).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNull(response.getValue());
        }).verifyComplete();

        StepVerifier.create(client.getTrunk(DELETE_FQDN)).verifyComplete();
    }

    private void validateTrunks(List<SipTrunk> expected, List<SipTrunk> actual) {
        assertEquals(expected.size(), actual.size());
        for (SipTrunk expectedTrunk : expected) {
            Optional<SipTrunk> actualTrunk = actual.stream()
                .filter(value -> Objects.equals(expectedTrunk.getFqdn(), value.getFqdn())).findAny();
            assertTrue(actualTrunk.isPresent());
            assertEquals(expectedTrunk.getSipSignalingPort(), actualTrunk.get().getSipSignalingPort());
        }
    }

    private void validateExpectedRoutes(PagedFlux<SipTrunkRoute> routes) {
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

    private SipRoutingAsyncClient getClientWithConnectionString(HttpClient httpClient, String testName) {
        SipRoutingClientBuilder builder = super.getClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    private SipRoutingAsyncClient getClientWithManagedIdentity(HttpClient httpClient, String testName) {
        SipRoutingClientBuilder builder = super.getClientBuilderUsingManagedIdentity(httpClient);
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    private <T> List<T> getAsList(PagedFlux<T> flux) {
        List<T> list = new ArrayList<T>();

        flux.toIterable().forEach(item -> {
            list.add(item);
        });

        return list;
    }
}
