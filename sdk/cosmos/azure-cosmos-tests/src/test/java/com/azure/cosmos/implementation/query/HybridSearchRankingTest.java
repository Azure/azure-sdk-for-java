// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.query.HybridSearchDocumentQueryExecutionContext.ComponentWeight;
import com.azure.cosmos.implementation.query.HybridSearchDocumentQueryExecutionContext.ScoreTuple;
import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryInfo;
import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryResult;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.SqlQuerySpec;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.query.HybridSearchDocumentQueryExecutionContext.coalesceAndSortResults;
import static com.azure.cosmos.implementation.query.HybridSearchDocumentQueryExecutionContext.computeRRFScores;
import static com.azure.cosmos.implementation.query.HybridSearchDocumentQueryExecutionContext.computeRanks;
import static com.azure.cosmos.implementation.query.HybridSearchDocumentQueryExecutionContext.retrieveComponentScores;
import static com.azure.cosmos.implementation.query.HybridSearchDocumentQueryExecutionContext.retrieveComponentWeights;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class HybridSearchRankingTest {
    private static final String RANKING_PROPERTY = "COSMOS.HYBRID_SEARCH_USE_COMPETITION_RANKING";

    @DataProvider(name = "rankingModes")
    public Object[][] rankingModes() {
        return new Object[][] {
            {false, Arrays.asList(1, 2, 3, 4, 5)},
            {true, Arrays.asList(1, 2, 2, 2, 5)}
        };
    }

    @Test(groups = "unit", dataProvider = "rankingModes")
    public void tiedScoresUseOrdinalOrCompetitionRanks(boolean competition, List<Integer> expected) {
        List<HybridSearchQueryResult<Document>> rows = Arrays.asList(
            row(5, 10), row(4, 5), row(3, 5), row(2, 5), row(1, 0));
        List<ComponentWeight> weights = weights(null, SortOrder.Descending);

        List<List<Integer>> ranks = ranks(rows, weights, competition);
        assertThat(ranks).containsExactly(expected);

        List<HybridSearchQueryResult<Document>> fused = computeRRFScores(
            Mono.just(ranks), Mono.just(new ArrayList<>(rows)), weights).collectList().block();
        assertThat(fused).containsExactlyElementsOf(rows);
        for (int i = 0; i < rows.size(); i++) {
            assertThat(rows.get(i).getScore()).isCloseTo(1.0 / (60 + expected.get(i)), within(1e-15));
        }
    }

    @DataProvider(name = "directions")
    public Object[][] directions() {
        return new Object[][] {
            {SortOrder.Ascending, Arrays.asList(1, 2)},
            {SortOrder.Descending, Arrays.asList(2, 1)}
        };
    }

    @Test(groups = "unit", dataProvider = "directions")
    public void tiesFollowPlanDirectionRatherThanEncodedRidOrWeight(
        SortOrder direction, List<Integer> expected) {
        HybridSearchQueryResult<Document> low = row(1, 5);
        HybridSearchQueryResult<Document> high = row(256, 5);
        assertThat(low.getRid().compareTo(high.getRid())).isPositive();

        // A negative original weight can arrive normalized to positive with an ascending plan.
        List<ComponentWeight> weights = weights(Collections.singletonList(2.0), direction);
        assertThat(ranks(Arrays.asList(low, high), weights, false)).containsExactly(expected);
        assertThat(ranks(Arrays.asList(high, low), weights, false))
            .containsExactly(Arrays.asList(expected.get(1), expected.get(0)));
        assertThat(ranks(Arrays.asList(low, high), weights, true))
            .containsExactly(Arrays.asList(1, 1));
    }

    @Test(groups = "unit")
    public void mixedDirectionsPreserveWeightsAndFormula() {
        List<HybridSearchQueryResult<Document>> rows = Arrays.asList(row(1, 10, 5), row(256, 5, 10));
        List<ComponentWeight> weights = weights(Arrays.asList(2.0, 3.0),
            SortOrder.Descending, SortOrder.Ascending);
        List<List<Integer>> ranks = ranks(rows, weights, false);

        assertThat(ranks).containsExactly(Arrays.asList(1, 2), Arrays.asList(1, 2));
        computeRRFScores(Mono.just(ranks), Mono.just(rows), weights).collectList().block();
        assertThat(rows.get(0).getScore()).isCloseTo(5.0 / 61, within(1e-15));
        assertThat(rows.get(1).getScore()).isCloseTo(5.0 / 62, within(1e-15));
    }

    @Test(groups = "unit")
    public void finalScoreTiesUseDescendingNumericDocumentRid() {
        List<HybridSearchQueryResult<Document>> rows = Arrays.asList(row(1, 10, 5), row(256, 5, 10));
        List<ComponentWeight> weights = weights(null, SortOrder.Descending, SortOrder.Descending);
        List<HybridSearchQueryResult<Document>> fused = computeRRFScores(
            Mono.just(ranks(rows, weights, false)), Mono.just(rows), weights).collectList().block();

        assertThat(fused).extracting(HybridSearchQueryResult::getDocumentRid).containsExactly(256L, 1L);
        assertThat(fused.get(0).getScore()).isEqualTo(fused.get(1).getScore());
    }

    @Test(groups = "unit")
    public void ridDecodingUsesLittleEndianAndCosmosBase64() {
        long documentRid = 0x000102030405ffffL;
        HybridSearchQueryResult<Document> result = row(documentRid, 5);

        assertThat(result.getRid()).contains("-");
        assertThat(result.getDocumentRid()).isEqualTo(documentRid);
        assertThat(row(128, 5).getDocumentRid()).isEqualTo(128);
    }

    @Test(groups = "unit")
    public void numericRidIsCachedBeforeRankingAndFusion() {
        HybridSearchQueryResult<Document> low = row(1, 5);
        HybridSearchQueryResult<Document> high = row(256, 5);
        low.rid = "not-a-rid";
        high.rid = "not-a-rid-either";
        List<HybridSearchQueryResult<Document>> rows = Arrays.asList(low, high);
        List<ComponentWeight> weights = weights(null, SortOrder.Descending);

        assertThat(low.getDocumentRid()).isEqualTo(1);
        assertThat(high.getDocumentRid()).isEqualTo(256);
        assertThat(ranks(rows, weights, false)).containsExactly(Arrays.asList(2, 1));
        List<HybridSearchQueryResult<Document>> fused = computeRRFScores(
            Mono.just(ranks(rows, weights, true)), Mono.just(rows), weights).collectList().block();
        assertThat(fused).containsExactly(high, low);
    }

    @Test(groups = "unit")
    public void numericRidComparisonsAreUnsigned() {
        HybridSearchQueryResult<Document> high = spy(row(256, 5));
        doReturn(-1L).when(high).getDocumentRid();
        HybridSearchQueryResult<Document> low = row(1, 5);
        List<HybridSearchQueryResult<Document>> rows = Arrays.asList(low, high);
        List<ComponentWeight> weights = weights(null, SortOrder.Descending);

        assertThat(ranks(rows, weights, false)).containsExactly(Arrays.asList(2, 1));
        List<HybridSearchQueryResult<Document>> fused = computeRRFScores(
            Mono.just(ranks(rows, weights, true)), Mono.just(rows), weights).collectList().block();
        assertThat(fused).containsExactly(high, low);
    }

    @Test(groups = "unit")
    public void deduplicationKeepsEncodedIdentityAndFirstPayload() {
        String rid = rid(1, 1);
        Document first = document(rid, "first", 10);
        Document duplicate = document(rid, "duplicate", 100);
        Document otherCollection = document(rid(2, 1), "other-collection", 5);
        Document high = document(rid(1, 256), "high", 3);

        List<HybridSearchQueryResult<Document>> rows = coalesceAndSortResults(
            Flux.just(first, duplicate, otherCollection, high)).block();

        assertThat(rows).hasSize(3);
        assertThat(rows).extracting(HybridSearchQueryResult::getDocumentRid).containsExactly(256L, 1L, 1L);
        assertThat(rows).extracting(result -> result.getPayload().getId())
            .containsExactly("high", "first", "other-collection");
        assertThat(rows.get(1).getComponentScores()).containsExactly(10.0);
    }

    @Test(groups = "unit", dataProvider = "rankingModes")
    public void emptyScoresUsePlanDimensionsAndExistingUndefinedValue(
        boolean competition, List<Integer> ignored) {
        List<HybridSearchQueryResult<Document>> rows = Arrays.asList(row(256), row(1, 10, 5));
        List<ComponentWeight> weights = weights(null, SortOrder.Descending, SortOrder.Ascending);
        List<List<ScoreTuple>> scores = retrieveComponentScores(Mono.just(rows), weights).block();

        assertThat(scores).hasSize(2);
        assertThat(scores.get(0)).extracting(ScoreTuple::getScore).containsExactly(10.0, -999999.0);
        assertThat(scores.get(1)).extracting(ScoreTuple::getScore).containsExactly(-999999.0, 5.0);
        assertThat(ranks(rows, weights, competition)).containsExactly(Arrays.asList(2, 1), Arrays.asList(1, 2));

        Collections.reverse(rows);
        assertThat(ranks(rows, weights, competition)).containsExactly(Arrays.asList(1, 2), Arrays.asList(2, 1));
    }

    @Test(groups = "unit")
    public void emptyInputAndAllEmptyScoresAreSupported() {
        List<ComponentWeight> weights = weights(null, SortOrder.Descending, SortOrder.Descending);
        Mono<List<HybridSearchQueryResult<Document>>> empty = coalesceAndSortResults(Flux.empty()).cache();
        Mono<List<List<Integer>>> emptyRanks = computeRanks(retrieveComponentScores(empty, weights), false);
        assertThat(computeRRFScores(emptyRanks, empty, weights).collectList().block()).isEmpty();

        List<HybridSearchQueryResult<Document>> rows = Arrays.asList(row(256), row(1));
        assertThat(ranks(rows, weights, false)).containsExactly(Arrays.asList(1, 2), Arrays.asList(1, 2));
        assertThat(ranks(rows, weights, true)).containsExactly(Arrays.asList(1, 1), Arrays.asList(1, 1));
        assertThat(ranks(Collections.singletonList(row(1)), weights, false))
            .containsExactly(Collections.singletonList(1), Collections.singletonList(1));
    }

    @Test(groups = "unit")
    public void rankingReusesCollectedRowsAndPreservesPayloadPaging() throws ReflectiveOperationException {
        AtomicInteger subscriptions = new AtomicInteger();
        Mono<List<HybridSearchQueryResult<Document>>> rows = coalesceAndSortResults(Flux.defer(() -> {
            subscriptions.incrementAndGet();
            return Flux.just(document(rid(1, 1), "low", 5), document(rid(1, 256), "high", 5),
                document(rid(1, 128), "middle", 5));
        })).cache();
        List<ComponentWeight> weights = weights(null, SortOrder.Descending);
        Flux<HybridSearchQueryResult<Document>> fused = computeRRFScores(
            computeRanks(retrieveComponentScores(rows, weights), false), rows, weights);
        HybridSearchDocumentQueryExecutionContext context = context();
        Field observable = HybridSearchDocumentQueryExecutionContext.class.getDeclaredField("hybridObservable");
        observable.setAccessible(true);
        observable.set(context, fused);

        List<FeedResponse<Document>> pages = context.drainAsync(2).collectList().block();

        assertThat(subscriptions.get()).isEqualTo(1);
        assertThat(pages).hasSize(2);
        assertThat(pages.get(0).getResults()).extracting(Document::getId).containsExactly("high", "middle");
        assertThat(pages.get(1).getResults()).extracting(Document::getId).containsExactly("low");
    }

    @Test(groups = "unit")
    public void configurationIsResolvedOncePerQuery() throws ReflectiveOperationException {
        String previous = System.getProperty(RANKING_PROPERTY);
        try {
            System.clearProperty(RANKING_PROPERTY);
            assertThat(Configs.isHybridSearchCompetitionRankingEnabled()).isEqualTo(
                Boolean.parseBoolean(System.getenv("AZURE_COSMOS_HYBRID_SEARCH_USE_COMPETITION_RANKING")));

            System.setProperty(RANKING_PROPERTY, "false");
            HybridSearchDocumentQueryExecutionContext ordinal = context();
            System.setProperty(RANKING_PROPERTY, "true");
            HybridSearchDocumentQueryExecutionContext competition = context();
            Field rankingFlag = HybridSearchDocumentQueryExecutionContext.class.getDeclaredField("useCompetitionRanking");
            rankingFlag.setAccessible(true);
            assertThat(rankingFlag.getBoolean(ordinal)).isFalse();
            assertThat(rankingFlag.getBoolean(competition)).isTrue();
        } finally {
            if (previous == null) {
                System.clearProperty(RANKING_PROPERTY);
            } else {
                System.setProperty(RANKING_PROPERTY, previous);
            }
        }
    }

    private static HybridSearchDocumentQueryExecutionContext context() {
        return new HybridSearchDocumentQueryExecutionContext(null, mock(IDocumentQueryClient.class),
            ResourceType.Document, new SqlQuerySpec("SELECT * FROM c"), new CosmosQueryRequestOptions(),
            "dbs/db/colls/coll", null, UUID.randomUUID(), new AtomicBoolean(), new HybridSearchQueryInfo());
    }

    private static List<List<Integer>> ranks(List<HybridSearchQueryResult<Document>> rows,
                                           List<ComponentWeight> weights, boolean competition) {
        return computeRanks(retrieveComponentScores(Mono.just(rows), weights), competition).block();
    }

    private static List<ComponentWeight> weights(List<Double> values, SortOrder... directions) {
        List<QueryInfo> components = new ArrayList<>();
        for (SortOrder direction : directions) {
            QueryInfo info = new QueryInfo();
            info.setOrderBy(Collections.singletonList(direction));
            components.add(info);
        }
        return retrieveComponentWeights(values, components);
    }

    private static HybridSearchQueryResult<Document> row(long documentRid, double... scores) {
        return new HybridSearchQueryResult<>(document(rid(1, documentRid), "doc-" + documentRid, scores).toJson());
    }

    private static Document document(String rid, String id, double... scores) {
        return new Document("{\"_rid\":\"" + rid + "\",\"payload\":{\"componentScores\":"
            + Arrays.toString(scores) + ",\"payload\":{\"id\":\"" + id + "\"}}}");
    }

    private static String rid(int database, long document) {
        byte[] bytes = new byte[16];
        ByteBuffer.wrap(bytes).putInt(database);
        bytes[4] = (byte) 0x80;
        ByteBuffer.wrap(bytes, 8, 8).order(ByteOrder.LITTLE_ENDIAN).putLong(document);
        return Base64.getEncoder().encodeToString(bytes).replace('/', '-');
    }
}
