/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import com.microsoft.azure.telemetry.TelemetrySender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.telemetry.TelemetryData.SERVICE_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.getClassPackageSimpleName;

@Configuration
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "azure.activedirectory", value = "tenant-id")
@PropertySource("classpath:/aad-oauth2-common.properties")
@PropertySource(value = "classpath:serviceEndpoints.properties")
@EnableConfigurationProperties({AADAuthenticationProperties.class, ServiceEndpointsProperties.class})
public class AADOAuth2AutoConfiguration {

    private final AADAuthenticationProperties aadAuthProps;

    private final ServiceEndpointsProperties serviceEndpointsProps;

    public AADOAuth2AutoConfiguration(AADAuthenticationProperties aadAuthProperties,
                                      ServiceEndpointsProperties serviceEndpointsProps) {
        this.aadAuthProps = aadAuthProperties;
        this.serviceEndpointsProps = serviceEndpointsProps;
    }

    @Bean
    @ConditionalOnProperty(prefix = "azure.activedirectory", value = "active-directory-groups")
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        return new AADOAuth2UserService(aadAuthProps, serviceEndpointsProps);
    }

    @PostConstruct
    private void sendTelemetry() {
        if (aadAuthProps.isAllowTelemetry()) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();

            events.put(SERVICE_NAME, getClassPackageSimpleName(AADOAuth2AutoConfiguration.class));

            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }
}
