// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.configuration.AADPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.filter.AADAppRoleStatelessAuthenticationFilter;
import com.azure.spring.cloud.autoconfigure.aad.filter.AADAuthenticationFilter;
import com.azure.spring.cloud.autoconfigure.aad.filter.UserPrincipalManager;
import com.azure.spring.cloud.autoconfigure.aad.properties.AADAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AADAuthorizationServerEndpoints;
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
 * A stateless filter {@link AADAppRoleStatelessAuthenticationFilter} will be auto-configured by specifying {@literal
 * spring.cloud.azure.active-directory.session-stateless=true}. Otherwise, {@link AADAuthenticationFilter} will be configured.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnExpression("${spring.cloud.azure.active-directory.enabled:false}")
@ConditionalOnMissingClass({ "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken" })
@Import(AADPropertiesConfiguration.class)
public class AADAuthenticationFilterAutoConfiguration {
    /**
     * The property prefix
     */
    public static final String PROPERTY_PREFIX = "spring.cloud.azure.active-directory";

    private static final Logger LOG = LoggerFactory.getLogger(AADAuthenticationProperties.class);

    private final AADAuthenticationProperties properties;
    private final AADAuthorizationServerEndpoints endpoints;

    /**
     * Creates a new instance of {@link AADAuthenticationFilterAutoConfiguration}.
     *
     * @param properties the AAD authentication properties
     */
    public AADAuthenticationFilterAutoConfiguration(AADAuthenticationProperties properties) {
        this.properties = properties;
        this.endpoints = new AADAuthorizationServerEndpoints(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
            properties.getProfile().getTenantId());
    }

    /**
     * Declare AADAuthenticationFilter bean.
     *
     * @return AADAuthenticationFilter bean
     */
    @Bean
    @ConditionalOnMissingBean(AADAuthenticationFilter.class)
    @ConditionalOnExpression("${spring.cloud.azure.active-directory.session-stateless:false} == false")
    public AADAuthenticationFilter azureADJwtTokenFilter() {
        LOG.info("AzureADJwtTokenFilter Constructor.");
        return new AADAuthenticationFilter(
            properties,
            endpoints,
            getJWTResourceRetriever(),
            getJWKSetCache()
        );
    }

    /**
     * Declare AADAppRoleStatelessAuthenticationFilter bean.
     *
     * @param resourceRetriever the resource retriever
     * @return AADAppRoleStatelessAuthenticationFilter bean
     */
    @Bean
    @ConditionalOnMissingBean(AADAppRoleStatelessAuthenticationFilter.class)
    @ConditionalOnExpression("${spring.cloud.azure.active-directory.session-stateless:false} == true")
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

    /**
     * Declare JWT ResourceRetriever bean.
     *
     * @return JWT ResourceRetriever bean
     */
    @Bean
    @ConditionalOnMissingBean(ResourceRetriever.class)
    public ResourceRetriever getJWTResourceRetriever() {
        return new DefaultResourceRetriever(
            properties.getJwtConnectTimeout(),
            properties.getJwtReadTimeout(),
            properties.getJwtSizeLimit()
        );
    }

    /**
     * Declare JWTSetCache bean.
     *
     * @return JWTSetCache bean
     */
    @Bean
    @ConditionalOnMissingBean(JWKSetCache.class)
    public JWKSetCache getJWKSetCache() {
        long lifespan = properties.getJwkSetCacheLifespan();
        long refreshTime = properties.getJwkSetCacheRefreshTime();
        return new DefaultJWKSetCache(lifespan, refreshTime, TimeUnit.MILLISECONDS);
    }
}
