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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserPrincipalMicrosoftGraphTests {

    private static final String MOCK_MICROSOFT_GRAPH_ENDPOINT = "http://localhost:8080/";

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
        properties.getProfile().getEnvironment().setMicrosoftGraphEndpoint(MOCK_MICROSOFT_GRAPH_ENDPOINT);
        endpoints = new AadAuthorizationServerEndpoints(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint(), properties.getProfile().getTenantId());
        clientId = "client";
        clientSecret = "pass";
    }

    @Test
    void getGroups() throws Exception {
        properties.getUserGroup().setAllowedGroupNames(Arrays.asList("group1", "group2", "group3"));

        RestTemplate template = new RestTemplate();
        AadGraphClient client = new AadGraphClient(clientId, clientSecret, properties, endpoints, new RestTemplateBuilder());
        client.setRestOperations(template);

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(template);
        mockServer
                .expect(ExpectedCount.once(), requestTo(new URI(MOCK_MICROSOFT_GRAPH_ENDPOINT + "v1.0/me/memberOf")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ACCEPT, APPLICATION_JSON_VALUE))
                .andExpect(header(AUTHORIZATION, String.format("Bearer %s", accessToken)))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(userGroupsJson));

        Set<String> groups = client.getGroups(MicrosoftGraphConstants.BEARER_TOKEN);
        assertThat(groups)
            .isNotEmpty()
            .containsExactlyInAnyOrder("group1", "group2", "group3");

        mockServer.verify();
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
