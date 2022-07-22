// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public abstract class AbstractReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter {

    private final HeadersConverter headersConverter;
    private final ParametersConverter parametersConverter;
    protected final OAuth2AuthorizationCodeGrantRequestEntityConverter entityConverter;

    protected AbstractReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter(@NotNull OAuth2AuthorizationCodeGrantRequestEntityConverter entityConverter) {
        this.headersConverter = new HeadersConverter(this);
        this.parametersConverter = new ParametersConverter(this);
        this.entityConverter = entityConverter;
    }

    protected static class HeadersConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, HttpHeaders> {

        private final AbstractReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter entityConverter;

        public HeadersConverter(AbstractReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter entityConverter) {
            this.entityConverter = entityConverter;
        }

        @Override
        public HttpHeaders convert(OAuth2AuthorizationCodeGrantRequest source) {
            return this.entityConverter.convertHeaders(source);
        }

    }

    protected static class ParametersConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> {

        private final AbstractReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter entityConverter;

        public ParametersConverter(AbstractReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter entityConverter) {
            this.entityConverter = entityConverter;
        }

        @Override
        public MultiValueMap<String, String> convert(OAuth2AuthorizationCodeGrantRequest source) {
            return this.entityConverter.convertParameters(source);
        }
    }

    protected HttpHeaders convertHeaders(OAuth2AuthorizationCodeGrantRequest request) {
        Assert.notNull(request, "Request must not be null.");
        RequestEntity<?> requestEntity = this.entityConverter.convert(request);
        HttpHeaders httpHeaders = new HttpHeaders();
        Optional.of(requestEntity)
            .map(HttpEntity::getHeaders)
            .ifPresent(httpHeaders::putAll);
        return httpHeaders;
    }

    public Converter<OAuth2AuthorizationCodeGrantRequest, HttpHeaders> getHeadersConverter() {
        return this.headersConverter;
    }

    protected MultiValueMap<String, String> convertParameters(OAuth2AuthorizationCodeGrantRequest request) {
        Assert.notNull(request, "Request must not be null.");
        RequestEntity<?> requestEntity = this.entityConverter.convert(request);
        @SuppressWarnings("unchecked") MultiValueMap<String, String> body = (requestEntity == null) ? null : (MultiValueMap<String, String>) requestEntity.getBody();
        Assert.notNull(body, "body can not be null");
        return body;
    }

    public Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> getParametersConverter() {
        return this.parametersConverter;
    }

}
