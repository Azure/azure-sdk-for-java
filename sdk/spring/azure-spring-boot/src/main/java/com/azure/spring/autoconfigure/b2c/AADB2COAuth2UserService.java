// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.b2c;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

/**
 * This implementation will retrieve user information.
 * Then map group to {@link GrantedAuthority} and save to session.
 */
public class AADB2COAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService oidcUserService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private static final String DEFAULT_OIDC_USER = "defaultOidcUser";

    public AADB2COAuth2UserService(
        ClientRegistrationRepository clientRegistrationRepository
    ) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.oidcUserService = new OidcUserService();
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(true);
        if (authentication != null) {
            return (DefaultOidcUser) session.getAttribute(DEFAULT_OIDC_USER);
        }

        // Delegate to the default implementation for loading a user
        DefaultOidcUser oidcUser = (DefaultOidcUser) oidcUserService.loadUser(userRequest);
        session.setAttribute(DEFAULT_OIDC_USER, oidcUser);
        return oidcUser;
    }
}
