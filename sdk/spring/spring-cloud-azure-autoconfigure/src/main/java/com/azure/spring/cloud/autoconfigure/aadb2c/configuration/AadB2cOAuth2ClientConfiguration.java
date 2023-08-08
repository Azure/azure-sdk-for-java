// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.configuration;

import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cClientRegistrationRepository;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cConditions;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cUrl;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AuthorizationClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.ClientCredentialsOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.PasswordOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultPasswordTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestTemplateCreator.createOAuth2AccessTokenResponseClientRestTemplate;

/**
 * Configuration for AAD B2C OAuth2 client support, when depends on the Spring OAuth2 Client module.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.b2c.enabled", havingValue = "true")
@Conditional(AadB2cConditions.ClientRegistrationCondition.class)
@Import(AadB2cPropertiesConfiguration.class)
@ConditionalOnClass({ OAuth2LoginAuthenticationFilter.class })
public class AadB2cOAuth2ClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadB2cOAuth2ClientConfiguration.class);
    private final AadB2cProperties properties;
    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Creates a new instance of {@link AadB2cOAuth2ClientConfiguration}.
     *
     * @param properties the AAD B2C properties
     * @param restTemplateBuilder the restTemplateBuilder
     */
    public AadB2cOAuth2ClientConfiguration(AadB2cProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    /**
     * Declare ClientRegistrationRepository bean.
     * @return ClientRegistrationRepository bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientRegistrationRepository clientRegistrationRepository() {
        Stream<ClientRegistration> clientRegistrationStream = properties.getUserFlows()
                                                                        .entrySet()
                                                                        .stream()
                                                                        .map(this::buildUserFlowClientRegistration);

        Stream<ClientRegistration> authorizationClientRegistrations = properties.getAuthorizationClients()
                                                                                .entrySet()
                                                                                .stream()
                                                                                .map(this::buildClientRegistration);
        final List<ClientRegistration> clientRegistrations = Stream.concat(clientRegistrationStream,
                                                                       authorizationClientRegistrations)
                                                                   .collect(Collectors.toList());
        return new AadB2cClientRegistrationRepository(properties.getLoginFlow(), clientRegistrations);
    }

    /**
     * Build user flow client registration.
     * @param client user flow properties
     * @return ClientRegistration
     */
    private ClientRegistration buildUserFlowClientRegistration(Map.Entry<String, String> client) {
        return ClientRegistration.withRegistrationId(client.getValue()) // Use flow as registration ID.
                                 .clientName(client.getKey())
                                 .clientId(properties.getCredential().getClientId())
                                 .clientSecret(properties.getCredential().getClientSecret())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUri(properties.getReplyUrl())
                                 .scope(properties.getCredential().getClientId(), "openid", "offline_access")
                                 .authorizationUri(AadB2cUrl.getAuthorizationUrl(properties.getBaseUri()))
                                 .tokenUri(AadB2cUrl.getTokenUrl(properties.getBaseUri(), client.getValue()))
                                 .jwkSetUri(AadB2cUrl.getJwkSetUrl(properties.getBaseUri(), client.getValue()))
                                 .userNameAttributeName(properties.getUserNameAttributeName())
                                 .build();
    }

    /**
     * Create client registration, only support OAuth2 client credentials.
     *
     * @param client each client properties
     * @return ClientRegistration
     */
    private ClientRegistration buildClientRegistration(Map.Entry<String, AuthorizationClientProperties> client) {
        AuthorizationGrantType authGrantType = client.getValue().getAuthorizationGrantType();
        if (!AuthorizationGrantType.CLIENT_CREDENTIALS.equals(authGrantType)) {
            LOGGER.warn("The authorization type of the {} client registration is not supported.", client.getKey());
        }
        return ClientRegistration.withRegistrationId(client.getKey())
                                 .clientName(client.getKey())
                                 .clientId(properties.getCredential().getClientId())
                                 .clientSecret(properties.getCredential().getClientSecret())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                 .authorizationGrantType(authGrantType)
                                 .scope(client.getValue().getScopes())
                                 .tokenUri(AadB2cUrl.getAADTokenUrl(properties.getProfile().getTenantId()))
                                 .jwkSetUri(AadB2cUrl.getAADJwkSetUrl(properties.getProfile().getTenantId()))
                                 .build();
    }

    /**
     * Declare OAuth2AuthorizedClientManager bean.
     *
     * @param clients the client registration repository
     * @param authorizedClients the OAuth2 authorized client repository
     * @return OAuth2AuthorizedClientManager bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clients,
            OAuth2AuthorizedClientRepository authorizedClients) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .provider(azureRefreshTokenProvider())
                .provider(azureClientCredentialProvider())
                .provider(azurePasswordProvider())
                .build();
        DefaultOAuth2AuthorizedClientManager manager = new DefaultOAuth2AuthorizedClientManager(clients, authorizedClients);
        manager.setAuthorizedClientProvider(authorizedClientProvider);
        return manager;
    }

    private RefreshTokenOAuth2AuthorizedClientProvider azureRefreshTokenProvider() {
        RefreshTokenOAuth2AuthorizedClientProvider provider = new RefreshTokenOAuth2AuthorizedClientProvider();
        DefaultRefreshTokenTokenResponseClient responseClient = new DefaultRefreshTokenTokenResponseClient();
        responseClient.setRestOperations(createOAuth2AccessTokenResponseClientRestTemplate(restTemplateBuilder));
        provider.setAccessTokenResponseClient(responseClient);
        return provider;
    }

    private ClientCredentialsOAuth2AuthorizedClientProvider azureClientCredentialProvider() {
        ClientCredentialsOAuth2AuthorizedClientProvider provider = new ClientCredentialsOAuth2AuthorizedClientProvider();
        DefaultClientCredentialsTokenResponseClient responseClient = new DefaultClientCredentialsTokenResponseClient();
        responseClient.setRestOperations(createOAuth2AccessTokenResponseClientRestTemplate(restTemplateBuilder));
        provider.setAccessTokenResponseClient(responseClient);
        return provider;
    }

    private PasswordOAuth2AuthorizedClientProvider azurePasswordProvider() {
        PasswordOAuth2AuthorizedClientProvider provider = new PasswordOAuth2AuthorizedClientProvider();
        DefaultPasswordTokenResponseClient responseClient = new DefaultPasswordTokenResponseClient();
        responseClient.setRestOperations(createOAuth2AccessTokenResponseClientRestTemplate(restTemplateBuilder));
        provider.setAccessTokenResponseClient(responseClient);
        return provider;
    }
}
