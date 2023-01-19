// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.graph;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
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

    private String accessToken = "fake-accesstoken";
    private String graphMembershipUri = "fake-url";

    @Test
    void testGetGroupInformationCorrectly() {
        AadAuthenticationProperties properties = new AadAuthenticationProperties() {
            @Override
            public String getGraphMembershipUri() {
                return graphMembershipUri;
            }
        };
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        RestTemplate operations = mock(RestTemplate.class);
        Memberships memberships = new Memberships(null, new ArrayList<>());
        ResponseEntity<Memberships> response = new ResponseEntity<>(memberships, HttpStatus.OK);
        when(restTemplateBuilder.build()).thenReturn(operations);
        GraphClient graphClient = new GraphClient(properties, restTemplateBuilder);
        when(operations.exchange(any(), eq(HttpMethod.GET), any(), eq(Memberships.class), any(Object[].class))).thenReturn(response);
        Optional<Memberships> userMemberships = graphClient.getUserMemberships(accessToken, graphMembershipUri);
        assertTrue(userMemberships.isPresent());
    }

    @Test
    void testGetGroupInformationWithBadHttpStatus() {
        AadAuthenticationProperties properties = new AadAuthenticationProperties() {
            @Override
            public String getGraphMembershipUri() {
                return graphMembershipUri;
            }
        };
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        RestTemplate operations = mock(RestTemplate.class);
        Memberships memberships = new Memberships(null, new ArrayList<>());
        ResponseEntity<Memberships> response = new ResponseEntity<>(memberships, HttpStatus.BAD_REQUEST);
        when(restTemplateBuilder.build()).thenReturn(operations);
        GraphClient graphClient = new GraphClient(properties, restTemplateBuilder);
        when(operations.exchange(any(), eq(HttpMethod.GET), any(), eq(Memberships.class), any(Object[].class))).thenReturn(response);
        Optional<Memberships> userMemberships = graphClient.getUserMemberships(accessToken, graphMembershipUri);
        assertTrue(userMemberships.isEmpty());
    }

    @Test
    void testGetGroupInformationWithNotFoundError() {
        AadAuthenticationProperties properties = new AadAuthenticationProperties() {
            @Override
            public String getGraphMembershipUri() {
                return graphMembershipUri;
            }
        };
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        RestTemplate operations = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(operations);
        GraphClient graphClient = new GraphClient(properties, restTemplateBuilder);
        when(operations.exchange(any(), eq(HttpMethod.GET), any(), eq(Memberships.class), any(Object[].class))).thenThrow(HttpClientErrorException.NotFound.class);
        Optional<Memberships> userMemberships = graphClient.getUserMemberships(accessToken, graphMembershipUri);
        assertTrue(userMemberships.isEmpty());
    }

    @Test
    void testGetGroupInformationWithInternalServerError() throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        AadAuthenticationProperties properties = new AadAuthenticationProperties() {
            @Override
            public String getGraphMembershipUri() {
                return "http://localhost:8080/v1.0/me/memberOf";
            }
        };
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        GraphClient graphClient = new GraphClient(properties, restTemplateBuilder);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer
            .expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8080/v1.0/me/memberOf")))
            .andRespond(withServerError());
        Optional<Memberships> userMemberships =
            graphClient.getUserMemberships(accessToken, "http://localhost:8080/v1.0/me/memberOf");
        assertTrue(userMemberships.isEmpty());
    }

}
