// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Optional;
import java.util.Set;

/**
 * This implementation will retrieve group info of user from Microsoft Graph and map groups to {@link
 * GrantedAuthority}.
 */
public class AADOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final OidcUserService oidcUserService;
    private final GraphWebClient graphWebClient;

    public AADOAuth2UserService(
        GraphWebClient graphWebClient
    ) {
        this.graphWebClient = graphWebClient;
        this.oidcUserService = new OidcUserService();
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default implementation for loading a user
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);
        final Set<SimpleGrantedAuthority> mappedAuthorities = graphWebClient.getGrantedAuthorities();
        String nameAttributeKey =
            Optional.of(userRequest)
                    .map(OAuth2UserRequest::getClientRegistration)
                    .map(ClientRegistration::getProviderDetails)
                    .map(ClientRegistration.ProviderDetails::getUserInfoEndpoint)
                    .map(ClientRegistration.ProviderDetails.UserInfoEndpoint::getUserNameAttributeName)
                    .filter(s -> !s.isEmpty())
                    .orElse(AADTokenClaim.NAME);
        // Create a copy of oidcUser but use the mappedAuthorities instead
        return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), nameAttributeKey);
    }
}
