// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.aad.webapp.AuthorizationServerEndpoints;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.List;

/**
 * Client registration initialization based on AAD properties. Web application and resource server will quote.
 */
public class ClientRegistrationInitialization {

    private final AADAuthenticationProperties aadAuthenticationProperties;

    public ClientRegistrationInitialization(AADAuthenticationProperties aadAuthenticationProperties) {
        this.aadAuthenticationProperties = aadAuthenticationProperties;
    }

    public List<ClientRegistration> createAuthzClients() {
        List<ClientRegistration> result = new ArrayList<>();
        for (String name : aadAuthenticationProperties.getWebApiClients().keySet()) {
            WebApiAuthorizationProperties authz = aadAuthenticationProperties.getWebApiClients().get(name);
            result.add(createClientBuilder(name, authz));
        }
        return result;
    }

    private ClientRegistration createClientBuilder(String id, WebApiAuthorizationProperties authz) {
        ClientRegistration.Builder result = createClientBuilder(id);
        result.scope(authz.getScopes());
        return result.build();
    }

    private ClientRegistration.Builder createClientBuilder(String id) {
        ClientRegistration.Builder result = ClientRegistration.withRegistrationId(id);
        result.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);
        result.redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}");

        result.clientId(aadAuthenticationProperties.getClientId());
        result.clientSecret(aadAuthenticationProperties.getClientSecret());

        AuthorizationServerEndpoints endpoints =
            new AuthorizationServerEndpoints(aadAuthenticationProperties.getAuthorizationServerUri());
        result.authorizationUri(endpoints.authorizationEndpoint(aadAuthenticationProperties.getTenantId()));
        result.tokenUri(endpoints.tokenEndpoint(aadAuthenticationProperties.getTenantId()));
        result.jwkSetUri(endpoints.jwkSetEndpoint(aadAuthenticationProperties.getTenantId()));

        return result;
    }
}
