// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.azure.spring.cloud.autoconfigure.implementation.aad.serde.jackson.SerializerUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.serde.jackson.SerializerUtils.serializeOAuth2AuthorizedClientMap;

/**
 * An implementation of an {@link OAuth2AuthorizedClientRepository} that stores {@link OAuth2AuthorizedClient}'s in the
 * {@code HttpSession}. To make it compatible with different spring versions. Refs:
 * <a href="https://github.com/spring-projects/spring-security/issues/9204">spring-security/issues/9204</a>
 *
 * @see OAuth2AuthorizedClientRepository
 * @see OAuth2AuthorizedClient
 */
public class JacksonHttpSessionOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {
    private static final String AUTHORIZED_CLIENTS_ATTR_NAME =
            JacksonHttpSessionOAuth2AuthorizedClientRepository.class.getName() + ".AUTHORIZED_CLIENTS";

    private static final String MSG_REQUEST_CANNOT_BE_NULL = "request cannot be null";

    @SuppressWarnings("unchecked")
    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId,
                                                                     Authentication principal,
                                                                     HttpServletRequest request) {
        Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
        Assert.notNull(request, MSG_REQUEST_CANNOT_BE_NULL);
        return (T) this.getAuthorizedClients(request).get(clientRegistrationId);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal,
                                     HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(authorizedClient, "authorizedClient cannot be null");
        Assert.notNull(request, MSG_REQUEST_CANNOT_BE_NULL);
        Assert.notNull(response, "response cannot be null");
        Map<String, OAuth2AuthorizedClient> authorizedClients =
                new HashMap<>(this.getAuthorizedClients(request));
        authorizedClients.put(authorizedClient.getClientRegistration().getRegistrationId(), authorizedClient);
        request.getSession().setAttribute(AUTHORIZED_CLIENTS_ATTR_NAME,
                serializeOAuth2AuthorizedClientMap(authorizedClients));
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, Authentication principal,
                                       HttpServletRequest request, HttpServletResponse response) {
        Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
        Assert.notNull(request, MSG_REQUEST_CANNOT_BE_NULL);
        Map<String, OAuth2AuthorizedClient> authorizedClients = new HashMap<>(this.getAuthorizedClients(request));
        if (authorizedClients.remove(clientRegistrationId) != null) {
            if (authorizedClients.isEmpty()) {
                request.getSession().removeAttribute(AUTHORIZED_CLIENTS_ATTR_NAME);
            } else {
                request.getSession().setAttribute(AUTHORIZED_CLIENTS_ATTR_NAME,
                        serializeOAuth2AuthorizedClientMap(authorizedClients));
            }
        }

    }

    private Map<String, OAuth2AuthorizedClient> getAuthorizedClients(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return Optional.ofNullable(session)
                .map(s -> s.getAttribute(AUTHORIZED_CLIENTS_ATTR_NAME))
                .map(Object::toString)
                .map(SerializerUtils::deserializeOAuth2AuthorizedClientMap)
                .orElse(Collections.emptyMap());
    }
}
