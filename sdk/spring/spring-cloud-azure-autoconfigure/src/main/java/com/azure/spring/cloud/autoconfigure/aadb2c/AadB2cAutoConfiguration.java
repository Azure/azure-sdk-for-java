// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c;

import com.azure.spring.cloud.autoconfigure.aad.configuration.AadOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2cOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2cPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cConditions;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cOidcIdTokenDecoderFactory;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.client.RestTemplate;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestTemplateCreator.createRestTemplate;

/**
 * Configure the necessary beans for AAD B2C authentication and authorization,
 * and import {@link AadB2cOAuth2ClientConfiguration} class for AAD B2C OAuth2 client support.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.b2c.enabled", havingValue = "true")
@Conditional(AadB2cConditions.UserFlowCondition.class)
@Import({ AadB2cPropertiesConfiguration.class, AadB2cOAuth2ClientConfiguration.class})
public class AadB2cAutoConfiguration {

    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Creates a new instance of {@link AadOAuth2ClientConfiguration}.
     *
     * @param restTemplateBuilder the RestTemplateBuilder
     */
    public AadB2cAutoConfiguration(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    /**
     * Declare AadB2cAuthorizationRequestResolver bean.
     * @param repository The clientRegistrationRepository,
     * @param properties The AADB2CProperties,
     * @return AadB2cAuthorizationRequestResolver bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AadB2cAuthorizationRequestResolver b2cOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository repository, AadB2cProperties properties) {
        return new AadB2cAuthorizationRequestResolver(repository, properties);
    }

    /**
     * Declare LogoutSuccessHandler bean.
     * @return OidcClientInitiatedLogoutSuccessHandler bean
     */
    @Bean
    @ConditionalOnMissingBean
    public LogoutSuccessHandler b2cLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository,
                                                        AadB2cProperties properties) {
        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
            new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        logoutSuccessHandler.setPostLogoutRedirectUri(properties.getLogoutSuccessUrl());
        return logoutSuccessHandler;
    }

    /**
     * Declare AadB2cOidcLoginConfigurer bean.
     *
     * @param handler the logout success handler
     * @param resolver the AAD B2C authorization request resolver
     * @return AadB2cOidcLoginConfigurer bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AadB2cOidcLoginConfigurer b2cLoginConfigurer(LogoutSuccessHandler handler,
                                                        AadB2cAuthorizationRequestResolver resolver) {
        return new AadB2cOidcLoginConfigurer(handler, resolver, null, restTemplateBuilder);
    }

    /**
     * Provide {@link JwtDecoderFactory} used in {@link OAuth2LoginConfigurer#init}. The {@link JwtDecoder} created by
     * current {@link JwtDecoderFactory} will use {@link RestTemplate} created by {@link RestTemplateBuilder} bean.
     *
     * @return JwtDecoderFactory
     */
    @Bean
    @ConditionalOnMissingBean
    JwtDecoderFactory<ClientRegistration> azureAdJwtDecoderFactory() {
        return new AadB2cOidcIdTokenDecoderFactory(createRestTemplate(restTemplateBuilder));
    }
}
