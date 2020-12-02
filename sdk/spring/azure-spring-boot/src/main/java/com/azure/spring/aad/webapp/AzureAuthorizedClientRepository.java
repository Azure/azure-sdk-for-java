// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AzureAuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private AzureClientRegistrationRepository repo;
    private OAuth2AuthorizedClientRepository delegate;

    private static OAuth2AuthorizedClientRepository createDefaultDelegate(ClientRegistrationRepository repo) {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }

    public AzureAuthorizedClientRepository(AzureClientRegistrationRepository repo) {
        this(repo, createDefaultDelegate(repo));
    }

    public AzureAuthorizedClientRepository(AzureClientRegistrationRepository repo,
                                           OAuth2AuthorizedClientRepository delegate) {
        this.repo = repo;
        this.delegate = delegate;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient client,
                                     Authentication principal,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        delegate.saveAuthorizedClient(client, principal, request, response);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String id,
                                                                     Authentication principal,
                                                                     HttpServletRequest request) {
        OAuth2AuthorizedClient result = delegate.loadAuthorizedClient(id, principal, request);
        if (result != null) {
            return (T) result;
        }

        if (repo.isAuthzClient(id)) {
            OAuth2AuthorizedClient client = loadAuthorizedClient(defaultClientRegistrationId(), principal, request);
            return (T) createInitAuthzClient(client, id, principal);
        }
        return null;
    }

    private String defaultClientRegistrationId() {
        return repo.getAzureClient().getClient().getRegistrationId();
    }

    private OAuth2AuthorizedClient createInitAuthzClient(OAuth2AuthorizedClient client,
                                                         String id,
                                                         Authentication principal) {
        if (client == null || client.getRefreshToken() == null) {
            return null;
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "non-access-token",
            Instant.MIN,
            Instant.now().minus(100, ChronoUnit.DAYS));

        return new OAuth2AuthorizedClient(
            repo.findByRegistrationId(id),
            principal.getName(),
            accessToken,
            client.getRefreshToken()
        );
    }

    @Override
    public void removeAuthorizedClient(String id,
                                       Authentication principal,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        delegate.removeAuthorizedClient(id, principal, request, response);
    }
}
