// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapi.AADOBOOAuth2AuthorizedClientProvider;
import com.azure.spring.aad.webapi.validator.AADJwtAudienceValidator;
import com.azure.spring.aad.webapi.validator.AADJwtIssuerValidator;
import com.azure.spring.aad.webapp.AADOAuth2AuthorizedClientRepository;
import com.azure.spring.aad.webapp.AADOAuth2UserService;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Configure the beans needed for azure-spring-boot-starter-active-directory.
 * </p>
 */
@Configuration
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@EnableConfigurationProperties({ AADAuthenticationProperties.class })
public class AADConfiguration {

    private final AADAuthenticationProperties properties;

    public AADConfiguration(AADAuthenticationProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class, AADClientRegistrationRepository.class })
    public AADClientRegistrationRepository clientRegistrationRepository() {
        return new AADClientRegistrationRepository(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository(AADClientRegistrationRepository repo) {
        return new AADOAuth2AuthorizedClientRepository(repo);
    }

    @Bean
    OAuth2AuthorizedClientManager authorizeClientManager(ClientRegistrationRepository clients,
                                                         OAuth2AuthorizedClientRepository authorizedClients) {
        OAuth2AuthorizedClientProvider authorizedClientProviders =
            OAuth2AuthorizedClientProviderBuilder.builder()
                                                 .authorizationCode()
                                                 .refreshToken()
                                                 .clientCredentials()
                                                 .password()
                                                 .provider(new AADOBOOAuth2AuthorizedClientProvider())
                                                 .build();
        DefaultOAuth2AuthorizedClientManager manager =
            new DefaultOAuth2AuthorizedClientManager(clients, authorizedClients);
        manager.setAuthorizedClientProvider(authorizedClientProviders);
        return manager;
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(AADAuthenticationProperties properties) {
        return new AADOAuth2UserService(properties);
    }

    /**
     * Use JwkKeySetUri to create JwtDecoder
     *
     * @return JwtDecoder bean
     */
    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder() {
        AADAuthorizationServerEndpoints identityEndpoints = new AADAuthorizationServerEndpoints(
            properties.getBaseUri(), properties.getTenantId());
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(identityEndpoints.jwkSetEndpoint()).build();
        List<OAuth2TokenValidator<Jwt>> validators = createDefaultValidator();
        nimbusJwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return nimbusJwtDecoder;
    }

    public List<OAuth2TokenValidator<Jwt>> createDefaultValidator() {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        List<String> validAudiences = new ArrayList<>();
        if (StringUtils.hasText(properties.getAppIdUri())) {
            validAudiences.add(properties.getAppIdUri());
        }
        if (StringUtils.hasText(properties.getClientId())) {
            validAudiences.add(properties.getClientId());
        }
        if (!validAudiences.isEmpty()) {
            validators.add(new AADJwtAudienceValidator(validAudiences));
        }
        validators.add(new AADJwtIssuerValidator());
        validators.add(new JwtTimestampValidator());
        return validators;
    }
}
