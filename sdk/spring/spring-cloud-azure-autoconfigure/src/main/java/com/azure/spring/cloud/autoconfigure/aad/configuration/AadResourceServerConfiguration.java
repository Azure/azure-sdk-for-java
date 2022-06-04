// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.configuration;


import com.azure.spring.cloud.autoconfigure.aad.AadResourceServerWebSecurityConfigurerAdapter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.ResourceServerCondition;
import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.validator.AadJwtIssuerValidator;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationServerEndpoints;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * The configuration will not be activated if no {@link BearerTokenAuthenticationToken} class provided.
 * </p>
 * By default, creating a JwtDecoder through JwkKeySetUri will be auto-configured.
 */
@Configuration(proxyBeanMethods = false)
@Conditional(ResourceServerCondition.class)
public class AadResourceServerConfiguration {

    /**
     * Use JwkKeySetUri to create JwtDecoder
     *
     * @param aadAuthenticationProperties the AAD properties
     * @return Get the jwtDecoder instance.
     */
    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder(AadAuthenticationProperties aadAuthenticationProperties) {
        AadAuthorizationServerEndpoints identityEndpoints = new AadAuthorizationServerEndpoints(
            aadAuthenticationProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint(), aadAuthenticationProperties.getProfile().getTenantId());
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(identityEndpoints.getJwkSetEndpoint()).build();
        List<OAuth2TokenValidator<Jwt>> validators = createDefaultValidator(aadAuthenticationProperties);
        nimbusJwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return nimbusJwtDecoder;
    }

    /**
     * Creates a default validator.
     * @param aadAuthenticationProperties the AAD properties
     * @return a default validator
     */
    public List<OAuth2TokenValidator<Jwt>> createDefaultValidator(AadAuthenticationProperties aadAuthenticationProperties) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        List<String> validAudiences = new ArrayList<>();
        if (StringUtils.hasText(aadAuthenticationProperties.getAppIdUri())) {
            validAudiences.add(aadAuthenticationProperties.getAppIdUri());
        }
        if (StringUtils.hasText(aadAuthenticationProperties.getCredential().getClientId())) {
            validAudiences.add(aadAuthenticationProperties.getCredential().getClientId());
        }
        if (!validAudiences.isEmpty()) {
            validators.add(new JwtClaimValidator<List<String>>(AadJwtClaimNames.AUD, validAudiences::containsAll));
        }
        validators.add(new AadJwtIssuerValidator());
        validators.add(new JwtTimestampValidator());
        return validators;
    }

    /**
     * Default configuration class for using AAD authentication and authorization. User can write another configuration
     * bean to override it.
     */
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    @ConditionalOnExpression("!'${spring.cloud.azure.active-directory.application-type}'.equalsIgnoreCase('web_application_and_resource_server')")
    public static class DefaultAadResourceServerWebSecurityConfigurerAdapter extends
        AadResourceServerWebSecurityConfigurerAdapter {

        /**
         * configure
         *
         * @param http the {@link HttpSecurity} to use
         * @throws Exception Configuration failed
         *
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
        }
    }
}

