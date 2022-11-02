// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.RequestOptionsFactory;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedHelper;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation for LeaseStore.
 */
class LeaseStoreImpl implements LeaseStore {
    private final Logger logger = LoggerFactory.getLogger(LeaseStoreImpl.class);
    private final ChangeFeedContextClient client;
    private final String containerNamePrefix;
    private final CosmosAsyncContainer leaseCollectionLink;
    private final RequestOptionsFactory requestOptionsFactory;
    private volatile String lockETag;

    public LeaseStoreImpl(
            ChangeFeedContextClient client,
            String containerNamePrefix,
            CosmosAsyncContainer leaseCollectionLink,
            RequestOptionsFactory requestOptionsFactory) {

        this.client = client;
        this.containerNamePrefix = containerNamePrefix;
        this.leaseCollectionLink = leaseCollectionLink;
        this.requestOptionsFactory = requestOptionsFactory;
    }

    @Override
    public Mono<Boolean> isInitialized() {
        String markerDocId = this.getStoreMarkerName();

        InternalObjectNode doc = new InternalObjectNode();
        doc.setId(markerDocId);

        CosmosItemRequestOptions requestOptions = this.requestOptionsFactory.createItemRequestOptions(
            ServiceItemLeaseV1.fromDocument(doc));

        return this.client.readItem(markerDocId, new PartitionKey(markerDocId), requestOptions, InternalObjectNode.class)
            .flatMap(documentResourceResponse -> Mono.just(BridgeInternal.getProperties(documentResourceResponse) != null))
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosException) {
                    CosmosException e = (CosmosException) throwable;
                    if (Exceptions.isNotFound(e)) {
                        logger.info("Lease synchronization document not found");
                        return Mono.just(false);
                    }
                }
                logger.error("Unexpected exception thrown", throwable);
                return Mono.error(throwable);
            });
    }

    @Override
    public Mono<Boolean> markInitialized() {
        String markerDocId = this.getStoreMarkerName();
        InternalObjectNode containerDocument = new InternalObjectNode();
        containerDocument.setId(markerDocId);

        return this.client.createItem(this.leaseCollectionLink, containerDocument, new CosmosItemRequestOptions(), false)
            .map( item -> {
                return true;
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosException) {
                    CosmosException e = (CosmosException) throwable;
                    if (Exceptions.isConflict(e)) {
                        logger.info("Lease synchronization document was created by a different instance");
                        return Mono.just(true);
                    }
                }
                logger.error("Unexpected exception thrown", throwable);
                return Mono.just(false);
            });
    }

    @Override
    public Mono<Boolean> acquireInitializationLock(Duration lockExpirationTime) {
        String lockId = this.getStoreLockName();
        InternalObjectNode containerDocument = new InternalObjectNode();
        containerDocument.setId(lockId);
        BridgeInternal.setProperty(containerDocument, Constants.Properties.TTL, Long.valueOf(lockExpirationTime.getSeconds()).intValue());

        return this.client.createItem(this.leaseCollectionLink, containerDocument, new CosmosItemRequestOptions(), false)
            .map(documentResourceResponse -> {
                if (BridgeInternal.getProperties(documentResourceResponse) != null) {
                    this.lockETag = BridgeInternal.getProperties(documentResourceResponse).getETag();
                    return true;
                } else {
                    return false;
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosException) {
                    CosmosException e = (CosmosException) throwable;
                    if (e.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
                        logger.info("Lease synchronization document was acquired by a different instance");
                        return Mono.just(false);
                    }
                }
                logger.error("Unexpected exception thrown", throwable);
                return Mono.error(throwable);
            });
    }

    @Override
    public Mono<Boolean> releaseInitializationLock() {
        String lockId = this.getStoreLockName();
        InternalObjectNode doc = new InternalObjectNode();
        doc.setId(lockId);

        CosmosItemRequestOptions requestOptions = this.requestOptionsFactory.createItemRequestOptions(
            ServiceItemLeaseV1.fromDocument(doc));

        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }

        requestOptions.setIfMatchETag(this.lockETag);

        return this.client.deleteItem(lockId, new PartitionKey(lockId), requestOptions)
            .map(documentResourceResponse -> {
                if (documentResourceResponse.getItem() != null) {
                    this.lockETag = null;
                    return true;
                } else {
                    return false;
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosException) {
                    CosmosException e = (CosmosException) throwable;
                    if (e.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
                        logger.info("Lease synchronization document was acquired by a different instance");
                        return Mono.just(false);
                    }
                }

                logger.error("Unexpected exception thrown", throwable);
                return Mono.error(throwable);
            });
    }

    private String getStoreMarkerName()
    {
        return this.containerNamePrefix + ".info";
    }

    private String getStoreLockName()
    {
        return this.containerNamePrefix + ".lock";
    }

}
