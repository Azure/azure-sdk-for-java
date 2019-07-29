// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.Exceptions;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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
    public Flux<FeedResponse<T>> executeAsync() {

        Function<? super Throwable, ? extends Flux<? extends FeedResponse<T>>> func  = t -> {

            logger.debug("Received non result message from gateway", t);
            if (!(t instanceof Exception)) {
                logger.error("Unexpected failure", t);
                return Flux.error(t);
            }
            
            if (!isCrossPartitionQuery((Exception) t)) {
                // If this is not a cross partition query then propagate error
                logger.debug("Failure from gateway", t);
                return Flux.error(t);
            }

            logger.debug("Setting up query pipeline using the query plan received form gateway");

            // cross partition query construct pipeline

            CosmosClientException dce = (CosmosClientException) t;

            PartitionedQueryExecutionInfo partitionedQueryExecutionInfo = new
                    PartitionedQueryExecutionInfo(dce.error().getPartitionedQueryExecutionInfo());

            logger.debug("Query Plan from gateway {}", partitionedQueryExecutionInfo);

            DefaultDocumentQueryExecutionContext<T> queryExecutionContext =
                    (DefaultDocumentQueryExecutionContext<T>) this.innerExecutionContext;

            Mono<List<PartitionKeyRange>> partitionKeyRanges = queryExecutionContext.getTargetPartitionKeyRanges(collection.resourceId(),
                    partitionedQueryExecutionInfo.getQueryRanges());

            Flux<IDocumentQueryExecutionContext<T>> exContext = partitionKeyRanges.flux()
                    .flatMap(pkranges -> DocumentQueryExecutionContextFactory.createSpecializedDocumentQueryExecutionContextAsync(
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
                            this.correlatedActivityId));

            return exContext.flatMap(IDocumentQueryExecutionContext::executeAsync);
        };

        return this.innerExecutionContext.executeAsync().onErrorResume(func);
    }

    private boolean isCrossPartitionQuery(Exception exception) {

        CosmosClientException clientException = Utils.as(exception, CosmosClientException.class);

        if (clientException == null) {
            return false;
        }

        return (Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.BADREQUEST) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.CROSS_PARTITION_QUERY_NOT_SERVABLE));
    }

    public static <T extends Resource> Flux<ProxyDocumentQueryExecutionContext<T>> createAsync(IDocumentQueryClient client,
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

        return Flux.just(new ProxyDocumentQueryExecutionContext<T>(innerExecutionContext, client,
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
