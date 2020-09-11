// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.microsoft.azure.telemetry.TelemetrySender;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.telemetry.TelemetryData.SERVICE_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.getClassPackageSimpleName;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Active Authentication filters.
 * <p>
 * The configuration will not be activated if no {@literal azure.activedirectory.client-id} property provided.
 * <p>
 * A stateless filter {@link AADAppRoleStatelessAuthenticationFilter} will be auto-configured by specifying
 * {@literal azure.activedirectory.session-stateless=true}. Otherwise, {@link AADAuthenticationFilter} will be
 * configured.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@ConditionalOnProperty(prefix = AADAuthenticationFilterAutoConfiguration.PROPERTY_PREFIX, value = { "client-id" })
@EnableConfigurationProperties({ AADAuthenticationProperties.class, ServiceEndpointsProperties.class })
@PropertySource(value = "classpath:service-endpoints.properties")
public class AADAuthenticationFilterAutoConfiguration {
    public static final String PROPERTY_PREFIX = "azure.activedirectory";
    private static final Logger LOG = LoggerFactory.getLogger(AADAuthenticationProperties.class);

    private final AADAuthenticationProperties aadAuthProps;
    private final ServiceEndpointsProperties serviceEndpointsProps;

    public AADAuthenticationFilterAutoConfiguration(AADAuthenticationProperties aadAuthFilterProps,
                                                    ServiceEndpointsProperties serviceEndpointsProps) {
        this.aadAuthProps = aadAuthFilterProps;
        this.serviceEndpointsProps = serviceEndpointsProps;
    }

    /**
     * Declare AADAuthenticationFilter bean.
     *
     * @return AADAuthenticationFilter bean
     */
    @Bean
    @ConditionalOnMissingBean(AADAuthenticationFilter.class)
    @ConditionalOnProperty(prefix = PROPERTY_PREFIX, value = {"client-id", "client-secret"})
    @ConditionalOnExpression("${azure.activedirectory.session-stateless:false} == false")
    public AADAuthenticationFilter azureADJwtTokenFilter() {
        LOG.info("AzureADJwtTokenFilter Constructor.");
        return new AADAuthenticationFilter(
            aadAuthProps,
            serviceEndpointsProps,
            getJWTResourceRetriever(),
            getJWKSetCache()
        );
    }

    @Bean
    @ConditionalOnMissingBean(AADAppRoleStatelessAuthenticationFilter.class)
    @ConditionalOnExpression("${azure.activedirectory.session-stateless:false} == true")
    public AADAppRoleStatelessAuthenticationFilter azureADStatelessAuthFilter(ResourceRetriever resourceRetriever) {
        LOG.info("Creating AzureADStatelessAuthFilter bean.");
        final boolean useExplicitAudienceCheck = true;
        return new AADAppRoleStatelessAuthenticationFilter(
            new UserPrincipalManager(
                serviceEndpointsProps,
                aadAuthProps,
                resourceRetriever,
                useExplicitAudienceCheck
            )
        );
    }

    @Bean
    @ConditionalOnMissingBean(ResourceRetriever.class)
    public ResourceRetriever getJWTResourceRetriever() {
        return new DefaultResourceRetriever(
            aadAuthProps.getJwtConnectTimeout(),
            aadAuthProps.getJwtReadTimeout(),
            aadAuthProps.getJwtSizeLimit()
        );
    }

    @Bean
    @ConditionalOnMissingBean(JWKSetCache.class)
    public JWKSetCache getJWKSetCache() {
        return new DefaultJWKSetCache(aadAuthProps.getJwkSetCacheLifespan(), TimeUnit.MILLISECONDS);
    }

    @PostConstruct
    private void sendTelemetry() {
        if (aadAuthProps.isAllowTelemetry()) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();
            events.put(SERVICE_NAME, getClassPackageSimpleName(AADAuthenticationFilterAutoConfiguration.class));
            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }
}
