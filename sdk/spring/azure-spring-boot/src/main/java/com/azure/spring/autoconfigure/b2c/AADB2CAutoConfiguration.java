// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.common.JacksonHttpSessionOAuth2AuthorizedClientRepository;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.spring.telemetry.TelemetryData.SERVICE_NAME;
import static com.azure.spring.telemetry.TelemetryData.TENANT_NAME;
import static com.azure.spring.telemetry.TelemetryData.getClassPackageSimpleName;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for AAD B2C Authentication.
 * <p>
 * The configuration will not be activated if no {@literal azure.activedirectory.b2c.tenant-id, client-id,
 * client-secret, reply-url and sign-up-or-sign-in} property provided.
 * <p>
 * A client registration repository service {@link InMemoryClientRegistrationRepository} will be auto-configured by
 * specifying {@literal azure.activedirectory.b2c.oidc-enabled} property as true or ignore it.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnResource(resources = "classpath:aadb2c.enable.config")
@ConditionalOnProperty(
    prefix = AADB2CProperties.PREFIX,
    value = {
        "tenant",
        "client-id",
        "client-secret",
        "reply-url",
        AADB2CProperties.USER_FLOW_SIGN_UP_OR_SIGN_IN
    }
)
@EnableConfigurationProperties(AADB2CProperties.class)
public class AADB2CAutoConfiguration {

    private final AADB2CProperties properties;

    public AADB2CAutoConfiguration(@NonNull AADB2CProperties properties) {
        this.properties = properties;
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

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository() {
        return new JacksonHttpSessionOAuth2AuthorizedClientRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2UserService<OidcUserRequest, OidcUser>
        oAuth2UserService(ClientRegistrationRepository clientRegistrationRepository) {
        return new AADB2COAuth2UserService(clientRegistrationRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    protected OAuth2AuthorizationRequestResolver
        oAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        return new AADB2CAuthorizationRequestResolver(clientRegistrationRepository, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientRegistrationRepository clientRegistrationRepository() {
        final List<ClientRegistration> signUpOrSignInRegistrations = new ArrayList<>(1);
        final List<ClientRegistration> otherRegistrations = new ArrayList<>();

        addB2CClientRegistration(signUpOrSignInRegistrations, properties.getUserFlows().getSignUpOrSignIn());
        addB2CClientRegistration(otherRegistrations, properties.getUserFlows().getProfileEdit());
        addB2CClientRegistration(otherRegistrations, properties.getUserFlows().getPasswordReset());

        if (null != properties.getAuthorizationClients()) {
            for (String name : properties.getAuthorizationClients().keySet()) {
                if (properties.getUserFlows().getSignUpOrSignIn().equals(name)) {
                    continue;
                }
                AuthorizationClientScopesProperties authz = properties.getAuthorizationClients().get(name);
                otherRegistrations.add(createClientBuilder(name, authz));
            }
        }
        return new AADB2CClientRegistrationRepository(signUpOrSignInRegistrations, otherRegistrations);
    }

    private void addB2CClientRegistration(@NonNull List<ClientRegistration> registrations, String userFlow) {
        if (StringUtils.hasText(userFlow)) {
            registrations.add(b2cClientRegistration(userFlow));
        }
    }

    private ClientRegistration createClientBuilder(String id, AuthorizationClientScopesProperties authz) {
        String userFlow = properties.getUserFlows().getSignUpOrSignIn();
        ClientRegistration.Builder result = ClientRegistration.withRegistrationId(id);
        result.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
        result.redirectUriTemplate(properties.getReplyUrl());
        result.clientId(properties.getClientId());
        result.clientSecret(properties.getClientSecret());
        result.clientAuthenticationMethod(ClientAuthenticationMethod.POST);
        result.authorizationUri(AADB2CURL.getAuthorizationUrl(properties.getTenant()));
        result.tokenUri(AADB2CURL.getTokenUrl(properties.getTenant(), userFlow));
        result.jwkSetUri(AADB2CURL.getJwkSetUrl(properties.getTenant(), userFlow));
        result.userNameAttributeName(properties.getUserNameAttributeName());
        if (authz.getScopes().contains("openid")) {
            authz.getScopes().add("openid");
        }
        if (authz.getScopes().contains("offline_access")) {
            authz.getScopes().add("offline_access");
        }
        result.scope(authz.getScopes());
        return result.build();
    }

    private ClientRegistration b2cClientRegistration(String userFlow) {
        Assert.hasText(userFlow, "User flow should contains text.");
        return ClientRegistration.withRegistrationId(userFlow) // Use flow as registration Id.
                                 .clientId(properties.getClientId())
                                 .clientSecret(properties.getClientSecret())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUriTemplate(properties.getReplyUrl())
                                 .scope(Arrays.asList(properties.getClientId(), "openid", "offline_access"))
                                 .authorizationUri(AADB2CURL.getAuthorizationUrl(properties.getTenant()))
                                 .tokenUri(AADB2CURL.getTokenUrl(properties.getTenant(), userFlow))
                                 .jwkSetUri(AADB2CURL.getJwkSetUrl(properties.getTenant(), userFlow))
                                 .userNameAttributeName(properties.getUserNameAttributeName())
                                 .clientName(userFlow)
                                 .build();
    }

    /**
     * Automatic configuration class for oauth2 login.
     */
    @Configuration
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    @EnableWebSecurity
    @ConditionalOnResource(resources = "classpath:aadb2c.enable.config")
    public class DefaultAADB2CWebSecurityConfigurerAdapter extends AADB2CWebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
        }
    }
}
