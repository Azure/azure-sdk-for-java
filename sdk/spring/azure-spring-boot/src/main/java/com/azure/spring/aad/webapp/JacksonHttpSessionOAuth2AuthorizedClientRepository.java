package com.azure.spring.aad.webapp;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class JacksonHttpSessionOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {
    private static final String DEFAULT_AUTHORIZED_CLIENTS_ATTR_NAME =
        HttpSessionOAuth2AuthorizedClientRepository.class.getName() +  ".AUTHORIZED_CLIENTS";
    private final String sessionAttributeName = DEFAULT_AUTHORIZED_CLIENTS_ATTR_NAME;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, Authentication principal,
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
        request.getSession().setAttribute(this.sessionAttributeName, authorizedClients);
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
                    request.getSession().setAttribute(this.sessionAttributeName, authorizedClients);
                } else {
                    request.getSession().removeAttribute(this.sessionAttributeName);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, OAuth2AuthorizedClient> getAuthorizedClients(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, OAuth2AuthorizedClient> authorizedClients = session == null ? null :
            (Map<String, OAuth2AuthorizedClient>) session.getAttribute(this.sessionAttributeName);
        if (authorizedClients == null) {
            authorizedClients = new HashMap<>();
        }
        return authorizedClients;
    }
}
