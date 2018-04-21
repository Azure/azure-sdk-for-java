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
package com.microsoft.azure.cosmosdb.rx.internal;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.ChangeFeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.Paths;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.Utils;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyInternal;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyRangeIdentity;
import com.microsoft.azure.cosmosdb.rx.internal.query.Paginator;

import rx.Observable;
import rx.Single;
import rx.functions.Func1;
import rx.functions.Func2;

class ChangeFeedQueryImpl<T extends Resource> {

    private static final String IfNonMatchAllHeaderValue = "*";
    private final RxDocumentClientImpl client;
    private final ResourceType resourceType;
    private final Class<T> klass;
    private final String documentsLink;
    private final ChangeFeedOptions options;

    public ChangeFeedQueryImpl(RxDocumentClientImpl client, 
            ResourceType resourceType, 
            Class<T> klass,
            String collectionLink,
            ChangeFeedOptions changeFeedOptions) {

        this.client = client;
        this.resourceType = resourceType;
        this.klass = klass;
        this.documentsLink = Utils.joinPath(collectionLink, Paths.DOCUMENTS_PATH_SEGMENT);
        changeFeedOptions = changeFeedOptions != null ? changeFeedOptions: new ChangeFeedOptions();
        

        if (resourceType.isPartitioned() && changeFeedOptions.getPartitionKeyRangeId() == null && changeFeedOptions.getPartitionKey() == null) {
            throw new IllegalArgumentException(RMResources.PartitionKeyRangeIdOrPartitionKeyMustBeSpecified);
        }

        if (changeFeedOptions.getPartitionKey() != null && 
                !Strings.isNullOrEmpty(changeFeedOptions.getPartitionKeyRangeId())) {

            throw new IllegalArgumentException(String.format(
                    RMResources.PartitionKeyAndParitionKeyRangeIdBothSpecified
                    , "feedOptions"));
        }

        String initialNextIfNoneMatch = null;
        
        boolean canUseStartFromBeginning = true;
        if (changeFeedOptions.getRequestContinuation() != null) {
            initialNextIfNoneMatch = changeFeedOptions.getRequestContinuation();
            canUseStartFromBeginning = false;
        }

        if (canUseStartFromBeginning && !changeFeedOptions.isStartFromBeginning()) {
            initialNextIfNoneMatch = IfNonMatchAllHeaderValue;
        }

        this.options = getChangeFeedOptions(changeFeedOptions, initialNextIfNoneMatch);
    }

    private RxDocumentServiceRequest createDocumentServiceRequest(String continuationToken, int pageSize) {
        Map<String, String> headers = new HashMap<>();

        if (options.getMaxItemCount() != null) {
            headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, String.valueOf(options.getMaxItemCount()));
        }

        // On REST level, change feed is using IfNoneMatch/ETag instead of continuation.
        if(continuationToken != null) {
            headers.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, continuationToken);
        }

        headers.put(HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);

        if (options.getPartitionKey() != null) {
            PartitionKeyInternal partitionKey = options.getPartitionKey().getInternalPartitionKey();
            headers.put(HttpConstants.HttpHeaders.PARTITION_KEY, partitionKey.toJson());
        }

        RxDocumentServiceRequest req = RxDocumentServiceRequest.create(
                OperationType.ReadFeed,
                resourceType,
                documentsLink,
                headers);

        if (options.getPartitionKeyRangeId() != null) {
            req.routeTo(new PartitionKeyRangeIdentity(this.options.getPartitionKeyRangeId()));
        }

        return req;
    }

    private ChangeFeedOptions getChangeFeedOptions(ChangeFeedOptions options, String continuationToken) {
        ChangeFeedOptions newOps = new ChangeFeedOptions(options);
        newOps.setRequestContinuation(continuationToken);
        return newOps;
    }
    
    public Observable<FeedResponse<T>> executeAsync() {

        Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc = (continuationToken, pageSize) -> this.createDocumentServiceRequest(continuationToken, pageSize);

        // TODO: clean up if we want to use single vs observable.
        Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc = request -> this.executeRequestAsync(request).toObservable();

        return Paginator.getPaginatedChangeFeedQueryResultAsObservable(options, createRequestFunc, executeFunc, klass, options.getMaxItemCount() != null ? options.getMaxItemCount(): -1);
    }

    private Single<FeedResponse<T>> executeRequestAsync(RxDocumentServiceRequest request) {
        return client.readFeed(request).toSingle()
                .map( rsp -> BridgeInternal.toChaneFeedResponsePage(rsp, klass));
    }
}
