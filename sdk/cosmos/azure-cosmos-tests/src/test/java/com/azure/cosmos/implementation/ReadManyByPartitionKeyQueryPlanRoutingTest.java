// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.query.DocumentQueryExecutionContextFactory;
import com.azure.cosmos.implementation.query.IDocumentQueryClient;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.SqlQuerySpec;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests that pin the bifurcation of the validation-only QueryPlan request issued by
 * {@code readManyByPartitionKeys} when a custom query is supplied.
 *
 * <p>The fork lives in {@code QueryPlanRetriever.getQueryPlanThroughGatewayAsync}:
 * <pre>queryPlanRequest.useGatewayMode = partitionKeyDefinition == null;</pre>
 *
 * <p>For {@code readManyByPartitionKeys} the {@code PartitionKeyDefinition} is derived from
 * the {@link DocumentCollection} plumbed all the way through
 * {@code validateCustomQueryForReadManyByPartitionKeys} →
 * {@link DocumentQueryExecutionContextFactory#fetchQueryPlanForValidation} → {@code fetchQueryPlan}.
 * These tests assert both halves of that contract:
 * <ul>
 *   <li>collection absent ⇒ request pinned to Gateway V1 (Compute Gateway);</li>
 *   <li>collection present ⇒ request is thin-client eligible (Gateway V2 / proxy) for accounts
 *       and containers configured for thin-client routing — the {@code useGatewayMode=false}
 *       flag is the prerequisite the store-model layer reads to bifurcate.</li>
 * </ul>
 */
public class ReadManyByPartitionKeyQueryPlanRoutingTest {

    @Test(groups = { "unit" })
    public void validationQueryPlanRoutesToGatewayV1WhenCollectionAbsent() {
        ArgumentCaptor<RxDocumentServiceRequest> requestCaptor =
            ArgumentCaptor.forClass(RxDocumentServiceRequest.class);
        IDocumentQueryClient queryClient = mockQueryClient(requestCaptor);

        DocumentQueryExecutionContextFactory
            .fetchQueryPlanForValidation(
                Mockito.mock(DiagnosticsClientContext.class),
                queryClient,
                new SqlQuerySpec("SELECT * FROM c"),
                "dbs/db/colls/col",
                new CosmosQueryRequestOptions(),
                /* collection */ null,
                /* queryPlanCachingEnabled */ false,
                Collections.emptyMap())
            .block();

        assertThat(requestCaptor.getAllValues())
            .as("a single validation query-plan request must be issued")
            .hasSize(1);
        assertThat(requestCaptor.getValue().useGatewayMode)
            .as("validation query-plan must pin to Gateway V1 when no DocumentCollection is plumbed through")
            .isTrue();
    }

    @Test(groups = { "unit" })
    public void validationQueryPlanIsThinClientEligibleWhenCollectionProvided() {
        ArgumentCaptor<RxDocumentServiceRequest> requestCaptor =
            ArgumentCaptor.forClass(RxDocumentServiceRequest.class);
        IDocumentQueryClient queryClient = mockQueryClient(requestCaptor);

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setPaths(Collections.singletonList("/pk"));
        DocumentCollection collection = new DocumentCollection();
        collection.setPartitionKey(partitionKeyDefinition);

        DocumentQueryExecutionContextFactory
            .fetchQueryPlanForValidation(
                Mockito.mock(DiagnosticsClientContext.class),
                queryClient,
                new SqlQuerySpec("SELECT * FROM c"),
                "dbs/db/colls/col",
                new CosmosQueryRequestOptions(),
                collection,
                /* queryPlanCachingEnabled */ false,
                Collections.emptyMap())
            .block();

        assertThat(requestCaptor.getAllValues())
            .as("a single validation query-plan request must be issued")
            .hasSize(1);
        assertThat(requestCaptor.getValue().useGatewayMode)
            .as("validation query-plan must be thin-client eligible (useGatewayMode=false) "
                + "when a DocumentCollection with a PartitionKeyDefinition is plumbed through")
            .isFalse();
    }

    private static IDocumentQueryClient mockQueryClient(ArgumentCaptor<RxDocumentServiceRequest> requestCaptor) {
        IDocumentQueryClient queryClient = Mockito.mock(IDocumentQueryClient.class);
        // executeFeedOperationWithAvailabilityStrategy is a generic <T> method; use doAnswer so the
        // returned Mono.empty() is supplied at invocation time without forcing a generic cast on the
        // stubbing call site. We only care about the captured request, not the downstream payload.
        Mockito
            .doAnswer(invocation -> Mono.empty())
            .when(queryClient)
            .executeFeedOperationWithAvailabilityStrategy(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                requestCaptor.capture(),
                Mockito.any(),
                Mockito.anyString());
        return queryClient;
    }
}
