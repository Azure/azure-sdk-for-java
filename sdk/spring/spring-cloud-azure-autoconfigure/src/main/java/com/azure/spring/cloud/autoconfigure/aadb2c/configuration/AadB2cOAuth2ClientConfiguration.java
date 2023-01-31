// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.configuration;

import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cClientRegistrationRepository;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cClientRegistrationRepositoryBuilderConfigurer;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cClientRegistrationRepositoryBuilder;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cConditions;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.ClientRegistrationRepositoryConfigurerAdapter;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;

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

    @Bean
    AadB2cClientRegistrationRepositoryBuilder aadB2cClientRegistrationRepositoryBuilder() {
        return new AadB2cClientRegistrationRepositoryBuilder();
    }

    /**
     * Declare ClientRegistrationRepository bean.
     * @return ClientRegistrationRepository bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AadB2cClientRegistrationRepository clientRegistrationRepository(AadB2cClientRegistrationRepositoryBuilder repositoryBuilder,
                                                                     ObjectProvider<OAuth2ClientProperties> oAuth2ClientProperties,
                                                                     ObjectProvider<ClientRegistrationRepositoryConfigurerAdapter<AadB2cClientRegistrationRepository>> configurers) throws Exception {
        AadB2cClientRegistrationRepositoryBuilderConfigurer defaultB2cConfigurer =
            repositoryBuilder.b2cClientRegistration()
                                 .clientId(properties.getCredential().getClientId())
                                 .clientSecret(properties.getCredential().getClientSecret())
                                 .tenantId(properties.getProfile().getTenantId())
                                 .baseUri(properties.getBaseUri())
                                 .loginFlow(properties.getLoginFlow())
                                 .replyUrl(properties.getReplyUrl())
                                 .userNameAttributeName(properties.getUserNameAttributeName())
                                 .userFlows(properties.getUserFlows())
                                 .authorizationClients(properties.getAuthorizationClients());
        oAuth2ClientProperties.ifAvailable(repositoryBuilder::oAuth2ClientRegistrations);
        repositoryBuilder.apply(defaultB2cConfigurer);
        configurers.orderedStream().forEach(repositoryBuilder::apply);
        return repositoryBuilder.build();
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
