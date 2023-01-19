// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.graph;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

@ExtendWith({ OutputCaptureExtension.class })
@Isolated("Run this by itself as it captures System.out")
class GraphClientTest {

    @Test
    void testGetGroupInformationCorrectly() {
        AadAuthenticationProperties properties = new AadAuthenticationProperties();
        properties.getProfile().getEnvironment().setMicrosoftGraphEndpoint("https://graph.microsoft.com/");
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        RestTemplate operations = mock(RestTemplate.class);
        Memberships memberships = new Memberships(null, new ArrayList<>());
        ResponseEntity<Memberships> response = new ResponseEntity<>(memberships, HttpStatus.OK);
        when(restTemplateBuilder.build()).thenReturn(operations);
        GraphClient graphClient = new GraphClient(properties, restTemplateBuilder);
        when(operations.exchange(any(), eq(HttpMethod.GET), any(), eq(Memberships.class), any(Object[].class))).thenReturn(response);
        GroupInformation groupInformation = graphClient.getGroupInformation("fake-accesstoken");
        assertNotNull(groupInformation);
    }

    @Test
    void testGetGroupInformationWithBadHttpStatus(CapturedOutput capturedOutput) {
        AadAuthenticationProperties properties = new AadAuthenticationProperties();
        properties.getProfile().getEnvironment().setMicrosoftGraphEndpoint("https://graph.microsoft.com/");
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        RestTemplate operations = mock(RestTemplate.class);
        Memberships memberships = new Memberships(null, new ArrayList<>());
        ResponseEntity<Memberships> response = new ResponseEntity<>(memberships, HttpStatus.BAD_REQUEST);
        when(restTemplateBuilder.build()).thenReturn(operations);
        GraphClient graphClient = new GraphClient(properties, restTemplateBuilder);
        when(operations.exchange(any(), eq(HttpMethod.GET), any(), eq(Memberships.class), any(Object[].class))).thenReturn(response);
        graphClient.getGroupInformation("fake-accesstoken");
        String allOutput = capturedOutput.getAll();
        assertTrue(allOutput.contains("Response code [400 BAD_REQUEST] is not 200, the response body is"));
    }

    @Test
    void testGetGroupInformationWithNotFoundError(CapturedOutput capturedOutput) {
        AadAuthenticationProperties properties = mock(AadAuthenticationProperties.class);
        when(properties.getGraphMembershipUri()).thenReturn("https://graph.microsoft.com/newurl1");
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        RestTemplate operations = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(operations);
        GraphClient graphClient = new GraphClient(properties, restTemplateBuilder);
        when(operations.exchange(any(), eq(HttpMethod.GET), any(), eq(Memberships.class), any(Object[].class))).thenThrow(HttpClientErrorException.NotFound.class);
        graphClient.getGroupInformation("fake-accesstoken");
        String allOutput = capturedOutput.getAll();
        assertTrue(allOutput.contains("Can not get group information from graph server.")
            && allOutput.contains("org.springframework.web.client.HttpClientErrorException$NotFound"));
    }

    @Test
    void testGetGroupInformationWithInternalServerError(CapturedOutput capturedOutput) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        AadAuthenticationProperties properties = new AadAuthenticationProperties();
        properties.getProfile().getEnvironment().setMicrosoftGraphEndpoint("http://localhost:8080/");
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        GraphClient graphClient = new GraphClient(properties, restTemplateBuilder);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer
            .expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8080/v1.0/me/memberOf")))
            .andRespond(withServerError());
        graphClient.getGroupInformation("fake-accesstoken");
        String allOutput = capturedOutput.getAll();
        assertTrue(allOutput.contains("Can not get group information from graph server.")
            && allOutput.contains("org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 "
            + "Internal Server Error"));
    }

}
