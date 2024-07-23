// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.conditions.WebApplicationCondition;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadOAuth2UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadWebApplicationHttpSecurityConfigurer.aadWebApplication;

@Configuration(proxyBeanMethods = false)
@Conditional(WebApplicationCondition.class)
class AadWebApplicationConfiguration {

    private final RestTemplateBuilder restTemplateBuilder;

    AadWebApplicationConfiguration(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Bean
    @ConditionalOnMissingBean
    OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(AadAuthenticationProperties properties) {
        return new AadOAuth2UserService(properties, restTemplateBuilder);
    }

    @EnableWebSecurity
    @EnableMethodSecurity
    @ConditionalOnDefaultWebSecurity
    @ConditionalOnExpression("!'${spring.cloud.azure.active-directory.application-type}'.equalsIgnoreCase('web_application_and_resource_server')")
    static class DefaultAadWebSecurityConfiguration {

        @SuppressWarnings({"deprecation", "removal"})
        @Bean
        SecurityFilterChain defaultAadWebApplicationFilterChain(HttpSecurity http) throws Exception {
            http
                .apply(aadWebApplication())
                    .and()
                .authorizeHttpRequests()
                    .requestMatchers("/login").permitAll()
                    .anyRequest().authenticated();
            return http.build();
        }
    }
}
