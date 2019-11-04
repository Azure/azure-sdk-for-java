// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.AccessCondition;
import com.azure.data.cosmos.AccessConditionType;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.internal.changefeed.LeaseStore;
import com.azure.data.cosmos.internal.changefeed.RequestOptionsFactory;
import com.azure.data.cosmos.internal.changefeed.ServiceItemLease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation for LeaseStore.
 */
class DocumentServiceLeaseStore implements LeaseStore {
    private final Logger logger = LoggerFactory.getLogger(BootstrapperImpl.class);
    private ChangeFeedContextClient client;
    private String containerNamePrefix;
    private CosmosContainer leaseCollectionLink;
    private RequestOptionsFactory requestOptionsFactory;
    private volatile String lockETag;

    //  TODO: rename to LeaseStoreImpl
    public DocumentServiceLeaseStore(
            ChangeFeedContextClient client,
            String containerNamePrefix,
            CosmosContainer leaseCollectionLink,
            RequestOptionsFactory requestOptionsFactory) {

        this.client = client;
        this.containerNamePrefix = containerNamePrefix;
        this.leaseCollectionLink = leaseCollectionLink;
        this.requestOptionsFactory = requestOptionsFactory;
    }

    @Override
    public Mono<Boolean> isInitialized() {
        String markerDocId = this.getStoreMarkerName();

        CosmosItemProperties doc = new CosmosItemProperties();
        doc.id(markerDocId);

        CosmosItemRequestOptions requestOptions = this.requestOptionsFactory.createRequestOptions(
            ServiceItemLease.fromDocument(doc));

        CosmosItem docItem = this.client.getContainerClient().getItem(markerDocId, markerDocId);
        return this.client.readItem(docItem, requestOptions)
            .flatMap(documentResourceResponse -> Mono.just(documentResourceResponse.item() != null))
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
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
        CosmosItemProperties containerDocument = new CosmosItemProperties();
        containerDocument.id(markerDocId);

        return this.client.createItem(this.leaseCollectionLink, containerDocument, new CosmosItemRequestOptions(markerDocId), false)
            .map( item -> true)
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
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
        CosmosItemProperties containerDocument = new CosmosItemProperties();
        containerDocument.id(lockId);
        BridgeInternal.setProperty(containerDocument, com.azure.data.cosmos.internal.Constants.Properties.TTL, Long.valueOf(lockExpirationTime.getSeconds()).intValue());

        return this.client.createItem(this.leaseCollectionLink, containerDocument, new CosmosItemRequestOptions(lockId), false)
            .map(documentResourceResponse -> {
                if (documentResourceResponse.item() != null) {
                    this.lockETag = documentResourceResponse.properties().etag();
                    return true;
                } else {
                    return false;
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
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
        CosmosItemProperties doc = new CosmosItemProperties();
        doc.id(lockId);

        CosmosItemRequestOptions requestOptions = this.requestOptionsFactory.createRequestOptions(
            ServiceItemLease.fromDocument(doc));

        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }

        AccessCondition accessCondition = new AccessCondition();
        accessCondition.type(AccessConditionType.IF_MATCH);
        accessCondition.condition(this.lockETag);
        requestOptions.accessCondition(accessCondition);

        CosmosItem docItem = this.client.getContainerClient().getItem(lockId, lockId);
        return this.client.deleteItem(docItem, requestOptions)
            .map(documentResourceResponse -> {
                if (documentResourceResponse.item() != null) {
                    this.lockETag = null;
                    return true;
                } else {
                    return false;
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
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
