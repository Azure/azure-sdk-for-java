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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties.DEFAULT_KEY_SIGN_UP_OR_SIGN_IN;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

/**
 * A builder for {@link AadB2cClientRegistrationRepositoryBuilder}.
 */
public final class AadB2cClientRegistrationsBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadB2cClientRegistrationsBuilder.class);

    private static final String USER_FLOW_INSTANCE_ID_PREFIX = "B2C_1_";
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String userNameAttributeName;
    private String replyUrl = "{baseUrl}/login/oauth2/code/";
    private String baseUri;

    private final Map<String, Tuple2<AuthorizationGrantType, Set<String>>> authorizationClients = new HashMap<>();
    private String signInUserFlow = DEFAULT_KEY_SIGN_UP_OR_SIGN_IN;

    private Set<String> userFlows = Collections.emptySet();

    private final AadB2cClientRegistrationRepositoryBuilder repositoryBuilder;
    private final AadB2cClientRegistrationRepositoryBuilderConfigurer configurer;

    public AadB2cClientRegistrationsBuilder(AadB2cClientRegistrationRepositoryBuilder repositoryBuilder) {
        this.repositoryBuilder = repositoryBuilder;
        this.configurer = new AadB2cClientRegistrationConfigurer();
        this.repositoryBuilder.configure(this.configurer);
    }

    public AadB2cClientRegistrationRepositoryBuilder and() {
        return repositoryBuilder;
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

    public AadB2cClientRegistrationsBuilder authorizationClient(String registrationId,
                                                                AuthorizationGrantType authorizationGrantType,
                                                                String... scope) {
        Set<String> scopes = Collections.emptySet();
        if (scope != null && scope.length > 0) {
            scopes = Collections.unmodifiableSet(new LinkedHashSet(Arrays.asList(scope)));
        }

        this.authorizationClients.put(registrationId, Tuples.of(authorizationGrantType, scopes));

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

    private class AadB2cClientRegistrationConfigurer implements AadB2cClientRegistrationRepositoryBuilderConfigurer {
        @Override
        public void configure(AadB2cClientRegistrationRepositoryBuilder builder) {
            // TODO validate properties

            List<ClientRegistration> registrations = new ArrayList<>();

            final List<ClientRegistration> userFlowRegistrations = userFlows
                .stream()
                .map(flow -> buildClientRegistration(flow))
                .collect(Collectors.toList());

            final List<ClientRegistration> authorizationClientRegistrations = authorizationClients
                .entrySet()
                .stream()
                .map(entry -> buildClientRegistration(entry.getKey(), entry.getValue().getT1(), entry.getValue().getT2()))
                .collect(Collectors.toList());

            registrations.addAll(userFlowRegistrations);
            registrations.addAll(authorizationClientRegistrations);

            builder.clientRegistrations(registrations.toArray(new ClientRegistration[0]));

            builder.nonSignInClientRegistrationIds(userFlows.stream().filter(f -> !f.equals(signInUserFlow)).toArray(String[]::new));
        }

        /**
         * Build xxx
         * @param userFlow
         * @return
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
         *
         * @param clientRegistrationId
         * @param authorizationGrantType
         * @param scopes
         * @return
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
}
