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

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestOperationConfiguration.AZURE_AD_ACCESS_TOKEN_RETRIEVER_REST_OPERATIONS_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestOperationConfiguration.AZURE_AD_ACCESS_TOKEN_VALIDATOR_REST_OPERATIONS_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AadRestOperationConfigurationTest {

    @Test
    public void testRestOperationBeanConfiguration() {
        new ApplicationContextRunner()
                .withUserConfiguration(AadRestOperationConfiguration.class, RestTemplateAutoConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasBean(AZURE_AD_ACCESS_TOKEN_RETRIEVER_REST_OPERATIONS_BEAN_NAME);
                    RestTemplate retrieverRestTemplate = (RestTemplate) context.getBean(AZURE_AD_ACCESS_TOKEN_RETRIEVER_REST_OPERATIONS_BEAN_NAME);
                    assertTrue(hasOAuth2ErrorResponseErrorHandler(retrieverRestTemplate));
                    assertTrue(hasMessageConvertersForAccessToken(retrieverRestTemplate));

                    assertThat(context).hasBean(AZURE_AD_ACCESS_TOKEN_VALIDATOR_REST_OPERATIONS_BEAN_NAME);
                    RestTemplate validatorRestTemplate = (RestTemplate) context.getBean(AZURE_AD_ACCESS_TOKEN_VALIDATOR_REST_OPERATIONS_BEAN_NAME);
                    assertFalse(hasOAuth2ErrorResponseErrorHandler(validatorRestTemplate));
                    assertFalse(hasMessageConvertersForAccessToken(validatorRestTemplate));
                });
    }

    private boolean hasOAuth2ErrorResponseErrorHandler(RestTemplate restTemplate) {
        return restTemplate.getErrorHandler() instanceof OAuth2ErrorResponseErrorHandler;
    }

    private boolean hasMessageConvertersForAccessToken(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        return hasFormHttpMessageConverter(converters) && hasOAuth2AccessTokenResponseHttpMessageConverter(converters);
    }

    private boolean hasFormHttpMessageConverter(List<HttpMessageConverter<?>> converters) {
        return converters.stream()
                .anyMatch(converter -> converter instanceof FormHttpMessageConverter);
    }

    private boolean hasOAuth2AccessTokenResponseHttpMessageConverter(List<HttpMessageConverter<?>> converters) {
        return converters.stream()
                .anyMatch(converter -> converter instanceof OAuth2AccessTokenResponseHttpMessageConverter);
    }

}
