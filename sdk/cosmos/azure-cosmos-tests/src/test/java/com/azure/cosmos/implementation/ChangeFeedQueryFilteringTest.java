// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.http.HttpClient;
import io.netty.handler.codec.http.HttpMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangeFeedQueryFilteringTest {
    @DataProvider(name = "modesAndRanges")
    public Object[][] modesAndRanges() {
        return new Object[][] {
            { ChangeFeedMode.INCREMENTAL, FeedRangeEpkImpl.forFullRange() },
            { ChangeFeedMode.INCREMENTAL, new FeedRangeEpkImpl(new Range<>("AA", "BB", true, false)) },
            { ChangeFeedMode.INCREMENTAL, FeedRange.forLogicalPartition(new PartitionKey("tenant")) },
            { ChangeFeedMode.INCREMENTAL, new FeedRangePartitionKeyRangeImpl("7") },
            { ChangeFeedMode.FULL_FIDELITY, FeedRangeEpkImpl.forFullRange() },
            { ChangeFeedMode.FULL_FIDELITY, new FeedRangeEpkImpl(new Range<>("AA", "BB", true, false)) },
            { ChangeFeedMode.FULL_FIDELITY, FeedRange.forLogicalPartition(new PartitionKey("tenant")) },
            { ChangeFeedMode.FULL_FIDELITY, new FeedRangePartitionKeyRangeImpl("7") }
        };
    }

    @Test(groups = "unit", dataProvider = "modesAndRanges")
    public void filteredRequestPreservesModeAndRouting(ChangeFeedMode mode, FeedRange feedRange) throws Exception {
        String sql = "SELECT * FROM c WHERE c.state = \"WA\" AND c.name = 'a\\\\b'";
        ChangeFeedStateV1 state = new ChangeFeedStateV1("rid", (FeedRangeInternal) feedRange, mode,
            ChangeFeedStartFromInternal.createFromNow(), null);
        CosmosChangeFeedRequestOptions options = ModelBridgeInternal.createChangeFeedRequestOptionsForChangeFeedState(state);
        options.setQuery(sql);
        options.setQuotaInfoEnabled(true);
        ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor()
            .setHeader(options, "x-ms-custom-cf-option", "preserved");

        RxDocumentClientImpl client = mock(RxDocumentClientImpl.class);
        ChangeFeedQueryImpl<Document> query = createQuery(client, options);
        RxDocumentServiceRequest request = query.createDocumentServiceRequest();
        state.populateRequest(request, 25);
        populateRouting((FeedRangeInternal) feedRange, request);

        assertThat(request.getOperationType()).isEqualTo(OperationType.Query);
        assertThat(request.getHeaders()).containsEntry(HttpConstants.HttpHeaders.IS_QUERY, "true")
            .containsEntry(HttpConstants.HttpHeaders.CONTENT_TYPE, RuntimeConstants.MediaTypes.QUERY_JSON)
            .containsEntry(HttpConstants.HttpHeaders.PAGE_SIZE, "25")
            .containsEntry(HttpConstants.HttpHeaders.POPULATE_QUOTA_INFO, "true")
            .containsEntry("x-ms-custom-cf-option", "preserved")
            .containsEntry(HttpConstants.HttpHeaders.A_IM, mode == ChangeFeedMode.INCREMENTAL
                ? HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED : HttpConstants.A_IMHeaderValues.FULL_FIDELITY_FEED);
        assertThat(Utils.getSimpleObjectMapper().readTree(request.getContentAsByteArray()).size()).isEqualTo(1);
        assertThat(Utils.getSimpleObjectMapper().readTree(request.getContentAsByteArray()).get("query").asText())
            .isEqualTo(sql);

        options.setQuery(null);
        RxDocumentServiceRequest unfiltered = createQuery(client, options).createDocumentServiceRequest();
        state.populateRequest(unfiltered, 25);
        populateRouting((FeedRangeInternal) feedRange, unfiltered);
        for (Map.Entry<String, String> header : unfiltered.getHeaders().entrySet()) {
            assertThat(request.getHeaders()).containsEntry(header.getKey(), header.getValue());
        }
        assertThat(request.getFeedRange()).isEqualTo(unfiltered.getFeedRange());
        assertThat(request.getEffectiveRange()).isNotNull().isEqualTo(unfiltered.getEffectiveRange());
        assertThat(request.getPartitionKeyRangeIdentity()).isEqualTo(unfiltered.getPartitionKeyRangeIdentity());
        assertThat(request.useGatewayMode).isEqualTo(unfiltered.useGatewayMode);
        if (feedRange instanceof FeedRangeEpkImpl
            && !FeedRangeEpkImpl.forFullRange().equals(feedRange)) {
            assertThat(request.getHeaders()).containsEntry(HttpConstants.HttpHeaders.START_EPK, "AA")
                .containsEntry(HttpConstants.HttpHeaders.END_EPK, "BB")
                .containsEntry(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID, "7");
        } else if (feedRange instanceof FeedRangePartitionKeyRangeImpl) {
            assertThat(request.getHeaders()).containsEntry(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID, "7");
        } else if (!(feedRange instanceof FeedRangeEpkImpl)) {
            assertThat(request.getHeaders()).containsEntry(HttpConstants.HttpHeaders.PARTITION_KEY, "[\"tenant\"]");
        }
    }

    @Test(groups = "unit")
    public void emptyQueryUsesUnchangedReadFeedPathAndWhitespaceIsNotSilentlyDiscarded() {
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRangeEpkImpl.forFullRange());
        RxDocumentClientImpl client = mock(RxDocumentClientImpl.class);
        for (String absent : new String[] { null, "" }) {
            options.setQuery(absent);
            RxDocumentServiceRequest request = createQuery(client, options).createDocumentServiceRequest();
            assertThat(options.getQuery()).isNull();
            assertThat(request.getOperationType()).isEqualTo(OperationType.ReadFeed);
            assertThat(request.getHeaders()).doesNotContainKeys(HttpConstants.HttpHeaders.IS_QUERY,
                HttpConstants.HttpHeaders.CONTENT_TYPE);
            assertThat(request.getContentAsByteArray()).isNull();
        }
        options.setQuery(" ");
        RxDocumentServiceRequest request = createQuery(client, options).createDocumentServiceRequest();
        assertThat(request.getOperationType()).isEqualTo(OperationType.Query);
        assertThat(new String(request.getContentAsByteArray(), StandardCharsets.UTF_8)).contains("\"query\":\" \"");
    }

    @Test(groups = "unit")
    public void rawSqlSurvivesCloningAndChangedQueryResumption() {
        String original = "SELECT * FROM c WHERE c.a = 1";
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRangeEpkImpl.forFullRange()).setQuery(original);
        ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.CosmosChangeFeedRequestOptionsAccessor accessor =
            ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor();
        assertThat(accessor.clone(options).getQuery()).isEqualTo(original);
        ChangeFeedStateV1 state = new ChangeFeedStateV1("rid", FeedRangeEpkImpl.forFullRange(),
            ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromInternal.createFromBeginning(), null);
        String continuation = state.toString();
        options.setQuery("SELECT * FROM c WHERE c.a = 2");
        CosmosPagedFluxOptions paging = new CosmosPagedFluxOptions();
        paging.setRequestContinuation(continuation);
        CosmosChangeFeedRequestOptions resumed = ModelBridgeInternal.getEffectiveChangeFeedRequestOptions(options, paging);
        assertThat(resumed.getQuery()).isEqualTo(options.getQuery());
        assertThat(accessor.getImpl(resumed).getContinuation().toString()).isEqualTo(continuation);
        assertThat(state.toJson()).doesNotContain(original, "query");
    }

    @Test(groups = "unit")
    public void gatewayUsesPostAndJsonRegardlessOfQueryCompatibilityMode() throws Exception {
        java.lang.reflect.Method queryMethod = RxGatewayStoreModel.class
            .getDeclaredMethod("query", RxDocumentServiceRequest.class);
        queryMethod.setAccessible(true);
        java.lang.reflect.Method method = RxGatewayStoreModel.class
            .getDeclaredMethod("getHttpMethod", RxDocumentServiceRequest.class);
        method.setAccessible(true);
        for (QueryCompatibilityMode compatibility : QueryCompatibilityMode.values()) {
            RxGatewayStoreModel gateway = new RxGatewayStoreModel(TestUtils.mockDiagnosticsClientContext(),
                mock(ISessionContainer.class), ConsistencyLevel.SESSION, compatibility, new UserAgentContainer(),
                mock(GlobalEndpointManager.class), mock(HttpClient.class), null, null, null) {
                @Override
                public Mono<RxDocumentServiceResponse> performRequest(RxDocumentServiceRequest request) {
                    return Mono.empty();
                }
            };
            CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(FeedRangeEpkImpl.forFullRange()).setQuery("SELECT * FROM c");
            RxDocumentServiceRequest request = createQuery(mock(RxDocumentClientImpl.class), options)
                .createDocumentServiceRequest();
            new ChangeFeedStateV1("rid", FeedRangeEpkImpl.forFullRange(), ChangeFeedMode.INCREMENTAL,
                ChangeFeedStartFromInternal.createFromNow(), null).populateRequest(request, 100);
            ((Mono<?>) queryMethod.invoke(gateway, request)).block();
            assertThat(method.invoke(null, request)).isEqualTo(HttpMethod.POST);
            assertThat(request.getHeaders()).containsEntry(HttpConstants.HttpHeaders.CONTENT_TYPE,
                RuntimeConstants.MediaTypes.QUERY_JSON);
            request.getHeaders().remove(HttpConstants.HttpHeaders.A_IM);
            queryMethod.invoke(gateway, request);
            assertThat(request.getHeaders()).containsEntry(HttpConstants.HttpHeaders.CONTENT_TYPE,
                compatibility == QueryCompatibilityMode.SqlQuery
                    ? RuntimeConstants.MediaTypes.SQL : RuntimeConstants.MediaTypes.QUERY_JSON);
        }
    }

    @Test(groups = "unit")
    public void serviceValidationErrorPropagatesUnchanged() {
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRangeEpkImpl.forFullRange()).setQuery("SELECT c.id FROM c");
        RxDocumentClientImpl client = mock(RxDocumentClientImpl.class);
        when(client.getGlobalPartitionEndpointManagerForCircuitBreaker())
            .thenReturn(mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class));
        when(client.getGlobalPartitionEndpointManagerForPerPartitionAutomaticFailover())
            .thenReturn(mock(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class));
        BadRequestException failure = new BadRequestException("Unsupported projection");
        when(client.readFeed(any())).thenReturn(Mono.error(failure));
        ChangeFeedQueryImpl<Document> query = createQuery(client, options);
        StepVerifier.create(query.executeRequestAsync(query.createDocumentServiceRequest()))
            .expectErrorMatches(error -> error == failure && failure.getStatusCode() == 400)
            .verify();
    }

    private ChangeFeedQueryImpl<Document> createQuery(RxDocumentClientImpl client, CosmosChangeFeedRequestOptions options) {
        return new ChangeFeedQueryImpl<>(client, ResourceType.Document, Document.class, "dbs/db/colls/coll",
            "rid", options, TestUtils.mockDiagnosticsClientContext(), null);
    }

    private void populateRouting(FeedRangeInternal range, RxDocumentServiceRequest request) {
        IRoutingMapProvider routing = mock(IRoutingMapProvider.class);
        PartitionKeyRange physicalRange = new PartitionKeyRange().setId("7")
            .setMinInclusive("").setMaxExclusive("FF");
        when(routing.tryGetOverlappingRangesAsync(any(), any(), any(), anyBoolean(), any()))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(Collections.singletonList(physicalRange))));
        when(routing.tryGetPartitionKeyRangeByIdAsync(any(), any(), any(), anyBoolean(), any()))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(physicalRange)));
        DocumentCollection collection = new DocumentCollection();
        collection.setPartitionKey(new PartitionKeyDefinition().setPaths(Collections.singletonList("/tenant")));
        range.populateFeedRangeFilteringHeaders(routing, request, Mono.just(new Utils.ValueHolder<>(collection))).block();
    }
}
