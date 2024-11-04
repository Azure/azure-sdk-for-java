// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security.graph;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GraphClientTest {

    private static final String FAKE_ACCESS_TOKEN = "fake-accesstoken";
    private static final String FAKE_GRAPH_MEMBERSHIP_URI = "http://localhost:8080/v1.0/me/memberOf";

    @Test
    void testGetUserMembershipsCorrectly() throws JsonProcessingException {
        Memberships memberships = new Memberships(null, new ArrayList<>());
        RestTemplate restTemplate = new RestTemplate();
        GraphClient graphClient = new GraphClient(new AadAuthenticationProperties(), restTemplate);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(FAKE_GRAPH_MEMBERSHIP_URI)).andRespond(withSuccess(new ObjectMapper().writeValueAsString(memberships), MediaType.APPLICATION_JSON));

        Optional<Memberships> userMemberships = graphClient.getUserMemberships(FAKE_ACCESS_TOKEN, FAKE_GRAPH_MEMBERSHIP_URI);

        assertTrue(userMemberships.isPresent());
    }

    @Test
    void testGetUserMembershipsWithNoContentError() {
        RestTemplate restTemplate = new RestTemplate();
        GraphClient graphClient = new GraphClient(new AadAuthenticationProperties(), restTemplate);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(FAKE_GRAPH_MEMBERSHIP_URI)).andRespond(withNoContent());

        Optional<Memberships> userMemberships = graphClient.getUserMemberships(FAKE_ACCESS_TOKEN, FAKE_GRAPH_MEMBERSHIP_URI);

        assertFalse(userMemberships.isPresent());
    }

    @Test
    void testGetUserMembershipsWithNotFoundError() {
        RestTemplate restTemplate = new RestTemplate();
        GraphClient graphClient = new GraphClient(new AadAuthenticationProperties(), restTemplate);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(FAKE_GRAPH_MEMBERSHIP_URI)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<Memberships> userMemberships = graphClient.getUserMemberships(FAKE_ACCESS_TOKEN, FAKE_GRAPH_MEMBERSHIP_URI);

        assertFalse(userMemberships.isPresent());
    }

    @Test
    void testGetUserMembershipsWithInternalServerError() {
        RestTemplate restTemplate = new RestTemplate();
        GraphClient graphClient = new GraphClient(new AadAuthenticationProperties(), restTemplate);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(FAKE_GRAPH_MEMBERSHIP_URI)).andRespond(withServerError());

        Optional<Memberships> userMemberships = graphClient.getUserMemberships(FAKE_ACCESS_TOKEN, FAKE_GRAPH_MEMBERSHIP_URI);

        assertFalse(userMemberships.isPresent());
    }

}
