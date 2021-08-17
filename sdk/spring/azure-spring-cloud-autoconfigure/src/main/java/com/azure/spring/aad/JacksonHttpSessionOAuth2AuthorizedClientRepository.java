// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.aad.implementation.jackson.SerializerUtils.deserializeOAuth2AuthorizedClientMap;
import static com.azure.spring.aad.implementation.jackson.SerializerUtils.serializeOAuth2AuthorizedClientMap;

/**
 * An implementation of an {@link OAuth2AuthorizedClientRepository} that stores {@link OAuth2AuthorizedClient}'s in the
 * {@code HttpSession}. To make it compatible with different spring versions. Refs:
 * https://github.com/spring-projects/spring-security/issues/9204
 *
 * @see OAuth2AuthorizedClientRepository
 * @see OAuth2AuthorizedClient
 */
public class JacksonHttpSessionOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {
    private static final String AUTHORIZED_CLIENTS_ATTR_NAME =
        JacksonHttpSessionOAuth2AuthorizedClientRepository.class.getName() + ".AUTHORIZED_CLIENTS";

    @SuppressWarnings("unchecked")
    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId,
                                                                     Authentication principal,
                                                                     HttpServletRequest request) {
        Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
        Assert.notNull(request, "request cannot be null");
        return (T) this.getAuthorizedClients(request).get(clientRegistrationId);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal,
                                     HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(authorizedClient, "authorizedClient cannot be null");
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");
        Map<String, OAuth2AuthorizedClient> authorizedClients = this.getAuthorizedClients(request);
        authorizedClients.put(authorizedClient.getClientRegistration().getRegistrationId(), authorizedClient);
        request.getSession().setAttribute(AUTHORIZED_CLIENTS_ATTR_NAME,
            serializeOAuth2AuthorizedClientMap(authorizedClients));
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, Authentication principal,
                                       HttpServletRequest request, HttpServletResponse response) {
        Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
        Assert.notNull(request, "request cannot be null");
        Map<String, OAuth2AuthorizedClient> authorizedClients = this.getAuthorizedClients(request);
        if (!authorizedClients.isEmpty()) {
            if (authorizedClients.remove(clientRegistrationId) != null) {
                if (!authorizedClients.isEmpty()) {
                    request.getSession().setAttribute(AUTHORIZED_CLIENTS_ATTR_NAME,
                        serializeOAuth2AuthorizedClientMap(authorizedClients));
                } else {
                    request.getSession().removeAttribute(AUTHORIZED_CLIENTS_ATTR_NAME);
                }
            }
        }
    }

    private Map<String, OAuth2AuthorizedClient> getAuthorizedClients(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String authorizedClientsString = (String) Optional.ofNullable(session)
                                                          .map(s -> s.getAttribute(AUTHORIZED_CLIENTS_ATTR_NAME))
                                                          .orElse(null);
        if (authorizedClientsString == null) {
            return new HashMap<>();
        }
        return deserializeOAuth2AuthorizedClientMap(authorizedClientsString);
    }
}
