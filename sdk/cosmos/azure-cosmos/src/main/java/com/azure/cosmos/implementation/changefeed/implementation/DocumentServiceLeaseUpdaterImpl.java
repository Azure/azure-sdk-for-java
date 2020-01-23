// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.AccessCondition;
import com.azure.cosmos.AccessConditionType;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.ServiceItemLease;
import com.azure.cosmos.implementation.changefeed.ServiceItemLeaseUpdater;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseConflictException;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

import static com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT;
import static com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND;
import static com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedHelper.HTTP_STATUS_CODE_PRECONDITION_FAILED;

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
    public Mono<Lease> updateLease(Lease cachedLease, String itemId, PartitionKey partitionKey,
                                   CosmosItemRequestOptions requestOptions, Function<Lease, Lease> updateLease) {
        Lease arrayLease[] = {cachedLease};
        arrayLease[0] = updateLease.apply(cachedLease);

        if (arrayLease[0] == null) {
            return Mono.empty();
        }

        arrayLease[0].setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

        return this.tryReplaceLease(arrayLease[0], itemId, partitionKey)
            .map(leaseDocument -> {
                arrayLease[0] = ServiceItemLease.fromDocument(leaseDocument);
                return arrayLease[0];
            })
            .hasElement()
            .flatMap(hasItems -> {
                if (hasItems) {
                    return Mono.just(arrayLease[0]);
                }
                // Partition lease update conflict. Reading the current version of lease.
                return this.client.readItem(itemId, partitionKey, requestOptions, CosmosItemProperties.class)
                    .onErrorResume(throwable -> {
                        if (throwable instanceof CosmosClientException) {
                            CosmosClientException ex = (CosmosClientException) throwable;
                            if (ex.getStatusCode() == HTTP_STATUS_CODE_NOT_FOUND) {
                                // Partition lease no longer exists
                                throw new LeaseLostException(arrayLease[0]);
                            }
                        }
                        return Mono.error(throwable);
                    })
                    .map(cosmosItemResponse -> {
                        CosmosItemProperties document = cosmosItemResponse.getProperties();
                        ServiceItemLease serverLease = ServiceItemLease.fromDocument(document);
                        logger.info(
                            "Partition {} update failed because the lease with token '{}' was updated by owner '{}' with token '{}'.",
                            arrayLease[0].getLeaseToken(),
                            arrayLease[0].getConcurrencyToken(),
                            serverLease.getOwner(),
                            serverLease.getConcurrencyToken());
                        arrayLease[0] = serverLease;

                        throw new LeaseConflictException(arrayLease[0], "Partition update failed");
                    });
            })
            .retry(RETRY_COUNT_ON_CONFLICT, throwable -> {
                if (throwable instanceof LeaseConflictException) {
                    logger.info(
                        "Partition {} for the lease with token '{}' failed to update for owner '{}'; will retry.",
                        arrayLease[0].getLeaseToken(),
                        arrayLease[0].getConcurrencyToken(),
                        arrayLease[0].getOwner());
                    return true;
                }
                return false;
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof LeaseConflictException) {
                    logger.warn(
                        "Partition {} for the lease with token '{}' failed to update for owner '{}'; current continuation token '{}'.",
                        arrayLease[0].getLeaseToken(),
                        arrayLease[0].getConcurrencyToken(),
                        arrayLease[0].getOwner(),
                        arrayLease[0].getContinuationToken(), throwable);

                    return Mono.just(arrayLease[0]);
                }
                return Mono.error(throwable);
            });
    }

    private Mono<CosmosItemProperties> tryReplaceLease(Lease lease, String itemId, PartitionKey partitionKey) 
                                                                                        throws LeaseLostException {
        return this.client.replaceItem(itemId, partitionKey, lease, this.getCreateIfMatchOptions(lease))
            .map(cosmosItemResponse -> cosmosItemResponse.getProperties())
            .onErrorResume(re -> {
                if (re instanceof CosmosClientException) {
                    CosmosClientException ex = (CosmosClientException) re;
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
                            return Mono.error(re);
                        }
                    }
                }
                return Mono.error(re);
            });
    }

    private CosmosItemRequestOptions getCreateIfMatchOptions(Lease lease) {
        AccessCondition ifMatchCondition = new AccessCondition();
        ifMatchCondition.setType(AccessConditionType.IF_MATCH);
        ifMatchCondition.setCondition(lease.getConcurrencyToken());

        CosmosItemRequestOptions createIfMatchOptions = new CosmosItemRequestOptions();
        createIfMatchOptions.setAccessCondition(ifMatchCondition);

        return createIfMatchOptions;
    }
}
