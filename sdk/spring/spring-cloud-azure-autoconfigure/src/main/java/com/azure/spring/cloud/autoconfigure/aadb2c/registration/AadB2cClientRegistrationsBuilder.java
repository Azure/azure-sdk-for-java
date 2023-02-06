// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.registration;

import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

/**
 * This class provides a fluent builder API to help aid the instantiation of {@link AadB2cClientRegistrations}
 * for Azure AD B2C, they serve as a set of client registration information corresponding
 * to an application registered in Azure AD B2C.
 */
public class AadB2cClientRegistrationsBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadB2cClientRegistrationsBuilder.class);
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String userNameAttributeName;
    private String replyUrl = "{baseUrl}/login/oauth2/code/";
    private String baseUri;
    private String signInUserFlow;
    private final Set<String> userFlows = new HashSet<>();
    private final Map<String, Tuple2<AuthorizationGrantType, Set<String>>> authorizationClients = new HashMap<>();

    /**
     * Sets the client identifier.
     *
     * @param clientId the client identifier
     * @return the updated {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the client secret.
     *
     * @param clientSecret the client secret
     * @return the updated {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Sets the id of the AD B2C tenant, it's required when setting the authorization clients.
     * @param tenantId the id of the AD B2C tenant.
     * @return the updated {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the username attribute name.
     *
     * @param userNameAttributeName the username attribute name
     * @return the updated {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder userNameAttributeName(String userNameAttributeName) {
        this.userNameAttributeName = userNameAttributeName;
        return this;
    }

    /**
     * Sets the uri for the redirection endpoint.
     *
     * @param replyUrl the uri for the redirection endpoint
     * @return the updated {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder replyUrl(String replyUrl) {
        this.replyUrl = replyUrl;
        return this;
    }

    /**
     * Sets the Azure AD B2C endpoint base uri.
     *
     * @param baseUri the base uri for the authorization endpoint
     * @return the updated {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder baseUri(String baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    /**
     * Sets the primary sign-in flow instance id, no need to set this value in {@link #userFlows} anymore.
     *
     * @param signInUserFlow the key for the primary sign-in or sign-up user flow id
     * @return the updated {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder signInUserFlow(String signInUserFlow) {
        this.signInUserFlow = signInUserFlow;
        return this;
    }

    /**
     * Sets the user flow instance id set, which should not include the sign in user flow instance id.
     * @param userFlows the user flow instance id for each user flow type.
     * @return the updated {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder userFlows(String... userFlows) {
        if (userFlows != null && userFlows.length > 0) {
            this.userFlows.addAll(new HashSet<>(Arrays.asList(userFlows)));
        }
        return this;
    }

    /**
     * Sets one authorization client to current AD B2C application, it only supports
     * to set the grant type {@link AuthorizationGrantType#CLIENT_CREDENTIALS}.
     * @param registrationId the client registration id of OAuth2 client.
     * @param authorizationGrantType the authorization grant type of OAuth2 client.
     * @param scopes the scope set of OAuth2 client.
     * @return the updated {@link AadB2cClientRegistrationsBuilder}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public AadB2cClientRegistrationsBuilder authorizationClient(String registrationId,
                                                                AuthorizationGrantType authorizationGrantType,
                                                                String... scopes) {
        Set<String> clientScopes = Collections.emptySet();
        if (scopes != null && scopes.length > 0) {
            clientScopes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(scopes)));
        }

        this.authorizationClients.put(registrationId, Tuples.of(authorizationGrantType, clientScopes));

        return this;
    }

    /**
     * Creates an {@link AadB2cClientRegistrations} based on configuration in this builder.
     * @return an {@link AadB2cClientRegistrations} created from the configurations in this builder.
     */
    public AadB2cClientRegistrations build() {
        validateConfig();
        List<ClientRegistration> registrations = new ArrayList<>();
        Set<String> nonSignInClientRegistrationIds = new HashSet<>();

        final List<ClientRegistration> userFlowRegistrations =
            Stream.concat(Stream.of(signInUserFlow), userFlows.stream())
                  .map(this::buildClientRegistration)
                  .collect(Collectors.toList());

        final List<ClientRegistration> authorizationClientRegistrations = authorizationClients
            .entrySet()
            .stream()
            .map(entry -> buildClientRegistration(entry.getKey(), entry.getValue().getT1(), entry.getValue().getT2()))
            .collect(Collectors.toList());
        registrations.addAll(userFlowRegistrations);
        registrations.addAll(authorizationClientRegistrations);

        nonSignInClientRegistrationIds.addAll(userFlows);
        nonSignInClientRegistrationIds.addAll(authorizationClients.keySet());
        return new AadB2cClientRegistrations(registrations, nonSignInClientRegistrationIds);
    }

    /**
     * Validate configurations, and throw an exception if found invalid configuration.
     */
    private void validateConfig() {
        Assert.isTrue(StringUtils.hasText(baseUri), "The 'baseUri' cannot be empty.");
        try {
            new java.net.URL(baseUri);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Found invalid URL '" + baseUri + "'.");
        }

        Assert.isTrue(StringUtils.hasText(signInUserFlow), "The 'signInUserFlow' user flow instance id cannot be empty.");
        if (userFlows.contains(signInUserFlow)) {
            throw new IllegalArgumentException("Found duplicate sign-in user flow instance id, "
                + "please remove '" + signInUserFlow + "' from 'userFlows'.");
        }

        if (authorizationClients.size() > 0 && !StringUtils.hasText(tenantId)) {
            throw new IllegalArgumentException("'tenantId' must be provided when using client credential grant type.");
        }
    }

    /**
     * Build OAuth2 login {@link ClientRegistration} of authorization code authorization grant, it's used for one Azure AD B2C user flow configuration.
     * @param userFlow the user flow instance id.
     * @return the client registration for the user flow instance.
     */
    private ClientRegistration buildClientRegistration(String userFlow) {
        Map<String, Object> providerConfigurationMetadata = null;
        if (userFlow.equals(signInUserFlow)) {
            providerConfigurationMetadata = new HashMap<>();
            providerConfigurationMetadata.put("end_session_endpoint", AadB2cUrl.getEndSessionUrl(baseUri, userFlow));
        }
        return ClientRegistration.withRegistrationId(userFlow)
                                 .clientName(userFlow)
                                 .clientId(clientId)
                                 .clientSecret(clientSecret)
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUri(replyUrl)
                                 .scope(clientId, "openid", "offline_access")
                                 .authorizationUri(AadB2cUrl.getAuthorizationUrl(baseUri))
                                 .tokenUri(AadB2cUrl.getTokenUrl(baseUri, userFlow))
                                 .jwkSetUri(AadB2cUrl.getJwkSetUrl(baseUri, userFlow))
                                 .userNameAttributeName(userNameAttributeName)
                                 .providerConfigurationMetadata(providerConfigurationMetadata)
                                 .build();
    }

    /**
     * Build OAuth2 {@link ClientRegistration} of client credentials authorization grant type.
     * It represents a unique resource client in an application registered with Azure AD B2C.
     * @param clientRegistrationId the OAuth2 client registration id.
     * @param authorizationGrantType the authorization grant type.
     * @param scopes the resource identifier (application ID URI).
     * @return the client registration for Azure AD B2C authorization client.
     */
    private ClientRegistration buildClientRegistration(String clientRegistrationId,
                                                       AuthorizationGrantType authorizationGrantType,
                                                       Set<String> scopes) {
        Assert.isTrue(CLIENT_CREDENTIALS.equals(authorizationGrantType),
            "The authorization type of the " + clientRegistrationId + " client registration is not supported.");
        return ClientRegistration.withRegistrationId(clientRegistrationId)
                                 .clientName(clientRegistrationId)
                                 .clientId(clientId)
                                 .clientSecret(clientSecret)
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                                 .authorizationGrantType(authorizationGrantType)
                                 .scope(scopes)
                                 .tokenUri(AadB2cUrl.getAADTokenUrl(tenantId))
                                 .jwkSetUri(AadB2cUrl.getAADJwkSetUrl(tenantId))
                                 .build();
    }
}
