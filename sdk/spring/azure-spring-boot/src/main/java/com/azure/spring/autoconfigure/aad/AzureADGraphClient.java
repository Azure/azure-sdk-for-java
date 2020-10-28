// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.spring.autoconfigure.aad.Constants.DEFAULT_AUTHORITY_SET;
import static com.azure.spring.autoconfigure.aad.Constants.ROLE_PREFIX;


/**
 * Microsoft Graph client encapsulation.
 */
public class AzureADGraphClient {

    private final ServiceEndpoints serviceEndpoints;
    private final AADAuthenticationProperties aadAuthenticationProperties;

    public AzureADGraphClient(AADAuthenticationProperties aadAuthenticationProperties,
                              ServiceEndpointsProperties serviceEndpointsProps) {
        this.aadAuthenticationProperties = aadAuthenticationProperties;
        this.serviceEndpoints = serviceEndpointsProps.getServiceEndpoints(aadAuthenticationProperties.getEnvironment());
    }

    private String getUserMemberships(String accessToken, String urlString) throws IOException {
        URL url = new URL(urlString);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Set the appropriate header fields in the request header.
        if (aadAuthenticationProperties.graphApiVersionIsV2()) {
            connection.setRequestMethod(HttpMethod.GET.toString());
            connection.setRequestProperty(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
            connection.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        } else {
            connection.setRequestMethod(HttpMethod.GET.toString());
            connection.setRequestProperty("api-version", "1.6");
            connection.setRequestProperty(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
            connection.setRequestProperty(HttpHeaders.ACCEPT, "application/json;odata=minimalmetadata");
        }
        final String responseInJson = getResponseString(connection);
        final int responseCode = connection.getResponseCode();
        if (responseCode == HTTPResponse.SC_OK) {
            return responseInJson;
        } else {
            throw new IllegalStateException(
                "Response is not " + HTTPResponse.SC_OK + ", response json: " + responseInJson);
        }
    }

    private String getUrlStringFromODataNextLink(String odataNextLink) {
        if (this.aadAuthenticationProperties.graphApiVersionIsV2()) {
            return odataNextLink;
        } else {
            String skipToken = odataNextLink.split("/memberOf\\?")[1];
            return serviceEndpoints.getAadMembershipRestUri() + "&" + skipToken;
        }
    }

    private static String getResponseString(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            final StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
            }
            return stringBuffer.toString();
        }
    }

    /**
     * @param graphApiToken token used to access graph api.
     * @return groups in graph api.
     * @throws IOException throw exception if get groups failed by IOException.
     */
    public Set<String> getGroups(String graphApiToken) throws IOException {
        final Set<String> groups = new LinkedHashSet<>();
        final ObjectMapper objectMapper = JacksonObjectMapperFactory.getInstance();
        String aadMembershipRestUri = serviceEndpoints.getAadMembershipRestUri();
        while (aadMembershipRestUri != null) {
            String membershipsJson = getUserMemberships(graphApiToken, aadMembershipRestUri);
            Memberships memberships = objectMapper.readValue(membershipsJson, Memberships.class);
            memberships.getValue()
                       .stream()
                       .filter(this::isGroupObject)
                       .map(Membership::getDisplayName)
                       .forEach(groups::add);
            aadMembershipRestUri = Optional.of(memberships)
                                           .map(Memberships::getOdataNextLink)
                                           .map(this::getUrlStringFromODataNextLink)
                                           .orElse(null);
        }
        return groups;
    }

    private boolean isGroupObject(final Membership membership) {
        return membership.getObjectType().equals(aadAuthenticationProperties.getUserGroup().getValue());
    }

    /**
     * @param graphApiToken token of graph api.
     * @return set of SimpleGrantedAuthority
     * @throws IOException throw exception if get groups failed by IOException.
     */
    public Set<SimpleGrantedAuthority> getGrantedAuthorities(String graphApiToken) throws IOException {
        return toGrantedAuthoritySet(getGroups(graphApiToken));
    }

    public Set<SimpleGrantedAuthority> toGrantedAuthoritySet(final Set<String> groups) {
        Set<SimpleGrantedAuthority> grantedAuthoritySet =
            groups.stream()
                  .filter(aadAuthenticationProperties::isAllowedGroup)
                  .map(group -> new SimpleGrantedAuthority(ROLE_PREFIX + group))
                  .collect(Collectors.toSet());
        return Optional.of(grantedAuthoritySet)
                       .filter(g -> !g.isEmpty())
                       .orElse(DEFAULT_AUTHORITY_SET);
    }
}
