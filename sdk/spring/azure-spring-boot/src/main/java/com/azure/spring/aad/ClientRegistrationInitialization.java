// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapp.AuthorizationProperties;
import com.azure.spring.aad.webapp.AuthorizationServerEndpoints;
import com.azure.spring.aad.webapp.AzureClientRegistration;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Client registration initialization based on AAD properties. Web application and resource server will quote.
 */
public class ClientRegistrationInitialization {

    private static final String AZURE_CLIENT_REGISTRATION_ID = "azure";

    private final AADAuthenticationProperties aadAuthenticationProperties;

    public ClientRegistrationInitialization(AADAuthenticationProperties aadAuthenticationProperties) {
        this.aadAuthenticationProperties = aadAuthenticationProperties;
    }

    public AzureClientRegistration createDefaultClient() {
        ClientRegistration.Builder builder = createClientBuilder(AZURE_CLIENT_REGISTRATION_ID);
        builder.scope(allScopes());
        ClientRegistration client = builder.build();

        return new AzureClientRegistration(client, accessTokenScopes());
    }

    private Set<String> allScopes() {
        Set<String> result = accessTokenScopes();
        for (AuthorizationProperties authProperties : aadAuthenticationProperties.getAuthorization().values()) {
            if (!authProperties.isOnDemand()) {
                result.addAll(authProperties.getScopes());
            }
        }
        return result;
    }

    private Set<String> accessTokenScopes() {
        Set<String> result = openidScopes();
        if (aadAuthenticationProperties.allowedGroupsConfigured()) {
            result.add("https://graph.microsoft.com/User.Read");
        }
        addAzureConfiguredScopes(result);
        return result;
    }

    private void addAzureConfiguredScopes(Set<String> result) {
        AuthorizationProperties azureProperties = aadAuthenticationProperties
            .getAuthorization()
            .get(AZURE_CLIENT_REGISTRATION_ID);
        if (azureProperties != null) {
            result.addAll(azureProperties.getScopes());
        }
    }

    private Set<String> openidScopes() {
        Set<String> result = new HashSet<>();
        result.add("openid");
        result.add("profile");

        if (!aadAuthenticationProperties.getAuthorization().isEmpty()) {
            result.add("offline_access");
        }
        return result;
    }

    public List<ClientRegistration> createAuthzClients() {
        List<ClientRegistration> result = new ArrayList<>();
        for (String name : aadAuthenticationProperties.getAuthorization().keySet()) {
            if (AZURE_CLIENT_REGISTRATION_ID.equals(name)) {
                continue;
            }

            AuthorizationProperties authz = aadAuthenticationProperties.getAuthorization().get(name);
            result.add(createClientBuilder(name, authz));
        }
        return result;
    }

    private ClientRegistration createClientBuilder(String id, AuthorizationProperties authz) {
        ClientRegistration.Builder result = createClientBuilder(id);
        result.scope(authz.getScopes());
        return result.build();
    }

    private ClientRegistration.Builder createClientBuilder(String id) {
        ClientRegistration.Builder result = ClientRegistration.withRegistrationId(id);
        result.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
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
