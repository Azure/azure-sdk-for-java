// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.AadAppRoleStatelessAuthenticationFilter;
import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.AadAuthenticationFilter;
import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.UserPrincipalManager;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jose.RestOperationsResourceRetriever;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.properties.AadAuthorizationServerEndpoints;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.ResourceRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Active Authentication filters.
 * <p>
 * The configuration will not be activated if no {@literal spring.cloud.azure.active-directory.credential.client-id} property provided.
 * <p>
 * A stateless filter {@link AadAppRoleStatelessAuthenticationFilter} will be auto-configured by specifying {@literal
 * spring.cloud.azure.active-directory.session-stateless=true}. Otherwise, {@link AadAuthenticationFilter} will be configured.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnExpression("${spring.cloud.azure.active-directory.enabled:false}")
@ConditionalOnMissingClass({ "org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken" })
@Import(AadPropertiesConfiguration.class)
public class AadAuthenticationFilterAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadAuthenticationProperties.class);
    private static final String MSG_MALFORMED_AD_KEY_DISCOVERY_URI = "Failed to parse active directory key discovery uri.";

    private final AadAuthenticationProperties properties;
    private final AadAuthorizationServerEndpoints endpoints;
    private final RestTemplateBuilder restTemplateBuilder;

    AadAuthenticationFilterAutoConfiguration(AadAuthenticationProperties properties,
                                             RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplateBuilder = restTemplateBuilder;
        this.endpoints = new AadAuthorizationServerEndpoints(
                properties.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
                properties.getProfile().getTenantId());
    }

    @Bean
    @ConditionalOnMissingBean(AadAuthenticationFilter.class)
    @ConditionalOnExpression("${spring.cloud.azure.active-directory.session-stateless:false} == false")
    AadAuthenticationFilter aadAuthenticationFilter(JWKSetSource<SecurityContext> jwkSetSource) {
        LOGGER.info("AadAuthenticationFilter Constructor.");
        return new AadAuthenticationFilter(
            properties,
            endpoints,
            jwkSetSource,
            restTemplateBuilder
        );
    }

    @Bean
    @ConditionalOnMissingBean(AadAppRoleStatelessAuthenticationFilter.class)
    @ConditionalOnExpression("${spring.cloud.azure.active-directory.session-stateless:false} == true")
    AadAppRoleStatelessAuthenticationFilter aadStatelessAuthFilter(JWKSetSource<SecurityContext> jwkSetSource) {
        LOGGER.info("Creating AadStatelessAuthFilter bean.");
        return new AadAppRoleStatelessAuthenticationFilter(
            new UserPrincipalManager(
                properties,
                true,
                jwkSetSource
            )
        );
    }

    @Bean
    @ConditionalOnMissingBean(ResourceRetriever.class)
    ResourceRetriever jwtResourceRetriever() {
        return new RestOperationsResourceRetriever(restTemplateBuilder);
    }

    @Bean
    @ConditionalOnBean(ResourceRetriever.class)
    @ConditionalOnMissingBean(JWKSetSource.class)
    @SuppressWarnings("deprecation")
    JWKSetSource<SecurityContext> jwkSetSource(ResourceRetriever resourceRetriever) {
        long timeToLive = properties.getJwkSetSourceTimeToLive().toMillis();
        long cacheRefreshTimeout = properties.getJwkSetSourceCacheRefreshTimeout().toMillis();
        try {
            URL jwkSetEndpoint = new URL(endpoints.getJwkSetEndpoint());
            JWKSetSource<SecurityContext> source = new URLBasedJWKSetSource<>(jwkSetEndpoint, resourceRetriever);
            return new CachingJWKSetSource<>(source, timeToLive, cacheRefreshTimeout, null);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(MSG_MALFORMED_AD_KEY_DISCOVERY_URI, e);
        }
    }
}
