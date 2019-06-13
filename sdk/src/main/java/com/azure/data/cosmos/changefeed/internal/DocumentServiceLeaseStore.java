/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.changefeed.internal;

import com.azure.data.cosmos.AccessCondition;
import com.azure.data.cosmos.AccessConditionType;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.Document;
import com.azure.data.cosmos.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.changefeed.LeaseStore;
import com.azure.data.cosmos.changefeed.RequestOptionsFactory;
import com.azure.data.cosmos.changefeed.ServiceItemLease;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation for LeaseStore.
 */
public class DocumentServiceLeaseStore implements LeaseStore {
    private ChangeFeedContextClient client;
    private String containerNamePrefix;
    private CosmosContainer leaseCollectionLink;
    private RequestOptionsFactory requestOptionsFactory;
    private String lockETag;

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

        CosmosItem docItem = this.client.getContainerClient().getItem(markerDocId, "/id");
        return this.client.readItem(docItem, requestOptions)
            .flatMap(documentResourceResponse -> Mono.just(documentResourceResponse.item() != null))
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
                        return Mono.just(false);
                    }
                }
                Mono.error(throwable);
                return Mono.empty();
            });
    }

    @Override
    public Mono<Boolean> markInitialized() {
        String markerDocId = this.getStoreMarkerName();
        CosmosItemProperties containerDocument = new CosmosItemProperties();
        containerDocument.id(markerDocId);

        return this.client.createItem(this.leaseCollectionLink, containerDocument, null, false)
            .map( item -> true)
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
                        return Mono.just(true);
                    }
                }
                return Mono.just(false);
            });
    }

    @Override
    public Mono<Boolean> acquireInitializationLock(Duration lockExpirationTime) {
        String lockId = this.getStoreLockName();
        Document containerDocument = new Document();
        containerDocument.id(lockId);
        containerDocument.setTimeToLive(Long.valueOf(lockExpirationTime.getSeconds()).intValue());
        DocumentServiceLeaseStore self = this;

        return this.client.createItem(this.leaseCollectionLink, containerDocument, null, false)
            .map(documentResourceResponse -> {
                if (documentResourceResponse.item() != null) {
                    self.lockETag = documentResourceResponse.properties().etag();
                    return true;
                } else {
                    return false;
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
                        return Mono.just(false);
                    }
                }
                Mono.error(throwable);
                return Mono.empty();
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
        DocumentServiceLeaseStore self = this;

        CosmosItem docItem = this.client.getContainerClient().getItem(lockId, "/id");
        return this.client.deleteItem(docItem, requestOptions)
            .map(documentResourceResponse -> {
                if (documentResourceResponse.item() != null) {
                    self.lockETag = null;
                    return true;
                } else {
                    return false;
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) throwable;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
                        return Mono.just(false);
                    }
                }

                Mono.error(throwable);
                return Mono.empty();
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
