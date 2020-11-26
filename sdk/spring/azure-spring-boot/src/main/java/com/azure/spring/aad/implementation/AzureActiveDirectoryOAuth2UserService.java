// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.AADTokenClaim;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

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
 * This implementation will retrieve group info of user from Microsoft Graph and map groups to {@link
 * GrantedAuthority}.
 */
public class AzureActiveDirectoryOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureActiveDirectoryOAuth2UserService.class);

    private final OidcUserService oidcUserService;
    private final AADAuthenticationProperties properties;

    public AzureActiveDirectoryOAuth2UserService(
        AADAuthenticationProperties properties
    ) {
        this.properties = properties;
        this.oidcUserService = new OidcUserService();
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default implementation for loading a user
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);
        Set<SimpleGrantedAuthority> authorities =
            Optional.of(userRequest)
                    .map(OAuth2UserRequest::getAccessToken)
                    .map(AbstractOAuth2Token::getTokenValue)
                    .map(this::getGroups)
                    .map(this::toGrantedAuthoritySet)
                    .filter(g -> !g.isEmpty())
                    .orElse(DEFAULT_AUTHORITY_SET);
        String nameAttributeKey =
            Optional.of(userRequest)
                    .map(OAuth2UserRequest::getClientRegistration)
                    .map(ClientRegistration::getProviderDetails)
                    .map(ClientRegistration.ProviderDetails::getUserInfoEndpoint)
                    .map(ClientRegistration.ProviderDetails.UserInfoEndpoint::getUserNameAttributeName)
                    .filter(s -> !s.isEmpty())
                    .orElse(AADTokenClaim.NAME);
        // Create a copy of oidcUser but use the mappedAuthorities instead
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), nameAttributeKey);
    }

    public Set<SimpleGrantedAuthority> toGrantedAuthoritySet(final Set<String> groups) {
        Set<SimpleGrantedAuthority> grantedAuthoritySet =
            groups.stream()
                  .filter(properties::isAllowedGroup)
                  .map(group -> new SimpleGrantedAuthority(ROLE_PREFIX + group))
                  .collect(Collectors.toSet());
        return Optional.of(grantedAuthoritySet)
                       .filter(g -> !g.isEmpty())
                       .orElse(DEFAULT_AUTHORITY_SET);
    }

    public Set<String> getGroups(String accessToken) {
        final Set<String> groups = new LinkedHashSet<>();
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
                     new InputStreamReader(connection.getInputStream(),
                         StandardCharsets.UTF_8))
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
        return membership.getObjectType().equals(properties.getUserGroup().getValue());
    }
}
