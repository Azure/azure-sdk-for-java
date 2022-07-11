// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.configuration.AadPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.filter.AadAppRoleStatelessAuthenticationFilter;
import com.azure.spring.cloud.autoconfigure.aad.filter.AadAuthenticationFilter;
import com.azure.spring.cloud.autoconfigure.aad.filter.UserPrincipalManager;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationServerEndpoints;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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
@ConditionalOnMissingClass({ "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken" })
@Import(AadPropertiesConfiguration.class)
public class AadAuthenticationFilterAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadAuthenticationProperties.class);

    private final AadAuthenticationProperties properties;
    private final AadAuthorizationServerEndpoints endpoints;

    /**
     * Creates a new instance of {@link AadAuthenticationFilterAutoConfiguration}.
     *
     * @param properties the AAD authentication properties
     */
    public AadAuthenticationFilterAutoConfiguration(AadAuthenticationProperties properties) {
        this.properties = properties;
        this.endpoints = new AadAuthorizationServerEndpoints(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
            properties.getProfile().getTenantId());
    }

    /**
     * Declare AADAuthenticationFilter bean.
     *
     * @param resourceRetriever the resource retriever
     * @param jwkSetCache the JWK set cache
     * @return AADAuthenticationFilter bean
     */
    @Bean
    @ConditionalOnMissingBean(AadAuthenticationFilter.class)
    @ConditionalOnExpression("${spring.cloud.azure.active-directory.session-stateless:false} == false")
    public AadAuthenticationFilter aadAuthenticationFilter(ResourceRetriever resourceRetriever, JWKSetCache jwkSetCache) {
        LOGGER.info("AadAuthenticationFilter Constructor.");
        return new AadAuthenticationFilter(
            properties,
            endpoints,
            resourceRetriever,
            jwkSetCache
        );
    }

    /**
     * Declare AADAppRoleStatelessAuthenticationFilter bean.
     *
     * @param resourceRetriever the resource retriever
     * @return AADAppRoleStatelessAuthenticationFilter bean
     */
    @Bean
    @ConditionalOnMissingBean(AadAppRoleStatelessAuthenticationFilter.class)
    @ConditionalOnExpression("${spring.cloud.azure.active-directory.session-stateless:false} == true")
    public AadAppRoleStatelessAuthenticationFilter aadStatelessAuthFilter(ResourceRetriever resourceRetriever) {
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

    /**
     * Declare JWT ResourceRetriever bean.
     *
     * @return JWT ResourceRetriever bean
     */
    @Bean
    @ConditionalOnMissingBean(ResourceRetriever.class)
    public ResourceRetriever jwtResourceRetriever() {
        return new DefaultResourceRetriever(
            (int) properties.getJwtConnectTimeout().toMillis(),
            (int) properties.getJwtReadTimeout().toMillis(),
            properties.getJwtSizeLimit()
        );
    }

    /**
     * Declare JWKSetCache bean.
     *
     * @return JWKSetCache bean
     */
    @Bean
    @ConditionalOnMissingBean(JWKSetCache.class)
    public JWKSetCache jwkSetCache() {
        long lifespan = properties.getJwkSetCacheLifespan().toMillis();
        long refreshTime = properties.getJwkSetCacheRefreshTime().toMillis();
        return new DefaultJWKSetCache(lifespan, refreshTime, TimeUnit.MILLISECONDS);
    }
}
