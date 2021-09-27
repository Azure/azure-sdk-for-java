// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.SqlQuerySpec;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ReadManySplitTest {
    private DiagnosticsClientContext diagnosticsClientContext;
    private IDocumentQueryClient client;

    @BeforeClass(groups = {"unit"})
    public void beforeClass() {
        diagnosticsClientContext = Mockito.mock(DiagnosticsClientContext.class);
        client = Mockito.mock(IDocumentQueryClient.class);
        when(client.getQueryCompatibilityMode()).thenReturn(IDocumentQueryClient.QueryCompatibilityMode.Query);
    }

    @Test(groups = {"unit"})
    public void requestCreationOnSplitScenario() {
        SqlQuerySpec querySpec = new SqlQuerySpec("Select * from C");
        ParallelDocumentQueryExecutionContextBase<Document> parallelDocumentQueryExecutionContextBase =
            new TestParallelDocumentQueryExecutionContext<Document>(diagnosticsClientContext,
                client,
                ResourceType.Document,
                Document.class,
                querySpec,
                new CosmosQueryRequestOptions(),
                null,
                "Select * from C",
                true,
                true,
                UUID.randomUUID()
            );
        PartitionKeyRange partitionKey = new PartitionKeyRange("0", "00", "FF");
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap = new HashMap<>();
        rangeQueryMap.put(partitionKey, querySpec);
        parallelDocumentQueryExecutionContextBase.initializeReadMany(client, "testCollectionRid", querySpec,
            rangeQueryMap,
            new CosmosQueryRequestOptions(), UUID.randomUUID(), "testCollectionRid");
        //Parent document producer created
        DocumentProducer<Document> documentProducer = parallelDocumentQueryExecutionContextBase.documentProducers.get(0);

        BiFunction<String, Integer, RxDocumentServiceRequest> sourcePartitionCreateRequestFunc =
            (token, maxItemCount) -> documentProducer.createRequestFunc.apply(documentProducer.feedRange, token, maxItemCount);

        RxDocumentServiceRequest serviceRequest = sourcePartitionCreateRequestFunc.apply("null", 1);
        assertThat(serviceRequest.getFeedRange()).isEqualTo(documentProducer.feedRange);

        //Split happens, parent spawn two new document producers
        PartitionKeyRange newPartitionKeyRange1 = new PartitionKeyRange("1", "00", "55555BC");
        PartitionKeyRange newPartitionKeyRange2 = new PartitionKeyRange("2", "55555BC", "FF");
        DocumentProducer<Document> childDocumentProducer1 =
            documentProducer.createChildDocumentProducerOnSplit(newPartitionKeyRange1, null);
        DocumentProducer<Document> childDocumentProducer2 =
            documentProducer.createChildDocumentProducerOnSplit(newPartitionKeyRange2, null);

        sourcePartitionCreateRequestFunc =
            (token, maxItemCount) -> childDocumentProducer1.createRequestFunc.apply(childDocumentProducer1.feedRange, token, maxItemCount);
        serviceRequest = sourcePartitionCreateRequestFunc.apply("null", 1);
        //Verifying new request on childDocumentProducer1 has correct feedRange
        assertThat(serviceRequest.getFeedRange()).isEqualTo(childDocumentProducer1.feedRange);

        sourcePartitionCreateRequestFunc =
            (token, maxItemCount) -> childDocumentProducer2.createRequestFunc.apply(childDocumentProducer2.feedRange, token, maxItemCount);
        serviceRequest = sourcePartitionCreateRequestFunc.apply("null", 1);
        //Verifying new request on childDocumentProducer1 has correct feedRange
        assertThat(serviceRequest.getFeedRange()).isEqualTo(childDocumentProducer2.feedRange);

    }

    private class TestParallelDocumentQueryExecutionContext<T extends Resource> extends ParallelDocumentQueryExecutionContextBase<T> {
        protected TestParallelDocumentQueryExecutionContext(DiagnosticsClientContext diagnosticsClientContext,
                                                            IDocumentQueryClient client,
                                                            ResourceType resourceTypeEnum, Class<T> resourceType,
                                                            SqlQuerySpec query,
                                                            CosmosQueryRequestOptions cosmosQueryRequestOptions,
                                                            String resourceLink, String rewrittenQuery,
                                                            boolean isContinuationExpected,
                                                            boolean getLazyFeedResponse,
                                                            UUID correlatedActivityId) {
            super(diagnosticsClientContext, client, resourceTypeEnum, resourceType, query, cosmosQueryRequestOptions,
                resourceLink, rewrittenQuery, isContinuationExpected, getLazyFeedResponse, correlatedActivityId);
        }

        @Override
        protected DocumentProducer<T> createDocumentProducer(String collectionRid, PartitionKeyRange targetRange,
                                                             String initialContinuationToken, int initialPageSize,
                                                             CosmosQueryRequestOptions cosmosQueryRequestOptions,
                                                             SqlQuerySpec querySpecForInit,
                                                             Map<String, String> commonRequestHeaders,
                                                             TriFunction<FeedRangeEpkImpl, String, Integer,
                                                                 RxDocumentServiceRequest> createRequestFunc,
                                                             Function<RxDocumentServiceRequest,
                                                                 Mono<FeedResponse<T>>> executeFunc,
                                                             Callable<DocumentClientRetryPolicy> createRetryPolicyFunc, FeedRangeEpkImpl feedRange) {
            return new DocumentProducer<T>(client, collectionRid, cosmosQueryRequestOptions, createRequestFunc,
                executeFunc, targetRange, "testCollectionLink", createRetryPolicyFunc, resourceType,
                correlatedActivityId, -1, initialContinuationToken, -1, feedRange);
        }

        @Override
        public Flux<FeedResponse<T>> drainAsync(int maxPageSize) {
            return null;
        }

        @Override
        public Flux<FeedResponse<T>> executeAsync() {
            return null;
        }
    }

}
