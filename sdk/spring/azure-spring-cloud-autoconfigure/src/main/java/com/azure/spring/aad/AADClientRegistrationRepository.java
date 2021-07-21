// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapp.AuthorizationClientProperties;
import com.azure.spring.aad.webapp.AzureClientRegistration;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.spring.aad.AADApplicationType.WEB_APPLICATION;
import static com.azure.spring.aad.AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER;
import static com.azure.spring.aad.AADAuthorizationGrantType.AUTHORIZATION_CODE;


/**
 * Manage all AAD OAuth2 clients configured by property "azure.activedirectory.xxx"
 */
public class AADClientRegistrationRepository
    implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADClientRegistrationRepository.class);

    public static final String AZURE_CLIENT_REGISTRATION_ID = "azure";

    protected final AzureClientRegistration azureClient;
    protected final Map<String, ClientRegistration> delegatedClients;
    protected final Map<String, ClientRegistration> allClients;
    protected final AADAuthenticationProperties properties;

    public AADClientRegistrationRepository(AADAuthenticationProperties properties) {
        this.properties = properties;
        this.azureClient = azureClientRegistration();
        this.delegatedClients = delegatedClientRegistrations();
        this.allClients = allClientRegistrations();
    }

    private AzureClientRegistration azureClientRegistration() {
        if (!needDelegation()) {
            return null;
        }

        AuthorizationClientProperties azureProperties =
            properties.getAuthorizationClients()
                      .getOrDefault(AZURE_CLIENT_REGISTRATION_ID, defaultAzureAuthorizationClientProperties());
        ClientRegistration.Builder builder = toClientRegistrationBuilder(AZURE_CLIENT_REGISTRATION_ID,
            azureProperties);
        Set<String> authorizationCodeScopes = azureClientAuthorizationCodeScopes();
        ClientRegistration client = builder.scope(authorizationCodeScopes).build();
        Set<String> accessTokenScopes = azureClientAccessTokenScopes();
        if (resourceServerCount(accessTokenScopes) == 0 && resourceServerCount((authorizationCodeScopes)) > 1) {
            // AAD server will return error if:
            // 1. authorizationCodeScopes have more than one resource server.
            // 2. accessTokenScopes have no resource server
            accessTokenScopes.add(properties.getGraphBaseUri() + "User.Read");
        }
        return new AzureClientRegistration(client, accessTokenScopes);
    }

    private boolean needDelegation() {
        return WEB_APPLICATION == properties.getApplicationType()
            || WEB_APPLICATION_AND_RESOURCE_SERVER == properties.getApplicationType();
    }

    private Map<String, ClientRegistration> delegatedClientRegistrations() {
        if (!needDelegation()) {
            return Collections.emptyMap();
        }
        return properties.getAuthorizationClients()
                         .entrySet()
                         .stream()
                         .filter(entry -> isAzureDelegatedClientRegistration(entry.getKey(), entry.getValue()))
                         .collect(Collectors.toMap(
                             Map.Entry::getKey,
                             entry -> toClientRegistration(entry.getKey(), entry.getValue())));
    }

    private Map<String, ClientRegistration> allClientRegistrations() {
        Map<String, ClientRegistration> result =
            properties.getAuthorizationClients()
                      .entrySet()
                      .stream()
                      .filter(entry -> !delegatedClients.containsKey(entry.getKey()))
                      .collect(Collectors.toMap(
                          Map.Entry::getKey,
                          entry -> toClientRegistration(entry.getKey(), entry.getValue())));
        if (needDelegation()) {
            result.putAll(delegatedClients);
            result.put(AZURE_CLIENT_REGISTRATION_ID, azureClient.getClient());
        }
        return Collections.unmodifiableMap(result);
    }

    private ClientRegistration.Builder toClientRegistrationBuilder(String registrationId,
                                                                   AuthorizationClientProperties clientProperties) {
        AADAuthorizationServerEndpoints endpoints =
            new AADAuthorizationServerEndpoints(properties.getBaseUri(), properties.getTenantId());
        AuthorizationGrantType authorizationGrantType;
        switch (clientProperties.getAuthorizationGrantType()) {
            case AUTHORIZATION_CODE:
                authorizationGrantType = AuthorizationGrantType.AUTHORIZATION_CODE;
                break;
            case ON_BEHALF_OF:
                authorizationGrantType = new AuthorizationGrantType(AADAuthorizationGrantType.ON_BEHALF_OF.getValue());
                break;
            case CLIENT_CREDENTIALS:
                authorizationGrantType = AuthorizationGrantType.CLIENT_CREDENTIALS;
                break;
            default:
                throw new IllegalArgumentException("Unsupported authorization type "
                    + clientProperties.getAuthorizationGrantType().getValue());
        }
        return ClientRegistration.withRegistrationId(registrationId)
                                 .clientName(registrationId)
                                 .authorizationGrantType(authorizationGrantType)
                                 .scope(toScopes(clientProperties))
                                 .redirectUri(properties.getRedirectUriTemplate())
                                 .userNameAttributeName(properties.getUserNameAttribute())
                                 .clientId(properties.getClientId())
                                 .clientSecret(properties.getClientSecret())
                                 .authorizationUri(endpoints.authorizationEndpoint())
                                 .tokenUri(endpoints.tokenEndpoint())
                                 .jwkSetUri(endpoints.jwkSetEndpoint())
                                 .providerConfigurationMetadata(providerConfigurationMetadata(endpoints));
    }

    private AuthorizationClientProperties defaultAzureAuthorizationClientProperties() {
        AuthorizationClientProperties result = new AuthorizationClientProperties();
        result.setAuthorizationGrantType(AUTHORIZATION_CODE);
        return result;
    }

    private List<String> toScopes(AuthorizationClientProperties clientProperties) {
        List<String> result = clientProperties.getScopes();
        if (clientProperties.isOnDemand()) {
            if (!result.contains("openid")) {
                result.add("openid");
            }
            if (!result.contains("profile")) {
                result.add("profile");
            }
        }
        return result;
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

    private Set<String> azureClientAuthorizationCodeScopes() {
        Set<String> result = azureClientAccessTokenScopes();
        result.addAll(delegatedClientsAccessTokenScopes());
        return result;
    }

    private Set<String> delegatedClientsAccessTokenScopes() {
        return properties.getAuthorizationClients()
                         .values()
                         .stream()
                         .filter(p -> !p.isOnDemand() && AUTHORIZATION_CODE.equals(p.getAuthorizationGrantType()))
                         .flatMap(p -> p.getScopes().stream())
                         .collect(Collectors.toSet());
    }

    private Set<String> azureClientAccessTokenScopes() {
        Set<String> result = Optional.of(properties)
                                     .map(AADAuthenticationProperties::getAuthorizationClients)
                                     .map(clients -> clients.get(AZURE_CLIENT_REGISTRATION_ID))
                                     .map(AuthorizationClientProperties::getScopes)
                                     .map(Collection::stream)
                                     .orElseGet(Stream::empty)
                                     .collect(Collectors.toSet());
        result.addAll(azureClientOpenidScopes());
        if (properties.allowedGroupIdsConfigured() || properties.allowedGroupNamesConfigured()) {
            // The 2 scopes are need to get group name from graph.
            result.add(properties.getGraphBaseUri() + "User.Read");
            result.add(properties.getGraphBaseUri() + "Directory.Read.All");
        }
        return result;
    }

    private Set<String> azureClientOpenidScopes() {
        Set<String> result = new HashSet<>();
        result.add("openid");
        result.add("profile");

        if (!properties.getAuthorizationClients().isEmpty()) {
            result.add("offline_access");
        }
        return result;
    }

    private ClientRegistration toClientRegistration(String registrationId,
                                                    AuthorizationClientProperties clientProperties) {
        return toClientRegistrationBuilder(registrationId, clientProperties).build();
    }

    private boolean isAzureDelegatedClientRegistration(String registrationId,
                                                       AuthorizationClientProperties clientProperties) {
        return !AZURE_CLIENT_REGISTRATION_ID.equals(registrationId)
            && AUTHORIZATION_CODE.equals(clientProperties.getAuthorizationGrantType())
            && !clientProperties.isOnDemand();
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        Assert.hasText(registrationId, "registrationId cannot be empty");
        return allClients.get(registrationId);
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        if (!needDelegation()) {
            return allClients.values().iterator();
        }
        return Collections.singleton(azureClient.getClient()).iterator();
    }

    public AzureClientRegistration getAzureClient() {
        return azureClient;
    }

    public boolean isAzureDelegatedClientRegistration(ClientRegistration client) {
        return delegatedClients.containsValue(client);
    }

    public boolean isAzureDelegatedClientRegistration(String registrationId) {
        return delegatedClients.containsKey(registrationId);
    }

    public static boolean isDefaultClient(String registrationId) {
        return AZURE_CLIENT_REGISTRATION_ID.equals(registrationId);
    }
}
