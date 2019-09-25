// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
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
        

        if (resourceType.isPartitioned() && partitionKeyRangeIdInternal(changeFeedOptions) == null && changeFeedOptions.getPartitionKey() == null) {
            throw new IllegalArgumentException(RMResources.PartitionKeyRangeIdOrPartitionKeyMustBeSpecified);
        }

        if (changeFeedOptions.getPartitionKey() != null &&
                !Strings.isNullOrEmpty(partitionKeyRangeIdInternal(changeFeedOptions))) {

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

        if(changeFeedOptions.getStartDateTime() != null){
            canUseStartFromBeginning = false;
        }

        if (canUseStartFromBeginning && !changeFeedOptions.getStartFromBeginning()) {
            initialNextIfNoneMatch = IfNonMatchAllHeaderValue;
        }

        this.options = getChangeFeedOptions(changeFeedOptions, initialNextIfNoneMatch);
    }

    private RxDocumentServiceRequest createDocumentServiceRequest(String continuationToken, int pageSize) {
        Map<String, String> headers = new HashMap<>();

        if (options.getMaxItemCount() != null) {
            headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, String.valueOf(options.getMaxItemCount()));
        }

        // On REST level, change feed is using IF_NONE_MATCH/ETag instead of continuation.
        if(continuationToken != null) {
            headers.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, continuationToken);
        }

        headers.put(HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);

        if (options.getPartitionKey() != null) {
            PartitionKeyInternal partitionKey = options.getPartitionKey().getInternalPartitionKey();
            headers.put(HttpConstants.HttpHeaders.PARTITION_KEY, partitionKey.toJson());
        }

        if(options.getStartDateTime() != null){
            String dateTimeInHttpFormat = Utils.zonedDateTimeAsUTCRFC1123(options.getStartDateTime());
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
        newOps.setRequestContinuation(continuationToken);
        return newOps;
    }
    
    public Flux<FeedResponse<T>> executeAsync() {

        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc = this::createDocumentServiceRequest;

        Function<RxDocumentServiceRequest, Flux<FeedResponse<T>>> executeFunc = this::executeRequestAsync;

        return Paginator.getPaginatedChangeFeedQueryResultAsObservable(options, createRequestFunc, executeFunc, klass, options.getMaxItemCount() != null ? options.getMaxItemCount(): -1);
    }

    private Flux<FeedResponse<T>> executeRequestAsync(RxDocumentServiceRequest request) {
        return client.readFeed(request)
                .map( rsp -> BridgeInternal.toChaneFeedResponsePage(rsp, klass));
    }
}
