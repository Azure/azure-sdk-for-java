// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Consumer;

import static com.azure.spring.aad.AADClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;

/**
 * OAuth2AuthorizedClientRepository used for AAD oauth2 clients.
 */
public class AADOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADOAuth2AuthorizedClientRepository.class);

    private final AADClientRegistrationRepository repo;
    private final OAuth2AuthorizedClientRepository delegate;
    private final OAuth2AuthorizedClientProvider provider;

    public AADOAuth2AuthorizedClientRepository(AADClientRegistrationRepository repo) {
        this(repo,
            new JacksonHttpSessionOAuth2AuthorizedClientRepository(),
            new RefreshTokenOAuth2AuthorizedClientProvider());
    }

    public AADOAuth2AuthorizedClientRepository(AADClientRegistrationRepository repo,
                                               OAuth2AuthorizedClientRepository delegate,
                                               OAuth2AuthorizedClientProvider provider) {
        this.repo = repo;
        this.delegate = delegate;
        this.provider = provider;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient,
                                     Authentication principal,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        // Todo (rujche) Fix OAuth2AuthorizedClient deserializer and memory leakage problem
        //  1. Fix the deserializer of grant type 'on_behalf_of' when loading OAuth2AuthorizedClient of OBO client from JacksonHttpSessionOAuth2AuthorizedClientRepository.
        //  2. Fix the memory leakage problem when saving the resource server's OAuth2AuthorizedClient into InMemoryOAuth2AuthorizedClientService.
        delegate.saveAuthorizedClient(authorizedClient, principal, request, response);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId,
                                                                     Authentication principal,
                                                                     HttpServletRequest request) {
        OAuth2AuthorizedClient result = delegate.loadAuthorizedClient(clientRegistrationId, principal, request);
        if (result != null) {
            return (T) result;
        }

        if (repo.isAzureDelegatedClientRegistration(clientRegistrationId)) {
            OAuth2AuthorizedClient azureClient = loadAuthorizedClient(getAzureClientId(), principal, request);
            if (azureClient == null) {
                throw new ClientAuthorizationRequiredException(AZURE_CLIENT_REGISTRATION_ID);
            }
            OAuth2AuthorizedClient fakeAuthzClient = createFakeAuthzClient(azureClient, clientRegistrationId, principal);
            OAuth2AuthorizationContext.Builder contextBuilder =
                OAuth2AuthorizationContext.withAuthorizedClient(fakeAuthzClient);
            String[] scopes = null;
            if (!AADClientRegistrationRepository.isDefaultClient(clientRegistrationId)) {
                scopes = repo.findByRegistrationId(clientRegistrationId).getScopes().toArray(new String[0]);
            }
            OAuth2AuthorizationContext context = contextBuilder
                .principal(principal)
                .attributes(getAttributesConsumer(scopes))
                .build();
            OAuth2AuthorizedClient clientGotByRefreshToken = provider.authorize(context);
            try {
                ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                delegate.saveAuthorizedClient(clientGotByRefreshToken, principal, request, attributes.getResponse());
            } catch (IllegalStateException exception) {
                LOGGER.warn("Can not save OAuth2AuthorizedClient.", exception);
            }
            return (T) clientGotByRefreshToken;
        }
        return null;
    }

    private Consumer<Map<String, Object>> getAttributesConsumer(String[] scopes) {
        return attributes -> attributes.put(OAuth2AuthorizationContext.REQUEST_SCOPE_ATTRIBUTE_NAME, scopes);
    }

    private String getAzureClientId() {
        return repo.getAzureClient().getClient().getRegistrationId();
    }

    private OAuth2AuthorizedClient createFakeAuthzClient(OAuth2AuthorizedClient azureClient,
                                                         String clientRegistrationId,
                                                         Authentication principal) {
        if (azureClient == null || azureClient.getRefreshToken() == null) {
            return null;
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "non-access-token",
            Instant.MIN,
            Instant.now().minus(100, ChronoUnit.DAYS));

        return new OAuth2AuthorizedClient(
            repo.findByRegistrationId(clientRegistrationId),
            principal.getName(),
            accessToken,
            azureClient.getRefreshToken()
        );
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId,
                                       Authentication principal,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        delegate.removeAuthorizedClient(clientRegistrationId, principal, request, response);
    }
}
