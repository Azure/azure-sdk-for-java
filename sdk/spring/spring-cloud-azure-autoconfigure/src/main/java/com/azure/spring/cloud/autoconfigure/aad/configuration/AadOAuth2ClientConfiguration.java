// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import com.azure.spring.cloud.autoconfigure.aad.AadClientRegistrationRepository;
import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.ClientRegistrationCondition;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtClientAuthenticationParametersConverter;
import com.azure.spring.cloud.autoconfigure.aad.AadOAuth2ClientAuthenticationJWKResolver;
import com.azure.spring.cloud.autoconfigure.aad.OAuth2ClientAuthenticationJWKResolver;
import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.JacksonHttpSessionOAuth2AuthorizedClientRepository;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.AadJwtBearerGrantRequestEntityConverter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadAzureDelegatedOAuth2AuthorizedClientProvider;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.nimbusds.jose.jwk.JWK;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.JwtBearerOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultJwtBearerTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultPasswordTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import java.util.function.Function;

/**
 * <p>
 * The configuration will not be activated if no {@link ClientRegistration} classes provided.
 * </p>
 */
@Configuration(proxyBeanMethods = false)
@Conditional(ClientRegistrationCondition.class)
public class AadOAuth2ClientConfiguration {

    /**
     * Declare ClientRegistrationRepository bean.
     *
     * @param properties the AAD authentication properties
     * @return ClientRegistrationRepository bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientRegistrationRepository clientRegistrationRepository(AadAuthenticationProperties properties) {
        return new AadClientRegistrationRepository(properties);
    }

    /**
     * Declare OAuth2AuthorizedClientRepository bean.
     *
     * @return OAuth2AuthorizedClientRepository bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository() {
        return new JacksonHttpSessionOAuth2AuthorizedClientRepository();
    }

    /**
     * Return the resolver to resolve a {@link JWK} through the {@link ClientRegistration}.
     *
     * @param properties the AAD authentication properties
     * @return the function that will resolve out the JWK.
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2ClientAuthenticationJWKResolver oAuth2ClientAuthenticationJWKResolver(AadAuthenticationProperties properties) {
        return new AadOAuth2ClientAuthenticationJWKResolver(properties.getCredential().getClientCertificatePath(),
            properties.getCredential().getClientCertificatePassword());
    }

    /**
     * Declare OAuth2AuthorizedClientManager bean.
     *
     * @param clientRegistrations the client registration repository
     * @param authorizedClients the OAuth2 authorized client repository
     * @param jwkResolver the {@link JWK} function resolver
     * @return OAuth2AuthorizedClientManager bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrations,
                                                                 OAuth2AuthorizedClientRepository authorizedClients,
                                                                 OAuth2ClientAuthenticationJWKResolver jwkResolver) {
        DefaultOAuth2AuthorizedClientManager manager =
            new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
        Function<ClientRegistration, JWK> jwkFunction = jwkResolver.resolve();
        RefreshTokenOAuth2AuthorizedClientProvider refreshTokenProvider = getRefreshTokenProvider(jwkFunction);
        AadAzureDelegatedOAuth2AuthorizedClientProvider azureDelegatedProvider =
            new AadAzureDelegatedOAuth2AuthorizedClientProvider(refreshTokenProvider, authorizedClients);
        JwtBearerOAuth2AuthorizedClientProvider jwtBearerProvider =
            getJwtBearerProvider(jwkFunction);
        // @formatter:off
        OAuth2AuthorizedClientProvider providers =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .authorizationCode()
                .clientCredentials(builder -> clientCredentialsBuilderConsumer(jwkFunction, builder))
                .password(builder -> passwordBuilderConsumer(jwkFunction, builder))
                .provider(refreshTokenProvider)
                .provider(jwtBearerProvider)
                .provider(azureDelegatedProvider)
                .build();
        // @formatter:on
        manager.setAuthorizedClientProvider(providers);
        return manager;
    }

    private void passwordBuilderConsumer(Function<ClientRegistration, JWK> jwkFunction,
                           OAuth2AuthorizedClientProviderBuilder.PasswordGrantBuilder builder) {
        DefaultPasswordTokenResponseClient client = new DefaultPasswordTokenResponseClient();
        OAuth2PasswordGrantRequestEntityConverter converter = new OAuth2PasswordGrantRequestEntityConverter();
        converter.addParametersConverter(new AadJwtClientAuthenticationParametersConverter<>(jwkFunction));
        client.setRequestEntityConverter(converter);
        builder.accessTokenResponseClient(client);
    }

    private void clientCredentialsBuilderConsumer(Function<ClientRegistration, JWK> jwkFunction,
                                                  OAuth2AuthorizedClientProviderBuilder.ClientCredentialsGrantBuilder builder) {
        DefaultClientCredentialsTokenResponseClient client = new DefaultClientCredentialsTokenResponseClient();
        OAuth2ClientCredentialsGrantRequestEntityConverter converter =
            new OAuth2ClientCredentialsGrantRequestEntityConverter();
        converter.addParametersConverter(new AadJwtClientAuthenticationParametersConverter<>(jwkFunction));
        client.setRequestEntityConverter(converter);
        builder.accessTokenResponseClient(client);
    }

    private JwtBearerOAuth2AuthorizedClientProvider getJwtBearerProvider(Function<ClientRegistration, JWK> jwkFunction) {
        JwtBearerOAuth2AuthorizedClientProvider jwtBearerProvider = new JwtBearerOAuth2AuthorizedClientProvider();
        DefaultJwtBearerTokenResponseClient jwtBearerClient = new DefaultJwtBearerTokenResponseClient();
        AadJwtBearerGrantRequestEntityConverter jwtBearerConverter = new AadJwtBearerGrantRequestEntityConverter();
        jwtBearerConverter.addParametersConverter(new AadJwtClientAuthenticationParametersConverter<>(jwkFunction));
        jwtBearerClient.setRequestEntityConverter(jwtBearerConverter);
        jwtBearerProvider.setAccessTokenResponseClient(jwtBearerClient);
        return jwtBearerProvider;
    }

    private RefreshTokenOAuth2AuthorizedClientProvider getRefreshTokenProvider(Function<ClientRegistration, JWK> jwkFunction) {
        RefreshTokenOAuth2AuthorizedClientProvider refreshTokenProvider =
            new RefreshTokenOAuth2AuthorizedClientProvider();
        DefaultRefreshTokenTokenResponseClient refreshTokenTokenResponseClient =
            new DefaultRefreshTokenTokenResponseClient();
        OAuth2RefreshTokenGrantRequestEntityConverter refreshTokenConverter =
            new OAuth2RefreshTokenGrantRequestEntityConverter();
        refreshTokenConverter.addParametersConverter(new AadJwtClientAuthenticationParametersConverter<>(jwkFunction));
        refreshTokenTokenResponseClient.setRequestEntityConverter(refreshTokenConverter);
        refreshTokenProvider.setAccessTokenResponseClient(refreshTokenTokenResponseClient);
        return refreshTokenProvider;
    }
}
