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
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
 * When the configuration matches the {@link AADB2CConditions.CommonCondition.WebApiMode} condition, configure the
 * necessary beans for AAD B2C resource server beans, and import {@link AADB2COAuth2ClientConfiguration} class for AAD
 * B2C OAuth2 client support.
 */
@Deprecated
@Configuration
@ConditionalOnResource(resources = "classpath:aadb2c.enable.config")
@Conditional(AADB2CConditions.CommonCondition.class)
@ConditionalOnClass(BearerTokenAuthenticationToken.class)
@EnableConfigurationProperties(AADB2CProperties.class)
@Import(AADB2COAuth2ClientConfiguration.class)
public class AADB2CResourceServerAutoConfiguration {

    private final AADB2CProperties properties;

    public AADB2CResourceServerAutoConfiguration(@NonNull AADB2CProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public AADTrustedIssuerRepository trustedIssuerRepository() {
        return new AADB2CTrustedIssuerRepository(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JWTClaimsSetAwareJWSKeySelector<SecurityContext> aadIssuerJWSKeySelector(
        AADTrustedIssuerRepository aadTrustedIssuerRepository) {
        return new AADIssuerJWSKeySelector(aadTrustedIssuerRepository, properties.getJwtConnectTimeout(),
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
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(JWTProcessor<SecurityContext> jwtProcessor,
                                 AADTrustedIssuerRepository trustedIssuerRepository) {
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        List<String> validAudiences = new ArrayList<>();
        if (StringUtils.hasText(properties.getAppIdUri())) {
            validAudiences.add(properties.getAppIdUri());
        }
        if (StringUtils.hasText(properties.getClientId())) {
            validAudiences.add(properties.getClientId());
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

