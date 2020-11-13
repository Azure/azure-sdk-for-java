// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * An OAuth2AuthorizedClientRepository that manage all AAD authorizedClients.
 */
public class AzureOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private final AzureClientRegistrationRepository azureClientRegistrationRepository;
    private final OAuth2AuthorizedClientRepository delegatedOAuth2AuthorizedClientRepository;

    private static OAuth2AuthorizedClientRepository createDefaultDelegate() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }

    public AzureOAuth2AuthorizedClientRepository(AzureClientRegistrationRepository azureClientRegistrationRepository) {
        this(azureClientRegistrationRepository, createDefaultDelegate());
    }

    public AzureOAuth2AuthorizedClientRepository(
        AzureClientRegistrationRepository azureClientRegistrationRepository,
        OAuth2AuthorizedClientRepository delegatedOAuth2AuthorizedClientRepository
    ) {
        this.azureClientRegistrationRepository = azureClientRegistrationRepository;
        this.delegatedOAuth2AuthorizedClientRepository = delegatedOAuth2AuthorizedClientRepository;
    }

    @Override
    public void saveAuthorizedClient(
        OAuth2AuthorizedClient oAuth2AuthorizedClient,
        Authentication principal,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        delegatedOAuth2AuthorizedClientRepository.saveAuthorizedClient(
            oAuth2AuthorizedClient, principal, request, response);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
        String id,
        Authentication principal,
        HttpServletRequest request
    ) {
        OAuth2AuthorizedClient result =
            delegatedOAuth2AuthorizedClientRepository.loadAuthorizedClient(id, principal, request);
        if (result != null) {
            return (T) result;
        }
        if (azureClientRegistrationRepository.isAuthorizedClient(id)) {
            OAuth2AuthorizedClient defaultOAuth2AuthorizedClient =
                loadAuthorizedClient(defaultClientRegistrationId(), principal, request);
            return (T) toOauth2AuthorizedClient(defaultOAuth2AuthorizedClient, id, principal);
        }
        return null;
    }

    private String defaultClientRegistrationId() {
        return azureClientRegistrationRepository.defaultClient().getClientRegistration().getRegistrationId();
    }

    private OAuth2AuthorizedClient toOauth2AuthorizedClient(
        OAuth2AuthorizedClient oAuth2AuthorizedClient,
        String id,
        Authentication principal
    ) {
        OAuth2RefreshToken oAuth2RefreshToken = Optional.ofNullable(oAuth2AuthorizedClient)
                                                        .map(OAuth2AuthorizedClient::getRefreshToken)
                                                        .orElse(null);
        if (oAuth2RefreshToken == null) {
            return null;
        }
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "non-access-token",
            Instant.MIN,
            Instant.now().minus(100, ChronoUnit.DAYS)
        );
        return new OAuth2AuthorizedClient(
            azureClientRegistrationRepository.findByRegistrationId(id),
            principal.getName(),
            accessToken,
            oAuth2RefreshToken
        );
    }

    @Override
    public void removeAuthorizedClient(
        String id,
        Authentication principal,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        delegatedOAuth2AuthorizedClientRepository.removeAuthorizedClient(id, principal, request, response);
    }
}
