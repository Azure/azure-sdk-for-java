// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class GraphOboClientAzureADGraphTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9519);

    private GraphOboClient graphOboClient;
    private AADAuthenticationProperties aadAuthenticationProperties;
    private ServiceEndpointsProperties serviceEndpointsProperties;
    private static String userGroupsJson;

    static {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final Map<String, Object> json = objectMapper.readValue(
                GraphOboClientAzureADGraphTest.class
                    .getClassLoader()
                    .getResourceAsStream("aad/azure-ad-graph-user-groups.json"),
                new TypeReference<HashMap<String, Object>>() {
                }
            );
            userGroupsJson = objectMapper.writeValueAsString(json);
        } catch (IOException e) {
            e.printStackTrace();
            userGroupsJson = null;
        }
        Assert.assertNotNull(userGroupsJson);
    }

    @Before
    public void setup() {
        aadAuthenticationProperties = new AADAuthenticationProperties();
        serviceEndpointsProperties = new ServiceEndpointsProperties();
        final ServiceEndpoints serviceEndpoints = new ServiceEndpoints();
        serviceEndpoints.setAadMembershipRestUri("http://localhost:9519/memberOf");
        serviceEndpointsProperties.getEndpoints().put("global", serviceEndpoints);
    }

    @Test
    public void getAuthoritiesByUserGroups() throws Exception {
        aadAuthenticationProperties.getUserGroup().setAllowedGroups(Collections.singletonList("group1"));
        this.graphOboClient = new GraphOboClient(aadAuthenticationProperties, serviceEndpointsProperties);

        stubFor(get(urlEqualTo("/memberOf"))
            .withHeader(ACCEPT, equalTo("application/json;odata=minimalmetadata"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(userGroupsJson)));

        assertThat(graphOboClient.getGrantedAuthorities(TestConstants.ACCESS_TOKEN))
            .isNotEmpty()
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_group1");

        verify(getRequestedFor(urlMatching("/memberOf"))
            .withHeader(AUTHORIZATION, equalTo(TestConstants.BEARER_TOKEN))
            .withHeader(ACCEPT, equalTo("application/json;odata=minimalmetadata"))
            .withHeader("api-version", equalTo("1.6")));
    }

    @Test
    public void getGroups() throws Exception {
        aadAuthenticationProperties.getUserGroup().setAllowedGroups(Arrays.asList("group1", "group2", "group3"));
        this.graphOboClient = new GraphOboClient(aadAuthenticationProperties, serviceEndpointsProperties);

        stubFor(get(urlEqualTo("/memberOf"))
            .withHeader(ACCEPT, equalTo("application/json;odata=minimalmetadata"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(userGroupsJson)));

        final Collection<? extends GrantedAuthority> authorities = graphOboClient
            .getGrantedAuthorities(TestConstants.ACCESS_TOKEN);

        assertThat(authorities)
            .isNotEmpty()
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_group1", "ROLE_group2", "ROLE_group3");

        verify(getRequestedFor(urlMatching("/memberOf"))
            .withHeader(AUTHORIZATION, equalTo(TestConstants.BEARER_TOKEN))
            .withHeader(ACCEPT, equalTo("application/json;odata=minimalmetadata"))
            .withHeader("api-version", equalTo("1.6")));
    }
}
