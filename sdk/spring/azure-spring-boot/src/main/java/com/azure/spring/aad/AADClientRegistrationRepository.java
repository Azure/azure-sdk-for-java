// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapp.AuthorizationClientProperties;
import com.azure.spring.aad.webapp.AzureClientRegistration;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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

import static com.azure.spring.aad.AADApplicationType.applicationType;
import static com.azure.spring.aad.AADApplicationType.isResourceServerOnly;
import static com.azure.spring.aad.AADApplicationType.isResourceServerWithObo;
import static com.azure.spring.aad.AADApplicationType.isWebApplicationAndResourceServer;
import static com.azure.spring.aad.AADApplicationType.isWebApplicationOnly;
import static com.azure.spring.aad.AADAuthorizationGrantType.AUTHORIZATION_CODE;


/**
 * Manage all AAD oauth2 clients configured by property "azure.activedirectory.xxx"
 */
public class AADClientRegistrationRepository
    implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADClientRegistrationRepository.class);

    public static final String AZURE_CLIENT_REGISTRATION_ID = "azure";

    protected final AzureClientRegistration azureClientRegistration;
    protected final Map<String, ClientRegistration> delegatedClientRegistrations;
    protected final Map<String, ClientRegistration> allClientRegistrations;
    protected final AADAuthenticationProperties properties;
    private final Boolean isResourceServer;
    private final Boolean isWebApplicationOnly;
    private final Boolean isWebApplicationAndResourceServer;

    public AADClientRegistrationRepository(AADAuthenticationProperties properties) {
        this.properties = properties;

        AADApplicationType applicationType = applicationType(properties);
        this.isWebApplicationOnly = isWebApplicationOnly(applicationType);
        this.isResourceServer = isResourceServerOnly(applicationType) || isResourceServerWithObo(applicationType);
        this.isWebApplicationAndResourceServer = isWebApplicationAndResourceServer(applicationType);
        properties.getAuthorizationClients().forEach(this::handleDefaultAADAuthorizationGrantType);
        this.azureClientRegistration = azureClientRegistration();
        this.delegatedClientRegistrations = delegatedClientRegistrations();
        this.allClientRegistrations = allClientRegistrations();
    }

    private AzureClientRegistration azureClientRegistration() {
        if (!isWebApplicationOnly && !isWebApplicationAndResourceServer) {
            return emptyAzureClientRegistration();
        }

        AuthorizationClientProperties azureProperties =
            properties.getAuthorizationClients()
                      .getOrDefault(AZURE_CLIENT_REGISTRATION_ID, defaultAzureAuthorizationClientProperties());
        ClientRegistration.Builder builder = toClientRegistrationBuilder(AZURE_CLIENT_REGISTRATION_ID,
            azureProperties);
        Set<String> authorizationCodeScopes = authorizationCodeScopes();
        ClientRegistration client = builder.scope(authorizationCodeScopes).build();
        Set<String> accessTokenScopes = accessTokenScopes();
        if (resourceServerCount(accessTokenScopes) == 0 && resourceServerCount((authorizationCodeScopes)) > 1) {
            // AAD server will return error if:
            // 1. authorizationCodeScopes have more than one resource server.
            // 2. accessTokenScopes have no resource server
            accessTokenScopes.add(properties.getGraphBaseUri() + "User.Read");
        }
        return new AzureClientRegistration(client, accessTokenScopes);
    }

    private Map<String, ClientRegistration> delegatedClientRegistrations() {
        if (!isWebApplicationOnly && !isWebApplicationAndResourceServer) {
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
                      .filter(entry -> !delegatedClientRegistrations.containsKey(entry.getKey()))
                      .collect(Collectors.toMap(
                          Map.Entry::getKey,
                          entry -> toClientRegistration(entry.getKey(), entry.getValue())));
        result.putAll(delegatedClientRegistrations);
        ClientRegistration azureClient = azureClientRegistration.getClient();
        result.put(AZURE_CLIENT_REGISTRATION_ID, azureClient);
        return Collections.unmodifiableMap(result);
    }

    private void handleDefaultAADAuthorizationGrantType(String registrationId,
                                                        AuthorizationClientProperties properties) {
        if (null == properties.getAuthorizationGrantType()) {
            if (isWebApplicationOnly || AZURE_CLIENT_REGISTRATION_ID.equals(registrationId)) {
                properties.setAuthorizationGrantType(AUTHORIZATION_CODE);
                LOGGER.debug("The client '{}' sets the default value of AADAuthorizationGrantType to "
                    + "'authorization_code'.", registrationId);
            } else if (isResourceServer) {
                properties.setAuthorizationGrantType(AADAuthorizationGrantType.ON_BEHALF_OF);
                LOGGER.debug("The client '{}' sets the default value of AADAuthorizationGrantType to "
                    + "'on-behalf-of'.", registrationId);
            } else if (isWebApplicationAndResourceServer) {
                throw new IllegalStateException("azure.activedirectory.authorization-clients." + registrationId
                    + ".authorization-grant-type must be configured. ");
            }
        }

        if (AZURE_CLIENT_REGISTRATION_ID.equals(registrationId)
            && !AUTHORIZATION_CODE.equals(properties.getAuthorizationGrantType())) {
            throw new IllegalStateException("azure.activedirectory.authorization-clients."
                + AZURE_CLIENT_REGISTRATION_ID
                + ".authorization-grant-type must be configured to 'authorization_code'.");
        }
    }

    private ClientRegistration.Builder toClientRegistrationBuilder(String registrationId,
                                                                   AuthorizationClientProperties clientProperties) {
        Assert.notNull(clientProperties.getAuthorizationGrantType(),
            "AuthorizationGrantType can not be empty. " + "registrationId: " + registrationId + ".");
        LOGGER.debug("Client {} AuthorizationClientProperties: {}.", registrationId, clientProperties);
        AADAuthorizationServerEndpoints endpoints =
            new AADAuthorizationServerEndpoints(properties.getBaseUri(), properties.getTenantId());
        ClientRegistration.Builder clientRegistrationBuilder;
        switch (clientProperties.getAuthorizationGrantType()) {
            case AUTHORIZATION_CODE:
                clientRegistrationBuilder = toAuthorizationCodeBuilder(registrationId, endpoints, clientProperties);
                break;
            case ON_BEHALF_OF:
                clientRegistrationBuilder = toOnBehalfOfBuilder(registrationId, endpoints, clientProperties);
                break;
            case CLIENT_CREDENTIALS:
                clientRegistrationBuilder = toClientCredentialsBuilder(registrationId, endpoints, clientProperties);
                break;
            default:
                throw new IllegalArgumentException("Unsupported authorization type "
                    + clientProperties.getAuthorizationGrantType().getValue());
        }
        return clientRegistrationBuilder;
    }

    private ClientRegistration.Builder toAuthorizationCodeBuilder(String registrationId,
                                                              AADAuthorizationServerEndpoints endpoints,
                                                              AuthorizationClientProperties clientProperties) {
        return ClientRegistration.withRegistrationId(registrationId)
                                 .clientName(registrationId)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .scope(toScopes(clientProperties))
                                 .redirectUri(properties.getRedirectUriTemplate())
                                 .userNameAttributeName(properties.getUserNameAttribute())
                                 .clientId(toClientId(properties))
                                 .clientSecret(properties.getClientSecret())
                                 .authorizationUri(endpoints.authorizationEndpoint())
                                 .tokenUri(endpoints.tokenEndpoint())
                                 .jwkSetUri(endpoints.jwkSetEndpoint())
                                 .providerConfigurationMetadata(providerConfigurationMetadata(endpoints));
    }

    private ClientRegistration.Builder toOnBehalfOfBuilder(String registrationId,
                                                           AADAuthorizationServerEndpoints endpoints,
                                                           AuthorizationClientProperties clientProperties) {
        return ClientRegistration.withRegistrationId(registrationId)
                                 .clientName(registrationId)
                                 .authorizationGrantType(new AuthorizationGrantType(
                                     AADAuthorizationGrantType.ON_BEHALF_OF.getValue()))
                                 .scope(clientProperties.getScopes())
                                 .redirectUri("{baseUrl}/login/oauth2/code/")
                                 .userNameAttributeName(properties.getUserNameAttribute())
                                 .clientId(toClientId(properties))
                                 .clientSecret(properties.getClientSecret())
                                 .authorizationUri(endpoints.authorizationEndpoint());
    }

    private ClientRegistration.Builder toClientCredentialsBuilder(String registrationId,
                                                                  AADAuthorizationServerEndpoints endpoints,
                                                                  AuthorizationClientProperties clientProperties) {
        return ClientRegistration.withRegistrationId(registrationId)
                                 .clientName(registrationId)
                                 .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                 .scope(toScopes(clientProperties))
                                 .redirectUri(properties.getRedirectUriTemplate())
                                 .userNameAttributeName(properties.getUserNameAttribute())
                                 .clientId(toClientId(properties))
                                 .clientSecret(properties.getClientSecret())
                                 .tokenUri(endpoints.tokenEndpoint());
    }

    private AzureClientRegistration emptyAzureClientRegistration() {
        ClientRegistration.Builder clientRegistrationBuilder = toClientRegistrationBuilder(
            AZURE_CLIENT_REGISTRATION_ID, defaultAzureAuthorizationClientProperties());
        return new AzureClientRegistration(clientRegistrationBuilder.build(), new HashSet<>());
    }

    private AuthorizationClientProperties defaultAzureAuthorizationClientProperties() {
        AuthorizationClientProperties result = new AuthorizationClientProperties();
        result.setAuthorizationGrantType(AUTHORIZATION_CODE);
        return result;
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

    private Set<String> authorizationCodeScopes() {
        Set<String> result = accessTokenScopes();
        for (AuthorizationClientProperties authProperties : properties.getAuthorizationClients().values()) {
            if (!authProperties.isOnDemand()
                && isDefaultAuthorizationGrantType(authProperties)) {
                result.addAll(authProperties.getScopes());
            }
        }
        return result;
    }

    private boolean isDefaultAuthorizationGrantType(AuthorizationClientProperties authProperties) {
        return authProperties.getAuthorizationGrantType() == null
            || AUTHORIZATION_CODE.equals(authProperties.getAuthorizationGrantType());
    }

    private Set<String> accessTokenScopes() {
        Set<String> result = Optional.of(properties)
                                     .map(AADAuthenticationProperties::getAuthorizationClients)
                                     .map(clients -> clients.get(AZURE_CLIENT_REGISTRATION_ID))
                                     .map(AuthorizationClientProperties::getScopes)
                                     .map(Collection::stream)
                                     .orElseGet(Stream::empty)
                                     .collect(Collectors.toSet());
        result.addAll(openidScopes());
        if (properties.allowedGroupIdsConfigured() || properties.allowedGroupNamesConfigured()) {
            // The 2 scopes are need to get group name from graph.
            result.add(properties.getGraphBaseUri() + "User.Read");
            result.add(properties.getGraphBaseUri() + "Directory.Read.All");
        }
        return result;
    }


    private Set<String> openidScopes() {
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
        return allClientRegistrations.get(registrationId);
    }

    @NotNull
    @Override
    public Iterator<ClientRegistration> iterator() {
        return Collections.singleton(azureClientRegistration.getClient()).iterator();
    }

    public AzureClientRegistration getAzureClientRegistration() {
        return azureClientRegistration;
    }

    public boolean isAzureDelegatedClientRegistrations(ClientRegistration client) {
        return delegatedClientRegistrations.containsValue(client);
    }

    public boolean isAzureDelegatedClientRegistrations(String registrationId) {
        return delegatedClientRegistrations.containsKey(registrationId);
    }

    public boolean isClientCredentials(String registrationId) {
        return Optional.ofNullable(allClientRegistrations.get(registrationId))
             .map(ClientRegistration::getAuthorizationGrantType)
             .map(AuthorizationGrantType::getValue)
             .map(v -> v.equals(AADAuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
             .orElse(false);
    }

    public static boolean isDefaultClient(String registrationId) {
        return AZURE_CLIENT_REGISTRATION_ID.equals(registrationId);
    }
}
