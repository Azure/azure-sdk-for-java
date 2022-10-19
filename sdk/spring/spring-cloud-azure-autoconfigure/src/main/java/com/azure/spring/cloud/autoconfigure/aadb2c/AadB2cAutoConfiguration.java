// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c;

import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2cOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2cPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cConditions;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

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

    /**
     * Declare AADB2CAuthorizationRequestResolver bean.
     * @param repository The clientRegistrationRepository,
     * @param properties The AADB2CProperties,
     * @return AADB2CAuthorizationRequestResolver bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AadB2cAuthorizationRequestResolver b2cOAuth2AuthorizationRequestResolver(
        ClientRegistrationRepository repository, AadB2cProperties properties) {
        return new AadB2cAuthorizationRequestResolver(repository, properties);
    }

    /**
     * Declare AADB2CLogoutSuccessHandler bean.
     * @param properties The AADB2CProperties
     * @return AADB2CLogoutSuccessHandler bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AadB2cLogoutSuccessHandler b2cLogoutSuccessHandler(AadB2cProperties properties) {
        return new AadB2cLogoutSuccessHandler(properties);
    }

    /**
     * Declare AADB2COidcLoginConfigurer bean.
     *
     * @param handler the AAD B2C logout success handler
     * @param resolver the AAD B2C authorization request resolver
     * @return AADB2COidcLoginConfigurer bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AadB2cOidcLoginConfigurer b2cLoginConfigurer(AadB2cLogoutSuccessHandler handler,
                                                        AadB2cAuthorizationRequestResolver resolver) {
        return new AadB2cOidcLoginConfigurer(handler, resolver);
    }
}
