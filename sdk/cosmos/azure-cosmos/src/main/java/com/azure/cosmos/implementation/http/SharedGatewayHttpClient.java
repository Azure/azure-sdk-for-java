// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.LifeCycleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class uses a shared HttpClient for multiple Cosmos Clients.
 * The benefit is the underlying connections can be shared if possible across multiple Cosmos client instances.
 */
public class SharedGatewayHttpClient implements HttpClient {
    private static final Logger logger = LoggerFactory.getLogger(SharedGatewayHttpClient.class);
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static SharedGatewayHttpClient sharedGatewayHttpClient;

    public static HttpClient getOrCreateInstance(HttpClientConfig httpClientConfig) {
        synchronized (SharedGatewayHttpClient.class) {
            if (sharedGatewayHttpClient == null) {
                assert counter.get() == 0;
                logger.info("creating a new shared HttpClient");
                sharedGatewayHttpClient = new SharedGatewayHttpClient(httpClientConfig);
            } else {
                logger.info("Reusing an instance of HttpClient");
            }

            counter.incrementAndGet();
            return sharedGatewayHttpClient;
        }
    }

    private final HttpClient httpClient;

    private SharedGatewayHttpClient(HttpClientConfig httpClientConfig) {
        this.httpClient = HttpClient.createFixed(httpClientConfig);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return httpClient.send(request);
    }

    public int getReferenceCounter() {
        return counter.get();
    }

    @Override
    public void shutdown() {
        synchronized (SharedGatewayHttpClient.class) {
            final int numberOfActiveHttpClients = counter.decrementAndGet();
            logger.info("closing one reference to the shared HttpClient, the number of remaining references is {}", numberOfActiveHttpClients);
            if (numberOfActiveHttpClients == 0) {
                logger.info("All references to shared HttpClient are closed. Closing the underlying HttpClient");
                LifeCycleUtils.closeQuietly(sharedGatewayHttpClient.httpClient);
                sharedGatewayHttpClient = null;
            }
        }
    }
}
