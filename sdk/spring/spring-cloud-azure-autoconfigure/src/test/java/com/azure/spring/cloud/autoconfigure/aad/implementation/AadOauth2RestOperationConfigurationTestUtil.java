// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class AadOauth2RestOperationConfigurationTestUtil {


    static boolean hasOAuth2ErrorResponseErrorHandler(RestTemplate restTemplate) {
        return restTemplate.getErrorHandler() instanceof OAuth2ErrorResponseErrorHandler;
    }

    static boolean hasMessageConvertersForAccessToken(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        return hasFormHttpMessageConverter(converters) && hasOAuth2AccessTokenResponseHttpMessageConverter(converters);
    }

    static boolean hasFormHttpMessageConverter(List<HttpMessageConverter<?>> converters) {
        return converters.stream()
                .anyMatch(converter -> converter instanceof FormHttpMessageConverter);
    }

    static boolean hasOAuth2AccessTokenResponseHttpMessageConverter(List<HttpMessageConverter<?>> converters) {
        return converters.stream()
                .anyMatch(converter -> converter instanceof OAuth2AccessTokenResponseHttpMessageConverter);
    }

}
