// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AADAuthorizationGrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
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
@Deprecated
@Configuration
@ConditionalOnResource(resources = "classpath:aadb2c.enable.config")
@Conditional({ AADB2CConditions.CommonCondition.class, AADB2CConditions.ClientRegistrationCondition.class })
@EnableConfigurationProperties(AADB2CProperties.class)
@ConditionalOnClass({ OAuth2LoginAuthenticationFilter.class })
public class AADB2COAuth2ClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADB2COAuth2ClientConfiguration.class);
    private final AADB2CProperties properties;

    public AADB2COAuth2ClientConfiguration(@NonNull AADB2CProperties properties) {
        this.properties = properties;
    }

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
        return new AADB2CClientRegistrationRepository(properties.getLoginFlow(), clientRegistrations);
    }

    /**
     * Build user flow client registration.
     * @param client user flow properties
     * @return ClientRegistration
     */
    private ClientRegistration buildUserFlowClientRegistration(Map.Entry<String, String> client) {
        return ClientRegistration.withRegistrationId(client.getValue()) // Use flow as registration Id.
                                 .clientName(client.getKey())
                                 .clientId(properties.getClientId())
                                 .clientSecret(properties.getClientSecret())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUri(properties.getReplyUrl())
                                 .scope(properties.getClientId(), "openid", "offline_access")
                                 .authorizationUri(AADB2CURL.getAuthorizationUrl(properties.getBaseUri()))
                                 .tokenUri(AADB2CURL.getTokenUrl(properties.getBaseUri(), client.getValue()))
                                 .jwkSetUri(AADB2CURL.getJwkSetUrl(properties.getBaseUri(), client.getValue()))
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
                                                                .map(AADAuthorizationGrantType::getValue)
                                                                .map(AuthorizationGrantType::new)
                                                                .orElse(null);
        if (!AuthorizationGrantType.CLIENT_CREDENTIALS.equals(authGrantType)) {
            LOGGER.warn("The authorization type of the {} client registration is not supported.", client.getKey());
        }
        return ClientRegistration.withRegistrationId(client.getKey())
                                 .clientName(client.getKey())
                                 .clientId(properties.getClientId())
                                 .clientSecret(properties.getClientSecret())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                 .authorizationGrantType(authGrantType)
                                 .scope(client.getValue().getScopes())
                                 .tokenUri(AADB2CURL.getAADTokenUrl(properties.getTenantId()))
                                 .jwkSetUri(AADB2CURL.getAADJwkSetUrl(properties.getTenantId()))
                                 .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clients,
                                                         OAuth2AuthorizedClientRepository authorizedClients) {
        return new DefaultOAuth2AuthorizedClientManager(clients, authorizedClients);
    }
}
