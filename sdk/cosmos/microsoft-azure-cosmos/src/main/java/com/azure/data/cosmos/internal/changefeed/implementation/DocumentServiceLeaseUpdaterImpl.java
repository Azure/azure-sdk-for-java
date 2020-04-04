// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.AccessCondition;
import com.azure.data.cosmos.AccessConditionType;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.ServiceItemLease;
import com.azure.data.cosmos.internal.changefeed.ServiceItemLeaseUpdater;
import com.azure.data.cosmos.internal.changefeed.exceptions.LeaseConflictException;
import com.azure.data.cosmos.internal.changefeed.exceptions.LeaseLostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

import static com.azure.data.cosmos.internal.changefeed.implementation.ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT;
import static com.azure.data.cosmos.internal.changefeed.implementation.ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND;
import static com.azure.data.cosmos.internal.changefeed.implementation.ChangeFeedHelper.HTTP_STATUS_CODE_PRECONDITION_FAILED;

/**
 * Implementation for service lease updater interface.
 */
class DocumentServiceLeaseUpdaterImpl implements ServiceItemLeaseUpdater {
    private final Logger logger = LoggerFactory.getLogger(DocumentServiceLeaseUpdaterImpl.class);
    private final int RETRY_COUNT_ON_CONFLICT = 5;
    private final ChangeFeedContextClient client;

    public DocumentServiceLeaseUpdaterImpl(ChangeFeedContextClient client) {
        if (client == null) {
            throw new IllegalArgumentException("client");
        }

        this.client = client;
    }

    @Override
    public Mono<Lease> updateLease(final Lease cachedLease, CosmosItem itemLink, CosmosItemRequestOptions requestOptions, Function<Lease, Lease> updateLease) {
        Lease localLease = updateLease.apply(cachedLease);

        if (localLease == null) {
            return Mono.empty();
        }

        localLease.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

        cachedLease.setServiceItemLease(localLease);

        return this.tryReplaceLease(cachedLease, itemLink)
            .map(leaseDocument -> {
                cachedLease.setServiceItemLease(ServiceItemLease.fromDocument(leaseDocument));
                return cachedLease;
            })
            .hasElement()
            .flatMap(hasItems -> {
                if (hasItems) {
                    return Mono.just(cachedLease);
                }
                // Partition lease update conflict. Reading the current version of lease.
                return this.client.readItem(itemLink, requestOptions)
                    .onErrorResume(throwable -> {
                        if (throwable instanceof CosmosClientException) {
                            CosmosClientException ex = (CosmosClientException) throwable;
                            if (ex.statusCode() == HTTP_STATUS_CODE_NOT_FOUND) {
                                // Partition lease no longer exists
                                throw new LeaseLostException(cachedLease);
                            }
                        }
                        return Mono.error(throwable);
                    })
                    .map(cosmosItemResponse -> {
                        CosmosItemProperties document = cosmosItemResponse.properties();
                        ServiceItemLease serverLease = ServiceItemLease.fromDocument(document);
                        logger.info(
                            "Partition {} update failed because the lease with token '{}' was updated by owner '{}' with token '{}'.",
                            cachedLease.getLeaseToken(),
                            cachedLease.getConcurrencyToken(),
                            serverLease.getOwner(),
                            serverLease.getConcurrencyToken());
                        cachedLease.setConcurrencyToken(serverLease.getConcurrencyToken());
                        cachedLease.setOwner(serverLease.getOwner());

                        throw new LeaseConflictException(cachedLease, "Partition update failed");
                    });
            })
            .retry(RETRY_COUNT_ON_CONFLICT, throwable -> {
                if (throwable instanceof LeaseConflictException) {
                    logger.info(
                        "Partition {} for the lease with token '{}' failed to update for owner '{}'; will retry.",
                        cachedLease.getLeaseToken(),
                        cachedLease.getConcurrencyToken(),
                        cachedLease.getOwner());
                    return true;
                }
                return false;
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof LeaseConflictException) {
                    logger.warn(
                        "Partition {} for the lease with token '{}' failed to update for owner '{}'; current continuation token '{}'.",
                        cachedLease.getLeaseToken(),
                        cachedLease.getConcurrencyToken(),
                        cachedLease.getOwner(),
                        cachedLease.getContinuationToken(), throwable);

                    return Mono.just(cachedLease);
                }
                return Mono.error(throwable);
            });
    }

    private Mono<CosmosItemProperties> tryReplaceLease(Lease lease, CosmosItem itemLink) throws LeaseLostException {
        return this.client.replaceItem(itemLink, lease, this.getCreateIfMatchOptions(lease))
            .map(cosmosItemResponse -> cosmosItemResponse.properties())
            .onErrorResume(re -> {
                if (re instanceof CosmosClientException) {
                    CosmosClientException ex = (CosmosClientException) re;
                    switch (ex.statusCode()) {
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
                            return Mono.error(re);
                        }
                    }
                }
                return Mono.error(re);
            });
    }

    private CosmosItemRequestOptions getCreateIfMatchOptions(Lease lease) {
        AccessCondition ifMatchCondition = new AccessCondition();
        ifMatchCondition.type(AccessConditionType.IF_MATCH);
        ifMatchCondition.condition(lease.getConcurrencyToken());

        CosmosItemRequestOptions createIfMatchOptions = new CosmosItemRequestOptions();
        createIfMatchOptions.accessCondition(ifMatchCondition);

        return createIfMatchOptions;
    }
}
