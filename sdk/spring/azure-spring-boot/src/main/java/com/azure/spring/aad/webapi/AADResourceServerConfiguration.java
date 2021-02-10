// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;


import com.azure.spring.aad.AADAuthorizationServerEndpoints;
import com.azure.spring.aad.webapi.validator.AADJwtAudienceValidator;
import com.azure.spring.aad.webapi.validator.AADJwtIssuerValidator;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@EnableConfigurationProperties({ AADAuthenticationProperties.class })
@ConditionalOnClass(BearerTokenAuthenticationToken.class)
public class AADResourceServerConfiguration {

    @Autowired
    private AADAuthenticationProperties aadAuthenticationProperties;

    /**
     * Use JwkKeySetUri to create JwtDecoder
     *
     * @return JwtDecoder bean
     */
    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder() {
        AADAuthorizationServerEndpoints identityEndpoints = new AADAuthorizationServerEndpoints(
            aadAuthenticationProperties.getBaseUri(), aadAuthenticationProperties.getTenantId());
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(identityEndpoints.jwkSetEndpoint()).build();
        List<OAuth2TokenValidator<Jwt>> validators = createDefaultValidator();
        nimbusJwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return nimbusJwtDecoder;
    }

    public List<OAuth2TokenValidator<Jwt>> createDefaultValidator() {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        List<String> validAudiences = new ArrayList<>();
        if (!StringUtils.isEmpty(aadAuthenticationProperties.getAppIdUri())) {
            validAudiences.add(aadAuthenticationProperties.getAppIdUri());
        }
        if (!StringUtils.isEmpty(aadAuthenticationProperties.getClientId())) {
            validAudiences.add(aadAuthenticationProperties.getClientId());
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
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    public static class DefaultAADResourceServerWebSecurityConfigurerAdapter extends
        AADResourceServerWebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
        }
    }
}

