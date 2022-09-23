// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestTemplateCreator.createOAuth2AccessTokenResponseClientRestTemplate;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestTemplateCreator.createOAuth2ErrorResponseHandledRestTemplate;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestTemplateCreator.createRestTemplate;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestTemplateCreatorTest.RestTemplateProxyCustomizerConfiguration.FACTORY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AadRestTemplateCreatorTest {

    @Test
    void testAadRestOperationConfiguration() {
        new ApplicationContextRunner()
                .withUserConfiguration(RestTemplateAutoConfiguration.class)
                .run((context) -> {
                    RestTemplateBuilder builder = context.getBean(RestTemplateBuilder.class);

                    RestTemplate restTemplate = createRestTemplate(builder);
                    ResponseErrorHandler handler = restTemplate.getErrorHandler();
                    assertNotEquals(handler.getClass(), OAuth2ErrorResponseErrorHandler.class);
                    List<HttpMessageConverter<?>>  converters = restTemplate.getMessageConverters();
                    assertFalse(hasItemOfClass(converters, FormHttpMessageConverter.class));
                    assertFalse(hasItemOfClass(converters, OAuth2AccessTokenResponseHttpMessageConverter.class));

                    restTemplate = createOAuth2ErrorResponseHandledRestTemplate(builder);
                    handler = restTemplate.getErrorHandler();
                    assertEquals(handler.getClass(), OAuth2ErrorResponseErrorHandler.class);
                    converters = restTemplate.getMessageConverters();
                    assertFalse(hasItemOfClass(converters, FormHttpMessageConverter.class));
                    assertFalse(hasItemOfClass(converters, OAuth2AccessTokenResponseHttpMessageConverter.class));

                    restTemplate = createOAuth2AccessTokenResponseClientRestTemplate(builder);
                    handler = restTemplate.getErrorHandler();
                    assertEquals(handler.getClass(), OAuth2ErrorResponseErrorHandler.class);
                    converters = restTemplate.getMessageConverters();
                    assertTrue(hasItemOfClass(converters, FormHttpMessageConverter.class));
                    assertTrue(hasItemOfClass(converters, OAuth2AccessTokenResponseHttpMessageConverter.class));
                });
    }

    @Test
    void testRestOperationProxyConfiguration() {
        new ApplicationContextRunner()
                .withUserConfiguration(
                        RestTemplateAutoConfiguration.class,
                        RestTemplateProxyCustomizerConfiguration.class)
                .run((context) -> {
                    RestTemplate restTemplate = context.getBean(RestTemplateBuilder.class).build();
                    assertSame(restTemplate.getRequestFactory(), FACTORY);
                });
    }

    static boolean hasItemOfClass(List<?> list, Class<?> clazz) {
        return list.stream()
                .anyMatch(item -> item.getClass().equals(clazz));
    }

    @Configuration
    static class RestTemplateProxyCustomizerConfiguration {

        static final SimpleClientHttpRequestFactory FACTORY = createProxyFactory();

        @Bean
        public RestTemplateCustomizer proxyRestTemplateCustomizer() {
            return (RestTemplate restTemplate) -> restTemplate.setRequestFactory(FACTORY);
        }

        static SimpleClientHttpRequestFactory createProxyFactory() {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8080));
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setProxy(proxy);
            return factory;
        }
    }

}
