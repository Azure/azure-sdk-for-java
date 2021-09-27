// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * When the configuration matches the {@link AADB2CConditions.CommonCondition.WebAppMode} condition,
 * configure the necessary beans for AAD B2C authentication and authorization,
 * and import {@link AADB2COAuth2ClientConfiguration} class for AAD B2C OAuth2 client support.
 *
 * @deprecated All Azure AD B2C features supported by Spring security, please refer to https://github.com/zhichengliu12581/azure-spring-boot-samples/blob/add-samples-for-aad-b2c-with-only-spring-security/aad/aad-b2c-with-spring-security/README.adoc
 */
@Deprecated
@Configuration
@ConditionalOnResource(resources = "classpath:aadb2c.enable.config")
@Conditional({ AADB2CConditions.CommonCondition.class, AADB2CConditions.UserFlowCondition.class })
@EnableConfigurationProperties(AADB2CProperties.class)
@Import(AADB2COAuth2ClientConfiguration.class)
public class AADB2CAutoConfiguration {

    private final ClientRegistrationRepository repository;
    private final AADB2CProperties properties;

    public AADB2CAutoConfiguration(@NonNull ClientRegistrationRepository repository,
                                   @NonNull AADB2CProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public AADB2CAuthorizationRequestResolver b2cOAuth2AuthorizationRequestResolver() {
        return new AADB2CAuthorizationRequestResolver(repository, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AADB2CLogoutSuccessHandler b2cLogoutSuccessHandler() {
        return new AADB2CLogoutSuccessHandler(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AADB2COidcLoginConfigurer b2cLoginConfigurer(AADB2CLogoutSuccessHandler handler,
                                                        AADB2CAuthorizationRequestResolver resolver) {
        return new AADB2COidcLoginConfigurer(handler, resolver);
    }
}
