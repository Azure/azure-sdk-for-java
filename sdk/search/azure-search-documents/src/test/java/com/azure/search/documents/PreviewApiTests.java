// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.models.RandomScoringFunction;
import com.azure.search.documents.indexes.models.KnowledgeBaseMicrosoftRetriever1Model;
import com.azure.search.documents.indexes.models.KnowledgeBaseModel;
import com.azure.search.documents.indexes.models.KnowledgeSourceFileIngestionOperation;
import com.azure.search.documents.indexes.models.RandomScoringParameters;
import com.azure.search.documents.indexes.models.ScoringFunction;
import com.azure.search.documents.indexes.models.MagnitudeScoringFunction;
import com.azure.search.documents.indexes.models.MagnitudeScoringParameters;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHint;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHintAutoConfiguration;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseGraphSearchActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceCache;
import com.azure.search.documents.models.BalancedVectorQueryTopicGraph;
import com.azure.search.documents.models.DiverseVectorQueryTopicGraph;
import com.azure.search.documents.models.GraphOptions;
import com.azure.search.documents.models.GraphQueryExecutionMode;
import com.azure.search.documents.models.GraphQueryExplainStage;
import com.azure.search.documents.models.GraphQueryResult;
import com.azure.search.documents.models.NoneVectorQueryTopicGraph;
import com.azure.search.documents.models.SearchPagedResponse;
import com.azure.search.documents.models.VectorQueryTopicGraph;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Arrays;
import java.util.Map;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PreviewApiTests {
    private static final String GRAPH_RESPONSE = "{\"@odata.count\":1,\"value\":[{\"name\":\"Ada\",\"score\":0.75}],"
        + "\"@search.graphStats\":{\"executionMode\":\"planOnly\",\"planningMs\":12},"
        + "\"@search.graphDiagnostics\":[{\"code\":\"PlanReady\",\"severity\":\"info\",\"phase\":\"plan\","
        + "\"span\":null,\"suggestions\":[\"execute\"]}],"
        + "\"@search.graphExplain\":{\"logical\":{\"operator\":\"scan\"}}}";

    @Test
    public void preservesGaVersionsAndOnlyLatestPreview() {
        assertEquals("2026-11-01-preview", SearchServiceVersion.getLatest().getVersion());
        assertEquals(Collections.singletonList(SearchServiceVersion.V2026_11_01_PREVIEW),
            Arrays.stream(SearchServiceVersion.values())
                .filter(version -> version.getVersion().endsWith("-preview"))
                .collect(java.util.stream.Collectors.toList()));
        assertEquals("2026-10-01", SearchServiceVersion.V2026_10_01.getVersion());
        assertEquals("2026-04-01", SearchServiceVersion.V2026_04_01.getVersion());
        assertEquals("2020-06-30", SearchServiceVersion.V2020_06_30.getVersion());
    }

    @Test
    public void existingListConveniencesUseRenamedProtocolOperations() {
        com.azure.core.http.HttpClient transport = request -> Mono
            .just(new MockHttpResponse(request, 200, "{\"value\":[]}".getBytes(StandardCharsets.UTF_8)));
        SearchIndexClientBuilder indexes = new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .httpClient(transport);
        SearchIndexerClientBuilder indexers
            = new SearchIndexerClientBuilder().endpoint("https://example.search.windows.net")
                .credential(new AzureKeyCredential("key"))
                .httpClient(transport);
        assertEquals(0, indexes.buildClient().listSynonymMaps().stream().count());
        assertEquals(0, indexes.buildAsyncClient().listSynonymMaps().collectList().block().size());
        assertEquals(0, indexers.buildClient().listDataSourceConnections().stream().count());
        assertEquals(0, indexers.buildAsyncClient().listDataSourceConnections().collectList().block().size());
        assertEquals(0, indexers.buildClient().listIndexers().stream().count());
        assertEquals(0, indexers.buildAsyncClient().listIndexers().collectList().block().size());
        assertEquals(0, indexers.buildClient().listSkillsets().stream().count());
        assertEquals(0, indexers.buildAsyncClient().listSkillsets().collectList().block().size());
    }

    @Test
    public void fileDeletePreservesExistingParameterOrder() {
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                assertTrue(request.getUrl().getPath().contains("/knowledgesources('source')/files('file')"));
                return Mono.just(new MockHttpResponse(request, 204));
            });
        builder.buildClient().deleteKnowledgeSourceFile("file", "source");
        builder.buildAsyncClient().deleteKnowledgeSourceFile("file", "source").block();
    }

    @Test
    public void syncAndAsyncGraphClientsForwardOptionsAndDeserializeDiagnostics() {
        SearchClientBuilder builder = new SearchClientBuilder().endpoint("https://example.search.windows.net")
            .indexName("graph")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                assertTrue(request.getUrl().toString().contains("/docs/search.graph"));
                assertTrue(request.getUrl().getQuery().contains("api-version=2026-11-01-preview"));
                assertEquals("source-token",
                    request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-query-source-authorization")));
                assertEquals("false",
                    request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-enable-elevated-read")));
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                assertEquals("MATCH (n) RETURN n", body.get("gql"));
                assertEquals("planOnly", body.get("executionMode"));
                assertEquals(Boolean.TRUE, body.get("includeDiagnostics"));
                return Mono.just(new MockHttpResponse(request, 200, GRAPH_RESPONSE.getBytes(StandardCharsets.UTF_8)));
            });
        GraphOptions options
            = new GraphOptions("MATCH (n) RETURN n").setExecutionMode(GraphQueryExecutionMode.PLAN_ONLY)
                .setExplain(Collections.singletonList(GraphQueryExplainStage.fromString("logical")))
                .setIncludeDiagnostics(true)
                .setQuerySourceAuthorization("source-token")
                .setEnableElevatedRead(false);

        assertGraphResult(builder.buildClient().graph(options));
        assertGraphResult(builder.buildAsyncClient().graph(options).block());
        Response<GraphQueryResult> syncResponse = builder.buildClient().graphWithResponse(options, null);
        assertEquals(200, syncResponse.getStatusCode());
        assertGraphResult(syncResponse.getValue());
        Response<GraphQueryResult> asyncResponse = builder.buildAsyncClient().graphWithResponse(options, null).block();
        assertNotNull(asyncResponse);
        assertEquals(200, asyncResponse.getStatusCode());
        assertGraphResult(asyncResponse.getValue());
    }

    private static void assertGraphResult(GraphQueryResult result) {
        assertNotNull(result);
        assertEquals(1L, result.getCount());
        assertEquals("Ada", result.getValue().get(0).getAdditionalProperties().get("name"));
        assertEquals(12L, result.getStatistics().getPlanningMs());
        assertEquals("PlanReady", result.getDiagnostics().get(0).getCode());
        assertTrue(result.getExplain().getAdditionalProperties().containsKey("logical"));
    }

    @Test
    public void graphRejectionsPreserveResponseDiagnostics() {
        String error = "{\"error\":{\"code\":\"InvalidRequestParameter\",\"message\":\"Invalid graph query\"},"
            + "\"@search.graphDiagnostics\":[{\"code\":\"UnknownVariable\",\"severity\":\"error\",\"phase\":\"bind\"}]}";
        SearchClientBuilder builder = new SearchClientBuilder().endpoint("https://example.search.windows.net")
            .indexName("graph")
            .credential(new AzureKeyCredential("key"))
            .httpClient(
                request -> Mono.just(new MockHttpResponse(request, 400, error.getBytes(StandardCharsets.UTF_8))));
        HttpResponseException sync = assertThrows(HttpResponseException.class,
            () -> builder.buildClient().graph(new GraphOptions("MATCH (n) RETURN missing")));
        HttpResponseException async = assertThrows(HttpResponseException.class,
            () -> builder.buildAsyncClient().graph(new GraphOptions("MATCH (n) RETURN missing")).block());
        assertEquals(400, sync.getResponse().getStatusCode());
        assertTrue(sync.getResponse().getBodyAsString().block().contains("UnknownVariable"));
        assertTrue(async.getResponse().getBodyAsString().block().contains("UnknownVariable"));
    }

    @Test
    public void topicGraphDiscriminatorsRoundTrip() {
        assertTopicGraph("{\"kind\":\"none\"}", NoneVectorQueryTopicGraph.class);
        assertTopicGraph("{\"kind\":\"diverse\",\"diversityFactor\":0.5,\"minDiversityPoolSize\":500}",
            DiverseVectorQueryTopicGraph.class);
        assertTopicGraph("{\"kind\":\"balanced\",\"minDiversityPoolSize\":500}", BalancedVectorQueryTopicGraph.class);
    }

    private static void assertTopicGraph(String json, Class<? extends VectorQueryTopicGraph> type) {
        VectorQueryTopicGraph value = BinaryData.fromString(json).toObject(VectorQueryTopicGraph.class);
        assertInstanceOf(type, value);
        assertEquals(BinaryData.fromString(json).toObject(Map.class), BinaryData.fromObject(value).toObject(Map.class));
    }

    @Test
    public void automaticFilterHintsSerializeFacetConfiguration() {
        SearchIndexKnowledgeSourceFilterHint hint
            = new SearchIndexKnowledgeSourceFilterHint("category").setAutoConfiguration(
                new SearchIndexKnowledgeSourceFilterHintAutoConfiguration().setCacheTtl(900).setMaxFacetValueCount(32));
        SearchIndexKnowledgeSourceFilterHint restored
            = BinaryData.fromObject(hint).toObject(SearchIndexKnowledgeSourceFilterHint.class);
        assertEquals("category", restored.getField());
        assertEquals(900, restored.getAutoConfiguration().getCacheTtl());
        assertEquals(32, restored.getAutoConfiguration().getMaxFacetValueCount());
    }

    @Test
    public void randomScoringOmitsFieldAndBoostWhileClassicBaseRemainsCompatible() {
        RandomScoringFunction random = new RandomScoringFunction().setParameters(new RandomScoringParameters("seed"));
        Map<?, ?> json = BinaryData.fromObject(random).toObject(Map.class);
        assertEquals("random", json.get("type"));
        assertFalse(json.containsKey("fieldName"));
        assertFalse(json.containsKey("boost"));
        assertInstanceOf(RandomScoringFunction.class, BinaryData.fromObject(random).toObject(ScoringFunction.class));

        ScoringFunction classic = new ScoringFunction("rating", 2.5);
        ScoringFunction restored = BinaryData.fromObject(classic).toObject(ScoringFunction.class);
        assertEquals("rating", restored.getFieldName());
        assertEquals(2.5, restored.getBoost());
        ScoringFunction magnitude = new MagnitudeScoringFunction("rating", 2.5, new MagnitudeScoringParameters(0, 5));
        assertEquals("rating", magnitude.getFieldName());
        assertEquals(2.5, magnitude.getBoost());
        assertEquals(BinaryData.fromObject(magnitude).toObject(Map.class),
            BinaryData.fromObject(BinaryData.fromObject(magnitude).toObject(ScoringFunction.class))
                .toObject(Map.class));
    }

    @Test
    public void pagedResponsesExposePreviewQueryDiagnostics() {
        SearchPagedResponse page = new SearchPagedResponse(
            new SimpleResponse<>(new HttpRequest(HttpMethod.POST, "https://example.search.windows.net"), 200,
                new HttpHeaders(),
                BinaryData.fromString("{\"value\":[],\"@search.debug\":{\"queryRewrites\":{}},"
                    + "\"@search.semanticQueryRewritesResultType\":\"originalQueryOnly\"}")),
            SearchServiceVersion.V2026_11_01_PREVIEW);
        assertNotNull(page.getDebugInfo());
        assertNotNull(page.getDebugInfo().getQueryRewrites());
        assertEquals("originalQueryOnly", page.getSemanticQueryRewritesResultType().toString());
    }

    @Test
    public void knowledgeCacheAndGraphActivityDeserialize() {
        KnowledgeSourceCache cache = new KnowledgeSourceCache("UseDevelopmentStorage=true").setEnableReprocessing(true);
        KnowledgeSourceCache restored = BinaryData.fromObject(cache).toObject(KnowledgeSourceCache.class);
        assertEquals(cache.getStorageConnectionString(), restored.getStorageConnectionString());
        assertEquals(Boolean.TRUE, restored.isEnableReprocessing());
        KnowledgeBaseActivityRecord activity
            = BinaryData
                .fromString("{\"type\":\"graphSearch\",\"id\":1,\"graphSearchArguments\":{\"query\":\"connections\","
                    + "\"modelName\":\"model\",\"graphIndexName\":\"graph\"}}")
                .toObject(KnowledgeBaseActivityRecord.class);
        assertInstanceOf(KnowledgeBaseGraphSearchActivityRecord.class, activity);
        assertEquals("model",
            ((KnowledgeBaseGraphSearchActivityRecord) activity).getGraphSearchArguments().getModelName());
    }

    @Test
    public void microsoftRetrieverModelRoundTripsPolymorphically() {
        KnowledgeBaseModel model = new KnowledgeBaseMicrosoftRetriever1Model();
        KnowledgeBaseModel restored = BinaryData.fromObject(model).toObject(KnowledgeBaseModel.class);
        assertInstanceOf(KnowledgeBaseMicrosoftRetriever1Model.class, restored);
        assertEquals("microsoftRetriever1", restored.getKind().toString());
    }

    @Test
    public void fileIngestionOperationRetainsItsWireKind() {
        KnowledgeSourceFileIngestionOperation operation = BinaryData
            .fromString("{\"id\":\"operation\",\"status\":\"succeeded\",\"kind\":\"knowledgeSourceFileIngestion\"}")
            .toObject(KnowledgeSourceFileIngestionOperation.class);
        assertEquals("knowledgeSourceFileIngestion", operation.getKind());
        assertEquals(operation.getKind(), BinaryData.fromObject(operation).toObject(Map.class).get("kind"));
    }
}
