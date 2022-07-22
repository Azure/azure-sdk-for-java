// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import reactor.core.publisher.Mono;

/**
 * This implementation will retrieve group info of user from Microsoft Graph. Then map group to {@link
 * GrantedAuthority}.
 *
 * @see OidcUserService
 * @see OAuth2UserService
 */
public class AadReactiveOAuth2UserService implements ReactiveOAuth2UserService<OidcUserRequest, OidcUser> {

    private final AadOAuth2UserService userService;

    /**
     * Creates a new instance of {@link AadReactiveOAuth2UserService}.
     *
     * @param service the underlying user service.
     */
    public AadReactiveOAuth2UserService(AadOAuth2UserService service) {
        this.userService = service;
    }

    /**
     * Returns an {@link OAuth2User} after obtaining the user attributes of the End-User
     * from the UserInfo Endpoint.
     *
     * @param userRequest the user request
     * @return an {@link OAuth2User}
     * @throws OAuth2AuthenticationException if an error occurs while attempting to obtain
     * the user attributes from the UserInfo Endpoint
     */
    @Override
    public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return Mono.just(this.userService.loadUser(userRequest));
    }
}
