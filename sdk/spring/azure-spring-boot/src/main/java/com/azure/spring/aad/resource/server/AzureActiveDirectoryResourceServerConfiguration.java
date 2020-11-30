// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.resource.server;


import com.azure.spring.aad.implementation.AuthorizationServerEndpoints;
import com.azure.spring.aad.resource.server.validator.AzureJwtAudienceValidator;
import com.azure.spring.aad.resource.server.validator.AzureJwtIssuerValidator;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

/**
 * <p>
 * The configuration will not be activated if no {@link BearerTokenAuthenticationToken} class provided.
 * <p>
 * By default, creating a JwtDecoder through JwkKeySetUri will be auto-configured.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({AADAuthenticationProperties.class})
@ConditionalOnClass(BearerTokenAuthenticationToken.class)
public class AzureActiveDirectoryResourceServerConfiguration {

    @Autowired
    private AADAuthenticationProperties aadAuthenticationProperties;

    /**
     * Use JwkKeySetUri to create JwtDecoder
     *
     * @return JwtDecoder bean
     */
    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoderByJwkKeySetUri() {
        if (StringUtils.isEmpty(aadAuthenticationProperties.getTenantId())) {
            aadAuthenticationProperties.setTenantId("common");
        }
        AuthorizationServerEndpoints identityEndpoints = new AuthorizationServerEndpoints(
            aadAuthenticationProperties.getAuthorizationServerUri());
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(identityEndpoints.jwkSetEndpoint(aadAuthenticationProperties.getTenantId())).build();
        List<OAuth2TokenValidator<Jwt>> validators = createDefaultValidator();
        nimbusJwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return nimbusJwtDecoder;
    }

    public List<OAuth2TokenValidator<Jwt>> createDefaultValidator() {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        if (!StringUtils.isEmpty(aadAuthenticationProperties.getAppIdUri())) {
            List<String> validAudiences = new ArrayList<>();
            validAudiences.add(aadAuthenticationProperties.getAppIdUri());
            validators.add(new AzureJwtAudienceValidator(validAudiences));
        }
        validators.add(new AzureJwtIssuerValidator());
        validators.add(new JwtTimestampValidator());
        return validators;
    }

    /**
     * Default configuration class for using AAD authentication and authorization. User can write another configuration
     * bean to override it.
     */
    @Configuration
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    @EnableWebSecurity
    public static class DefaultAzureOAuth2ResourceServerWebSecurityConfigurerAdapter extends
        WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests((requests) -> requests.anyRequest().authenticated())
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(new AzureJwtBearerTokenAuthenticationConverter());
        }
    }
}

