// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.*;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.conditions.ClientCertificatePropertiesCondition;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.conditions.ClientRegistrationCondition;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.properties.AadAuthorizationServerEndpoints;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadProfileProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.JwtBearerOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.RestClientJwtBearerTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.RestClientRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.web.client.RestClient;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createOAuth2AccessTokenResponseClientRestTemplate;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createRestTemplate;

@Configuration(proxyBeanMethods = false)
@Conditional(ClientRegistrationCondition.class)
class AadOAuth2ClientConfiguration {

    private final RestTemplateBuilder restTemplateBuilder;

    AadOAuth2ClientConfiguration(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Bean
    @ConditionalOnMissingBean
    ClientRegistrationRepository clientRegistrationRepository(AadAuthenticationProperties properties) {
        return new AadClientRegistrationRepository(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository() {
        return new JacksonHttpSessionOAuth2AuthorizedClientRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional(ClientCertificatePropertiesCondition.class)
    OAuth2ClientAuthenticationJwkResolver oAuth2ClientAuthenticationJwkResolver(AadAuthenticationProperties properties) {
        return new AadOAuth2ClientAuthenticationJwkResolver(
                properties.getCredential().getClientCertificatePath(),
                properties.getCredential().getClientCertificatePassword());
    }

    @Bean
    @ConditionalOnMissingBean
    OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrations,
            OAuth2AuthorizedClientRepository authorizedClients,
            RefreshTokenOAuth2AuthorizedClientProvider refreshTokenProvider,
            JwtBearerOAuth2AuthorizedClientProvider jwtBearerProvider,
            ObjectProvider<OAuth2ClientAuthenticationJwkResolver> jwkResolvers) {
        DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);

        OAuth2ClientAuthenticationJwkResolver jwkResolver = jwkResolvers.getIfUnique();
        // @formatter:off
        OAuth2AuthorizedClientProvider providers =
                OAuth2AuthorizedClientProviderBuilder
                        .builder()
                        .authorizationCode()
                        .clientCredentials(builder ->
                                clientCredentialsGrantBuilderAccessTokenResponseClientCustomizer(builder, jwkResolver))
                        .provider(refreshTokenProvider)
                        .provider(jwtBearerProvider)
                        .provider(azureDelegatedOAuth2AuthorizedClientProvider(refreshTokenProvider, authorizedClients))
                        .build();
        // @formatter:on
        manager.setAuthorizedClientProvider(providers);
        return manager;
    }

    @Bean
    @ConditionalOnMissingBean
    JwtBearerOAuth2AuthorizedClientProvider azureAdJwtBearerProvider(
            ObjectProvider<OAuth2ClientAuthenticationJwkResolver> resolvers) {
        JwtBearerOAuth2AuthorizedClientProvider provider = new JwtBearerOAuth2AuthorizedClientProvider();
        OAuth2ClientAuthenticationJwkResolver resolver = resolvers.getIfUnique();
        RestClientJwtBearerTokenResponseClient client = new RestClientJwtBearerTokenResponseClient();
        client.setRestClient(RestClient.create(createOAuth2AccessTokenResponseClientRestTemplate(restTemplateBuilder)));
        client.addParametersConverter(new AadJwtBearerGrantRequestParametersConverter());
        if (resolver != null) {
            client.addParametersConverter(new AadJwtClientAuthenticationParametersConverter<>(resolver::resolve));
        }
        provider.setAccessTokenResponseClient(client);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    RefreshTokenOAuth2AuthorizedClientProvider azureRefreshTokenProvider(
            ObjectProvider<OAuth2ClientAuthenticationJwkResolver> resolvers) {
        RefreshTokenOAuth2AuthorizedClientProvider provider = new RefreshTokenOAuth2AuthorizedClientProvider();
        OAuth2ClientAuthenticationJwkResolver resolver = resolvers.getIfUnique();
        RestClientRefreshTokenTokenResponseClient client = new RestClientRefreshTokenTokenResponseClient();
        client.setRestClient(RestClient.create(createOAuth2AccessTokenResponseClientRestTemplate(restTemplateBuilder)));
        if (resolver != null) {
            client.addParametersConverter(new AadJwtClientAuthenticationParametersConverter<>(resolver::resolve));
        }
        provider.setAccessTokenResponseClient(client);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    JwtDecoderFactory<ClientRegistration> azureAdJwtDecoderFactory(AadAuthenticationProperties properties) {
        AadProfileProperties profile = properties.getProfile();
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(
                profile.getEnvironment().getActiveDirectoryEndpoint(), profile.getTenantId());
        return new AadOidcIdTokenDecoderFactory(endpoints.getJwkSetEndpoint(), createRestTemplate(restTemplateBuilder));
    }

    private void clientCredentialsGrantBuilderAccessTokenResponseClientCustomizer(
            OAuth2AuthorizedClientProviderBuilder.ClientCredentialsGrantBuilder builder,
            OAuth2ClientAuthenticationJwkResolver resolver) {
        RestClientClientCredentialsTokenResponseClient client = new RestClientClientCredentialsTokenResponseClient();
        client.setRestClient(RestClient.create(createOAuth2AccessTokenResponseClientRestTemplate(restTemplateBuilder)));
        if (resolver != null) {
            client.addParametersConverter(new AadJwtClientAuthenticationParametersConverter<>(resolver::resolve));
        }
        builder.accessTokenResponseClient(client);
    }

    private AadAzureDelegatedOAuth2AuthorizedClientProvider azureDelegatedOAuth2AuthorizedClientProvider(
            RefreshTokenOAuth2AuthorizedClientProvider refreshTokenProvider,
            OAuth2AuthorizedClientRepository authorizedClients) {
        return new AadAzureDelegatedOAuth2AuthorizedClientProvider(refreshTokenProvider, authorizedClients);
    }
}
