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
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ChangeFeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.query.Paginator;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.data.cosmos.CommonsBridgeInternal.partitionKeyRangeIdInternal;

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
        

        if (resourceType.isPartitioned() && partitionKeyRangeIdInternal(changeFeedOptions) == null && changeFeedOptions.partitionKey() == null) {
            throw new IllegalArgumentException(RMResources.PartitionKeyRangeIdOrPartitionKeyMustBeSpecified);
        }

        if (changeFeedOptions.partitionKey() != null &&
                !Strings.isNullOrEmpty(partitionKeyRangeIdInternal(changeFeedOptions))) {

            throw new IllegalArgumentException(String.format(
                    RMResources.PartitionKeyAndParitionKeyRangeIdBothSpecified
                    , "feedOptions"));
        }

        String initialNextIfNoneMatch = null;
        
        boolean canUseStartFromBeginning = true;
        if (changeFeedOptions.requestContinuation() != null) {
            initialNextIfNoneMatch = changeFeedOptions.requestContinuation();
            canUseStartFromBeginning = false;
        }

        if(changeFeedOptions.startDateTime() != null){
            canUseStartFromBeginning = false;
        }

        if (canUseStartFromBeginning && !changeFeedOptions.startFromBeginning()) {
            initialNextIfNoneMatch = IfNonMatchAllHeaderValue;
        }

        this.options = getChangeFeedOptions(changeFeedOptions, initialNextIfNoneMatch);
    }

    private RxDocumentServiceRequest createDocumentServiceRequest(String continuationToken, int pageSize) {
        Map<String, String> headers = new HashMap<>();

        if (options.maxItemCount() != null) {
            headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, String.valueOf(options.maxItemCount()));
        }

        // On REST level, change feed is using IF_NONE_MATCH/ETag instead of continuation.
        if(continuationToken != null) {
            headers.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, continuationToken);
        }

        headers.put(HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);

        if (options.partitionKey() != null) {
            PartitionKeyInternal partitionKey = options.partitionKey().getInternalPartitionKey();
            headers.put(HttpConstants.HttpHeaders.PARTITION_KEY, partitionKey.toJson());
        }

        if(options.startDateTime() != null){
            String dateTimeInHttpFormat = Utils.zonedDateTimeAsUTCRFC1123(options.startDateTime());
            headers.put(HttpConstants.HttpHeaders.IF_MODIFIED_SINCE, dateTimeInHttpFormat);
        }

        RxDocumentServiceRequest req = RxDocumentServiceRequest.create(
                OperationType.ReadFeed,
                resourceType,
                documentsLink,
                headers,
                options);

        if (partitionKeyRangeIdInternal(options) != null) {
            req.routeTo(new PartitionKeyRangeIdentity(partitionKeyRangeIdInternal(this.options)));
        }

        return req;
    }

    private ChangeFeedOptions getChangeFeedOptions(ChangeFeedOptions options, String continuationToken) {
        ChangeFeedOptions newOps = new ChangeFeedOptions(options);
        newOps.requestContinuation(continuationToken);
        return newOps;
    }
    
    public Flux<FeedResponse<T>> executeAsync() {

        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc = this::createDocumentServiceRequest;

        Function<RxDocumentServiceRequest, Flux<FeedResponse<T>>> executeFunc = this::executeRequestAsync;

        return Paginator.getPaginatedChangeFeedQueryResultAsObservable(options, createRequestFunc, executeFunc, klass, options.maxItemCount() != null ? options.maxItemCount(): -1);
    }

    private Flux<FeedResponse<T>> executeRequestAsync(RxDocumentServiceRequest request) {
        return client.readFeed(request)
                .map( rsp -> BridgeInternal.toChaneFeedResponsePage(rsp, klass));
    }
}
