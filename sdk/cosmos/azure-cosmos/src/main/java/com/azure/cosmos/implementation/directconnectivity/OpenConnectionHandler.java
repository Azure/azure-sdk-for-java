// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class OpenConnectionHandler implements IOpenConnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(OpenConnectionHandler.class);
    private static final int DEFAULT_CONNECTION_CONCURRENCY = 10;

    private final TransportClient transportClient;

    public OpenConnectionHandler(TransportClient transportClient) {
        this.transportClient = transportClient;
    }

    @Override
    public Mono<Void> openConnections(PartitionKeyRangeIdentity pkRangeIdentity, AddressInformation[] addressInformations) {
        checkArgument(addressInformations != null, "addressInformations");
        checkArgument(pkRangeIdentity != null, "pkRangeIdentity");

        if (logger.isDebugEnabled()) {
            logger.debug(
                "Start to try to open connections for collectionRid {}, pkRangeId {}",
                pkRangeIdentity.getCollectionRid(),
                pkRangeIdentity.getPartitionKeyRangeId());
        }

        return Flux.fromIterable(Arrays.asList(addressInformations))
            .flatMap(addressInformation -> this.openConnection(addressInformation.getPhysicalUri(), RxOpenConnectionRequest.INSTANCE), DEFAULT_CONNECTION_CONCURRENCY)
            .then();
    }

    private Mono<RntbdOpenConnectionResponse> openConnection(Uri physicalUri, RxOpenConnectionRequest openConnectionRequest) {
        return this.transportClient.openConnectionAsync(physicalUri, openConnectionRequest)
            .flatMap(response -> {

                logger.info("Successfully opened connection to {}", physicalUri);

                if (logger.isDebugEnabled()) {
                    logger.debug("Successfully opened connection to {}", physicalUri);
                }

                return Mono.just(response);
            })
            .onErrorResume(throwable -> {
                logger.debug("Failed to open connection to {}", physicalUri, throwable);
                // if it failed to open the connection, probably should refresh the address list and then retry
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to open connection to {}", physicalUri, throwable);
                }

                return Mono.empty();
            });
    }
}
