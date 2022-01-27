// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c;

import com.azure.spring.cloud.autoconfigure.aad.AADTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AADIssuerJWSKeySelector;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.validator.AADJwtAudienceValidator;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.validator.AADJwtIssuerValidator;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AADB2COAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AADB2CPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AADB2CProperties;
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
import org.springframework.context.annotation.Import;
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
 * Configure necessary beans for AAD B2C resource server beans, and import {@link AADB2COAuth2ClientConfiguration} class for AAD
 * B2C OAuth2 client support.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.b2c.enabled", havingValue = "true")
@ConditionalOnClass(BearerTokenAuthenticationToken.class)
@Import({ AADB2CPropertiesConfiguration.class, AADB2COAuth2ClientConfiguration.class})
public class AADB2CResourceServerAutoConfiguration {

    private final AADB2CProperties properties;

    /**
     * Creates a new instance of {@link AADB2CResourceServerAutoConfiguration}.
     *
     * @param properties the AAD B2C properties
     */
    public AADB2CResourceServerAutoConfiguration(AADB2CProperties properties) {
        this.properties = properties;
    }

    /**
     * Declare AADTrustedIssuerRepository bean.
     *
     * @return AADTrustedIssuerRepository bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AADTrustedIssuerRepository trustedIssuerRepository() {
        return new AADB2CTrustedIssuerRepository(properties);
    }

    /**
     * Declare JWTClaimsSetAwareJWSKeySelector bean.
     *
     * @param aadTrustedIssuerRepository the AAD trusted issuer repository
     * @return JWTClaimsSetAwareJWSKeySelector bean
     */
    @Bean
    @ConditionalOnMissingBean
    public JWTClaimsSetAwareJWSKeySelector<SecurityContext> aadIssuerJWSKeySelector(
        AADTrustedIssuerRepository aadTrustedIssuerRepository) {
        return new AADIssuerJWSKeySelector(aadTrustedIssuerRepository, properties.getJwtConnectTimeout(),
            properties.getJwtReadTimeout(), properties.getJwtSizeLimit());
    }

    /**
     * Declare JWTProcessor bean.
     *
     * @param keySelector the JWT claims set aware JWS key selector
     * @return JWTProcessor bean
     */
    @Bean
    @ConditionalOnMissingBean
    public JWTProcessor<SecurityContext> jwtProcessor(
        JWTClaimsSetAwareJWSKeySelector<SecurityContext> keySelector) {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(keySelector);
        return jwtProcessor;
    }

    /**
     * Declare JwtDecoder bean.
     *
     * @param jwtProcessor the JWT processor
     * @param trustedIssuerRepository the AAD trusted issuer repository
     * @return JwtDecoder bean
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(JWTProcessor<SecurityContext> jwtProcessor,
                                 AADTrustedIssuerRepository trustedIssuerRepository) {
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        List<String> validAudiences = new ArrayList<>();
        if (StringUtils.hasText(properties.getAppIdUri())) {
            validAudiences.add(properties.getAppIdUri());
        }
        if (StringUtils.hasText(properties.getCredential().getClientId())) {
            validAudiences.add(properties.getCredential().getClientId());
        }
        if (!validAudiences.isEmpty()) {
            validators.add(new AADJwtAudienceValidator(validAudiences));
        }
        validators.add(new AADJwtIssuerValidator(trustedIssuerRepository));
        validators.add(new JwtTimestampValidator());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }
}

