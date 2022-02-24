// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.configuration;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationGrantType;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2CClientRegistrationRepository;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2CConditions;
import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2CUrl;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2CProperties;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AuthorizationClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Configuration for AAD B2C OAuth2 client support, when depends on the Spring OAuth2 Client module.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.b2c.enabled", havingValue = "true")
@Conditional(AadB2CConditions.ClientRegistrationCondition.class)
@Import(AadB2CPropertiesConfiguration.class)
@ConditionalOnClass({ OAuth2LoginAuthenticationFilter.class })
public class AadB2COAuth2ClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadB2COAuth2ClientConfiguration.class);
    private final AadB2CProperties properties;

    /**
     * Creates a new instance of {@link AadB2COAuth2ClientConfiguration}.
     *
     * @param properties the AAD B2C properties
     */
    public AadB2COAuth2ClientConfiguration(AadB2CProperties properties) {
        this.properties = properties;
    }

    /**
     * Declare ClientRegistrationRepository bean.
     * @return ClientRegistrationRepository bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientRegistrationRepository clientRegistrationRepository() {
        final List<ClientRegistration> clientRegistrations = new ArrayList<>();
        clientRegistrations.addAll(properties.getUserFlows()
                  .entrySet()
                  .stream()
                  .map(this::buildUserFlowClientRegistration)
                  .collect(Collectors.toList()));
        clientRegistrations.addAll(properties.getAuthorizationClients()
                                             .entrySet()
                                             .stream()
                                             .map(this::buildClientRegistration)
                                             .collect(Collectors.toList()));
        return new AadB2CClientRegistrationRepository(properties.getLoginFlow(), clientRegistrations);
    }

    /**
     * Build user flow client registration.
     * @param client user flow properties
     * @return ClientRegistration
     */
    private ClientRegistration buildUserFlowClientRegistration(Map.Entry<String, String> client) {
        return ClientRegistration.withRegistrationId(client.getValue()) // Use flow as registration Id.
                                 .clientName(client.getKey())
                                 .clientId(properties.getCredential().getClientId())
                                 .clientSecret(properties.getCredential().getClientSecret())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUri(properties.getReplyUrl())
                                 .scope(properties.getCredential().getClientId(), "openid", "offline_access")
                                 .authorizationUri(AadB2CUrl.getAuthorizationUrl(properties.getBaseUri()))
                                 .tokenUri(AadB2CUrl.getTokenUrl(properties.getBaseUri(), client.getValue()))
                                 .jwkSetUri(AadB2CUrl.getJwkSetUrl(properties.getBaseUri(), client.getValue()))
                                 .userNameAttributeName(properties.getUserNameAttributeName())
                                 .build();
    }

    /**
     * Create client registration, only support OAuth2 client credentials.
     *
     * @param client each client properties
     * @return ClientRegistration
     */
    private ClientRegistration buildClientRegistration(Map.Entry<String, AuthorizationClientProperties> client) {
        AuthorizationGrantType authGrantType = Optional.ofNullable(client.getValue().getAuthorizationGrantType())
                                                                .map(AadAuthorizationGrantType::getValue)
                                                                .map(AuthorizationGrantType::new)
                                                                .orElse(null);
        if (!AuthorizationGrantType.CLIENT_CREDENTIALS.equals(authGrantType)) {
            LOGGER.warn("The authorization type of the {} client registration is not supported.", client.getKey());
        }
        return ClientRegistration.withRegistrationId(client.getKey())
                                 .clientName(client.getKey())
                                 .clientId(properties.getCredential().getClientId())
                                 .clientSecret(properties.getCredential().getClientSecret())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                 .authorizationGrantType(authGrantType)
                                 .scope(client.getValue().getScopes())
                                 .tokenUri(AadB2CUrl.getAADTokenUrl(properties.getProfile().getTenantId()))
                                 .jwkSetUri(AadB2CUrl.getAADJwkSetUrl(properties.getProfile().getTenantId()))
                                 .build();
    }

    /**
     * Declare OAuth2AuthorizedClientManager bean.
     *
     * @param clients the client registration repository
     * @param authorizedClients the OAuth2 authorized client repository
     * @return OAuth2AuthorizedClientManager bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clients,
                                                         OAuth2AuthorizedClientRepository authorizedClients) {
        return new DefaultOAuth2AuthorizedClientManager(clients, authorizedClients);
    }
}
