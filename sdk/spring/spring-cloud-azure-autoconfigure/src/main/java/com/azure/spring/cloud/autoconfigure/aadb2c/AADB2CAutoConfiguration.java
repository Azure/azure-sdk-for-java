// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c;

import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AADB2COAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AADB2CPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AADB2CConditions;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AADB2CProperties;
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
 * and import {@link AADB2COAuth2ClientConfiguration} class for AAD B2C OAuth2 client support.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.b2c.enabled", havingValue = "true")
@Conditional(AADB2CConditions.UserFlowCondition.class)
@Import({ AADB2CPropertiesConfiguration.class, AADB2COAuth2ClientConfiguration.class})
public class AADB2CAutoConfiguration {

    /**
     * Declare AADB2CAuthorizationRequestResolver bean.
     * @param repository The clientRegistrationRepository,
     * @param properties The AADB2CProperties,
     * @return AADB2CAuthorizationRequestResolver bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AADB2CAuthorizationRequestResolver b2cOAuth2AuthorizationRequestResolver(
        ClientRegistrationRepository repository, AADB2CProperties properties) {
        return new AADB2CAuthorizationRequestResolver(repository, properties);
    }

    /**
     * Declare AADB2CLogoutSuccessHandler bean.
     * @param properties The AADB2CProperties
     * @return AADB2CLogoutSuccessHandler bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AADB2CLogoutSuccessHandler b2cLogoutSuccessHandler(AADB2CProperties properties) {
        return new AADB2CLogoutSuccessHandler(properties);
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
    public AADB2COidcLoginConfigurer b2cLoginConfigurer(AADB2CLogoutSuccessHandler handler,
                                                        AADB2CAuthorizationRequestResolver resolver) {
        return new AADB2COidcLoginConfigurer(handler, resolver);
    }
}
