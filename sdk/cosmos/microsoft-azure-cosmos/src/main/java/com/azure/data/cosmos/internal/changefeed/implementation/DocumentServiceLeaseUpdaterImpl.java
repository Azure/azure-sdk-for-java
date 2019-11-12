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
    public Mono<Lease> updateLease(Lease cachedLease, CosmosItem itemLink, CosmosItemRequestOptions requestOptions, Function<Lease, Lease> updateLease) {
        Lease arrayLease[] = {cachedLease};
        arrayLease[0] = updateLease.apply(cachedLease);

        if (arrayLease[0] == null) {
            return Mono.empty();
        }

        arrayLease[0].setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

        return this.tryReplaceLease(arrayLease[0], itemLink)
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
                return this.client.readItem(itemLink, requestOptions)
                    .onErrorResume(throwable -> {
                        if (throwable instanceof CosmosClientException) {
                            CosmosClientException ex = (CosmosClientException) throwable;
                            if (ex.statusCode() == HTTP_STATUS_CODE_NOT_FOUND) {
                                // Partition lease no longer exists
                                throw new LeaseLostException(arrayLease[0]);
                            }
                        }
                        return Mono.error(throwable);
                    })
                    .map(cosmosItemResponse -> {
                        CosmosItemProperties document = cosmosItemResponse.properties();
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
