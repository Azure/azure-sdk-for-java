// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.telemetry.TelemetrySender;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.spring.telemetry.TelemetryData.SERVICE_NAME;
import static com.azure.spring.telemetry.TelemetryData.TENANT_NAME;
import static com.azure.spring.telemetry.TelemetryData.getClassPackageSimpleName;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for AAD B2C Authentication.
 * <p>
 * The configuration will not be activated if no {@literal azure.activedirectory.b2c.client-id,
 * client-secret and sign-in-user-flow} property provided.
 * </p>
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnResource(resources = "classpath:aadb2c.enable.config")
@ConditionalOnProperty(
    prefix = AADB2CProperties.PREFIX,
    value = {
        "client-id",
        "client-secret"
    }
)
@EnableConfigurationProperties(AADB2CProperties.class)
public class AADB2CAutoConfiguration {

    private final ClientRegistrationRepository repository;

    private final AADB2CProperties properties;

    public AADB2CAutoConfiguration(@NonNull ClientRegistrationRepository repository,
                                   @NonNull AADB2CProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public AADB2CAuthorizationRequestResolver b2cOAuth2AuthorizationRequestResolver() {
        return new AADB2CAuthorizationRequestResolver(repository, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AADB2CLogoutSuccessHandler b2cLogoutSuccessHandler() {
        return new AADB2CLogoutSuccessHandler(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AADB2COidcLoginConfigurer b2cLoginConfigurer(AADB2CLogoutSuccessHandler handler,
                                                        AADB2CAuthorizationRequestResolver resolver) {
        return new AADB2COidcLoginConfigurer(handler, resolver);
    }

    @PostConstruct
    private void sendTelemetry() {
        if (properties.isAllowTelemetry()) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();
            events.put(SERVICE_NAME, getClassPackageSimpleName(AADB2CAutoConfiguration.class));
            events.put(TENANT_NAME, properties.getTenant());
            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }

    /**
     * Automatic configuration class of AADB2COidc
     */
    @Configuration
    @ConditionalOnResource(resources = "classpath:aadb2c.enable.config")
    public static class AADB2COidcAutoConfiguration {

        private final AADB2CProperties properties;

        public AADB2COidcAutoConfiguration(@NonNull AADB2CProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean
        public ClientRegistrationRepository clientRegistrationRepository() {
            final List<ClientRegistration> signUpOrSignInRegistrations = new ArrayList<>(1);
            final List<ClientRegistration> otherRegistrations = new ArrayList<>();
            signUpOrSignInRegistrations.add(b2cClientRegistration(properties.getLoginFlow(),
                properties.getUserFlows().get(properties.getLoginFlow())));
            for (String clientName : properties.getUserFlows().keySet()) {
                if (!clientName.equals(properties.getLoginFlow())) {
                    otherRegistrations.add(b2cClientRegistration(clientName, properties.getUserFlows().get(clientName)));
                }
            }
            return new AADB2CClientRegistrationRepository(signUpOrSignInRegistrations, otherRegistrations);
        }

        private ClientRegistration b2cClientRegistration(String clientName, String userFlow) {
            Assert.hasText(userFlow, "User flow should contains text.");

            return ClientRegistration.withRegistrationId(userFlow) // Use flow as registration Id.
                                     .clientName(clientName)
                                     .clientId(properties.getClientId())
                                     .clientSecret(properties.getClientSecret())
                                     .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                     .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                     .redirectUriTemplate(properties.getReplyUrl())
                                     .scope(properties.getClientId(), "openid")
                                     .authorizationUri(AADB2CURL.getAuthorizationUrl(properties.getBaseUri()))
                                     .tokenUri(AADB2CURL.getTokenUrl(properties.getBaseUri(), userFlow))
                                     .jwkSetUri(AADB2CURL.getJwkSetUrl(properties.getBaseUri(), userFlow))
                                     .userNameAttributeName(properties.getUserNameAttributeName())
                                     .clientName(userFlow)
                                     .build();
        }
    }
}
