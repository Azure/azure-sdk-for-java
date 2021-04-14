// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AADAuthorizationGrantType;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Configure the B2C necessary beans used for client registration.
 */
@Configuration
@ConditionalOnResource(resources = "classpath:aadb2c.enable.config")
@EnableConfigurationProperties(AADB2CProperties.class)
@ConditionalOnClass({ OAuth2LoginAuthenticationFilter.class })
public class AADB2CConfiguration {

    private final AADB2CProperties properties;

    public AADB2CConfiguration(@NonNull AADB2CProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional({ ClientCondition.class})
    public ClientRegistrationRepository clientRegistrationRepository() {
        long invalidCount = properties.getAuthorizationClients()
                                      .entrySet()
                                      .stream()
                                      .filter(entry -> entry.getValue().getAuthorizationGrantType() != null
                                          && !AADAuthorizationGrantType.CLIENT_CREDENTIALS.equals(entry.getValue().getAuthorizationGrantType()))
                                      .count();
        if (invalidCount > 0) {
            throw new IllegalStateException("Web Application does not support non 'client_credentials' grant type.");
        }

        final List<ClientRegistration> clientRegistrations = new ArrayList<>();
        clientRegistrations.addAll(properties.getUserFlows()
                  .entrySet()
                  .stream()
                  .map(this::b2cClientRegistration)
                  .collect(Collectors.toList()));
        clientRegistrations.addAll(properties.getAuthorizationClients()
                                             .entrySet()
                                             .stream()
                                             .map(this::b2cClientCredentialRegistration)
                                             .collect(Collectors.toList()));
        return new AADB2CClientRegistrationRepository(properties.getLoginFlow(), clientRegistrations);
    }

    @NotNull
    private List<ClientRegistration> getClientRegistrationList(Map<String, String> userFlows) {
        return userFlows.entrySet()
                        .stream()
                        .map(this::b2cClientRegistration)
                        .collect(Collectors.toList());
    }

    private ClientRegistration b2cClientRegistration(Map.Entry<String, String> client) {
        return ClientRegistration.withRegistrationId(client.getValue()) // Use flow as registration Id.
                                 .clientName(client.getKey())
                                 .clientId(properties.getClientId())
                                 .clientSecret(properties.getClientSecret())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUri(properties.getReplyUrl())
                                 .scope(properties.getClientId(), "openid")
                                 .authorizationUri(AADB2CURL.getAuthorizationUrl(properties.getBaseUri()))
                                 .tokenUri(AADB2CURL.getTokenUrl(properties.getBaseUri(), client.getValue()))
                                 .jwkSetUri(AADB2CURL.getJwkSetUrl(properties.getBaseUri(), client.getValue()))
                                 .userNameAttributeName(properties.getUserNameAttributeName())
                                 .build();
    }

    /**
     * Create client credential registration by default.
     *
     * @param client
     * @return ClientRegistration
     */
    private ClientRegistration b2cClientCredentialRegistration(Map.Entry<String, AuthorizationClientProperties> client) {
        AuthorizationGrantType authorizationGrantType = Optional.ofNullable(client.getValue().getAuthorizationGrantType())
                                                                .map(AADAuthorizationGrantType::getValue)
                                                                .map(AuthorizationGrantType::new)
                                                                .orElse(AuthorizationGrantType.CLIENT_CREDENTIALS);


        return ClientRegistration.withRegistrationId(client.getKey())
                                 .clientName(client.getKey())
                                 .clientId(properties.getClientId())
                                 .clientSecret(properties.getClientSecret())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                                 .authorizationGrantType(authorizationGrantType)
                                 .scope(client.getValue().getScopes())
                                 .tokenUri(AADB2CURL.getAADTokenUrl(properties.getTenantId()))
                                 .jwkSetUri(AADB2CURL.getAADJwkSetUrl(properties.getTenantId()))
                                 .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional({ ClientCondition.class})
    public OAuth2AuthorizedClientManager authorizeClientManager(ClientRegistrationRepository clients,
                                                         OAuth2AuthorizedClientRepository authorizedClients) {
        DefaultOAuth2AuthorizedClientManager manager =
            new DefaultOAuth2AuthorizedClientManager(clients, authorizedClients);

        // @formatter:off
        OAuth2AuthorizedClientProvider authorizedClientProviders = OAuth2AuthorizedClientProviderBuilder.builder()
                                                                    .authorizationCode()
                                                                    .refreshToken()
                                                                    .clientCredentials()
                                                                    .password()
                                                                    .build();
        // @formatter:on
        manager.setAuthorizedClientProvider(authorizedClientProviders);
        return manager;
    }

    private static final class ClientCondition extends AnyNestedCondition {
        ClientCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(
            prefix = AADB2CProperties.PREFIX,
            value = {
                "client-id",
                "client-secret"
            }
        )
        @ConditionalOnMissingClass({"org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken"})
        static class WebAppMode {
            WebAppMode() {
            }
        }

        @ConditionalOnProperty(prefix = AADB2CProperties.PREFIX, value = { "tenant-id" })
        @ConditionalOnClass({ BearerTokenAuthenticationToken.class })
        static class WebApiMode {
            WebApiMode() {
            }
        }
    }
}
