// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.ServiceItemLeaseUpdater;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseConflictException;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Instant;
import java.util.function.Function;

import static com.azure.cosmos.implementation.changefeed.common.ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT;
import static com.azure.cosmos.implementation.changefeed.common.ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND;
import static com.azure.cosmos.implementation.changefeed.common.ChangeFeedHelper.HTTP_STATUS_CODE_PRECONDITION_FAILED;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for service lease updater interface.
 */
class DocumentServiceLeaseUpdaterImpl implements ServiceItemLeaseUpdater {
    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceLeaseUpdaterImpl.class);
    private static final int RETRY_COUNT_ON_CONFLICT = 5;

    private final ChangeFeedContextClient client;

    public DocumentServiceLeaseUpdaterImpl(ChangeFeedContextClient client) {
        checkNotNull(client, "Argument 'client' can not be null");

        this.client = client;
    }

    @Override
    public Mono<Lease> updateLease(
            final Lease cachedLease,
            String itemId,
            PartitionKey partitionKey,
            CosmosItemRequestOptions requestOptions,
            Function<Lease, Lease> updateLease) {
        Lease localLease = updateLease.apply(cachedLease);

        if (localLease == null) {
            return Mono.empty();
        }

        localLease.setTimestamp(Instant.now());

        cachedLease.setServiceItemLease(localLease);

        return
            Mono.just(this)
            .flatMap( value -> this.tryReplaceLease(cachedLease, itemId, partitionKey, requestOptions))
            .map(leaseDocument -> {
                cachedLease.setServiceItemLease(ServiceItemLeaseV1.fromDocument(leaseDocument));
                return cachedLease;
            })
            .hasElement()
            .flatMap(hasItems -> {
                if (hasItems) {
                    return Mono.just(cachedLease);
                }
                // Partition lease update conflict. Reading the current version of lease.
                return this.client.readItem(itemId, partitionKey, requestOptions, InternalObjectNode.class)
                    .onErrorResume(throwable -> {
                        if (throwable instanceof CosmosException) {
                            CosmosException ex = (CosmosException) throwable;
                            if (Exceptions.isNotFound(ex)) {
                                logger.info("Lease with token {}: Failed to update. Lease could not be found.", cachedLease.getLeaseToken());
                                throw new LeaseLostException(cachedLease);
                            }
                        }
                        logger.info("Lease with token {} and owner {}: Failed to update.", cachedLease.getLeaseToken(), cachedLease.getOwner(), throwable);
                        return Mono.error(throwable);
                    })
                    .map(cosmosItemResponse -> {
                        InternalObjectNode document =
                            BridgeInternal.getProperties(cosmosItemResponse);
                        ServiceItemLeaseV1 serverLease = ServiceItemLeaseV1.fromDocument(document);
                        logger.info(
                            "Lease with token {}: Failed to update. Lease with concurrency token '{}' was updated by owner '{}' with concurrency token '{}'.",
                            cachedLease.getLeaseToken(),
                            cachedLease.getConcurrencyToken(),
                            serverLease.getOwner(),
                            serverLease.getConcurrencyToken());

                        // Check if we still have the expected ownership on the target lease.
                        if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(cachedLease.getOwner())) {
                            logger.info(
                                    "Lease with token {}: Failed to update. Lease was acquired already by owner '{}'",
                                    serverLease.getLeaseToken(),
                                    serverLease.getOwner());
                            throw new LeaseLostException(serverLease);
                        }

                        cachedLease.setTimestamp(Instant.now());
                        cachedLease.setConcurrencyToken(serverLease.getConcurrencyToken());

                        throw new LeaseConflictException(cachedLease, "Lease update failed");
                    });
            })
            .retryWhen(Retry.max(RETRY_COUNT_ON_CONFLICT).filter(throwable -> {
                if (throwable instanceof LeaseConflictException) {
                    logger.info(
                        "Lease with token {}: Failed to update lease with concurrency token '{}', owner '{}'; will retry",
                        cachedLease.getLeaseToken(),
                        cachedLease.getConcurrencyToken(),
                        cachedLease.getOwner());
                    return true;
                }
                return false;
            }))
            .onErrorResume(throwable -> {
                if (throwable instanceof LeaseConflictException) {
                    logger.warn(
                        "Lease with token in epk-range {} and continuation(eTag) {} failed to update due to lease conflict for owner: {}; current continuation token: {}",
                        cachedLease.getLeaseToken(),
                        cachedLease.getConcurrencyToken(),
                        cachedLease.getOwner(),
                        cachedLease.getReadableContinuationToken(), throwable);

                    return Mono.just(cachedLease);
                }
                logger.warn("Lease with token in epk-range {} lease update failed for owner {}; current continuation token {}.",
                    cachedLease.getLeaseToken(),
                    cachedLease.getOwner(),
                    cachedLease.getReadableContinuationToken(), throwable);
                return Mono.error(throwable);
            });
    }

    private Mono<InternalObjectNode> tryReplaceLease(
            Lease lease,
            String itemId,
            PartitionKey partitionKey,
            CosmosItemRequestOptions cosmosItemRequestOptions) throws LeaseLostException {
        return this.client.replaceItem(
                itemId,
                partitionKey,
                lease,
                this.getCreateIfMatchOptions(cosmosItemRequestOptions, lease))
            .map(cosmosItemResponse -> BridgeInternal.getProperties(cosmosItemResponse))
            .onErrorResume(re -> {
                if (re instanceof CosmosException) {
                    CosmosException ex = (CosmosException) re;
                    switch (ex.getStatusCode()) {
                        case HTTP_STATUS_CODE_PRECONDITION_FAILED: {
                            return Mono.empty();
                        }
                        case HTTP_STATUS_CODE_CONFLICT: {
                            throw new LeaseLostException(lease, ex, false);
                        }
                        case HTTP_STATUS_CODE_NOT_FOUND: {
                            throw new LeaseLostException(lease, ex, true);
                        }
                        default: {
                            logger.warn("Lease with token {} and owner {}: Failed to replace.", lease.getLeaseToken(), lease.getOwner(), re);
                            return Mono.error(re);
                        }
                    }
                }
                logger.warn("Lease with token {} and owner {}: Failed to replace.", lease.getLeaseToken(), lease.getOwner(), re);
                return Mono.error(re);
            });
    }

    private CosmosItemRequestOptions getCreateIfMatchOptions(CosmosItemRequestOptions createIfMatchOptions, Lease lease) {
        createIfMatchOptions.setIfMatchETag(lease.getConcurrencyToken());

        return createIfMatchOptions;
    }
}
