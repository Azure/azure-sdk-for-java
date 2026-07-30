// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;

@Configuration(proxyBeanMethods = false)
public class RecordingClientHttpRequestFactoryBuilderConfiguration {

    @Bean
    public RecordingClientHttpRequestFactoryBuilder recordingClientHttpRequestFactoryBuilder() {
        return new RecordingClientHttpRequestFactoryBuilder(ClientHttpRequestFactoryBuilder.detect());
    }

    public static final class RecordingClientHttpRequestFactoryBuilder
        implements ClientHttpRequestFactoryBuilder<ClientHttpRequestFactory> {

        private final ClientHttpRequestFactoryBuilder<? extends ClientHttpRequestFactory> delegate;
        private ClientHttpRequestFactorySettings clientSettings;

        RecordingClientHttpRequestFactoryBuilder(
            ClientHttpRequestFactoryBuilder<? extends ClientHttpRequestFactory> delegate) {
            this.delegate = delegate;
        }

        @Override
        public ClientHttpRequestFactory build(ClientHttpRequestFactorySettings settings) {
            this.clientSettings = settings;
            return this.delegate.build(settings);
        }

        public ClientHttpRequestFactorySettings getClientSettings() {
            return this.clientSettings;
        }
    }
}
