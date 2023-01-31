package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AuthorizationClientProperties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties.DEFAULT_KEY_SIGN_UP_OR_SIGN_IN;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;

/**
 * A builder for {@link AadB2cClientRegistrationRepositoryBuilder}.
 */
public final class AadB2cClientRegistrationRepositoryBuilderConfigurer extends ClientRegistrationRepositoryConfigurerAdapter<AadB2cClientRegistrationRepository> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadB2cClientRegistrationRepositoryBuilderConfigurer.class);

    private static final String USER_FLOW_INSTANCE_ID_PREFIX = "B2C_1_";
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String userNameAttributeName;
    private String replyUrl = "{baseUrl}/login/oauth2/code/";
    private String baseUri;
    private String loginFlow = DEFAULT_KEY_SIGN_UP_OR_SIGN_IN;

    private Map<String, String> userFlows = Collections.emptyMap();

    private Map<String, AuthorizationClientProperties> authorizationClients = Collections.emptyMap();

    public AadB2cClientRegistrationRepositoryBuilderConfigurer() {

    }

    /**
     * Sets the client identifier.
     *
     * @param clientId the client identifier
     * @return the {@link AadB2cClientRegistrationRepositoryBuilderConfigurer}
     */
    public AadB2cClientRegistrationRepositoryBuilderConfigurer clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the client secret.
     *
     * @param clientSecret the client secret
     * @return the {@link AadB2cClientRegistrationRepositoryBuilderConfigurer}
     */
    public AadB2cClientRegistrationRepositoryBuilderConfigurer clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public AadB2cClientRegistrationRepositoryBuilderConfigurer tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the username attribute name.
     *
     * @param userNameAttributeName the username attribute name
     * @return the {@link AadB2cClientRegistrationRepositoryBuilderConfigurer}
     */
    public AadB2cClientRegistrationRepositoryBuilderConfigurer userNameAttributeName(String userNameAttributeName) {
        this.userNameAttributeName = userNameAttributeName;
        return this;
    }

    /**
     * Sets the uri for the redirection endpoint.
     *
     * @param replyUrl the uri for the redirection endpoint
     * @return the {@link AadB2cClientRegistrationRepositoryBuilderConfigurer}
     * @since 5.4
     */
    public AadB2cClientRegistrationRepositoryBuilderConfigurer replyUrl(String replyUrl) {
        this.replyUrl = replyUrl;
        return this;
    }

    /**
     * Sets the Azure AD B2C endpoint base uri.
     *
     * @param baseUri the base uri for the authorization endpoint
     * @return the {@link AadB2cClientRegistrationRepositoryBuilderConfigurer}
     */
    public AadB2cClientRegistrationRepositoryBuilderConfigurer baseUri(String baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    /**
     * Sets the primary sign-in flow key.
     *
     * @param loginFlow the key for the primary sign-in or sign-up user flow id
     * @return the {@link AadB2cClientRegistrationRepositoryBuilderConfigurer}
     */
    public AadB2cClientRegistrationRepositoryBuilderConfigurer loginFlow(String loginFlow) {
        this.loginFlow = loginFlow;
        return this;
    }

    /**
     * Sets the user flow instance id mapping.
     *
     * @param userFlows the user flow instance id for each user flow type.
     * @return the {@link AadB2cClientRegistrationRepositoryBuilderConfigurer}
     */
    public AadB2cClientRegistrationRepositoryBuilderConfigurer userFlows(Map<String, String> userFlows) {
        this.userFlows = userFlows;
        return this;
    }

    public AadB2cClientRegistrationRepositoryBuilderConfigurer authorizationClients(Map<String, AuthorizationClientProperties> authorizationClients) {
        this.authorizationClients = authorizationClients;
        return this;
    }

    @Override
    public void configure(ClientRegistrationRepositoryBuilder<AadB2cClientRegistrationRepository> builder) throws Exception {

        // TODO validate properties

        List<ClientRegistration> registrations = this.create();
        List<ClientRegistration> signUpOrSignInList = registrations.stream()
                                                    .filter(this::filterSignInClientRegistrations)
                                                    .collect(Collectors.toList());
        builder.addSignInClientRegistrations(signUpOrSignInList);
        builder.addClientRegistrations(registrations);
    }

    @Override
    protected boolean filterSignInClientRegistrations(ClientRegistration clientRegistration) {
        if (clientRegistration.getRegistrationId().equals(userFlows.get(loginFlow))) {
            return true;
        } else return !clientRegistration.getRegistrationId().startsWith(USER_FLOW_INSTANCE_ID_PREFIX)
            && AUTHORIZATION_CODE.equals(clientRegistration.getAuthorizationGrantType());
    }

    public List<ClientRegistration> create() {
        List<ClientRegistration> registrations = new ArrayList<>();
        userFlows.entrySet().forEach(entry -> registrations.add(buildAadB2cUserFlowClientRegistrations(entry)));
        authorizationClients.entrySet().forEach(client -> registrations.add(buildAadB2cAuthorizationClientClientRegistrations(client)));
        return registrations;
    }

    @NotNull
    private ClientRegistration buildAadB2cAuthorizationClientClientRegistrations(Map.Entry<String, AuthorizationClientProperties> client) {
        AuthorizationGrantType authGrantType = client.getValue().getAuthorizationGrantType();
        Assert.isTrue(AuthorizationGrantType.CLIENT_CREDENTIALS.equals(authGrantType),
            "The authorization type of the " + client.getKey() + " client registration is not supported.");
        return ClientRegistration.withRegistrationId(client.getKey())
                                 .clientName(client.getKey())
                                 .clientId(this.clientId)
                                 .clientSecret(this.clientSecret)
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                                 .authorizationGrantType(authGrantType)
                                 .scope(client.getValue().getScopes())
                                 .tokenUri(AadB2cUrl.getAADTokenUrl(this.tenantId))
                                 .jwkSetUri(AadB2cUrl.getAADJwkSetUrl(this.tenantId))
                                 .build();
    }

    @NotNull
    private ClientRegistration buildAadB2cUserFlowClientRegistrations(Map.Entry<String, String> entry) {
        Map<String, Object> providerConfigurationMetadata = null;
        if (entry.getValue().equals(userFlows.get(loginFlow))) {
            providerConfigurationMetadata = new HashMap<>();
            providerConfigurationMetadata.put("end_session_endpoint",
                AadB2cUrl.getEndSessionUrl(this.baseUri, entry.getValue()));
        }
        return ClientRegistration.withRegistrationId(entry.getValue())
                                 .clientName(entry.getKey())
                                 .clientId(this.clientId)
                                 .clientSecret(this.clientSecret)
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                                 .authorizationGrantType(AUTHORIZATION_CODE)
                                 .redirectUri(this.replyUrl)
                                 .scope(this.clientId, "openid", "offline_access")
                                 .authorizationUri(AadB2cUrl.getAuthorizationUrl(this.baseUri))
                                 .tokenUri(AadB2cUrl.getTokenUrl(this.baseUri, entry.getValue()))
                                 .jwkSetUri(AadB2cUrl.getJwkSetUrl(this.baseUri, entry.getValue()))
                                 .userNameAttributeName(this.userNameAttributeName)
                                 .providerConfigurationMetadata(providerConfigurationMetadata)
                                 .build();
    }
}