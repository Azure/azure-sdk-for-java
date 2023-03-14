// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdOpenConnectionsHandler implements IOpenConnectionsHandler {
    private static final Logger logger = LoggerFactory.getLogger(RntbdOpenConnectionsHandler.class);
    private final RntbdEndpoint.Provider endpointProvider;
    private static final Map<String, SemaphoreSettings> semaphoreRegistry = new HashMap<>();
    public static final String DEFENSIVE_CONNECTIONS_MODE = "DEFENSIVE_SEMAPHORE_SETTINGS";
    public static final String AGGRESSIVE_CONNECTIONS_MODE = "AGGRESSIVE_SEMAPHORE_SETTINGS";


    static {
        semaphoreRegistry.put("AGGRESSIVE_SEMAPHORE_SETTINGS", new SemaphoreSettings(10 * Configs.getCPUCnt(), 30));
        semaphoreRegistry.put("DEFENSIVE_SEMAPHORE_SETTINGS", new SemaphoreSettings(Configs.getCPUCnt(), 10));
    }

    public RntbdOpenConnectionsHandler(RntbdEndpoint.Provider endpointProvider) {

        checkNotNull(endpointProvider, "Argument 'endpointProvider' can not be null");

        this.endpointProvider = endpointProvider;
    }

    @Override
    public Mono<OpenConnectionResponse> openConnection(Uri addressUri) {

        checkNotNull(addressUri, "Argument 'addressUri' should not be null");

        return openConnection(addressUri, AGGRESSIVE_CONNECTIONS_MODE);
    }

    @Override
    public Mono<OpenConnectionResponse> openConnection(Uri addressUri, String semaphoreSettingsMode) {

        SemaphoreSettings semaphoreSettings = semaphoreRegistry.getOrDefault(semaphoreSettingsMode, semaphoreRegistry.get(AGGRESSIVE_CONNECTIONS_MODE));
        Semaphore openConnectionsSemaphore = semaphoreSettings.semaphore;
        int timeout = semaphoreSettings.timeout;

        checkNotNull(addressUri, "Argument 'addressUri' should not be null");

        try {
            if (openConnectionsSemaphore.tryAcquire(timeout, TimeUnit.MINUTES)) {
                return Mono.just(addressUri.getURI())
                        .flatMap(address -> {

                            RntbdEndpoint endpoint = endpointProvider.get(address);
                            OpenConnectionRntbdRequestRecord openConnectionRequestRecord =
                                    endpoint.openConnection(addressUri);

                            return Mono.fromFuture(endpoint.openConnection(addressUri))
                                    .onErrorResume(throwable -> Mono.just(new OpenConnectionResponse(addressUri, false, throwable)))
                                    .doOnNext(response -> {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("Connection result: isConnected [{}], address [{}]", response.isConnected(), response.getUri());
                                        }
                                    })
                                    .doFinally(signalType -> {
                                        if (signalType.equals(SignalType.CANCEL)) {
                                            openConnectionRequestRecord.cancel(true);
                                        }

                                        openConnectionsSemaphore.release();
                                    });
                        });
            }
        } catch (InterruptedException e) {
            logger.warn("Acquire connection semaphore failed", e);
        }

        return Mono.just(new OpenConnectionResponse(addressUri, false, new IllegalStateException("Unable to acquire semaphore")));
    }

    @Override
    public Flux<OpenConnectionResponse> openConnections(URI serviceEndpoint, List<Uri> addresses) {

        checkNotNull(addresses, "Argument 'addresses' should not be null");

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Open connections for addresses {}",
                    StringUtils.join(addresses, ","));
        }

        return openConnections(serviceEndpoint, addresses, AGGRESSIVE_CONNECTIONS_MODE);
    }

    @Override
    public Flux<OpenConnectionResponse> openConnections(URI serviceEndpoint, List<Uri> addresses, String semaphoreSettingsMode) {

        checkNotNull(addresses, "Argument 'addresses' should not be null");

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Open connections for addresses {}",
                    StringUtils.join(addresses, ","));
        }

        return Flux.fromIterable(addresses)
                .flatMap(addressUri -> this.openConnection(addressUri, semaphoreSettingsMode));
    }

    private static final class SemaphoreSettings {
        private final Semaphore semaphore;
        private final int timeout;

        SemaphoreSettings(int permits, int timeout) {
            this.semaphore = new Semaphore(permits);
            this.timeout = timeout;
        }
    }
}
