// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.JacksonObjectMapperFactory;
import com.azure.spring.autoconfigure.aad.Membership;
import com.azure.spring.autoconfigure.aad.Memberships;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.spring.autoconfigure.aad.Constants.DEFAULT_AUTHORITY_SET;
import static com.azure.spring.autoconfigure.aad.Constants.ROLE_PREFIX;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;


/**
 * Microsoft Graph web client implemented by OAuth2 WebClient.
 */
public class GraphWebClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphWebClient.class);

    private final AADAuthenticationProperties aadAuthenticationProperties;
    private final WebClient webClient;

    public GraphWebClient(
        AADAuthenticationProperties aadAuthenticationProperties,
        WebClient webClient
    ) {
        this.aadAuthenticationProperties = aadAuthenticationProperties;
        this.webClient = webClient;
    }

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        return toGrantedAuthoritySet(getGroupsFromGraphApi());
    }

    public Set<String> getGroupsFromGraphApi() {
        final ObjectMapper objectMapper = JacksonObjectMapperFactory.getInstance();
        Set<String> groups = new LinkedHashSet<>();
        String aadMembershipRestUri = aadAuthenticationProperties.getGraphMembershipUri();
        while (aadMembershipRestUri != null) {
            String membershipsJson = getUserMembershipsJson(aadMembershipRestUri);
            Memberships memberships;
            try {
                memberships = objectMapper.readValue(membershipsJson, Memberships.class);
            } catch (JsonProcessingException e) {
                LOGGER.error("Can not get groups.", e);
                return Collections.emptySet();
            }
            memberships.getValue()
                       .stream()
                       .filter(this::isGroupObject)
                       .map(Membership::getDisplayName)
                       .forEach(groups::add);
            aadMembershipRestUri = Optional.of(memberships)
                                           .map(Memberships::getOdataNextLink)
                                           .orElse(null);
        }
        return groups;
    }

    private String getUserMembershipsJson(String urlString) {
        String responseInJson = webClient
            .get()
            .uri(urlString)
            .attributes(clientRegistrationId("graph"))
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .retrieve()
            .bodyToMono(String.class)
            .block();
        if (responseInJson == null || responseInJson.isEmpty()) {
            throw new IllegalStateException(
                "Response is not " + HTTPResponse.SC_OK + ", response json: " + responseInJson);
        }
        return responseInJson;
    }

    private boolean isGroupObject(final Membership membership) {
        return membership.getObjectType().equals(aadAuthenticationProperties.getUserGroup().getValue());
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
