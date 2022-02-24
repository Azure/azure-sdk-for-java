// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c;

import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2COAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2CPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2CConditions;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2CProperties;
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
 * and import {@link AadB2COAuth2ClientConfiguration} class for AAD B2C OAuth2 client support.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.b2c.enabled", havingValue = "true")
@Conditional(AadB2CConditions.UserFlowCondition.class)
@Import({ AadB2CPropertiesConfiguration.class, AadB2COAuth2ClientConfiguration.class})
public class AadB2CAutoConfiguration {

    /**
     * Declare AADB2CAuthorizationRequestResolver bean.
     * @param repository The clientRegistrationRepository,
     * @param properties The AADB2CProperties,
     * @return AADB2CAuthorizationRequestResolver bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AadB2CAuthorizationRequestResolver b2cOAuth2AuthorizationRequestResolver(
        ClientRegistrationRepository repository, AadB2CProperties properties) {
        return new AadB2CAuthorizationRequestResolver(repository, properties);
    }

    /**
     * Declare AADB2CLogoutSuccessHandler bean.
     * @param properties The AADB2CProperties
     * @return AADB2CLogoutSuccessHandler bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AadB2CLogoutSuccessHandler b2cLogoutSuccessHandler(AadB2CProperties properties) {
        return new AadB2CLogoutSuccessHandler(properties);
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
    public AadB2COidcLoginConfigurer b2cLoginConfigurer(AadB2CLogoutSuccessHandler handler,
                                                        AadB2CAuthorizationRequestResolver resolver) {
        return new AadB2COidcLoginConfigurer(handler, resolver);
    }
}
