// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.AADTokenClaim;
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
import org.springframework.util.StringUtils;

import java.util.Collections;
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

    private final OidcUserService oidcUserService;
    private final AADAuthenticationProperties properties;
    private final GraphClient graphClient;

    public AzureActiveDirectoryOAuth2UserService(
        AADAuthenticationProperties properties
    ) {
        this.properties = properties;
        this.oidcUserService = new OidcUserService();
        this.graphClient = new GraphClient(properties);
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default implementation for loading a user
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);
        Set<String> groups = Optional.of(userRequest)
                                     .map(OAuth2UserRequest::getAccessToken)
                                     .map(AbstractOAuth2Token::getTokenValue)
                                     .map(graphClient::getGroupsFromGraph)
                                     .orElseGet(Collections::emptySet);
        Set<String> groupRoles = groups.stream()
                                       .filter(properties::isAllowedGroup)
                                       .map(group -> ROLE_PREFIX + group)
                                       .collect(Collectors.toSet());
        Set<SimpleGrantedAuthority> authorities = groupRoles.stream()
                                                            .map(SimpleGrantedAuthority::new)
                                                            .collect(Collectors.toSet());
        if (authorities.isEmpty()) {
            authorities = DEFAULT_AUTHORITY_SET;
        }
        String nameAttributeKey =
            Optional.of(userRequest)
                    .map(OAuth2UserRequest::getClientRegistration)
                    .map(ClientRegistration::getProviderDetails)
                    .map(ClientRegistration.ProviderDetails::getUserInfoEndpoint)
                    .map(ClientRegistration.ProviderDetails.UserInfoEndpoint::getUserNameAttributeName)
                    .filter(StringUtils::hasText)
                    .orElse(AADTokenClaim.NAME);
        // Create a copy of oidcUser but use the mappedAuthorities instead
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), nameAttributeKey);
    }
}
