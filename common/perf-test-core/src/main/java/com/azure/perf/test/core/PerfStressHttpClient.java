// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import reactor.core.publisher.Mono;

/**
 * Represents the Http Client used to run performance tests.
 */
public class PerfStressHttpClient {
    /**
     * Creates an instance of the http client
     * @param options the configuration to create the http client with.
     * @return The Http client.
     */
    public static HttpClient create(PerfStressOptions options) {
        HttpClient httpClient = HttpClient.createDefault();

        if (options.isInsecure()) {
            makeInsecure(httpClient);
        }

        if (options.getHost() != null && options.getHost().length() > 0) {
            httpClient = new ChangeUriHttpClient(httpClient, options.getHost(), options.getPort());
        }

        return httpClient;
    }
     
    private static void makeInsecure(HttpClient httpClient) {
        Field nettyClientField;
        try {
            nettyClientField = httpClient.getClass().getDeclaredField("nettyClient");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
        nettyClientField.setAccessible(true);

        reactor.netty.http.client.HttpClient nettyClient;
        try {
            nettyClient = (reactor.netty.http.client.HttpClient) nettyClientField.get(httpClient);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE);

        try {
            nettyClientField.set(httpClient, nettyClient.secure(spec -> spec.sslContext(sslContextBuilder)));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ChangeUriHttpClient implements HttpClient {
        private final HttpClient httpClient;
        private final String host;
        private final int port;
    
        ChangeUriHttpClient(HttpClient httpClient, String host, int port) {
            this.httpClient = httpClient;
            this.host = host;
            this.port = port;
        }
    
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            request.getHeaders().put("Host", request.getUrl().getHost());
    
            String protocol = request.getUrl().getProtocol();
            String host = this.host;
            int port = this.port;
            String file = request.getUrl().getFile();
    
            try {
                request.setUrl(new URL(protocol, host, port, file));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
    
            return httpClient.send(request);
        }
    }    
}
