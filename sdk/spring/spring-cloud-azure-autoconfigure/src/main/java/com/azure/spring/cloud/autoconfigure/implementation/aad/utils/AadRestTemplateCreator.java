// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.utils;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

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
        builder = builder.errorHandler(new OAuth2ErrorResponseErrorHandler());
        return createRestTemplate(builder);
    }

    public static RestTemplate createOAuth2AccessTokenResponseClientRestTemplate(RestTemplateBuilder builder) {
        builder = builder.messageConverters(
                new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter());
        return createOAuth2ErrorResponseHandledRestTemplate(builder);
    }

}
