// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
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

import static com.azure.spring.aad.AADApplicationType.applicationType;
import static com.azure.spring.aad.AADApplicationType.isWebApplicationAndResourceServer;
import static com.azure.spring.aad.AADApplicationType.isWebApplicationOnly;
import static com.azure.spring.aad.AADClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;

/**
 * OAuth2AuthorizedClientRepository used for AAD oauth2 clients.
 */
public class AADOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADOAuth2AuthorizedClientRepository.class);

    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();
    private OAuth2AuthorizedClientRepository anonymousAuthorizedClientRepository =
        new HttpSessionOAuth2AuthorizedClientRepository();

    private final AADClientRegistrationRepository repo;
    private final OAuth2AuthorizedClientRepository delegate;
    private final OAuth2AuthorizedClientProvider provider;
    private final OAuth2AuthorizedClientService service;
    private final Boolean isWebApplicationOnly;
    private final Boolean isWebApplicationAndResourceServer;

    public AADOAuth2AuthorizedClientRepository(AADAuthenticationProperties properties,
                                               AADClientRegistrationRepository repo,
                                               OAuth2AuthorizedClientService service) {
        this(properties,
            repo,
            new JacksonHttpSessionOAuth2AuthorizedClientRepository(),
            new RefreshTokenOAuth2AuthorizedClientProvider(),
            service);
    }

    public AADOAuth2AuthorizedClientRepository(AADAuthenticationProperties properties,
                                               AADClientRegistrationRepository repo,
                                               OAuth2AuthorizedClientRepository delegate,
                                               OAuth2AuthorizedClientProvider provider,
                                               OAuth2AuthorizedClientService service) {

        AADApplicationType applicationType = applicationType(properties);
        this.isWebApplicationOnly = isWebApplicationOnly(applicationType);
        this.isWebApplicationAndResourceServer = isWebApplicationAndResourceServer(applicationType);

        this.repo = repo;
        this.delegate = delegate;
        this.provider = provider;
        this.service = service;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient,
                                     Authentication principal,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        if (this.isPrincipalAuthenticated(principal)) {
            if (isClientOfWebApplication(authorizedClient.getClientRegistration().getRegistrationId())) {
                delegate.saveAuthorizedClient(authorizedClient, principal, request, response);
            } else {
                service.saveAuthorizedClient(authorizedClient, principal);
            }
        } else {
            this.anonymousAuthorizedClientRepository.saveAuthorizedClient(
                authorizedClient, principal, request, response);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId,
                                                                     Authentication principal,
                                                                     HttpServletRequest request) {
        if (this.isPrincipalAuthenticated(principal)) {
            if (isClientOfWebApplication(clientRegistrationId)) {
                OAuth2AuthorizedClient result = delegate.loadAuthorizedClient(clientRegistrationId, principal, request);
                if (result != null || repo.isClientCredentials(clientRegistrationId)) {
                    return (T) result;
                }

                if (repo.isAzureDelegatedClientRegistrations(clientRegistrationId)) {
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
            } else {
                return service.loadAuthorizedClient(clientRegistrationId, principal.getName());
            }
            return null;
        }
        return this.anonymousAuthorizedClientRepository.loadAuthorizedClient(clientRegistrationId, principal, request);
    }

    private boolean isClientOfWebApplication(String clientRegistrationId) {
        return this.isWebApplicationOnly
            || (isWebApplicationAndResourceServer
                && (AZURE_CLIENT_REGISTRATION_ID.equals(clientRegistrationId)
                    || repo.isAzureDelegatedClientRegistrations(clientRegistrationId)));
    }

    private Consumer<Map<String, Object>> getAttributesConsumer(String[] scopes) {
        return attributes -> attributes.put(OAuth2AuthorizationContext.REQUEST_SCOPE_ATTRIBUTE_NAME, scopes);
    }

    private String getAzureClientId() {
        return repo.getAzureClientRegistration().getClient().getRegistrationId();
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

        if (this.isPrincipalAuthenticated(principal)) {
            if (isClientOfWebApplication(clientRegistrationId)) {
                delegate.removeAuthorizedClient(clientRegistrationId, principal, request, response);
            } else {
                service.removeAuthorizedClient(clientRegistrationId, principal.getName());
            }
        } else {
            this.anonymousAuthorizedClientRepository.removeAuthorizedClient(clientRegistrationId, principal, request,
                response);
        }
    }

    private boolean isPrincipalAuthenticated(Authentication authentication) {
        return authentication != null && !this.authenticationTrustResolver.isAnonymous(authentication)
            && authentication.isAuthenticated();
    }

    public OAuth2AuthorizedClientRepository getAnonymousAuthorizedClientRepository() {
        return anonymousAuthorizedClientRepository;
    }

    public void setAnonymousAuthorizedClientRepository(OAuth2AuthorizedClientRepository anonymousAuthorizedClientRepository) {
        this.anonymousAuthorizedClientRepository = anonymousAuthorizedClientRepository;
    }
}
