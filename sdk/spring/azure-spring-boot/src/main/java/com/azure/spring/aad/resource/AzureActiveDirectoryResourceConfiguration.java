// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.resource;


import com.azure.spring.aad.implementation.AzureActiveDirectoryProperties;
import com.azure.spring.aad.implementation.IdentityEndpoints;
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
@EnableConfigurationProperties({AzureActiveDirectoryProperties.class})
@ConditionalOnClass(BearerTokenAuthenticationToken.class)
public class AzureActiveDirectoryResourceConfiguration {

    @Autowired
    private AzureActiveDirectoryProperties azureActiveDirectoryProperties;

    /**
     * Use JwkKeySetUri to create JwtDecoder
     *
     * @return JwtDecoder bean
     */
    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoderByJwkKeySetUri() {
        if (StringUtils.isEmpty(azureActiveDirectoryProperties.getTenantId())) {
            azureActiveDirectoryProperties.setTenantId("common");
        }
        IdentityEndpoints identityEndpoints = new IdentityEndpoints(azureActiveDirectoryProperties.getUri());
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(identityEndpoints.jwkSetEndpoint(azureActiveDirectoryProperties.getTenantId())).build();
        List<OAuth2TokenValidator<Jwt>> validators = createDefaultValidator();
        nimbusJwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return nimbusJwtDecoder;
    }

    public List<OAuth2TokenValidator<Jwt>> createDefaultValidator() {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        if (!StringUtils.isEmpty(azureActiveDirectoryProperties.getAppIdUri())) {
            List<String> validAudiences = new ArrayList<>();
            validAudiences.add(azureActiveDirectoryProperties.getAppIdUri());
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
            http
                .authorizeRequests((requests) -> requests.anyRequest().authenticated())
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(new AzureJwtBearerTokenAuthenticationConverter());
        }
    }
}

