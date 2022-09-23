// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import com.azure.spring.cloud.autoconfigure.aad.configuration.AadOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.configuration.AadB2cOAuth2ClientConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Configure RestOperation bean used for accessing Azure AD (or Azure AD B2C) and Graph
 */
@Configuration(proxyBeanMethods = false)
public class AadRestOperationsConfiguration {

    /**
     * Name of RestOperation bean. The bean is used to access Azure AD (or Azure AD B2C) and Graph.
     */
    public static final String AAD_REST_OPERATIONS_BEAN_NAME = "aadRestOperations";

    /**
     * Name of RestOperation bean. The bean is used to get access token from Azure AD (or Azure AD B2C).
     */
    public static final String AAD_OAUTH2_RESPONSE_ERROR_HANDLED_REST_OPERATIONS_BEAN_NAME =
            "aadOAuth2ResponseErrorHandledRestOperations";

    /**
     * Declare {@link RestOperations} bean used to retrieve access token from Azure AD(or Azure AD B2C) endpoints like:
     * <a href="https://login.microsoftonline.com/common/oauth2/v2.0/token">
     * https://login.microsoftonline.com/common/oauth2/v2.0/token</a>
     * It's mainly used in kinds of {@link OAuth2AccessTokenResponseClient} bean(like
     * {@link DefaultAuthorizationCodeTokenResponseClient}). {@link OAuth2AccessTokenResponseClient}'s
     * {@link RestOperations} requires 1 specific handler and 2 specific converters:
     * <ul>
     *      <li> {@link OAuth2ErrorResponseErrorHandler} </li>
     *      <li> {@link FormHttpMessageConverter} </li>
     *      <li> {@link OAuth2AccessTokenResponseHttpMessageConverter} </li>
     * </ul>
     *
     * @param builder The RestTemplateBuilder bean, which can be provided in {@link RestTemplateAutoConfiguration}
     *                if customer not provide this bean.
     * @return RestOperations bean
     * @see AadOAuth2ClientConfiguration
     * @see AadB2cOAuth2ClientConfiguration
     */
    @Bean(AAD_OAUTH2_RESPONSE_ERROR_HANDLED_REST_OPERATIONS_BEAN_NAME)
    @ConditionalOnMissingBean(name = AAD_OAUTH2_RESPONSE_ERROR_HANDLED_REST_OPERATIONS_BEAN_NAME)
    public RestOperations aadOauth2AccessTokenResponseClientRestOperationsBean(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        if (!containsElementOfType(converters, FormHttpMessageConverter.class)) {
            converters.add(new FormHttpMessageConverter());
        }
        if (!containsElementOfType(converters, OAuth2AccessTokenResponseHttpMessageConverter.class)) {
            converters.add(new OAuth2AccessTokenResponseHttpMessageConverter());
        }
        return restTemplate;
    }

    boolean containsElementOfType(List<?> list, Class<?> clazz) {
        return list.stream().anyMatch(item -> item.getClass().equals(clazz));
    }

    /**
     * Declare {@link RestOperations} bean used to access Azure AD(or Azure AD B2C) and Graph.
     */
    @Bean(AAD_REST_OPERATIONS_BEAN_NAME)
    @ConditionalOnMissingBean(name = AAD_REST_OPERATIONS_BEAN_NAME)
    public RestOperations aadRestOperations(RestTemplateBuilder builder) {
        return builder.build();
    }

}
