// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncContainer;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.internal.changefeed.LeaseStore;
import com.azure.data.cosmos.internal.changefeed.RequestOptionsFactory;
import com.azure.data.cosmos.internal.changefeed.ServiceItemLease;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation for LeaseStore.
 */
class DocumentServiceLeaseStore implements LeaseStore {
    private ChangeFeedContextClient client;
    private String containerNamePrefix;
    private CosmosAsyncContainer leaseCollectionLink;
    private RequestOptionsFactory requestOptionsFactory;
    private volatile String lockETag;

    //  TODO: rename to LeaseStoreImpl
    public DocumentServiceLeaseStore(
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

        CosmosItemProperties doc = new CosmosItemProperties();
        doc.setId(markerDocId);

        CosmosItemRequestOptions requestOptions = this.requestOptionsFactory.createRequestOptions(
            ServiceItemLease.fromDocument(doc));

        CosmosAsyncItem docItem = this.client.getContainerClient().getItem(markerDocId, "/id");
        return this.client.readItem(docItem, requestOptions)
            .flatMap(documentResourceResponse -> Mono.just(documentResourceResponse.getItem() != null))
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
                        return Mono.just(false);
                    }
                }
                return Mono.error(throwable);
            });
    }

    @Override
    public Mono<Boolean> markInitialized() {
        String markerDocId = this.getStoreMarkerName();
        CosmosItemProperties containerDocument = new CosmosItemProperties();
        containerDocument.setId(markerDocId);

        return this.client.createItem(this.leaseCollectionLink, containerDocument, null, false)
            .map( item -> true)
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
                        return Mono.just(true);
                    }
                }
                return Mono.just(false);
            });
    }

    @Override
    public Mono<Boolean> acquireInitializationLock(Duration lockExpirationTime) {
        String lockId = this.getStoreLockName();
        CosmosItemProperties containerDocument = new CosmosItemProperties();
        containerDocument.setId(lockId);
        BridgeInternal.setProperty(containerDocument, com.azure.data.cosmos.internal.Constants.Properties.TTL, Long.valueOf(lockExpirationTime.getSeconds()).intValue());

        return this.client.createItem(this.leaseCollectionLink, containerDocument, null, false)
            .map(documentResourceResponse -> {
                if (documentResourceResponse.getItem() != null) {
                    this.lockETag = documentResourceResponse.getProperties().getETag();
                    return true;
                } else {
                    return false;
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
                        return Mono.just(false);
                    }
                }
                return Mono.error(throwable);
            });
    }

    @Override
    public Mono<Boolean> releaseInitializationLock() {
        String lockId = this.getStoreLockName();
        CosmosItemProperties doc = new CosmosItemProperties();
        doc.setId(lockId);

        CosmosItemRequestOptions requestOptions = this.requestOptionsFactory.createRequestOptions(
            ServiceItemLease.fromDocument(doc));

        if (requestOptions == null) {
            requestOptions = new CosmosItemRequestOptions();
        }

        AccessCondition accessCondition = new AccessCondition();
        accessCondition.setType(AccessConditionType.IF_MATCH);
        accessCondition.setCondition(this.lockETag);
        requestOptions.setAccessCondition(accessCondition);

        CosmosAsyncItem docItem = this.client.getContainerClient().getItem(lockId, "/id");
        return this.client.deleteItem(docItem, requestOptions)
            .map(documentResourceResponse -> {
                if (documentResourceResponse.getItem() != null) {
                    this.lockETag = null;
                    return true;
                } else {
                    return false;
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
                        return Mono.just(false);
                    }
                }

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
