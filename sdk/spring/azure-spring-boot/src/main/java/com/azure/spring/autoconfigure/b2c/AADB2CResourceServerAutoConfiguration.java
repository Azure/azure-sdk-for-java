// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AADIssuerJWSKeySelector;
import com.azure.spring.aad.AADTrustedIssuerRepository;
import com.azure.spring.aad.webapi.validator.AADJwtAudienceValidator;
import com.azure.spring.aad.webapi.validator.AADJwtIssuerValidator;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
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
 * Automatic configuration class of AADB2CResourceServer
 */
@Configuration
@ConditionalOnProperty(prefix = AADB2CProperties.PREFIX, value = { "tenant-id" })
@ConditionalOnClass(BearerTokenAuthenticationToken.class)
public class AADB2CResourceServerAutoConfiguration {

    private final AADB2CProperties properties;

    public AADB2CResourceServerAutoConfiguration(@NonNull AADB2CProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public AADTrustedIssuerRepository trustedIssuerRepository() {
        return new AADTrustedIssuerRepository(properties.getTenantId());
    }

    @Bean
    @ConditionalOnMissingBean
    public JWTClaimsSetAwareJWSKeySelector<SecurityContext> aadIssuerJWSKeySelector(
        AADTrustedIssuerRepository trustedIssuerRepository) {
        return new AADIssuerJWSKeySelector(trustedIssuerRepository, properties.getJwtConnectTimeout(),
            properties.getJwtReadTimeout(), properties.getJwtSizeLimit());
    }

    @Bean
    @ConditionalOnMissingBean
    public JWTProcessor<SecurityContext> jwtProcessor(
        JWTClaimsSetAwareJWSKeySelector<SecurityContext> keySelector) {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(keySelector);
        return jwtProcessor;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWTProcessor<SecurityContext> jwtProcessor) {
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        List<String> validAudiences = new ArrayList<>();
        if (!StringUtils.isEmpty(properties.getAppIdUri())) {
            validAudiences.add(properties.getAppIdUri());
        }
        if (!StringUtils.isEmpty(properties.getClientId())) {
            validAudiences.add(properties.getClientId());
        }
        if (!validAudiences.isEmpty()) {
            validators.add(new AADJwtAudienceValidator(validAudiences));
        }
        validators.add(new AADJwtIssuerValidator());
        validators.add(new JwtTimestampValidator());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }
}

