// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdOpenConnectionsHandler implements IOpenConnectionsHandler {
    private static final Logger logger = LoggerFactory.getLogger(RntbdOpenConnectionsHandler.class);
    private final RntbdEndpoint.Provider endpointProvider;

    public RntbdOpenConnectionsHandler(RntbdEndpoint.Provider endpointProvider) {

        checkNotNull(endpointProvider, "Argument 'endpointProvider' can not be null");

        this.endpointProvider = endpointProvider;
    }

    @Override
    public Flux<OpenConnectionResponse> openConnections(String collectionRid, List<RntbdEndpoint> endpoints, int minConnectionsRequiredForEndpoint) {
        // collectionRid may not always be available, especially for open connection flows
        // from the RntbdConnectionsStateListener, hence there is no null check for collectionRid
        checkNotNull(endpoints, "Argument 'endpoints' should not be null");

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Open connections for endpoints {}",
                    StringUtils.join(endpoints, ","));
        }

        return Flux.fromIterable(endpoints)
                .flatMap(endpoint -> {

                            Uri addressUri = endpoint.getAddressUri();

                            RxDocumentServiceRequest openConnectionRequest =
                                    this.getOpenConnectionRequest(collectionRid, endpoint.serviceEndpoint());

                            final RntbdRequestArgs requestArgs = new RntbdRequestArgs(openConnectionRequest, addressUri);

                            final int connectionsOpened = endpoint.channelsMetrics();

                            if (connectionsOpened < minConnectionsRequiredForEndpoint) {
                                OpenConnectionRntbdRequestRecord requestRecord = endpoint.openConnection(requestArgs);
                                return Mono.fromFuture(requestRecord)
                                        .onErrorResume(throwable -> Mono.just(new OpenConnectionResponse(addressUri, false, throwable, true, endpoint.channelsMetrics())))
                                        .doOnNext(response -> {

                                            if (logger.isDebugEnabled()) {
                                                logger.debug("Connection result: isConnected [{}], address [{}]", response.isConnected(), response.getUri());
                                            }
                                        });
                            }
                            // when open connection is not attempted and connected status is true
                            // do not submit open connection tasks to the sink for this endpoint
                            return Mono.just(new OpenConnectionResponse(addressUri, true, null, false, endpoint.channelsMetrics()));
                        }
                );
    }

    private RxDocumentServiceRequest getOpenConnectionRequest(String collectionRid, URI serviceEndpoint) {
        RxDocumentServiceRequest openConnectionRequest =
            RxDocumentServiceRequest.create(null, OperationType.Create, ResourceType.Connection);

        openConnectionRequest.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(serviceEndpoint);
        openConnectionRequest.requestContext.resolvedCollectionRid = collectionRid;
        openConnectionRequest.faultInjectionRequestContext.setRegionalRoutingContextToRoute(openConnectionRequest.requestContext.regionalRoutingContextToRoute);

        return openConnectionRequest;
    }
}
