// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapp.AuthorizationClientProperties;
import com.azure.spring.aad.webapp.AzureClientRegistration;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Manage all AAD oauth2 clients configured by property "azure.activedirectory.xxx"
 */
public class AADClientRegistrationRepository
    implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    public static final String AZURE_CLIENT_REGISTRATION_ID = "azure";

    private final Map<String, ClientRegistration> allRegistrations;
    private final Map<String, ClientRegistration> delegatedRegistrations;
    private final AzureClientRegistration azureRegistration;

    @NotNull
    @Override
    public Iterator<ClientRegistration> iterator() {
        return Collections.singleton(azureRegistration.getClient()).iterator();
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return allRegistrations.get(registrationId);
    }

    public AADClientRegistrationRepository(AADAuthenticationProperties properties) {
        delegatedRegistrations = azureDelegatedClientRegistrations(properties);
        azureRegistration = azureClientRegistration(properties, delegatedRegistrations);
        allRegistrations = allClientRegistrations(properties, delegatedRegistrations, azureRegistration);
    }

    Map<String, ClientRegistration> allClientRegistrations(AADAuthenticationProperties properties,
                                                           Map<String, ClientRegistration> delegatedRegistrations,
                                                           AzureClientRegistration azureClientRegistration) {
        Map<String, ClientRegistration> result =
            properties.getAuthorizationClients()
                      .entrySet()
                      .stream()
                      .filter(entry -> !delegatedRegistrations.containsKey(entry.getKey()))
                      .collect(Collectors.toMap(
                          Map.Entry::getKey,
                          entry -> toClientRegistration(properties, entry.getKey(), entry.getValue())));
        result.putAll(delegatedRegistrations);
        ClientRegistration azureClient = azureClientRegistration.getClient();
        result.put(AZURE_CLIENT_REGISTRATION_ID, azureClient);
        return result;
    }

    private Map<String, ClientRegistration> azureDelegatedClientRegistrations(AADAuthenticationProperties properties) {
        return properties.getAuthorizationClients()
                         .entrySet()
                         .stream()
                         .filter(entry -> isAzureDelegatedClientRegistration(entry.getKey(), entry.getValue()))
                         .collect(Collectors.toMap(
                             Map.Entry::getKey,
                             entry -> toClientRegistration(properties, entry.getKey(), entry.getValue())));
    }

    private boolean isAzureDelegatedClientRegistration(String id, AuthorizationClientProperties clientProperties) {
        return !id.equals(AZURE_CLIENT_REGISTRATION_ID)
            && clientProperties.getAuthorizationGrantType().equals(AuthorizationGrantType.AUTHORIZATION_CODE)
            && !clientProperties.isOnDemand();
    }

    private AzureClientRegistration azureClientRegistration(AADAuthenticationProperties properties,
                                                            Map<String, ClientRegistration> azureDelegatedClientRegistrations) {
        AuthorizationClientProperties azureProperties =
            properties.getAuthorizationClients()
                      .getOrDefault(AZURE_CLIENT_REGISTRATION_ID, defaultAzureAuthorizationClientProperties());
        ClientRegistration.Builder builder = toClientRegistrationBuilder(properties,
            AZURE_CLIENT_REGISTRATION_ID,
            azureProperties);
        Set<String> authorizationCodeScopes = authorizationCodeScopes(properties, azureDelegatedClientRegistrations);
        ClientRegistration client = builder.scope(authorizationCodeScopes).build();
        Set<String> accessTokenScopes = accessTokenScopes(properties);
        if (resourceServerCount(accessTokenScopes) == 0 && resourceServerCount((authorizationCodeScopes)) > 1) {
            // AAD server will return error if:
            // 1. authorizationCodeScopes have more than one resource server.
            // 2. accessTokenScopes have no resource server
            accessTokenScopes.add(properties.getGraphBaseUri() + "User.Read");
        }
        return new AzureClientRegistration(client, accessTokenScopes);
    }

    private AuthorizationClientProperties defaultAzureAuthorizationClientProperties() {
        AuthorizationClientProperties result = new AuthorizationClientProperties();
        result.setAuthorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
        return result;
    }

    private Set<String> authorizationCodeScopes(AADAuthenticationProperties properties,
                                                Map<String, ClientRegistration> azureDelegatedClientRegistrations) {
        Set<String> result = azureDelegatedClientRegistrations.values()
                                                              .stream()
                                                              .map(ClientRegistration::getScopes)
                                                              .filter(Objects::nonNull)
                                                              .flatMap(Collection::stream)
                                                              .collect(Collectors.toSet());
        result.addAll(accessTokenScopes(properties));
        return result;
    }

    private Set<String> accessTokenScopes(AADAuthenticationProperties properties) {
        Set<String> result = Optional.of(properties)
                                     .map(AADAuthenticationProperties::getAuthorizationClients)
                                     .map(clients -> clients.get(AZURE_CLIENT_REGISTRATION_ID))
                                     .map(AuthorizationClientProperties::getScopes)
                                     .map(Collection::stream)
                                     .orElseGet(Stream::empty)
                                     .collect(Collectors.toSet());
        result.addAll(openidScopes(properties));
        if (properties.allowedGroupsConfigured()) {
            // The 2 scopes are need to get group name from graph.
            result.add(properties.getGraphBaseUri() + "User.Read");
            result.add(properties.getGraphBaseUri() + "Directory.Read.All");
        }
        return result;
    }

    private Set<String> openidScopes(AADAuthenticationProperties properties) {
        Set<String> result = new HashSet<>();
        result.add("openid");
        result.add("profile");

        if (!properties.getAuthorizationClients().isEmpty()) {
            result.add("offline_access");
        }
        return result;
    }

    private ClientRegistration toClientRegistration(AADAuthenticationProperties properties,
                                                    String id,
                                                    AuthorizationClientProperties clientProperties) {
        return toClientRegistrationBuilder(properties, id, clientProperties).build();
    }

    private ClientRegistration.Builder toClientRegistrationBuilder(AADAuthenticationProperties properties,
                                                                   String id,
                                                                   AuthorizationClientProperties clientProperties) {
        AADAuthorizationServerEndpoints endpoints =
            new AADAuthorizationServerEndpoints(properties.getBaseUri(), properties.getTenantId());
        return ClientRegistration.withRegistrationId(id)
                                 .authorizationGrantType(clientProperties.getAuthorizationGrantType())
                                 .scope(toScopes(clientProperties))
                                 .redirectUri("{baseUrl}/login/oauth2/code/")
                                 .userNameAttributeName(properties.getUserNameAttribute())
                                 .clientId(toClientId(properties))
                                 .clientSecret(properties.getClientSecret())
                                 .authorizationUri(endpoints.authorizationEndpoint())
                                 .tokenUri(endpoints.tokenEndpoint())
                                 .jwkSetUri(endpoints.jwkSetEndpoint())
                                 .providerConfigurationMetadata(providerConfigurationMetadata(endpoints));
    }

    private String toClientId(AADAuthenticationProperties properties) {
        return Optional.of(properties)
                       .map(AADAuthenticationProperties::getClientId)
                       .filter(StringUtils::hasText)
                       .orElse("client-id-not-configured");
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

    public AzureClientRegistration getAzureRegistration() {
        return azureRegistration;
    }

    public boolean isAzureDelegatedClientRegistrations(ClientRegistration client) {
        return delegatedRegistrations.containsValue(client);
    }

    public boolean isAzureDelegatedClientRegistrations(String id) {
        return delegatedRegistrations.containsKey(id);
    }

    public static boolean isAzureClient(String registrationId) {
        return AZURE_CLIENT_REGISTRATION_ID.equals(registrationId);
    }
}
