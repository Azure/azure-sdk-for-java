// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Util class used to create RestTemplate for all Azure AD related http request.
 */
public class AadRestTemplateCreator {

    public static RestTemplate createRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    public static RestTemplate createOAuth2ErrorResponseHandledRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = createRestTemplate(builder);
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        return restTemplate;
    }

    public static RestTemplate createOAuth2AccessTokenResponseClientRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = createOAuth2ErrorResponseHandledRestTemplate(builder);
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        if (notContainsElementOfType(converters, FormHttpMessageConverter.class)) {
            converters.add(new FormHttpMessageConverter());
        }
        if (notContainsElementOfType(converters, OAuth2AccessTokenResponseHttpMessageConverter.class)) {
            converters.add(new OAuth2AccessTokenResponseHttpMessageConverter());
        }
        return restTemplate;
    }

    private static boolean notContainsElementOfType(List<?> list, Class<?> clazz) {
        return list.stream().noneMatch(item -> item.getClass().equals(clazz));
    }

}
