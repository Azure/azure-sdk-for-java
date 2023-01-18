// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.graph;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({ OutputCaptureExtension.class})
class GraphClientTest {

    @Test
    void testGetGroupInformationCorrectly() {
        AadAuthenticationProperties properties = mock(AadAuthenticationProperties.class);
        when(properties.getGraphMembershipUri()).thenReturn("https://graph.microsoft.com/v1.0/me/memberOf");
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
    void testGetGroupInformationWithRestClientException(CapturedOutput capturedOutput) {
        AadAuthenticationProperties properties = mock(AadAuthenticationProperties.class);
        when(properties.getGraphMembershipUri()).thenReturn("https://graph.microsoft.com/v1.0/me/memberOf");
        GraphClient graphClient = new GraphClient(properties, new RestTemplateBuilder());
        graphClient.getGroupInformation("fake-accesstoken");
        String allOutput = capturedOutput.getAll();
        assertTrue(allOutput.contains("Can not get group information from graph server."));
    }
}
