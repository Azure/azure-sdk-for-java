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
package com.azure.data.cosmos.internal.query;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;

import com.azure.data.cosmos.internal.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKeyRange;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.Utils;

import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 * 
 * This class is used as a proxy to wrap the
 * DefaultDocumentQueryExecutionContext which is needed for sending the query to
 * GATEWAY first and then uses PipelinedDocumentQueryExecutionContext after it
 * gets the necessary info.
 */
public class ProxyDocumentQueryExecutionContext<T extends Resource> implements IDocumentQueryExecutionContext<T> {

    private IDocumentQueryExecutionContext<T> innerExecutionContext;
    private IDocumentQueryClient client;
    private ResourceType resourceTypeEnum;
    private Class<T> resourceType;
    private FeedOptions feedOptions;
    private SqlQuerySpec query;
    private String resourceLink;
    private DocumentCollection collection;
    private UUID correlatedActivityId;
    private boolean isContinuationExpected;
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ProxyDocumentQueryExecutionContext(
            IDocumentQueryExecutionContext<T> innerExecutionContext,
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            Class<T> resourceType,
            SqlQuerySpec query,
            FeedOptions feedOptions,
            String resourceLink,
            DocumentCollection collection,
            boolean isContinuationExpected,
            UUID correlatedActivityId) {
        this.innerExecutionContext = innerExecutionContext;

        this.client = client;
        this.resourceTypeEnum = resourceTypeEnum;
        this.resourceType = resourceType;
        this.query = query;
        this.feedOptions = feedOptions;
        this.resourceLink = resourceLink;

        this.collection = collection;
        this.isContinuationExpected = isContinuationExpected;
        this.correlatedActivityId = correlatedActivityId;
    }

    @Override
    public Observable<FeedResponse<T>> executeAsync() {

        Func1<? super Throwable, ? extends Observable<? extends FeedResponse<T>>> func  = t -> { 

            logger.debug("Received non result message from gateway", t);
            if (!(t instanceof Exception)) {
                logger.error("Unexpected failure", t);
                return Observable.error(t);
            }
            
            if (!isCrossPartitionQuery((Exception) t)) {
                // If this is not a cross partition query then propagate error
                logger.debug("Failure from gateway", t);
                return Observable.error(t);
            }

            logger.debug("Setting up query pipeline using the query plan received form gateway");

            // cross partition query construct pipeline

            CosmosClientException dce = (CosmosClientException) t;

            PartitionedQueryExecutionInfo partitionedQueryExecutionInfo = new
                    PartitionedQueryExecutionInfo(dce.error().getPartitionedQueryExecutionInfo());

            logger.debug("Query Plan from gateway {}", partitionedQueryExecutionInfo);

            DefaultDocumentQueryExecutionContext<T> queryExecutionContext =
                    (DefaultDocumentQueryExecutionContext<T>) this.innerExecutionContext;

            Single<List<PartitionKeyRange>> partitionKeyRanges = queryExecutionContext.getTargetPartitionKeyRanges(collection.resourceId(),
                    partitionedQueryExecutionInfo.getQueryRanges());

            Observable<IDocumentQueryExecutionContext<T>> exContext = partitionKeyRanges.toObservable()
                    .flatMap(pkranges -> {
                        return DocumentQueryExecutionContextFactory.createSpecializedDocumentQueryExecutionContextAsync(
                                this.client,
                                this.resourceTypeEnum,
                                this.resourceType,
                                this.query,
                                this.feedOptions,
                                this.resourceLink,
                                isContinuationExpected,
                                partitionedQueryExecutionInfo,
                                pkranges,
                                this.collection.resourceId(),
                                this.correlatedActivityId);
                        });

            return exContext.flatMap(context -> context.executeAsync());
        };

        return this.innerExecutionContext.executeAsync().onErrorResumeNext(func);
    }

    private boolean isCrossPartitionQuery(Exception exception) {

        CosmosClientException clientException = Utils.as(exception, CosmosClientException.class);

        if (clientException == null) {
            return false;
        }

        return (Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.BADREQUEST) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.CROSS_PARTITION_QUERY_NOT_SERVABLE));
    }

    public static <T extends Resource> Observable<ProxyDocumentQueryExecutionContext<T>> createAsync(IDocumentQueryClient client,
            ResourceType resourceTypeEnum, Class<T> resourceType, SqlQuerySpec query, FeedOptions feedOptions,
            String resourceLink, DocumentCollection collection, boolean isContinuationExpected,
            UUID correlatedActivityId) {

        IDocumentQueryExecutionContext<T> innerExecutionContext =
                new DefaultDocumentQueryExecutionContext<T>(
                        client,
                        resourceTypeEnum,
                        resourceType,
                        query,
                        feedOptions,
                        resourceLink,
                        correlatedActivityId,
                        isContinuationExpected);

        return Observable.just(new ProxyDocumentQueryExecutionContext<T>(innerExecutionContext, client,
                resourceTypeEnum,
                resourceType,
                query,
                feedOptions,
                resourceLink,
                collection,
                isContinuationExpected,
                correlatedActivityId));        
    }
}
