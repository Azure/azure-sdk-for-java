// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapp.AuthorizationClientProperties;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.spring.aad.AADAuthorizationGrantType.AUTHORIZATION_CODE;
import static com.azure.spring.aad.AADAuthorizationGrantType.AZURE_DELEGATED;


/**
 * Manage all AAD OAuth2 clients configured by property "azure.activedirectory.xxx".
 * Do extra works:
 * 1. Make "azure" client's scope contains all "azure_delegated" clients' scope.
 *    This scope is used to request authorize code.
 * 2. Save azureClientAccessTokenScopes, this scope is used to request "azure" client's access_token.
 */
public class AADClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    public static final String AZURE_CLIENT_REGISTRATION_ID = "azure";

    private final Set<String> azureClientAccessTokenScopes;
    private final Set<String> onDemandRegistrationIds;
    private final Map<String, ClientRegistration> allClients;

    public AADClientRegistrationRepository(AADAuthenticationProperties properties) {
        Set<String> accessTokenScopes = azureClientAccessTokenScopes(properties); // Used to get access_token
        Set<String> delegatedScopes = delegatedClientsAccessTokenScopes(properties);
        Set<String> authorizationCodeScopes = new HashSet<>(); // Used to get authorization code.
        authorizationCodeScopes.addAll(accessTokenScopes);
        authorizationCodeScopes.addAll(delegatedScopes);
        if (resourceServerCount(accessTokenScopes) == 0 && resourceServerCount((authorizationCodeScopes)) > 1) {
            // AAD server will return error if:
            // 1. authorizationCodeScopes have more than one resource server.
            // 2. accessTokenScopes have no resource server
            String newScope = properties.getGraphBaseUri() + "User.Read";
            accessTokenScopes.add(newScope);
            authorizationCodeScopes.add(newScope);
        }
        this.azureClientAccessTokenScopes = accessTokenScopes;
        this.allClients =
            properties.getAuthorizationClients()
                      .entrySet()
                      .stream()
                      .collect(Collectors.toMap(
                          Map.Entry::getKey,
                          entry -> toClientRegistration(entry.getKey(), entry.getValue().getAuthorizationGrantType(),
                              entry.getValue().getScopes(), properties)));
        ClientRegistration azureClient =
            toClientRegistration(AZURE_CLIENT_REGISTRATION_ID, AUTHORIZATION_CODE, authorizationCodeScopes, properties);
        this.allClients.put(AZURE_CLIENT_REGISTRATION_ID, azureClient);
        this.onDemandRegistrationIds = getOnDemandRegistrationIds(properties);
    }

    private Set<String> getOnDemandRegistrationIds(AADAuthenticationProperties properties) {
        return properties.getAuthorizationClients()
                         .entrySet()
                         .stream()
                         .filter(entry -> entry.getValue().isOnDemand()
                             && AUTHORIZATION_CODE == entry.getValue().getAuthorizationGrantType())
                         .map(Map.Entry::getKey)
                         .collect(Collectors.toSet());
    }

    public Set<String> getAzureClientAccessTokenScopes() {
        return azureClientAccessTokenScopes;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        Assert.hasText(registrationId, "registrationId cannot be empty");
        return allClients.get(registrationId);
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        return allClients.values()
                         .stream()
                         .filter(client ->
                             client.getAuthorizationGrantType().getValue().equals(AUTHORIZATION_CODE.getValue())
                                 && !onDemandRegistrationIds.contains(client.getRegistrationId()))
                         .iterator();
    }

    private Set<String> azureClientAccessTokenScopes(AADAuthenticationProperties properties) {
        Set<String> result = Optional.of(properties)
                                     .map(AADAuthenticationProperties::getAuthorizationClients)
                                     .map(clients -> clients.get(AZURE_CLIENT_REGISTRATION_ID))
                                     .map(AuthorizationClientProperties::getScopes)
                                     .map(HashSet::new)
                                     .orElseGet(HashSet::new);
        if (!result.contains("openid")) {
            result.add("openid"); // "openid" allows to request an ID token.
        }
        if (!result.contains("profile")) {
            result.add("profile"); // "profile" allows to return additional claims in the ID token.
        }
        if (!result.contains("offline_access")) {
            result.add("offline_access"); // "offline_access" allows to request a refresh token.
        }
        // About "Directory.Read.All" and "User.Read", please refer to:
        // 1. https://docs.microsoft.com/en-us/graph/permissions-reference
        // 2. https://github.com/Azure/azure-sdk-for-java/issues/21284#issuecomment-888725241
        if (properties.allowedGroupNamesConfigured()) {
            // "Directory.Read.All" allows to get group id and group name.
            result.add(properties.getGraphBaseUri() + "Directory.Read.All");
        } else if (properties.allowedGroupIdsConfigured()) {
            // "User.Read" allows to get group id, but not allow to get group name.
            result.add(properties.getGraphBaseUri() + "User.Read");
        }
        return result;
    }

    private Set<String> delegatedClientsAccessTokenScopes(AADAuthenticationProperties properties) {
        return properties.getAuthorizationClients()
                         .values()
                         .stream()
                         .filter(p -> AZURE_DELEGATED.getValue().equals(p.getAuthorizationGrantType().getValue()))
                         .flatMap(p -> p.getScopes().stream())
                         .collect(Collectors.toSet());
    }

    private ClientRegistration toClientRegistration(String registrationId,
                                                    AADAuthorizationGrantType aadAuthorizationGrantType,
                                                    Collection<String> scopes,
                                                    AADAuthenticationProperties properties) {
        AADAuthorizationServerEndpoints endpoints =
            new AADAuthorizationServerEndpoints(properties.getBaseUri(), properties.getTenantId());
        return ClientRegistration.withRegistrationId(registrationId)
                                 .clientName(registrationId)
                                 .authorizationGrantType(new AuthorizationGrantType((aadAuthorizationGrantType.getValue())))
                                 .scope(scopes)
                                 .redirectUri(properties.getRedirectUriTemplate())
                                 .userNameAttributeName(properties.getUserNameAttribute())
                                 .clientId(properties.getClientId())
                                 .clientSecret(properties.getClientSecret())
                                 .authorizationUri(endpoints.authorizationEndpoint())
                                 .tokenUri(endpoints.tokenEndpoint())
                                 .jwkSetUri(endpoints.jwkSetEndpoint())
                                 .providerConfigurationMetadata(providerConfigurationMetadata(endpoints))
                                 .build();
    }

    private Map<String, Object> providerConfigurationMetadata(AADAuthorizationServerEndpoints endpoints) {
        Map<String, Object> result = new LinkedHashMap<>();
        String endSessionEndpoint = endpoints.endSessionEndpoint();
        result.put("end_session_endpoint", endSessionEndpoint);
        return result;
    }

    public static int resourceServerCount(Set<String> scopes) {
        return (int) scopes.stream()
                           .filter(scope -> scope.contains("/"))
                           .map(scope -> scope.substring(0, scope.lastIndexOf('/')))
                           .distinct()
                           .count();
    }
}
