// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

class AadOauth2RestOperationConfigurationTestUtil {

    static boolean hasItemOfClass(List<?> list, Class<?> clazz) {
        return list.stream()
                .anyMatch(item -> item.getClass().equals(clazz));
    }

    @Configuration
    static class RestTemplateProxyCustomizerConfiguration {

        static final SimpleClientHttpRequestFactory FACTORY = createProxyFactory();

        static SimpleClientHttpRequestFactory createProxyFactory() {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8080));
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setProxy(proxy);
            return factory;
        }

        @Bean
        public RestTemplateCustomizer proxyRestTemplateCustomizer() {
            return (RestTemplate restTemplate) -> restTemplate.setRequestFactory(FACTORY);
        }
    }

}
