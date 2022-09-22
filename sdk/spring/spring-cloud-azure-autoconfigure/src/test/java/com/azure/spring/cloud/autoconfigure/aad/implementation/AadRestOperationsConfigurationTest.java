// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadOauth2RestOperationConfigurationTestUtil.hasItemOfClass;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestOperationsConfiguration.AAD_OAUTH2_ACCESS_TOKEN_RESPONSE_CLIENT_REST_OPERATIONS_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestOperationsConfiguration.AAD_REST_OPERATIONS_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AadRestOperationsConfigurationTest {

    @Test
    void testAadAccessTokenResponseClientRestOperationBeanConfiguration() {
        new ApplicationContextRunner()
                .withUserConfiguration(
                        AadRestOperationsConfiguration.class,
                        RestTemplateAutoConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasBean(AAD_OAUTH2_ACCESS_TOKEN_RESPONSE_CLIENT_REST_OPERATIONS_BEAN_NAME);

                    RestTemplate restTemplate = (RestTemplate) context.getBean(AAD_OAUTH2_ACCESS_TOKEN_RESPONSE_CLIENT_REST_OPERATIONS_BEAN_NAME);
                    assertTrue(restTemplate.getErrorHandler() instanceof OAuth2ErrorResponseErrorHandler);

                    List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
                    assertTrue(hasItemOfClass(converters, FormHttpMessageConverter.class));
                    assertTrue(hasItemOfClass(converters, OAuth2AccessTokenResponseHttpMessageConverter.class));
                });
    }

    @Test
    void testRestOperationProxyConfiguration() {
        new ApplicationContextRunner()
                .withUserConfiguration(
                        AadRestOperationsConfiguration.class,
                        RestTemplateAutoConfiguration.class,
                        AadOauth2RestOperationConfigurationTestUtil.RestTemplateProxyCustomizerConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasBean(AAD_OAUTH2_ACCESS_TOKEN_RESPONSE_CLIENT_REST_OPERATIONS_BEAN_NAME);

                    RestTemplate restTemplate = (RestTemplate) context.getBean(AAD_OAUTH2_ACCESS_TOKEN_RESPONSE_CLIENT_REST_OPERATIONS_BEAN_NAME);
                    assertSame(restTemplate.getRequestFactory(), AadOauth2RestOperationConfigurationTestUtil.RestTemplateProxyCustomizerConfiguration.FACTORY);
                });
    }

    @Test
    void testAadRestOperationBeanConfiguration() {
        new ApplicationContextRunner()
                .withUserConfiguration(
                        AadRestOperationsConfiguration.class,
                        RestTemplateAutoConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasBean(AAD_REST_OPERATIONS_BEAN_NAME);

                    RestTemplate restTemplate = (RestTemplate) context.getBean(AAD_REST_OPERATIONS_BEAN_NAME);
                    assertFalse(restTemplate.getErrorHandler() instanceof OAuth2ErrorResponseErrorHandler);

                    List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
                    assertFalse(hasItemOfClass(converters, FormHttpMessageConverter.class));
                    assertFalse(hasItemOfClass(converters, OAuth2AccessTokenResponseHttpMessageConverter.class));
                });
    }

}
