// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.graph;

import com.azure.spring.cloud.autoconfigure.aad.filter.UserPrincipal;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationServerEndpoints;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Disabled("No available jakarta version for 'com.github.tomakehurst:wiremock-jre8'")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@WireMockTest(httpPort = 8080)
class UserPrincipalMicrosoftGraphTests {

    private String clientId;
    private String clientSecret;
    private AadAuthenticationProperties properties;
    private AadAuthorizationServerEndpoints endpoints;
    private String accessToken;
    private static String userGroupsJson;

    static {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final Map<String, Object> json = objectMapper.readValue(UserPrincipalMicrosoftGraphTests.class
                    .getClassLoader().getResourceAsStream("aad/microsoft-graph-user-groups.json"),
                new TypeReference<HashMap<String, Object>>() {
                });
            userGroupsJson = objectMapper.writeValueAsString(json);
        } catch (IOException e) {
            e.printStackTrace();
            userGroupsJson = null;
        }
        assertNotNull(userGroupsJson);
    }

    @BeforeAll
    void setup() {
        accessToken = MicrosoftGraphConstants.BEARER_TOKEN;
        properties = new AadAuthenticationProperties();
        properties.getProfile().getEnvironment().setMicrosoftGraphEndpoint("http://localhost:8080/");
        endpoints = new AadAuthorizationServerEndpoints(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint(), properties.getProfile().getTenantId());
        clientId = "client";
        clientSecret = "pass";
    }

    @Test
    void getGroups() throws Exception {
        properties.getUserGroup().setAllowedGroupNames(Arrays.asList("group1", "group2", "group3"));
        AadGraphClient graphClientMock = new AadGraphClient(clientId, clientSecret, properties,
            endpoints);
        stubFor(get(urlEqualTo("/v1.0/me/memberOf"))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(userGroupsJson)));

        Set<String> groups = graphClientMock.getGroups(MicrosoftGraphConstants.BEARER_TOKEN);
        assertThat(groups)
            .isNotEmpty()
            .containsExactlyInAnyOrder("group1", "group2", "group3");

        verify(getRequestedFor(urlMatching("/v1.0/me/memberOf"))
            .withHeader(AUTHORIZATION, equalTo(String.format("Bearer %s", accessToken)))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    void userPrincipalIsSerializable() throws ParseException, IOException, ClassNotFoundException {
        final File tmpOutputFile = File.createTempFile("test-user-principal", "txt");

        try (FileOutputStream fileOutputStream = new FileOutputStream(tmpOutputFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
             FileInputStream fileInputStream = new FileInputStream(tmpOutputFile);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            final JWSObject jwsObject = JWSObject.parse(MicrosoftGraphConstants.JWT_TOKEN);
            final JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject("fake-subject").build();
            final UserPrincipal principal = new UserPrincipal("", jwsObject, jwtClaimsSet);

            objectOutputStream.writeObject(principal);

            final UserPrincipal serializedPrincipal = (UserPrincipal) objectInputStream.readObject();

            assertNotNull(serializedPrincipal, "Serialized UserPrincipal not null");
            assertTrue(StringUtils.hasText(serializedPrincipal.getKeyId()), "Serialized UserPrincipal kid not empty");
            assertNotNull(serializedPrincipal.getClaims(), "Serialized UserPrincipal claims not null.");
            assertTrue(serializedPrincipal.getClaims().size() > 0, "Serialized UserPrincipal claims not empty.");
        } finally {
            Files.deleteIfExists(tmpOutputFile.toPath());
        }
    }
}
