// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdOpenConnectionsHandler implements IOpenConnectionsHandler {
    private static final Logger logger = LoggerFactory.getLogger(RntbdOpenConnectionsHandler.class);
    private static int DEFAULT_CONNECTION_SEMAPHORE_TIMEOUT_IN_MINUTES = 30;
    private final TransportClient transportClient;
    private final Semaphore openConnectionsSemaphore;

    public RntbdOpenConnectionsHandler(TransportClient transportClient) {

        checkNotNull(transportClient, "Argument 'transportClient' can not be null");

        this.transportClient = transportClient;
        this.openConnectionsSemaphore = new Semaphore(Configs.getCPUCnt() * 10);
    }

    @Override
    public Flux<OpenConnectionResponse> openConnections(String collectionRid, URI serviceEndpoint, List<Uri> addresses) {
        checkNotNull(addresses, "Argument 'addresses' should not be null");
        checkArgument(StringUtils.isNotEmpty(collectionRid), "Argument 'collectionRid' cannot be null nor empty");

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Open connections for addresses {}",
                    StringUtils.join(addresses, ","));
        }

        return Flux.fromIterable(addresses)
                .flatMap(addressUri -> {
                    try {
                        if (this.openConnectionsSemaphore.tryAcquire(DEFAULT_CONNECTION_SEMAPHORE_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES)) {

                            RxDocumentServiceRequest openConnectionRequest =
                                this.getOpenConnectionRequest(collectionRid, serviceEndpoint, addressUri);
                            return this.transportClient.openConnection(addressUri, openConnectionRequest)
                                    .onErrorResume(throwable -> Mono.just(new OpenConnectionResponse(addressUri, false, throwable)))
                                    .doOnNext(response -> {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("Connection result: isConnected [{}], address [{}]", response.isConnected(), response.getUri());
                                        }
                                    })
                                    .doOnTerminate(() -> this.openConnectionsSemaphore.release());
                        }

                    } catch (InterruptedException e) {
                        logger.warn("Acquire connection semaphore failed", e);
                    }

                    return Mono.just(new OpenConnectionResponse(addressUri, false, new IllegalStateException("Unable to acquire semaphore")));
                });
    }

    private RxDocumentServiceRequest getOpenConnectionRequest(String collectionRid, URI serviceEndpoint, Uri addressUri) {
        RxDocumentServiceRequest openConnectionRequest =
            RxDocumentServiceRequest.create(null, OperationType.Create, ResourceType.Connection);
        openConnectionRequest.requestContext.locationEndpointToRoute = serviceEndpoint;
        openConnectionRequest.requestContext.resolvedCollectionRid = collectionRid;
        openConnectionRequest.faultInjectionRequestContext.setLocationEndpointToRoute(serviceEndpoint);

        return openConnectionRequest;
    }
}
