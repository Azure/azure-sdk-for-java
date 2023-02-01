package com.azure.spring.cloud.autoconfigure.aadb2c.implementation.config;

import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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
 * This class provides a fluent builder API to help aid the instantiation of {@link ClientRegistration}
 * for Azure AD B2C OAuth2 Client.
 */
public final class AadB2cClientRegistrationsBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadB2cClientRegistrationsBuilder.class);
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String userNameAttributeName;
    private String replyUrl = "{baseUrl}/login/oauth2/code/";
    private String baseUri;

    private final Map<String, Tuple2<AuthorizationGrantType, Set<String>>> authorizationClients = new HashMap<>();
    private String signInUserFlow;

    private Set<String> userFlows = Collections.emptySet();

    /**
     * Creates a builder instance that is used to build {@link AadB2cClientRegistrations}.
     */
    public AadB2cClientRegistrationsBuilder() {

    }

    /**
     * Sets the client identifier.
     *
     * @param clientId the client identifier
     * @return the {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the client secret.
     *
     * @param clientSecret the client secret
     * @return the {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public AadB2cClientRegistrationsBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the username attribute name.
     *
     * @param userNameAttributeName the username attribute name
     * @return the {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder userNameAttributeName(String userNameAttributeName) {
        this.userNameAttributeName = userNameAttributeName;
        return this;
    }

    /**
     * Sets the uri for the redirection endpoint.
     *
     * @param replyUrl the uri for the redirection endpoint
     * @return the {@link AadB2cClientRegistrationsBuilder}
     * @since 5.4
     */
    public AadB2cClientRegistrationsBuilder replyUrl(String replyUrl) {
        this.replyUrl = replyUrl;
        return this;
    }

    /**
     * Sets the Azure AD B2C endpoint base uri.
     *
     * @param baseUri the base uri for the authorization endpoint
     * @return the {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder baseUri(String baseUri) {
        this.baseUri = baseUri;
        return this;
    }

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
     * Sets the primary sign-in flow key.
     *
     * @param signInUserFlow the key for the primary sign-in or sign-up user flow id
     * @return the {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder signInUserFlow(String signInUserFlow) {
        this.signInUserFlow = signInUserFlow;
        return this;
    }

    /**
     * Sets the user flow instance id mapping.
     *
     * @param userFlows the user flow instance id for each user flow type.
     * @return the {@link AadB2cClientRegistrationsBuilder}
     */
    public AadB2cClientRegistrationsBuilder userFlows(String... userFlows) {
        if (userFlows != null && userFlows.length > 0) {
            this.userFlows = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(userFlows)));
        }
        return this;
    }

    /**
     * Creates an {@link AadB2cClientRegistrations} based on configuration in the builder.
     * @return an {@link AadB2cClientRegistrations} created from the configurations in this builder.
     */
    public AadB2cClientRegistrations build() {
        // TODO validate properties

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
     * Build OAuth2 {@link ClientRegistration} of authorization code authorization grant, it's used for Azure AD B2C user flow configuration.
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
