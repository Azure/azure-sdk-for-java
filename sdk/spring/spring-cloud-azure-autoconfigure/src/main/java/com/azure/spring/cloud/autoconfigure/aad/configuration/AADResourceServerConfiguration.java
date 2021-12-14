// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.configuration;


import com.azure.spring.cloud.autoconfigure.aad.AADResourceServerWebSecurityConfigurerAdapter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.ResourceServerCondition;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.validator.AADJwtAudienceValidator;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.validator.AADJwtIssuerValidator;
import com.azure.spring.cloud.autoconfigure.aad.properties.AADAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AADAuthorizationServerEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AADResourceServerConfiguration {

    @Autowired
    private AADAuthenticationProperties aadAuthenticationProperties;

    /**
     * Use JwkKeySetUri to create JwtDecoder
     *
     * @return Get the jwtDecoder instance.
     */
    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder() {
        AADAuthorizationServerEndpoints identityEndpoints = new AADAuthorizationServerEndpoints(
            aadAuthenticationProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint(), aadAuthenticationProperties.getProfile().getTenantId());
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(identityEndpoints.jwkSetEndpoint()).build();
        List<OAuth2TokenValidator<Jwt>> validators = createDefaultValidator();
        nimbusJwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return nimbusJwtDecoder;
    }

    /**
     * Creates a default validator.
     *
     * @return a default validator
     */
    public List<OAuth2TokenValidator<Jwt>> createDefaultValidator() {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        List<String> validAudiences = new ArrayList<>();
        if (StringUtils.hasText(aadAuthenticationProperties.getAppIdUri())) {
            validAudiences.add(aadAuthenticationProperties.getAppIdUri());
        }
        if (StringUtils.hasText(aadAuthenticationProperties.getCredential().getClientId())) {
            validAudiences.add(aadAuthenticationProperties.getCredential().getClientId());
        }
        if (!validAudiences.isEmpty()) {
            validators.add(new AADJwtAudienceValidator(validAudiences));
        }
        validators.add(new AADJwtIssuerValidator());
        validators.add(new JwtTimestampValidator());
        return validators;
    }

    /**
     * Default configuration class for using AAD authentication and authorization. User can write another configuration
     * bean to override it.
     */
    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    @ConditionalOnExpression("!'${spring.cloud.azure.active-directory.application-type}'.equalsIgnoreCase('web_application_and_resource_server')")
    public static class DefaultAADResourceServerWebSecurityConfigurerAdapter extends
        AADResourceServerWebSecurityConfigurerAdapter {

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

