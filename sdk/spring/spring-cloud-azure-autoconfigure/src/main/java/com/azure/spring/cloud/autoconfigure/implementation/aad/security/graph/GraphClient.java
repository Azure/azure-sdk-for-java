// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security.graph;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createRestTemplate;

/**
 * GraphClient is used to access graph server. Mainly used to get groups information of a user.
 */
public class GraphClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphClient.class);

    private final AadAuthenticationProperties properties;
    private final RestOperations operations;

    GraphClient(AadAuthenticationProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.operations = restTemplate;
    }

    /**
     * Creates a new instance of {@link GraphClient}.
     *
     * @param properties the AAD authentication properties
     * @param restTemplateBuilder the restTemplateBuilder
     */
    public GraphClient(AadAuthenticationProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this(properties, createRestTemplate(restTemplateBuilder));
    }

    /**
     * Gets the group information.
     *
     * @param accessToken the access token
     * @return the group information
     */
    public GroupInformation getGroupInformation(String accessToken) {
        GroupInformation groupInformation = new GroupInformation();
        String aadMembershipRestUri = properties.getGraphMembershipUri();
        while (aadMembershipRestUri != null) {
            Optional<Memberships> userMemberships = getUserMemberships(accessToken, aadMembershipRestUri);
            if (userMemberships.isPresent()) {
                for (Membership membership : userMemberships.get().getValue()) {
                    if (isGroupObject(membership)) {
                        groupInformation.getGroupsIds().add(membership.getObjectID());
                        groupInformation.getGroupsNames().add(membership.getDisplayName());
                    }
                }
                aadMembershipRestUri = userMemberships
                    .map(Memberships::getOdataNextLink)
                    .orElse(null);
            } else {
                break;
            }
        }
        return groupInformation;
    }

    Optional<Memberships> getUserMemberships(String accessToken, String urlString) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Memberships> response = operations.exchange(urlString, HttpMethod.GET, entity, Memberships.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.of(response.getBody());
            } else {
                LOGGER.error("Response code [{}] is not 200, the response body is [{}].", response.getStatusCode(), response.getBody());
                return Optional.empty();
            }
        } catch (RestClientException restClientException) {
            LOGGER.error("Can not get group information from graph server.", restClientException);
            return Optional.empty();
        }
    }

    private boolean isGroupObject(final Membership membership) {
        return membership.getObjectType().equals(Membership.OBJECT_TYPE_GROUP);
    }
}
