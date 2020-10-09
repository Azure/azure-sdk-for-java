// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.microsoft.azure.telemetry.TelemetrySender;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Active Authentication OAuth 2.0.
 * <p>
 * The configuration will not be activated if no {@literal azure.activedirectory.tenant-id} property provided.
 * <p>
 * A OAuth2 user service {@link AADOAuth2UserService} will be auto-configured by specifying
 * {@literal azure.activedirectory.user-group.allowed-groups} property.
 */
@Configuration
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "azure.activedirectory", value = "tenant-id")
@PropertySource(value = "classpath:aad-oauth2-common.properties")
@PropertySource(value = "classpath:service-endpoints.properties")
@EnableConfigurationProperties({ AADAuthenticationProperties.class, ServiceEndpointsProperties.class })
public class AADOAuth2AutoConfiguration {

    private final AADAuthenticationProperties aadAuthProps;
    private final ServiceEndpointsProperties serviceEndpointsProps;

    public AADOAuth2AutoConfiguration(AADAuthenticationProperties aadAuthProperties,
                                      ServiceEndpointsProperties serviceEndpointsProps) {
        this.aadAuthProps = aadAuthProperties;
        this.serviceEndpointsProps = serviceEndpointsProps;
    }

    @Bean
    @ConditionalOnProperty(prefix = "azure.activedirectory.user-group", value = "allowed-groups")
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
