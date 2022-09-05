// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import com.azure.spring.cloud.autoconfigure.aad.AadAuthenticationFilterAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadResourceServerConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cResourceServerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2cOAuth2ClientConfiguration;
import com.nimbusds.jose.util.ResourceRetriever;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * Configure RestOperation bean used for accessing Azure AD and Azure AD B2C
 */
@Configuration(proxyBeanMethods = false)
public class AadRestOperationConfiguration {

    /**
     * Bean name of RestOperation bean. The bean is used to retrieve access token.
     *
     * @see AadRestOperationConfiguration#azureAdAccessTokenRetrieverRestOperations(RestTemplateBuilder)
     */
    public static final String AZURE_AD_ACCESS_TOKEN_RETRIEVER_REST_OPERATIONS_BEAN_NAME =
            "azureAdAccessTokenRetrieverRestOperations";

    /**
     * Bean name of RestOperation bean. The bean is used to get key to validate access token.
     *
     * @see AadRestOperationConfiguration#azureAdAccessTokenValidatorRestOperations(RestTemplateBuilder)
     */
    public static final String AZURE_AD_ACCESS_TOKEN_VALIDATOR_REST_OPERATIONS_BEAN_NAME =
            "azureAdAccessTokenValidatorRestOperations";


    /**
     * Declare {@link RestOperations} bean used to retrieve access token from Azure AD(or Azure AD B2C) endpoints like:
     *
     * <ul>
     *     <li> <a href="https://login.microsoftonline.com/common/oauth2/v2.0/token">
     *         https://login.microsoftonline.com/common/oauth2/v2.0/token</a>
     * </ul>
     * It's mainly used in these places:
     * <ul>
     *     <li> In kinds of {@link OAuth2AccessTokenResponseClient} bean(like
     *     {@link DefaultAuthorizationCodeTokenResponseClient}) declared in {@link AadOAuth2ClientConfiguration} and
     *     {@link AadB2cOAuth2ClientConfiguration}.
     * </ul>
     *
     * @param builder The RestTemplateBuilder bean, which can be provided in {@link RestTemplateAutoConfiguration}
     *                if customer not provide this bean.
     * @return RestOperations bean
     * @see AadOAuth2ClientConfiguration
     * @see AadB2cOAuth2ClientConfiguration
     */
    @Bean(AZURE_AD_ACCESS_TOKEN_RETRIEVER_REST_OPERATIONS_BEAN_NAME)
    @ConditionalOnMissingBean(name = AZURE_AD_ACCESS_TOKEN_RETRIEVER_REST_OPERATIONS_BEAN_NAME)
    public RestOperations azureAdAccessTokenRetrieverRestOperations(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        return restTemplate;
    }

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
     */
    @Bean(AZURE_AD_ACCESS_TOKEN_VALIDATOR_REST_OPERATIONS_BEAN_NAME)
    @ConditionalOnMissingBean(name = AZURE_AD_ACCESS_TOKEN_VALIDATOR_REST_OPERATIONS_BEAN_NAME)
    public RestOperations azureAdAccessTokenValidatorRestOperations(RestTemplateBuilder builder) {
        return builder.build();
    }

    /**
     * {@link FormHttpMessageConverter} is necessary for kinds of {@link OAuth2AccessTokenResponseClient}
     * implementations(like {@link DefaultAuthorizationCodeTokenResponseClient}). This bean will be used in
     * {@link HttpMessageConvertersAutoConfiguration}.
     *
     * @return FormHttpMessageConverter bean
     */
    @Bean
    @ConditionalOnMissingBean
    FormHttpMessageConverter formHttpMessageConverter() {
        return new FormHttpMessageConverter();
    }

    /**
     * {@link OAuth2AccessTokenResponseHttpMessageConverter} is necessary for kinds of
     * {@link OAuth2AccessTokenResponseClient} implementations(like
     * {@link DefaultAuthorizationCodeTokenResponseClient}).  This bean will be used in
     * {@link HttpMessageConvertersAutoConfiguration}.
     *
     * @return OAuth2AccessTokenResponseHttpMessageConverter bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2AccessTokenResponseHttpMessageConverter oAuth2AccessTokenResponseHttpMessageConverter() {
        return new OAuth2AccessTokenResponseHttpMessageConverter();
    }

}
