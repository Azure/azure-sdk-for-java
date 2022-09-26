// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c;

import com.azure.spring.cloud.autoconfigure.aad.AadTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadIssuerJwsKeySelector;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.RestOperationsResourceRetriever;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.validator.AadJwtIssuerValidator;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2cOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2cPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
 * Configure necessary beans for Azure AD B2C resource server beans, and import {@link AadB2cOAuth2ClientConfiguration} class for Azure AD
 * B2C OAuth2 client support.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.b2c.enabled", havingValue = "true")
@ConditionalOnClass(BearerTokenAuthenticationToken.class)
@Import({ AadB2cPropertiesConfiguration.class, AadB2cOAuth2ClientConfiguration.class})
public class AadB2cResourceServerAutoConfiguration {

    private final AadB2cProperties properties;
    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Creates a new instance of {@link AadB2cResourceServerAutoConfiguration}.
     *
     * @param properties the Azure AD B2C properties
     * @param restTemplateBuilder the restTemplateBuilder
     */
    public AadB2cResourceServerAutoConfiguration(AadB2cProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    /**
     * Declare AADTrustedIssuerRepository bean.
     *
     * @return AADTrustedIssuerRepository bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AadTrustedIssuerRepository trustedIssuerRepository() {
        return new AadB2cTrustedIssuerRepository(properties);
    }

    /**
     * Declare JWT ResourceRetriever bean.
     *
     * @return JWT ResourceRetriever bean
     */
    @Bean
    @ConditionalOnMissingBean(ResourceRetriever.class)
    public ResourceRetriever jwtResourceRetriever() {
        return new RestOperationsResourceRetriever(restTemplateBuilder);
    }

    /**
     * Declare JWTClaimsSetAwareJWSKeySelector bean.
     *
     * @param aadTrustedIssuerRepository the AAD trusted issuer repository
     * @param resourceRetriever the resource retriever
     * @return JWTClaimsSetAwareJWSKeySelector bean
     */
    @Bean
    @ConditionalOnMissingBean
    public JWTClaimsSetAwareJWSKeySelector<SecurityContext> aadIssuerJwsKeySelector(
        AadTrustedIssuerRepository aadTrustedIssuerRepository,
        ResourceRetriever resourceRetriever) {
        return new AadIssuerJwsKeySelector(
            restTemplateBuilder,
            aadTrustedIssuerRepository,
            resourceRetriever);
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
                                 AadTrustedIssuerRepository trustedIssuerRepository) {
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
            validators.add(new JwtClaimValidator<List<String>>(AadJwtClaimNames.AUD, validAudiences::containsAll));
        }
        validators.add(new AadJwtIssuerValidator(trustedIssuerRepository));
        validators.add(new JwtTimestampValidator());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }
}

