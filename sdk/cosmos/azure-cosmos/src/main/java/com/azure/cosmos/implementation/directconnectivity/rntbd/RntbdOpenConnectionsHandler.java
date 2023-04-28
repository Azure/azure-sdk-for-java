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
    public Flux<OpenConnectionResponse> openConnections(
            String collectionRid,
            URI serviceEndpoint,
            List<Uri> addresses,
            ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor,
            int minConnectionsRequiredForEndpoint) {
        // collectionRid may not always be available, especially for open connection flows
        // from the RntbdConnectionsStateListener, hence there is no null check for collectionRid

        checkNotNull(addresses, "Argument 'addresses' should not be null");

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Open connections for addresses {}",
                    StringUtils.join(addresses, ","));
        }

        return Flux.fromIterable(addresses)
                .flatMap(addressUri -> {
                            RxDocumentServiceRequest openConnectionRequest =
                                    this.getOpenConnectionRequest(collectionRid, serviceEndpoint, addressUri);

                            final RntbdRequestArgs requestArgs = new RntbdRequestArgs(openConnectionRequest, addressUri);
                            final RntbdEndpoint endpoint =
                                    this.endpointProvider.createIfAbsent(
                                            serviceEndpoint,
                                            addressUri.getURI(),
                                            proactiveOpenConnectionsProcessor,
                                            minConnectionsRequiredForEndpoint
                                    );

                            // if endpoint had already been instantiated with a
                            // different minConnectionsRequired value, check if
                            // the open connections flow through a different
                            // container for the same endpoint has a higher
                            // value for minConnectionsRequired and choose the higher
                            // value
                            final int minConnectionsRequired = endpoint.getMinChannelsRequired();
                            final int newMinConnectionsRequired = Math.max(minConnectionsRequired, minConnectionsRequiredForEndpoint);

                            endpoint.setMinChannelsRequired(newMinConnectionsRequired);

                            final int connectionsOpened = endpoint.channelsMetrics();

                            if (connectionsOpened < newMinConnectionsRequired) {
                                OpenConnectionRntbdRequestRecord requestRecord = endpoint.openConnection(requestArgs);
                                return Mono.fromFuture(requestRecord)
                                        .onErrorResume(throwable -> Mono.just(new OpenConnectionResponse(addressUri, false, throwable, true)))
                                        .doOnNext(response -> {

                                            if (logger.isDebugEnabled()) {
                                                logger.debug("Connection result: isConnected [{}], address [{}]", response.isConnected(), response.getUri());
                                            }
                                        });
                            }
                            // when open connection is not attempted and connected status is true
                            // do not submit open connection tasks to the sink for this endpoint
                            return Mono.just(new OpenConnectionResponse(addressUri, true, null, false));
                        }
                );
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
