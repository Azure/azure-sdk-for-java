// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of an {@link OAuth2AuthorizedClientRepository} that stores
 * {@link OAuth2AuthorizedClient}'s in the {@code HttpSession}. To make it compatible
 * with different spring versions.
 * Refs: https://github.com/spring-projects/spring-security/issues/9204
 *
 * @see OAuth2AuthorizedClientRepository
 * @see OAuth2AuthorizedClient
 */
public class JacksonHttpSessionOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {
    private static final String DEFAULT_AUTHORIZED_CLIENTS_ATTR_NAME =
        HttpSessionOAuth2AuthorizedClientRepository.class.getName() + ".AUTHORIZED_CLIENTS";
    private final String sessionAttributeName = DEFAULT_AUTHORIZED_CLIENTS_ATTR_NAME;
    private final ObjectMapper objectMapper;

    public JacksonHttpSessionOAuth2AuthorizedClientRepository() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new OAuth2ClientJackson2Module());
    }

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
        request.getSession().setAttribute(this.sessionAttributeName, toString(authorizedClients));
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
                    request.getSession().setAttribute(this.sessionAttributeName, toString(authorizedClients));
                } else {
                    request.getSession().removeAttribute(this.sessionAttributeName);
                }
            }
        }
    }

    private String toString(Map<String, OAuth2AuthorizedClient> authorizedClients) {
        String result;
        try {
            result = objectMapper.writeValueAsString(authorizedClients);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    private Map<String, OAuth2AuthorizedClient> getAuthorizedClients(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String authorizedClientsString = session == null ? null :
            (String) session.getAttribute(this.sessionAttributeName);
        TypeReference<Map<String, OAuth2AuthorizedClient>> typeReference =
            new TypeReference<Map<String, OAuth2AuthorizedClient>>() {
        };
        if (authorizedClientsString == null) {
            return new HashMap<>();
        }
        Map<String, OAuth2AuthorizedClient> authorizedClients;
        try {
            authorizedClients = objectMapper.readValue(authorizedClientsString, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        if (authorizedClients == null) {
            authorizedClients = new HashMap<>();
        }
        return authorizedClients;
    }
}
