// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Util class used to create {@link RestTemplate}s for all Azure AD related http requests.
 */
public final class AadRestTemplateCreator {

    private AadRestTemplateCreator() {
    }

    public static RestTemplate createRestTemplate(RestTemplateBuilder builder) {
        Assert.notNull(builder, "RestTemplateBuilder cannot be null");
        return builder.build();
    }

    public static RestTemplate createOAuth2ErrorResponseHandledRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = createRestTemplate(builder);
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        return restTemplate;
    }

    public static RestTemplate createOAuth2AccessTokenResponseClientRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = createOAuth2ErrorResponseHandledRestTemplate(builder);
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
