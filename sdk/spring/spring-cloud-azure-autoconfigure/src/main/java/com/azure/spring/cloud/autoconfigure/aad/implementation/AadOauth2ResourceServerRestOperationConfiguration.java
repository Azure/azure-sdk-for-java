// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import com.azure.spring.cloud.autoconfigure.aad.AadAuthenticationFilterAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadResourceServerConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cResourceServerAutoConfiguration;
import com.nimbusds.jose.util.ResourceRetriever;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestOperations;

/**
 * Configure RestOperation bean used for accessing Azure AD and Azure AD B2C
 */
@Configuration(proxyBeanMethods = false)
public class AadOauth2ResourceServerRestOperationConfiguration {

    /**
     * Bean name of RestOperation bean. The bean is used to get key to validate access token in oAuth2 resource server.
     *
     * @see AadOauth2ResourceServerRestOperationConfiguration#aadOauth2ResourceServerRestOperation(RestTemplateBuilder)
     */
    public static final String AAD_OAUTH_2_RESOURCE_SERVER_REST_OPERATION_BEAN_NAME =
            "aadOauth2ResourceServerRestOperation";

    /**
     * Declare {@link RestOperations} bean used to get key to validate Azure AD(or Azure AD B2C) access token. It is
     * used to access endpoints like:
     * <ul>
     *     <li> <a href="https://login.microsoftonline.com/common/.well-known/openid-configuration">
     *         https://login.microsoftonline.com/common/.well-known/openid-configuration</a>
     *     <li> <a href="https://login.microsoftonline.com/common/discovery/v2.0/keys">
     *         https://login.microsoftonline.com/common/discovery/v2.0/keys</a>
     * </ul>
     * It's used in these places:
     * <ul>
     *     <li> In {@link ResourceRetriever} declared in {@link AadAuthenticationFilterAutoConfiguration}.
     *     <li> In {@link JwtDecoder} declared in {@link AadResourceServerConfiguration} and
     *     {@link AadB2cResourceServerAutoConfiguration}
     * </ul>
     *
     * @param builder The RestTemplateBuilder bean, which can be provided in {@link RestTemplateAutoConfiguration}
     *                if customer not provide this bean.
     * @return RestOperations bean
     * @see AadAuthenticationFilterAutoConfiguration
     * @see AadResourceServerConfiguration
     * @see AadB2cResourceServerAutoConfiguration
     */
    @Bean(AAD_OAUTH_2_RESOURCE_SERVER_REST_OPERATION_BEAN_NAME)
    @ConditionalOnMissingBean(name = AAD_OAUTH_2_RESOURCE_SERVER_REST_OPERATION_BEAN_NAME)
    public RestOperations aadOauth2ResourceServerRestOperation(RestTemplateBuilder builder) {
        return builder.build();
    }

}
