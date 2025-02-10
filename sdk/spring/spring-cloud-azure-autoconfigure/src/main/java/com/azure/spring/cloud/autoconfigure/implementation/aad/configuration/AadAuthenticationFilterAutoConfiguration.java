// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.AadAppRoleStatelessAuthenticationFilter;
import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.AadAuthenticationFilter;
import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.UserPrincipalManager;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jose.RestOperationsResourceRetriever;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.properties.AadAuthorizationServerEndpoints;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.util.ResourceRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

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

    @SuppressWarnings("deprecation")
    @Bean
    @ConditionalOnMissingBean(AadAuthenticationFilter.class)
    @ConditionalOnExpression("${spring.cloud.azure.active-directory.session-stateless:false} == false")
    AadAuthenticationFilter aadAuthenticationFilter(ResourceRetriever resourceRetriever, JWKSetCache jwkSetCache) {
        LOGGER.info("AadAuthenticationFilter Constructor.");
        return new AadAuthenticationFilter(
            properties,
            endpoints,
            resourceRetriever,
            jwkSetCache,
            restTemplateBuilder
        );
    }

    @Bean
    @ConditionalOnMissingBean(AadAppRoleStatelessAuthenticationFilter.class)
    @ConditionalOnExpression("${spring.cloud.azure.active-directory.session-stateless:false} == true")
    AadAppRoleStatelessAuthenticationFilter aadStatelessAuthFilter(ResourceRetriever resourceRetriever) {
        LOGGER.info("Creating AadStatelessAuthFilter bean.");
        return new AadAppRoleStatelessAuthenticationFilter(
            new UserPrincipalManager(
                endpoints,
                properties,
                resourceRetriever,
                true
            )
        );
    }

    @Bean
    @ConditionalOnMissingBean(ResourceRetriever.class)
    ResourceRetriever jwtResourceRetriever() {
        return new RestOperationsResourceRetriever(restTemplateBuilder);
    }

    @SuppressWarnings("deprecation")
    @Bean
    @ConditionalOnMissingBean(JWKSetCache.class)
    JWKSetCache jwkSetCache() {
        long lifespan = properties.getJwkSetCacheLifespan().toMillis();
        long refreshTime = properties.getJwkSetCacheRefreshTime().toMillis();
        return new DefaultJWKSetCache(lifespan, refreshTime, TimeUnit.MILLISECONDS);
    }
}
