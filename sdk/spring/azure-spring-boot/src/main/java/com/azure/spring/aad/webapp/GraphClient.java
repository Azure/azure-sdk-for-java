// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.JacksonObjectMapperFactory;
import com.azure.spring.autoconfigure.aad.Membership;
import com.azure.spring.autoconfigure.aad.Memberships;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * GraphClient is used to access graph server. Mainly used to get groups information of a user.
 */
public class GraphClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphClient.class);

    private final AADAuthenticationProperties properties;

    public GraphClient(AADAuthenticationProperties properties) {
        this.properties = properties;
    }

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
        URL url = new URL(urlString);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(HttpMethod.GET.toString());
        connection.setRequestProperty(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
        connection.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        final String responseInJson = getResponseString(connection);
        final int responseCode = connection.getResponseCode();
        if (responseCode == HTTPResponse.SC_OK) {
            return responseInJson;
        } else {
            throw new IllegalStateException(
                "Response is not " + HTTPResponse.SC_OK + ", response json: " + responseInJson);
        }
    }

    private String getResponseString(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader =
                 new BufferedReader(
                     new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                 )
        ) {
            final StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
            }
            return stringBuffer.toString();
        }
    }

    private boolean isGroupObject(final Membership membership) {
        return membership.getObjectType().equals(Membership.OBJECT_TYPE_GROUP);
    }
}
