// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.models.IndexListingPagedResponse;
import com.azure.search.documents.indexes.models.IndexListingPagedIterable;
import com.azure.search.documents.indexes.models.IndexListingPagedFlux;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.RandomScoringFunction;
import com.azure.search.documents.indexes.models.KnowledgeBaseMicrosoftRetriever1Model;
import com.azure.search.documents.indexes.models.KnowledgeBaseModel;
import com.azure.search.documents.indexes.models.KnowledgeSourceFileIngestionOperation;
import com.azure.search.documents.indexes.models.KnowledgeSourceFile;
import com.azure.search.documents.indexes.models.FileKnowledgeSource;
import com.azure.search.documents.indexes.models.FileKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.FileUploadMetadata;
import com.azure.search.documents.indexes.models.UpdateKnowledgeSourceFileRequest;
import com.azure.search.documents.indexes.models.RandomScoringParameters;
import com.azure.search.documents.indexes.models.ScoringFunction;
import com.azure.search.documents.indexes.models.MagnitudeScoringFunction;
import com.azure.search.documents.indexes.models.MagnitudeScoringParameters;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHint;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHintAutoConfiguration;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseGraphSearchActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedSqlReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceCache;
import com.azure.search.documents.models.BalancedVectorQueryTopicGraph;
import com.azure.search.documents.models.ContentFileDetails;
import com.azure.search.documents.models.DiverseVectorQueryTopicGraph;
import com.azure.search.documents.models.GraphOptions;
import com.azure.search.documents.models.GraphQueryExecutionMode;
import com.azure.search.documents.models.GraphQueryExplainStage;
import com.azure.search.documents.models.GraphQueryResult;
import com.azure.search.documents.models.GraphQueryError;
import com.azure.search.documents.models.NoneVectorQueryTopicGraph;
import com.azure.search.documents.models.SearchPagedResponse;
import com.azure.search.documents.models.VectorQueryTopicGraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    public void metadataListingsRetainTotalsContinuationHttpMetadataAndContext() {
        AtomicInteger requests = new AtomicInteger();
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .addPolicy((context, next) -> {
                assertEquals("retained", context.getData("listing-context").orElse(null));
                return next.process();
            })
            .httpClient(request -> {
                requests.incrementAndGet();
                String query = request.getUrl().getQuery();
                assertFalse(query.contains("$count"));
                boolean last = query.contains("cursor=next");
                if (!last) {
                    assertTrue(query.contains("search=only"));
                    assertEquals("initial", request.getHeaders().getValue("X-Listing-Test"));
                }
                String nextLink = last
                    ? ""
                    : ",\"@odata.nextLink\":\"https://example.search.windows.net" + request.getUrl().getPath()
                        + "?api-version=2026-11-01-preview&cursor=next\"";
                String body = "{\"@odata.count\":42,\"value\":[{\"name\":\"" + (last ? "second" : "first")
                    + "\",\"fields\":[],\"documentCount\":1,\"storageSize\":2,\"vectorIndexSize\":3}]" + nextLink + "}";
                return Mono.just(new MockHttpResponse(request, 200,
                    new HttpHeaders().set("X-Listing-Page", last ? "second" : "first"),
                    body.getBytes(StandardCharsets.UTF_8)));
            });
        RequestOptions options = new RequestOptions().setContext(new Context("listing-context", "retained"))
            .setHeader("X-Listing-Test", "initial")
            .addQueryParam("search", "only");
        IndexListingPagedIterable<SearchIndex> indexes = builder.buildClient().listIndexesWithMetadata(options);
        IndexListingPagedFlux<SearchIndex> asyncIndexes = builder.buildAsyncClient().listIndexesWithMetadata(options);
        assertEquals(0, requests.get());
        assertMetadataPages(indexes.streamByPage().collect(Collectors.toList()));
        assertMetadataPages(asyncIndexes.byPage().collectList().block());
        assertMetadataPages(builder.buildClient()
            .listIndexStatsSummaryWithMetadata(options)
            .streamByPage()
            .collect(Collectors.toList()));
        assertMetadataPages(
            builder.buildAsyncClient().listIndexStatsSummaryWithMetadata(options).byPage().collectList().block());
        assertEquals(8, requests.get());
        assertEquals("second",
            indexes
                .iterableByPage(
                    "https://example.search.windows.net/indexes" + "?api-version=2026-11-01-preview&cursor=next")
                .iterator()
                .next()
                .getValue()
                .get(0)
                .getName());
    }

    private static void assertMetadataPages(List<? extends IndexListingPagedResponse<?>> pages) {
        assertNotNull(pages);
        assertEquals(2, pages.size());
        for (IndexListingPagedResponse<?> page : pages) {
            assertEquals(42L, page.getCount());
            assertEquals(1, page.getValue().size());
            assertEquals(200, page.getStatusCode());
            assertNotNull(page.getRequest());
        }
        assertEquals("first", pages.get(0).getHeaders().getValue("X-Listing-Page"));
        assertNotNull(pages.get(0).getContinuationToken());
        assertEquals("second", pages.get(1).getHeaders().getValue("X-Listing-Page"));
        assertNull(pages.get(1).getContinuationToken());
    }

    @Test
    public void metadataCountsRemainNullableAndSupportLongTotals() {
        for (String count : new String[] { null, "null", "0", "2147483648" }) {
            Long expected = count == null || "null".equals(count) ? null : Long.valueOf(count);
            String body = "{\"value\":[{\"name\":\"only\",\"fields\":[],"
                + "\"documentCount\":1,\"storageSize\":2,\"vectorIndexSize\":3}]"
                + (count == null ? "" : ",\"@odata.count\":" + count) + "}";
            SearchIndexClientBuilder builder = new SearchIndexClientBuilder()
                .endpoint("https://example.search.windows.net")
                .credential(new AzureKeyCredential("key"))
                .httpClient(
                    request -> Mono.just(new MockHttpResponse(request, 200, body.getBytes(StandardCharsets.UTF_8))));
            assertEquals(expected,
                builder.buildClient().listIndexesWithMetadata(null).iterableByPage().iterator().next().getCount());
            assertEquals(expected,
                builder.buildAsyncClient().listIndexesWithMetadata(null).byPage().blockFirst().getCount());
            assertEquals(expected,
                builder.buildClient()
                    .listIndexStatsSummaryWithMetadata(null)
                    .iterableByPage()
                    .iterator()
                    .next()
                    .getCount());
            assertEquals(expected,
                builder.buildAsyncClient().listIndexStatsSummaryWithMetadata(null).byPage().blockFirst().getCount());
            assertEquals("only", builder.buildClient().listIndexes(null).iterator().next().getName());
            assertEquals("only", builder.buildAsyncClient().listIndexes(null).blockFirst().getName());
            assertEquals("only", builder.buildClient().listIndexStatsSummary(null).iterator().next().getName());
            assertEquals("only", builder.buildAsyncClient().listIndexStatsSummary(null).blockFirst().getName());
        }
    }

    @Test
    public void metadataListingsKeepServiceErrorMapping() {
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 404,
                "{\"error\":{\"code\":\"NotFound\",\"message\":\"Missing\"}}".getBytes(StandardCharsets.UTF_8))));
        assertThrows(ResourceNotFoundException.class,
            () -> builder.buildClient().listIndexesWithMetadata(null).iterator().next());
        assertThrows(ResourceNotFoundException.class,
            () -> builder.buildAsyncClient().listIndexesWithMetadata(null).blockFirst());
        assertThrows(ResourceNotFoundException.class,
            () -> builder.buildClient().listIndexStatsSummaryWithMetadata(null).iterator().next());
        assertThrows(ResourceNotFoundException.class,
            () -> builder.buildAsyncClient().listIndexStatsSummaryWithMetadata(null).blockFirst());
    }

    @Test
    public void fileOperationsPreserveGaParameterOrder() {
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                assertTrue(request.getUrl().getPath().contains("/knowledgesources('source')/files('file')"));
                if (request.getHttpMethod() == HttpMethod.PUT) {
                    assertTrue(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE).startsWith("multipart/"));
                    return Mono.just(new MockHttpResponse(request, 200,
                        "{\"fileId\":\"file\",\"fileName\":\"notes.txt\"}".getBytes(StandardCharsets.UTF_8)));
                }
                assertEquals(HttpMethod.DELETE, request.getHttpMethod());
                return Mono.just(new MockHttpResponse(request, 204));
            });
        builder.buildClient().deleteKnowledgeSourceFile("source", "file");
        builder.buildAsyncClient().deleteKnowledgeSourceFile("source", "file").block();
        assertEquals(204,
            builder.buildClient().deleteKnowledgeSourceFileWithResponse("source", "file", null).getStatusCode());
        Response<Void> deleted
            = builder.buildAsyncClient().deleteKnowledgeSourceFileWithResponse("source", "file", null).block();
        assertNotNull(deleted);
        assertEquals(204, deleted.getStatusCode());
        UpdateKnowledgeSourceFileRequest update
            = new UpdateKnowledgeSourceFileRequest(new FileUploadMetadata().setFileName("notes.txt"),
                new ContentFileDetails(BinaryData.fromString("content")).setFilename("notes.txt"));
        assertEquals("file", builder.buildClient().updateKnowledgeSourceFile("source", "file", update).getFileId());
        KnowledgeSourceFile updated
            = builder.buildAsyncClient().updateKnowledgeSourceFile("source", "file", update).block();
        assertNotNull(updated);
        assertEquals("file", updated.getFileId());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void fileLifecycleSamplePreservesRequestPathsIncludingCleanup(boolean failUpdate) {
        String endpoint = "https://example.search.windows.net";
        String sourcePath = "/knowledgesources('my-source')";
        String metadata = "{\"team\":\"search\",\"release\":\"2026-08-01-preview\"}";
        String firstFile = sampleFile("file-123", "features.md", metadata);
        String secondFile = sampleFile("file-456", "migration.md", "{\"type\":\"guide\"}");
        String updatedFile = sampleFile("file-123", "features.md",
            "{\"team\":\"search\",\"release\":\"2026-08-01-preview\",\"status\":\"reviewed\"}");
        List<String> pages = Arrays.asList(
            "{\"value\":[" + firstFile + "],\"@odata.nextLink\":\"" + endpoint + sourcePath
                + "/files?api-version=2026-11-01-preview&cursor=next\"}",
            "{\"value\":[" + secondFile + "]}", "{\"value\":[" + firstFile + "]}", "{\"value\":[]}");
        FileKnowledgeSource source = new FileKnowledgeSource("my-source", new FileKnowledgeSourceParameters());
        List<String> mutations = new ArrayList<>();
        AtomicInteger uploads = new AtomicInteger();
        AtomicInteger listings = new AtomicInteger();
        SearchIndexClient client = new SearchIndexClientBuilder().endpoint(endpoint)
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                HttpMethod method = request.getHttpMethod();
                String path = request.getUrl().getPath();
                int status;
                String body;
                if (method == HttpMethod.POST) {
                    status = 201;
                    body = "/knowledgesources".equals(path)
                        ? BinaryData.fromObject(source).toString()
                        : uploads.getAndIncrement() == 0 ? firstFile : secondFile;
                } else if (method == HttpMethod.GET) {
                    status = 200;
                    body = pages.get(listings.getAndIncrement());
                } else if (method == HttpMethod.PUT) {
                    mutations.add(method + " " + path);
                    status = failUpdate ? 400 : 200;
                    body = failUpdate
                        ? "{\"error\":{\"code\":\"BadRequest\",\"message\":\"Simulated update failure\"}}"
                        : updatedFile;
                } else {
                    assertEquals(HttpMethod.DELETE, method);
                    mutations.add(method + " " + path);
                    status = 204;
                    body = "";
                }
                return Mono.just(new MockHttpResponse(request, status, body.getBytes(StandardCharsets.UTF_8)));
            })
            .buildClient();

        if (failUpdate) {
            HttpResponseException error = assertThrows(HttpResponseException.class,
                () -> KnowledgeSourceFilePreviewExample.runFileLifecycle(client, source));
            assertEquals(400, error.getResponse().getStatusCode());
        } else {
            KnowledgeSourceFilePreviewExample.runFileLifecycle(client, source);
        }
        assertEquals(Arrays.asList("PUT " + sourcePath + "/files('file-123')",
            "DELETE " + sourcePath + "/files('" + (failUpdate ? "file-123" : "file-456") + "')",
            "DELETE " + sourcePath + "/files('" + (failUpdate ? "file-456" : "file-123") + "')",
            "DELETE " + sourcePath), mutations);
    }

    private static String sampleFile(String fileId, String fileName, String metadata) {
        return "{\"fileId\":\"" + fileId + "\",\"fileName\":\"release-notes/august/" + fileName
            + "\",\"prefix\":\"release-notes/august/\",\"parsingMode\":\"markdown\",\"extractionMode\":\"minimal\","
            + "\"metadata\":" + metadata + "}";
    }

    @Test
    public void gaSqlDocumentUrlGetterPreservesWireMapping() {
        KnowledgeBaseIndexedSqlReference reference = BinaryData
            .fromString("{\"type\":\"indexedSql\",\"id\":\"sql\",\"activitySource\":1,"
                + "\"docUrl\":\"https://example.com/document\",\"citationUrl\":\"https://example.com/citation\"}")
            .toObject(KnowledgeBaseIndexedSqlReference.class);
        assertEquals("https://example.com/document", reference.getDocumentUrl());
        Map<?, ?> json = BinaryData.fromObject(reference).toObject(Map.class);
        assertEquals(reference.getDocumentUrl(), json.get("docUrl"));
        assertFalse(json.containsKey("documentUrl"));
        assertEquals("https://example.com/citation", json.get("citationUrl"));
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
        for (HttpResponseException exception : Arrays.asList(sync, async)) {
            GraphQueryError details = BinaryData.fromString(exception.getResponse().getBodyAsString().block())
                .toObject(GraphQueryError.class);
            assertEquals("InvalidRequestParameter", details.getError().getCode());
            assertEquals("UnknownVariable", details.getDiagnostics().get(0).getCode());
            assertNull(details.getStatistics());
            assertFalse(exception.getValue() instanceof GraphQueryError);
        }
    }

    @Test
    public void graphErrorsPreserveOptionalGraphFieldsAndStandardErrorBodies() {
        String body = "{\"error\":{\"code\":\"InvalidRequestParameter\",\"message\":\"Invalid graph query\"},"
            + "\"@search.graphProvenanceDocuments\":{\"doc1\":{\"title\":\"Ada\"}}," + GRAPH_RESPONSE.substring(1);
        GraphQueryError details = BinaryData.fromString(body).toObject(GraphQueryError.class);
        assertEquals(1L, details.getCount());
        assertEquals("Ada", details.getValue().get(0).getAdditionalProperties().get("name"));
        assertEquals(12L, details.getStatistics().getPlanningMs());
        assertEquals("PlanReady", details.getDiagnostics().get(0).getCode());
        assertTrue(details.getExplain().getAdditionalProperties().containsKey("logical"));
        assertEquals("Ada",
            details.getProvenanceDocuments()
                .getAdditionalProperties()
                .get("doc1")
                .getAdditionalProperties()
                .get("title"));
        assertEquals(BinaryData.fromString(body).toObject(Map.class),
            BinaryData.fromObject(details).toObject(Map.class));
        GraphQueryError minimal
            = BinaryData.fromString("{\"error\":{\"code\":\"InvalidRequestParameter\",\"message\":\"Invalid\"},"
                + "\"@search.graphDiagnostics\":null}").toObject(GraphQueryError.class);
        assertEquals("Invalid", minimal.getError().getMessage());
        assertNull(minimal.getDiagnostics());
        assertNull(minimal.getValue());
        assertNull(minimal.getCount());
        assertNull(minimal.getProvenanceDocuments());
        assertNull(BinaryData.fromString("null").toObject(GraphQueryError.class));
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
