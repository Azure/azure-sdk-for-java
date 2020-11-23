// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.TestConstants;
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

public class GraphWebClientMicrosoftGraphTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9519);

    private GraphWebClient graphWebClient;
    private AADAuthenticationProperties aadAuthenticationProperties;
    private static String userGroupsJson;

    static {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final Map<String, Object> json = objectMapper.readValue(
                GraphWebClientMicrosoftGraphTest.class
                    .getClassLoader()
                    .getResourceAsStream("aad/microsoft-graph-user-groups.json"),
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
        aadAuthenticationProperties.setEnvironment("global-v2-graph");
        aadAuthenticationProperties.getUserGroup().setKey("@odata.type");
        aadAuthenticationProperties.getUserGroup().setValue("#microsoft.graph.group");
        aadAuthenticationProperties.getUserGroup().setObjectIDKey("id");
        aadAuthenticationProperties.setGraphMembershipUri("http://localhost:9519/memberOf");
    }

    @Test
    public void getAuthoritiesByUserGroups() {
        aadAuthenticationProperties.getUserGroup().setAllowedGroups(Collections.singletonList("group1"));
        this.graphWebClient = new GraphWebClient(
            aadAuthenticationProperties,
            GraphWebClientTestUtil.createWebClientForTest()
        );

        stubFor(get(urlEqualTo("/memberOf"))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(userGroupsJson)));

        assertThat(graphWebClient.getGrantedAuthorities())
            .isNotEmpty()
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_group1");

        verify(getRequestedFor(urlMatching("/memberOf"))
            .withHeader(AUTHORIZATION, equalTo(String.format("Bearer %s", TestConstants.ACCESS_TOKEN)))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    public void getDirectGroups() {
        AADAuthenticationProperties.UserGroupProperties userGroupProperties =
            aadAuthenticationProperties.getUserGroup();
        userGroupProperties.setAllowedGroups(Arrays.asList("group1", "group2", "group3"));
        aadAuthenticationProperties.setUserGroup(userGroupProperties);
        this.graphWebClient = new GraphWebClient(
            aadAuthenticationProperties,
            GraphWebClientTestUtil.createWebClientForTest()
        );

        stubFor(get(urlEqualTo("/memberOf"))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(userGroupsJson)));

        final Collection<? extends GrantedAuthority> authorities = graphWebClient.getGrantedAuthorities();

        assertThat(authorities)
            .isNotEmpty()
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_group1", "ROLE_group2", "ROLE_group3");

        verify(getRequestedFor(urlMatching("/memberOf"))
            .withHeader(AUTHORIZATION, equalTo(String.format("Bearer %s", TestConstants.ACCESS_TOKEN)))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE)));
    }
}
