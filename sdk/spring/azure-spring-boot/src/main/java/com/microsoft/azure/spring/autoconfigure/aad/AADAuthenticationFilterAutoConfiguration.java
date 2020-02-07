/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import static com.microsoft.azure.telemetry.TelemetryData.SERVICE_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.getClassPackageSimpleName;

import com.microsoft.azure.telemetry.TelemetrySender;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Configuration
@ConditionalOnWebApplication
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@ConditionalOnProperty(prefix = AADAuthenticationFilterAutoConfiguration.PROPERTY_PREFIX, value = {"client-id"})
@EnableConfigurationProperties({AADAuthenticationProperties.class, ServiceEndpointsProperties.class})
@PropertySource(value = "classpath:serviceEndpoints.properties")
public class AADAuthenticationFilterAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(AADAuthenticationProperties.class);

    public static final String PROPERTY_PREFIX = "azure.activedirectory";
    private static final String PROPERTY_SESSION_STATELESS = "session-stateless";

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
        return new AADAuthenticationFilter(aadAuthProps, serviceEndpointsProps, getJWTResourceRetriever());
    }

    @Bean
    @ConditionalOnMissingBean(AADAppRoleStatelessAuthenticationFilter.class)
    @ConditionalOnProperty(prefix = PROPERTY_PREFIX, value = PROPERTY_SESSION_STATELESS, havingValue = "true")
    public AADAppRoleStatelessAuthenticationFilter azureADStatelessAuthFilter(ResourceRetriever resourceRetriever) {
        LOG.info("Creating AzureADStatelessAuthFilter bean.");
        final boolean useExplicitAudienceCheck = true;
        return new AADAppRoleStatelessAuthenticationFilter(new UserPrincipalManager(serviceEndpointsProps, aadAuthProps,
            resourceRetriever, useExplicitAudienceCheck));
    }

    @Bean
    @ConditionalOnMissingBean(ResourceRetriever.class)
    public ResourceRetriever getJWTResourceRetriever() {
        return new DefaultResourceRetriever(aadAuthProps.getJwtConnectTimeout(), aadAuthProps.getJwtReadTimeout(),
                aadAuthProps.getJwtSizeLimit());
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
