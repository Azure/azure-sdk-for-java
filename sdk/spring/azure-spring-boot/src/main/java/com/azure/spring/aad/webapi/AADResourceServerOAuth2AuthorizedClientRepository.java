// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.aad.AADAuthorizationGrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * AADResourceServerOAuth2AuthorizedClientRepository for obo flow and client credential flow
 * </p>
 */
public class AADResourceServerOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(AADResourceServerOAuth2AuthorizedClientRepository.class);


    private final ClientRegistrationRepository repository;

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    public AADResourceServerOAuth2AuthorizedClientRepository(ClientRegistrationRepository repository) {
        this(null, repository);
    }

    public AADResourceServerOAuth2AuthorizedClientRepository(OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
                                                             ClientRegistrationRepository repository) {
        this.repository = repository;
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String registrationId,
                                                                     Authentication authentication,
                                                                     HttpServletRequest request) {
        ClientRegistration clientRegistration = repository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            LOGGER.error("Not found the ClientRegistration, registrationId={}", registrationId);
            return null;
        }
        if (AADAuthorizationGrantType.CLIENT_CREDENTIALS
            .isSameGrantType(clientRegistration.getAuthorizationGrantType())
            || AADAuthorizationGrantType.ON_BEHALF_OF
                .isSameGrantType(clientRegistration.getAuthorizationGrantType())) {
            return this.oAuth2AuthorizedClientService.loadAuthorizedClient(registrationId, authentication.getName());
        }
        return null;
    }



    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient oAuth2AuthorizedClient, Authentication principal,
                                     HttpServletRequest request, HttpServletResponse response) {
        this.oAuth2AuthorizedClientService.saveAuthorizedClient(oAuth2AuthorizedClient, principal);
    }


    @Override
    public void removeAuthorizedClient(String clientRegistrationId, Authentication principal,
                                       HttpServletRequest request, HttpServletResponse response) {
        this.oAuth2AuthorizedClientService.removeAuthorizedClient(clientRegistrationId, principal.getName());
    }


}
