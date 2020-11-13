// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


public class UserPrincipalAzureADGraphTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9519);

    private GraphWebClient graphWebClient;
    private AADAuthenticationProperties aadAuthenticationProperties;
    private static String userGroupsJson;

    static {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final Map<String, Object> json = objectMapper.readValue(
                UserPrincipalAzureADGraphTest.class
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
        ServiceEndpointsProperties serviceEndpointsProperties = new ServiceEndpointsProperties();
        final ServiceEndpoints serviceEndpoints = new ServiceEndpoints();
        serviceEndpoints.setAadMembershipRestUri("http://localhost:9519/memberOf");
        serviceEndpointsProperties.getEndpoints().put("global", serviceEndpoints);
        this.graphWebClient = new GraphWebClient(
            aadAuthenticationProperties,
            serviceEndpointsProperties,
            createWebClientForTest()
        );
    }

    public static WebClient createWebClientForTest() {
        ClientRegistration clientRegistration =
            ClientRegistration.withRegistrationId("graph")
                              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                              .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
                              .clientId("test")
                              .clientSecret("test")
                              .authorizationUri("test")
                              .tokenUri("test")
                              .jwkSetUri("test")
                              .build();
        OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager = authorizeRequest -> new OAuth2AuthorizedClient(
            clientRegistration,
            "principalName",
            new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                TestConstants.ACCESS_TOKEN,
                Instant.now().minus(10, ChronoUnit.MINUTES),
                Instant.now().plus(10, ChronoUnit.MINUTES)
            )
        );
        ServletOAuth2AuthorizedClientExchangeFilterFunction servletOAuth2AuthorizedClientExchangeFilterFunction =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(oAuth2AuthorizedClientManager);
        return WebClient.builder()
                        .apply(servletOAuth2AuthorizedClientExchangeFilterFunction.oauth2Configuration())
                        .build();
    }

    @Test
    public void getAuthoritiesByUserGroups() {
        aadAuthenticationProperties.getUserGroup().setAllowedGroups(Collections.singletonList("group1"));
        stubFor(get(urlEqualTo("/memberOf"))
            .withHeader(ACCEPT, equalTo("application/json;odata=minimalmetadata"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(userGroupsJson)));

        assertThat(graphWebClient.getGrantedAuthorities())
            .isNotEmpty()
            .extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_group1");

        verify(getRequestedFor(urlMatching("/memberOf"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(String.format("Bearer %s", TestConstants.ACCESS_TOKEN)))
            .withHeader(ACCEPT, equalTo("application/json;odata=minimalmetadata"))
            .withHeader("api-version", equalTo("1.6")));
    }

    @Test
    public void getGroups() {
        aadAuthenticationProperties.getUserGroup().setAllowedGroups(Arrays.asList("group1", "group2", "group3"));
        stubFor(get(urlEqualTo("/memberOf"))
            .withHeader(ACCEPT, equalTo("application/json;odata=minimalmetadata"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(userGroupsJson)));

        final Collection<? extends GrantedAuthority> authorities = graphWebClient.getGrantedAuthorities();

        assertThat(authorities).isNotEmpty().extracting(GrantedAuthority::getAuthority)
                               .containsExactlyInAnyOrder("ROLE_group1", "ROLE_group2", "ROLE_group3");

        verify(getRequestedFor(urlMatching("/memberOf"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(String.format("Bearer %s", TestConstants.ACCESS_TOKEN)))
            .withHeader(ACCEPT, equalTo("application/json;odata=minimalmetadata"))
            .withHeader("api-version", equalTo("1.6")));
    }
}
