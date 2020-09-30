// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.query.Paginator;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

class ChangeFeedQueryImpl<T extends Resource> {

    private static final String IfNonMatchAllHeaderValue = "*";
    private final RxDocumentClientImpl client;
    private final DiagnosticsClientContext clientContext;
    private final ResourceType resourceType;
    private final Class<T> klass;
    private final String documentsLink;
    private final ChangeFeedOptions options;

    public ChangeFeedQueryImpl(RxDocumentClientImpl client,
            ResourceType resourceType,
            Class<T> klass,
            String collectionLink,
            ChangeFeedOptions changeFeedOptions) {
        this.clientContext = client;
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

        if(changeFeedOptions.getStartDateTime() != null){
            canUseStartFromBeginning = false;
        }

        if (canUseStartFromBeginning && !changeFeedOptions.isStartFromBeginning()) {
            initialNextIfNoneMatch = IfNonMatchAllHeaderValue;
        }

        this.options = getChangeFeedOptions(changeFeedOptions, initialNextIfNoneMatch);
    }

    private RxDocumentServiceRequest createDocumentServiceRequest(String continuationToken, int pageSize) {
        Map<String, String> headers = new HashMap<>();
        RxDocumentServiceRequest req = RxDocumentServiceRequest.create(clientContext,
            OperationType.ReadFeed,
            resourceType,
            documentsLink,
            headers,
            options);

        if (options.getMaxItemCount() != null) {
            headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, String.valueOf(options.getMaxItemCount()));
        }

        // On REST level, change feed is using IF_NONE_MATCH/ETag instead of continuation.
        if(continuationToken != null) {
            headers.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, continuationToken);
        }

        headers.put(HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);

        if (options.getPartitionKey() != null) {
            PartitionKeyInternal partitionKey = BridgeInternal.getPartitionKeyInternal(options.getPartitionKey());
            headers.put(HttpConstants.HttpHeaders.PARTITION_KEY, partitionKey.toJson());
            req.setPartitionKeyInternal(partitionKey);
        }

        if(options.getStartDateTime() != null){
            String dateTimeInHttpFormat = Utils.zonedDateTimeAsUTCRFC1123(options.getStartDateTime().atOffset(ZoneOffset.UTC));
            headers.put(HttpConstants.HttpHeaders.IF_MODIFIED_SINCE, dateTimeInHttpFormat);
        }

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

    public Flux<FeedResponse<T>> executeAsync() {

        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc = this::createDocumentServiceRequest;

        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc = this::executeRequestAsync;

        return Paginator.getPaginatedChangeFeedQueryResultAsObservable(options, createRequestFunc, executeFunc, klass, options.getMaxItemCount() != null ? options.getMaxItemCount(): -1);
    }

    private Mono<FeedResponse<T>> executeRequestAsync(RxDocumentServiceRequest request) {
        return client.readFeed(request)
                .map( rsp -> BridgeInternal.toChangeFeedResponsePage(rsp, klass));
    }
}
