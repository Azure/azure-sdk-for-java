// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.azure.spring.telemetry.TelemetrySender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.telemetry.TelemetryData.SERVICE_NAME;
import static com.azure.spring.telemetry.TelemetryData.getClassPackageSimpleName;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Active Authentication OAuth 2.0.
 * <p>
 * The configuration will be activated when configured:
 * 1. {@literal azure.activedirectory.client-id}
 * 2. {@literal azure.activedirectory.client-secret}
 * 3. {@literal azure.activedirectory.tenant-id}
 * client-id, client-secret, tenant-id used in ClientRegistration.
 * client-id, client-secret also used to get graphApiToken, then get groups.
 * <p>
 * A OAuth2 user service {@link AADOAuth2UserService} will be auto-configured by specifying {@literal
 * azure.activedirectory.user-group.allowed-groups} property.
 */
@Configuration
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "azure.activedirectory", value = {"client-id", "client-secret", "tenant-id"})
@PropertySource(value = "classpath:service-endpoints.properties")
@EnableConfigurationProperties({ AADAuthenticationProperties.class, ServiceEndpointsProperties.class })
public class AADOAuth2AutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADOAuth2AutoConfiguration.class);
    private final AADAuthenticationProperties aadAuthenticationProperties;
    private final ServiceEndpointsProperties serviceEndpointsProperties;

    public AADOAuth2AutoConfiguration(AADAuthenticationProperties aadAuthProperties,
                                      ServiceEndpointsProperties serviceEndpointsProperties) {
        this.aadAuthenticationProperties = aadAuthProperties;
        this.serviceEndpointsProperties = serviceEndpointsProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "azure.activedirectory.user-group", value = "allowed-groups")
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        return new AADOAuth2UserService(aadAuthenticationProperties, serviceEndpointsProperties);
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(azureClientRegistration());
    }

    private ClientRegistration azureClientRegistration() {
        String tenantId = aadAuthenticationProperties.getTenantId().trim();
        Assert.hasText(tenantId, "azure.activedirectory.tenant-id should have text.");
        Assert.doesNotContain(tenantId, " ", "azure.activedirectory.tenant-id should not contain ' '.");
        Assert.doesNotContain(tenantId, "/", "azure.activedirectory.tenant-id should not contain '/'.");

        String redirectUriTemplate = Optional.of(aadAuthenticationProperties)
                                             .map(AADAuthenticationProperties::getRedirectUriTemplate)
                                             .orElse("{baseUrl}/login/oauth2/code/{registrationId}");

        List<String> scope = aadAuthenticationProperties.getScope();
        if (!scope.toString().contains(".default")) {
            if (aadAuthenticationProperties.allowedGroupsConfigured()
                && !scope.contains("https://graph.microsoft.com/user.read")
            ) {
                scope.add("https://graph.microsoft.com/user.read");
                LOGGER.warn("scope 'https://graph.microsoft.com/user.read' has been added.");
            }
            if (!scope.contains("openid")) {
                scope.add("openid");
                LOGGER.warn("scope 'openid' has been added.");
            }
            if (!scope.contains("profile")) {
                scope.add("profile");
                LOGGER.warn("scope 'profile' has been added.");
            }
        }

        return ClientRegistration.withRegistrationId("azure")
                                 .clientId(aadAuthenticationProperties.getClientId())
                                 .clientSecret(aadAuthenticationProperties.getClientSecret())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUriTemplate(redirectUriTemplate)
                                 .scope(scope)
                                 .authorizationUri(
                                     String.format(
                                         "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize",
                                         tenantId
                                     )
                                 )
                                 .tokenUri(
                                     String.format(
                                         "https://login.microsoftonline.com/%s/oauth2/v2.0/token",
                                         tenantId
                                     )
                                 )
                                 .userInfoUri("https://graph.microsoft.com/oidc/userinfo")
                                 .userNameAttributeName(AADTokenClaim.NAME)
                                 .jwkSetUri(
                                     String.format(
                                         "https://login.microsoftonline.com/%s/discovery/v2.0/keys",
                                         tenantId
                                     )
                                 )
                                 .clientName("Azure")
                                 .build();
    }

    @PostConstruct
    private void sendTelemetry() {
        if (aadAuthenticationProperties.isAllowTelemetry()) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();
            events.put(SERVICE_NAME, getClassPackageSimpleName(AADOAuth2AutoConfiguration.class));
            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }
}
