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

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadOauth2ResourceServerRestOperationConfiguration.AAD_OAUTH_2_RESOURCE_SERVER_REST_OPERATION_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.ListTestUtil.hasItemOfClass;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AadOauth2ResourceServerRestOperationConfigurationTest {

    @Test
    void testRestOperationBeanConfiguration() {
        new ApplicationContextRunner()
                .withUserConfiguration(AadOauth2ResourceServerRestOperationConfiguration.class, RestTemplateAutoConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasBean(AAD_OAUTH_2_RESOURCE_SERVER_REST_OPERATION_BEAN_NAME);

                    RestTemplate restTemplate = (RestTemplate) context.getBean(AAD_OAUTH_2_RESOURCE_SERVER_REST_OPERATION_BEAN_NAME);
                    assertFalse(restTemplate.getErrorHandler() instanceof OAuth2ErrorResponseErrorHandler);

                    List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
                    assertFalse(hasItemOfClass(converters, FormHttpMessageConverter.class));
                    assertFalse(hasItemOfClass(converters, OAuth2AccessTokenResponseHttpMessageConverter.class));
                });
    }

}
