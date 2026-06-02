// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;


import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.conditions.ResourceServerCondition;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadResourceServerProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt.AadJwtIssuerValidator;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt.AadTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.properties.AadAuthorizationServerEndpoints;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.security.autoconfigure.web.servlet.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadResourceServerHttpSecurityConfigurer.aadResourceServer;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createRestTemplate;

@Configuration(proxyBeanMethods = false)
@Conditional(ResourceServerCondition.class)
class AadResourceServerConfiguration {

    private final RestTemplateBuilder restTemplateBuilder;

    AadResourceServerConfiguration(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    JwtDecoder jwtDecoder(AadAuthenticationProperties aadAuthenticationProperties) {
        String tenantId = getTrimmedTenantId(aadAuthenticationProperties);
        AadAuthorizationServerEndpoints identityEndpoints = new AadAuthorizationServerEndpoints(
            aadAuthenticationProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint(), tenantId);
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(identityEndpoints.getJwkSetEndpoint())
            .restOperations(createRestTemplate(restTemplateBuilder
                .connectTimeout(aadAuthenticationProperties.getJwtConnectTimeout())
                .readTimeout(aadAuthenticationProperties.getJwtReadTimeout())))
            .build();
        List<OAuth2TokenValidator<Jwt>> validators = createDefaultValidator(aadAuthenticationProperties);
        nimbusJwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return nimbusJwtDecoder;
    }

    List<OAuth2TokenValidator<Jwt>> createDefaultValidator(AadAuthenticationProperties aadAuthenticationProperties) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        List<String> validAudiences = new ArrayList<>();
        String tenantId = getTrimmedTenantId(aadAuthenticationProperties);
        validateTenantId(tenantId);
        if (StringUtils.hasText(aadAuthenticationProperties.getAppIdUri())) {
            validAudiences.add(aadAuthenticationProperties.getAppIdUri());
        }
        if (StringUtils.hasText(aadAuthenticationProperties.getCredential().getClientId())) {
            validAudiences.add(aadAuthenticationProperties.getCredential().getClientId());
        }
        if (!validAudiences.isEmpty()) {
            validators.add(new JwtClaimValidator<List<String>>(AadJwtClaimNames.AUD,
                audiences -> audiences != null
                    && !audiences.isEmpty()
                    && audiences.stream().anyMatch(validAudiences::contains)));
        }
        validators.add(new JwtClaimValidator<String>(AadJwtClaimNames.TID, tenantId::equals));
        validators.add(new AadJwtIssuerValidator(new AadTrustedIssuerRepository(tenantId)));
        validators.add(new JwtTimestampValidator());
        return validators;
    }

    private static String getTrimmedTenantId(AadAuthenticationProperties aadAuthenticationProperties) {
        String tenantId = aadAuthenticationProperties.getProfile().getTenantId();
        return tenantId != null ? tenantId.trim().toLowerCase(Locale.ROOT) : null;
    }

    private static void validateTenantId(String tenantId) {
        if (!StringUtils.hasText(tenantId)
            || "common".equalsIgnoreCase(tenantId)
            || "organizations".equalsIgnoreCase(tenantId)
            || "consumers".equalsIgnoreCase(tenantId)) {
            throw new IllegalArgumentException(
                "For resource server, "
                    + "'spring.cloud.azure.active-directory.profile.tenant-id' "
                    + "cannot be null, empty, or set to 'common', "
                    + "'organizations', or 'consumers'. "
                    + "These values are not supported for resource server token "
                    + "validation because a specific tenant ID is required to "
                    + "validate the token 'tid' claim and issuer against a "
                    + "single Azure AD tenant. "
                    + "Please configure an explicit tenant ID for your "
                    + "organization's tenant.");
        }
    }

    @EnableWebSecurity
    @EnableMethodSecurity
    @ConditionalOnDefaultWebSecurity
    @ConditionalOnExpression("!'${spring.cloud.azure.active-directory.application-type}'.equalsIgnoreCase('web_application_and_resource_server')")
    static class DefaultAadResourceServerConfiguration {

        @Bean
        @ConditionalOnBean(AadResourceServerProperties.class)
        SecurityFilterChain defaultAadResourceServerFilterChain(HttpSecurity http) throws Exception {
            http.with(aadResourceServer(), Customizer.withDefaults());
            return http.build();
        }
    }
}

