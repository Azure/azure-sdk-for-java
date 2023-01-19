// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.graph;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

class GraphClientTest {

    private static final String accessToken = "fake-accesstoken";
    private static final String graphMembershipUri = "fake-url";

    @Test
    void testGetUserMembershipsCorrectly() {
        RestTemplate operations = mock(RestTemplate.class);
        Memberships memberships = new Memberships(null, new ArrayList<>());
        ResponseEntity<Memberships> response = new ResponseEntity<>(memberships, HttpStatus.OK);
        GraphClient graphClient = new GraphClient(new AadAuthenticationProperties(), operations);

        when(operations.exchange(any(), eq(HttpMethod.GET), any(), eq(Memberships.class), any(Object[].class))).thenReturn(response);
        Optional<Memberships> userMemberships = graphClient.getUserMemberships(accessToken, graphMembershipUri);

        assertTrue(userMemberships.isPresent());
    }

    @Test
    void testGetUserMembershipsWithBadHttpStatusError() {
        RestTemplate operations = mock(RestTemplate.class);
        ResponseEntity<Memberships> response = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        GraphClient graphClient = new GraphClient(new AadAuthenticationProperties(), operations);

        when(operations.exchange(any(), eq(HttpMethod.GET), any(), eq(Memberships.class), any(Object[].class))).thenReturn(response);
        Optional<Memberships> userMemberships = graphClient.getUserMemberships(accessToken, graphMembershipUri);

        assertTrue(userMemberships.isEmpty());
    }

    @Test
    void testGetUserMembershipsWithNotFoundError() {
        RestTemplate operations = mock(RestTemplate.class);
        GraphClient graphClient = new GraphClient(new AadAuthenticationProperties(), operations);

        when(operations.exchange(any(), eq(HttpMethod.GET), any(), eq(Memberships.class), any(Object[].class))).thenThrow(HttpClientErrorException.NotFound.class);
        Optional<Memberships> userMemberships = graphClient.getUserMemberships(accessToken, graphMembershipUri);

        assertTrue(userMemberships.isEmpty());
    }

    @Test
    void testGetUserMembershipsWithInternalServerError() throws URISyntaxException {
        String membershipUrl = "http://localhost:8080/v1.0/me/memberOf";
        RestTemplate restTemplate = new RestTemplate();
        GraphClient graphClient = new GraphClient(new AadAuthenticationProperties(), restTemplate);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer.expect(ExpectedCount.once(), requestTo(new URI(membershipUrl))).andRespond(withServerError());
        Optional<Memberships> userMemberships = graphClient.getUserMemberships(accessToken, membershipUrl);

        assertTrue(userMemberships.isEmpty());
    }

}
