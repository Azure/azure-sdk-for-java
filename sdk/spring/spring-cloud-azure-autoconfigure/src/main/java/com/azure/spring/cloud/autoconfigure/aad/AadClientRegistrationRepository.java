// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationServerEndpoints;
import com.azure.spring.cloud.autoconfigure.aad.properties.AuthorizationClientProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.constants.Constants.AZURE_DELEGATED;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;


/**
 * Manage all AAD OAuth2 clients configured by property "spring.cloud.azure.active-directory.xxx".
 * Do extra works:
 * 1. Make "azure" client's scope contains all "azure_delegated" clients' scope.
 *    This scope is used to request authorize code.
 * 2. Save azureClientAccessTokenScopes, this scope is used to request "azure" client's access_token.
 */
public class AadClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    /**
     * Azure client registration ID
     */
    public static final String AZURE_CLIENT_REGISTRATION_ID = "azure";

    private final Set<String> azureClientAccessTokenScopes;
    private final Map<String, ClientRegistration> allClients;

    /**
     * Creates a new instance of {@link AadClientRegistrationRepository}.
     *
     * @param properties the AAD authentication properties
     */
    public AadClientRegistrationRepository(AadAuthenticationProperties properties) {
        Set<String> accessTokenScopes = azureClientAccessTokenScopes(properties); // Used to get access_token
        Set<String> delegatedScopes = delegatedClientsAccessTokenScopes(properties);
        Set<String> authorizationCodeScopes = new HashSet<>(); // Used to get authorization code.
        authorizationCodeScopes.addAll(accessTokenScopes);
        authorizationCodeScopes.addAll(delegatedScopes);
        if (resourceServerCount(accessTokenScopes) == 0 && resourceServerCount(authorizationCodeScopes) > 1) {
            // AAD server will return error if:
            // 1. authorizationCodeScopes have more than one resource server.
            // 2. accessTokenScopes have no resource server
            String newScope = properties.getProfile().getEnvironment().getMicrosoftGraphEndpoint() + "User.Read";
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
                          entry -> toClientRegistration(entry.getKey(),
                              entry.getValue().getAuthorizationGrantType(),
                              entry.getValue().getScopes(),
                              entry.getValue().getClientAuthenticationMethod(),
                              properties)));
        ClientAuthenticationMethod azureClientAuthMethod = getAzureDefaultClientAuthenticationMethod();
        ClientRegistration azureClient =
            toClientRegistration(AZURE_CLIENT_REGISTRATION_ID, AUTHORIZATION_CODE,
                authorizationCodeScopes, azureClientAuthMethod, properties);
        allClients.put(AZURE_CLIENT_REGISTRATION_ID, azureClient);
    }

    private ClientAuthenticationMethod getAzureDefaultClientAuthenticationMethod() {
        if (this.allClients.containsKey(AZURE_CLIENT_REGISTRATION_ID)) {
            return this.allClients.get(AZURE_CLIENT_REGISTRATION_ID).getClientAuthenticationMethod();
        }
        return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
    }

    /**
     * Gets the set of Azure client access token scopes.
     *
     * @return the set of Azure client access token scopes
     */
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
                         .filter(client -> AUTHORIZATION_CODE.equals(client.getAuthorizationGrantType()))
                         .iterator();
    }

    private Set<String> azureClientAccessTokenScopes(AadAuthenticationProperties properties) {
        Set<String> result = Optional.of(properties)
                                     .map(AadAuthenticationProperties::getAuthorizationClients)
                                     .map(clients -> clients.get(AZURE_CLIENT_REGISTRATION_ID))
                                     .map(AuthorizationClientProperties::getScopes)
                                     .map(HashSet::new)
                                     .orElseGet(HashSet::new);
        result.add("openid"); // "openid" allows to request an ID token.
        result.add("profile"); // "profile" allows to return additional claims in the ID token.
        result.add("offline_access"); // "offline_access" allows to request a refresh token.
        // About "Directory.Read.All" and "User.Read", please refer to:
        // 1. https://docs.microsoft.com/graph/permissions-reference
        // 2. https://github.com/Azure/azure-sdk-for-java/issues/21284#issuecomment-888725241
        if (properties.isAllowedGroupNamesConfigured()) {
            // "Directory.Read.All" allows to get group id and group name.
            result.add(properties.getProfile().getEnvironment().getMicrosoftGraphEndpoint() + "Directory.Read.All");
        } else if (properties.isAllowedGroupIdsConfigured()) {
            // "User.Read" allows getting group id, but not allow getting group name.
            result.add(properties.getProfile().getEnvironment().getMicrosoftGraphEndpoint() + "User.Read");
        }
        return result;
    }

    private Set<String> delegatedClientsAccessTokenScopes(AadAuthenticationProperties properties) {
        return properties.getAuthorizationClients()
                         .values()
                         .stream()
                         .filter(p -> AZURE_DELEGATED.getValue().equals(p.getAuthorizationGrantType().getValue()))
                         .flatMap(p -> p.getScopes().stream())
                         .collect(Collectors.toSet());
    }

    private ClientRegistration toClientRegistration(String registrationId,
                                                    AuthorizationGrantType authorizationGrantType,
                                                    Collection<String> scopes,
                                                    ClientAuthenticationMethod clientAuthenticationMethod,
                                                    AadAuthenticationProperties properties) {
        AadAuthorizationServerEndpoints endpoints =
            new AadAuthorizationServerEndpoints(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
                properties.getProfile().getTenantId());
        return ClientRegistration.withRegistrationId(registrationId)
                                 .clientName(registrationId)
                                 .authorizationGrantType(authorizationGrantType)
                                 .scope(scopes)
                                 .redirectUri(properties.getRedirectUriTemplate())
                                 .userNameAttributeName(properties.getUserNameAttribute())
                                 .clientId(properties.getCredential().getClientId())
                                 .clientSecret(properties.getCredential().getClientSecret())
                                 .clientAuthenticationMethod(clientAuthenticationMethod)
                                 .authorizationUri(endpoints.getAuthorizationEndpoint())
                                 .tokenUri(endpoints.getTokenEndpoint())
                                 .jwkSetUri(endpoints.getJwkSetEndpoint())
                                 .providerConfigurationMetadata(providerConfigurationMetadata(endpoints))
                                 .build();
    }

    private Map<String, Object> providerConfigurationMetadata(AadAuthorizationServerEndpoints endpoints) {
        Map<String, Object> result = new LinkedHashMap<>();
        String endSessionEndpoint = endpoints.getEndSessionEndpoint();
        result.put("end_session_endpoint", endSessionEndpoint);
        return result;
    }

    /**
     * Gets the resource server count.
     *
     * @param scopes the set of scope
     * @return the resource server count
     */
    public static int resourceServerCount(Set<String> scopes) {
        return (int) scopes.stream()
                           .filter(scope -> scope.contains("/"))
                           .map(scope -> scope.substring(0, scope.lastIndexOf('/')))
                           .distinct()
                           .count();
    }
}
