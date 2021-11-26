// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.filter;

import com.azure.spring.cloud.autoconfigure.aad.core.AADAuthorizationServerEndpoints;
import com.azure.spring.cloud.autoconfigure.aad.properties.AADAuthenticationProperties;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Active Authentication filters.
 * <p>
 * The configuration will not be activated if no {@literal spring.cloud.azure.active-directory.credential.client-id} property provided.
 * <p>
 * A stateless filter {@link AADAppRoleStatelessAuthenticationFilter} will be auto-configured by specifying {@literal
 * spring.cloud.azure.active-directory.session-stateless=true}. Otherwise, {@link AADAuthenticationFilter} will be configured.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnExpression("${spring.cloud.azure.active-directory.enabled:false}")
@ConditionalOnMissingClass({ "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken" })
@ConditionalOnProperty(prefix = AADAuthenticationFilterAutoConfiguration.PROPERTY_PREFIX, value = { "credential.client-id" })
@EnableConfigurationProperties({ AADAuthenticationProperties.class })
public class AADAuthenticationFilterAutoConfiguration {
    public static final String PROPERTY_PREFIX = "spring.cloud.azure.active-directory";
    private static final Logger LOG = LoggerFactory.getLogger(AADAuthenticationProperties.class);

    private final AADAuthenticationProperties properties;
    private final AADAuthorizationServerEndpoints endpoints;

    public AADAuthenticationFilterAutoConfiguration(AADAuthenticationProperties properties) {
        this.properties = properties;
        this.endpoints = new AADAuthorizationServerEndpoints(properties.getBaseUri(), properties.getTenantId());
    }

    /**
     * Declare AADAuthenticationFilter bean.
     *
     * @return AADAuthenticationFilter bean
     */
    @Bean
    @ConditionalOnMissingBean(AADAuthenticationFilter.class)
    @ConditionalOnExpression("${spring.cloud.azure.active-directory.session-stateless:false} == false")
    // client-id and client-secret used to: get graphApiToken -> groups
    @ConditionalOnProperty(prefix = PROPERTY_PREFIX, value = { "client-id", "client-secret" })
    public AADAuthenticationFilter azureADJwtTokenFilter() {
        LOG.info("AzureADJwtTokenFilter Constructor.");
        return new AADAuthenticationFilter(
            properties,
            endpoints,
            getJWTResourceRetriever(),
            getJWKSetCache()
        );
    }

    @Bean
    @ConditionalOnMissingBean(AADAppRoleStatelessAuthenticationFilter.class)
    @ConditionalOnExpression("${spring.cloud.azure.active-directory.session-stateless:false} == true")
    // client-id used to: userPrincipalManager.getValidator
    @ConditionalOnProperty(prefix = PROPERTY_PREFIX, value = { "client-id" })
    public AADAppRoleStatelessAuthenticationFilter azureADStatelessAuthFilter(ResourceRetriever resourceRetriever) {
        LOG.info("Creating AzureADStatelessAuthFilter bean.");
        return new AADAppRoleStatelessAuthenticationFilter(
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
    public ResourceRetriever getJWTResourceRetriever() {
        return new DefaultResourceRetriever(
            properties.getJwtConnectTimeout(),
            properties.getJwtReadTimeout(),
            properties.getJwtSizeLimit()
        );
    }

    @Bean
    @ConditionalOnMissingBean(JWKSetCache.class)
    public JWKSetCache getJWKSetCache() {
        long lifespan = properties.getJwkSetCacheLifespan();
        long refreshTime = properties.getJwkSetCacheRefreshTime();
        return new DefaultJWKSetCache(lifespan, refreshTime, TimeUnit.MILLISECONDS);
    }
}
