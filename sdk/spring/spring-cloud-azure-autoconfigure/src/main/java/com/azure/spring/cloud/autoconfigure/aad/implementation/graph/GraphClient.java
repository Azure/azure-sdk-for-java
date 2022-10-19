// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.graph;

import com.azure.spring.cloud.autoconfigure.aad.implementation.util.JacksonObjectMapperFactory;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.io.IOException;
import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestTemplateCreator.createRestTemplate;

/**
 * GraphClient is used to access graph server. Mainly used to get groups information of a user.
 */
public class GraphClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphClient.class);

    private final AadAuthenticationProperties properties;
    private final RestOperations operations;

    /**
     * Creates a new instance of {@link GraphClient}.
     *
     * @param properties the AAD authentication properties
     * @param restTemplateBuilder the restTemplateBuilder
     */
    public GraphClient(AadAuthenticationProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.operations = createRestTemplate(restTemplateBuilder);
    }

    /**
     * Gets the group information.
     *
     * @param accessToken the access token
     * @return the group information
     */
    public GroupInformation getGroupInformation(String accessToken) {
        GroupInformation groupInformation = new GroupInformation();
        final ObjectMapper objectMapper = JacksonObjectMapperFactory.getInstance();
        String aadMembershipRestUri = properties.getGraphMembershipUri();
        while (aadMembershipRestUri != null) {
            Memberships memberships;
            try {
                String membershipsJson = getUserMemberships(accessToken, aadMembershipRestUri);
                memberships = objectMapper.readValue(membershipsJson, Memberships.class);
            } catch (IOException ioException) {
                LOGGER.error("Can not get group information from graph server.", ioException);
                break;
            }
            for (Membership membership : memberships.getValue()) {
                if (isGroupObject(membership)) {
                    groupInformation.getGroupsIds().add(membership.getObjectID());
                    groupInformation.getGroupsNames().add(membership.getDisplayName());
                }
            }
            aadMembershipRestUri = Optional.of(memberships)
                                           .map(Memberships::getOdataNextLink)
                                           .orElse(null);
        }
        return groupInformation;
    }

    private String getUserMemberships(String accessToken, String urlString) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = operations.exchange(urlString, HttpMethod.GET, entity, String.class);
        String responseInJson = response.getBody();
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new IllegalStateException(
                "Response is not " + HTTPResponse.SC_OK + ", response json: " + responseInJson);
        }
    }

    private boolean isGroupObject(final Membership membership) {
        return membership.getObjectType().equals(Membership.OBJECT_TYPE_GROUP);
    }
}
