// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.AadB2cAuthorizationRequestResolver;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.AadB2cLogoutSuccessHandler;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.AadB2cOidcLoginConfigurer;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.conditions.AadB2cConditions;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.AadB2cOidcIdTokenDecoderFactory;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties.AadB2cProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createRestTemplate;

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

    AadB2cAutoConfiguration(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Bean
    @ConditionalOnMissingBean
    AadB2cAuthorizationRequestResolver b2cOAuth2AuthorizationRequestResolver(ClientRegistrationRepository repository,
                                                                             AadB2cProperties properties) {
        return new AadB2cAuthorizationRequestResolver(repository, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    AadB2cLogoutSuccessHandler b2cLogoutSuccessHandler(AadB2cProperties properties) {
        return new AadB2cLogoutSuccessHandler(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    AadB2cOidcLoginConfigurer b2cLoginConfigurer(AadB2cLogoutSuccessHandler handler,
                                                 AadB2cAuthorizationRequestResolver resolver) {
        return new AadB2cOidcLoginConfigurer(handler, resolver, null, restTemplateBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    JwtDecoderFactory<ClientRegistration> azureAdJwtDecoderFactory() {
        return new AadB2cOidcIdTokenDecoderFactory(createRestTemplate(restTemplateBuilder));
    }
}
