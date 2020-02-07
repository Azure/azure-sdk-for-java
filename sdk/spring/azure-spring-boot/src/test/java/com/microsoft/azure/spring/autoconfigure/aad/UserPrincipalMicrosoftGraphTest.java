/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;


public class UserPrincipalMicrosoftGraphTest {
    private AzureADGraphClient graphClientMock;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9519);

    private String clientId;
    private String clientSecret;
    private AADAuthenticationProperties aadAuthProps;
    private ServiceEndpointsProperties endpointsProps;
    private String accessToken;


    @Before
    public void setup() {
        accessToken = MicrosoftGraphConstants.BEARER_TOKEN;
        aadAuthProps = new AADAuthenticationProperties();
        aadAuthProps.setEnvironment("global-v2-graph");
        aadAuthProps.getUserGroup().setKey("@odata.type");
        aadAuthProps.getUserGroup().setValue("#microsoft.graph.group");
        aadAuthProps.getUserGroup().setObjectIDKey("id");
        endpointsProps = new ServiceEndpointsProperties();
        final ServiceEndpoints serviceEndpoints = new ServiceEndpoints();
        serviceEndpoints.setAadMembershipRestUri("http://localhost:9519/memberOf");
        endpointsProps.getEndpoints().put("global-v2-graph", serviceEndpoints);
        clientId = "client";
        clientSecret = "pass";
    }


    @Test
    public void getAuthoritiesByUserGroups() throws Exception {
        
        aadAuthProps.getUserGroup().setAllowedGroups(Collections.singletonList("group1"));
        this.graphClientMock = new AzureADGraphClient(clientId, clientSecret, aadAuthProps, endpointsProps);

        stubFor(get(urlEqualTo("/memberOf")).withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(aResponse().withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(MicrosoftGraphConstants.USERGROUPS_JSON)));

        assertThat(graphClientMock.getGrantedAuthorities(MicrosoftGraphConstants.BEARER_TOKEN)).isNotEmpty()
                .extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_group1");

        verify(getRequestedFor(urlMatching("/memberOf"))
                .withHeader(HttpHeaders.AUTHORIZATION, 
                        equalTo(String.format("%s %s", OAuth2AccessToken.TokenType.BEARER.getValue(), accessToken)))
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    public void getGroups() throws Exception {

        aadAuthProps.setActiveDirectoryGroups(Arrays.asList("group1", "group2", "group3"));
        this.graphClientMock = new AzureADGraphClient(clientId, clientSecret, aadAuthProps, endpointsProps);

        stubFor(get(urlEqualTo("/memberOf")).withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(aResponse().withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(MicrosoftGraphConstants.USERGROUPS_JSON)));
        final Collection<? extends GrantedAuthority> authorities = graphClientMock
                .getGrantedAuthorities(MicrosoftGraphConstants.BEARER_TOKEN);

        assertThat(authorities).isNotEmpty().extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_group1", "ROLE_group2", "ROLE_group3");

        verify(getRequestedFor(urlMatching("/memberOf"))
                .withHeader(HttpHeaders.AUTHORIZATION, 
                        equalTo(String.format("%s %s", OAuth2AccessToken.TokenType.BEARER.getValue(), accessToken)))
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    public void userPrinciplaIsSerializable() throws ParseException, IOException, ClassNotFoundException {
        final File tmpOutputFile = File.createTempFile("test-user-principal", "txt");

        try (final FileOutputStream fileOutputStream = new FileOutputStream(tmpOutputFile);
             final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
             final FileInputStream fileInputStream = new FileInputStream(tmpOutputFile);
                final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);) {

            final JWSObject jwsObject = JWSObject.parse(MicrosoftGraphConstants.JWT_TOKEN);
            final JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject("fake-subject").build();
            final UserPrincipal principal = new UserPrincipal(jwsObject, jwtClaimsSet);

            objectOutputStream.writeObject(principal);

            final UserPrincipal serializedPrincipal = (UserPrincipal) objectInputStream.readObject();

            Assert.assertNotNull("Serialized UserPrincipal not null", serializedPrincipal);
            Assert.assertTrue("Serialized UserPrincipal kid not empty",
                    !StringUtils.isEmpty(serializedPrincipal.getKid()));
            Assert.assertNotNull("Serialized UserPrincipal claims not null.", serializedPrincipal.getClaims());
            Assert.assertTrue("Serialized UserPrincipal claims not empty.",
                    serializedPrincipal.getClaims().size() > 0);
        } finally {
            Files.deleteIfExists(tmpOutputFile.toPath());
        }
    }
}
