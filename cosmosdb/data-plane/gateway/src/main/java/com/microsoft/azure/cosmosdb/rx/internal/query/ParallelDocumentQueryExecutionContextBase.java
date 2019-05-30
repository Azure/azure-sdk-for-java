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
package com.microsoft.azure.cosmosdb.rx.internal.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.routing.Range;
import com.microsoft.azure.cosmosdb.rx.internal.IDocumentClientRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func3;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public abstract class ParallelDocumentQueryExecutionContextBase<T extends Resource>
        extends DocumentQueryExecutionContextBase<T> implements IDocumentQueryExecutionComponent<T> {

    protected final List<DocumentProducer<T>> documentProducers;
    protected final List<PartitionKeyRange> partitionKeyRanges;
    protected final SqlQuerySpec querySpec;
    protected int pageSize;
    protected int top = -1;

    protected ParallelDocumentQueryExecutionContextBase(IDocumentQueryClient client,
            List<PartitionKeyRange> partitionKeyRanges, ResourceType resourceTypeEnum, Class<T> resourceType,
            SqlQuerySpec query, FeedOptions feedOptions, String resourceLink, String rewrittenQuery,
            boolean isContinuationExpected, boolean getLazyFeedResponse, UUID correlatedActivityId) {
        super(client, resourceTypeEnum, resourceType, query, feedOptions, resourceLink, getLazyFeedResponse,
                correlatedActivityId);

        documentProducers = new ArrayList<>();

        this.partitionKeyRanges = partitionKeyRanges;

        if (!Strings.isNullOrEmpty(rewrittenQuery)) {
            this.querySpec = new SqlQuerySpec(rewrittenQuery, super.query.getParameters());
        } else {
            this.querySpec = super.query;
        }
    }

    protected void initialize(String collectionRid,
            Map<PartitionKeyRange, String> partitionKeyRangeToContinuationTokenMap, int initialPageSize,
            SqlQuerySpec querySpecForInit) {
        this.pageSize = initialPageSize;
        Map<String, String> commonRequestHeaders = createCommonHeadersAsync(this.getFeedOptions(null, null));

        for (PartitionKeyRange targetRange : partitionKeyRangeToContinuationTokenMap.keySet()) {
            Func3<PartitionKeyRange, String, Integer, RxDocumentServiceRequest> createRequestFunc = (partitionKeyRange,
                    continuationToken, pageSize) -> {
                Map<String, String> headers = new HashMap<>(commonRequestHeaders);
                headers.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
                headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, Strings.toString(pageSize));
                return this.createDocumentServiceRequest(headers, querySpecForInit, partitionKeyRange, collectionRid);
            };

            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc = (request) -> {
                return this.executeRequestAsync(request).toObservable();
            };

            DocumentProducer<T> dp = createDocumentProducer(collectionRid, targetRange,
                    partitionKeyRangeToContinuationTokenMap.get(targetRange), initialPageSize, feedOptions,
                    querySpecForInit, commonRequestHeaders, createRequestFunc, executeFunc,
                    () -> client.getResetSessionTokenRetryPolicy().getRequestPolicy());

            documentProducers.add(dp);
        }
    }

    protected <TContinuationToken> int FindTargetRangeAndExtractContinuationTokens(
            List<PartitionKeyRange> partitionKeyRanges, Range<String> range) throws DocumentClientException {
        if (partitionKeyRanges == null) {
            throw new IllegalArgumentException("partitionKeyRanges can not be null.");
        }

        if (partitionKeyRanges.size() < 1) {
            throw new IllegalArgumentException("partitionKeyRanges must have atleast one element.");
        }

        for (PartitionKeyRange partitionKeyRange : partitionKeyRanges) {
            if (partitionKeyRange == null) {
                throw new IllegalArgumentException("partitionKeyRanges can not have null elements.");
            }
        }

        // Find the minimum index.
        PartitionKeyRange needle = new PartitionKeyRange(/* id */ null, range.getMin(), range.getMax());
        int minIndex;
        for (minIndex = 0; minIndex < partitionKeyRanges.size(); minIndex++) {
            if (needle.getMinInclusive().equals(partitionKeyRanges.get(minIndex).getMinInclusive())) {
                break;
            }
        }

        if (minIndex == partitionKeyRanges.size()) {
            throw new DocumentClientException(HttpConstants.StatusCodes.BADREQUEST,
                    String.format("Could not find partition key range for continuation token: {0}", needle));
        }

        return minIndex;
    }

    abstract protected DocumentProducer<T> createDocumentProducer(String collectionRid, PartitionKeyRange targetRange,
            String initialContinuationToken, int initialPageSize, FeedOptions feedOptions, SqlQuerySpec querySpecForInit,
            Map<String, String> commonRequestHeaders,
            Func3<PartitionKeyRange, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc,
            Func0<IDocumentClientRetryPolicy> createRetryPolicyFunc);

    @Override
    abstract public Observable<FeedResponse<T>> drainAsync(int maxPageSize);

    public void setTop(int newTop) {
        this.top = newTop;

        for (DocumentProducer<T> producer : this.documentProducers) {
            producer.top = newTop;
        }
    }
}
