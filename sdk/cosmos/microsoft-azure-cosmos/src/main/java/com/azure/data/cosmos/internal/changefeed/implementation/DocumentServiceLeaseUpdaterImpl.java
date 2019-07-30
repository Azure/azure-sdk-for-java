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
import com.azure.data.cosmos.internal.changefeed.exceptions.LeaseLostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
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
        DocumentServiceLeaseUpdaterImpl self = this;
        Lease arrayLease[] = {cachedLease};
        arrayLease[0] = updateLease.apply(cachedLease);

        if (arrayLease[0] == null) {
            return Mono.empty();
        }

        arrayLease[0].setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

        return self.tryReplaceLease(arrayLease[0], itemLink)
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
                                throw Exceptions.propagate(new LeaseLostException(arrayLease[0]));
                            }
                        }
                        return Mono.error(throwable);
                    })
                    .map(cosmosItemResponse -> {
                        CosmosItemProperties document = cosmosItemResponse.properties();
                        ServiceItemLease serverLease = ServiceItemLease.fromDocument(document);
                        logger.info(
                            "Partition {} update failed because the lease with token '{}' was updated by host '{}' with token '{}'.",
                            arrayLease[0].getLeaseToken(),
                            arrayLease[0].getConcurrencyToken(),
                            serverLease.getOwner(),
                            serverLease.getConcurrencyToken());
                        arrayLease[0] = serverLease;

                        throw Exceptions.propagate(new RuntimeException(""));
                    });
            })
            .retry(RETRY_COUNT_ON_CONFLICT, throwable -> {
                if (throwable instanceof RuntimeException) {
                    return throwable instanceof LeaseLostException;
                }
                return false;
            });

//        Lease lease = cachedLease;
//
//        for (int retryCount = RETRY_COUNT_ON_CONFLICT; retryCount > 0; retryCount--) {
//            lease = updateLease.apply(lease);
//
//            if (lease == null) {
//                return Mono.empty();
//            }
//
//            lease.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
//            CosmosItemProperties leaseDocument = this.tryReplaceLease(lease, itemLink).block();
//
//            if (leaseDocument != null) {
//                return Mono.just(ServiceItemLease.fromDocument(leaseDocument));
//            }
//
//            // Partition lease update conflict. Reading the current version of lease.
//            CosmosItemProperties document = null;
//            try {
//                CosmosItemResponse response = this.client.readItem(itemLink, requestOptions)
//                    .block();
//                document = response.properties();
//            } catch (RuntimeException re) {
//                if (re.getCause() instanceof CosmosClientException) {
//                    CosmosClientException ex = (CosmosClientException) re.getCause();
//                    if (ex.statusCode() == HTTP_STATUS_CODE_NOT_FOUND) {
//                        // Partition lease no longer exists
//                        throw new LeaseLostException(lease);
//                    }
//                }
//                throw  re;
//            }
//
//            ServiceItemLease serverLease = ServiceItemLease.fromDocument(document);
//            logger.info(
//                "Partition {} update failed because the lease with token '{}' was updated by host '{}' with token '{}'. Will retry, {} retry(s) left.",
//                lease.getLeaseToken(),
//                lease.getConcurrencyToken(),
//                serverLease.getOwner(),
//                serverLease.getConcurrencyToken(),
//                retryCount);
//
//            lease = serverLease;
//        }
//
//        throw new LeaseLostException(lease);
    }

    private Mono<CosmosItemProperties> tryReplaceLease(Lease lease, CosmosItem itemLink) throws LeaseLostException {
        DocumentServiceLeaseUpdaterImpl self = this;
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
                            throw Exceptions.propagate( new LeaseLostException(lease, ex, false));
                        }
                        case HTTP_STATUS_CODE_NOT_FOUND: {
                            throw Exceptions.propagate( new LeaseLostException(lease, ex, true));
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
