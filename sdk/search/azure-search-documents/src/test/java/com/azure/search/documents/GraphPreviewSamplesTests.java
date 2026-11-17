// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceCache;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.models.BalancedVectorQueryTopicGraph;
import com.azure.search.documents.models.DiverseVectorQueryTopicGraph;
import com.azure.search.documents.models.GraphQueryResult;
import com.azure.search.documents.models.NoneVectorQueryTopicGraph;
import com.azure.search.documents.models.VectorQueryTopicGraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphPreviewSamplesTests {
    private static final String ENDPOINT = "https://example.search.windows.net";

    @Test
    public void directGraphSeedsEvidenceAndRunsPlanIdsAndAuthorizedHydration() {
        List<String> deleted = new ArrayList<>();
        AtomicInteger graphQueries = new AtomicInteger();
        HttpClient transport = request -> {
            String path = request.getUrl().getPath();
            if (request.getHttpMethod() == HttpMethod.DELETE) {
                deleted.add(path);
                return response(request, 204, "");
            }
            if (request.getHttpMethod() == HttpMethod.GET) {
                assertTrue(path.endsWith("/docs/$count"));
                return response(request, 200, "2");
            }
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            if (path.endsWith("/indexes")) {
                if ("owned-graph".equals(body.get("name"))) {
                    Map<?, ?> profile = map(body.get("graphProfile"));
                    assertEquals("owned-source", profile.get("provenanceIndexName"));
                    Map<?, ?> fields = map(profile.get("fields"));
                    assertEquals("chunk_id", fields.get("sourceChunkIdField"));
                    assertEquals("parent_id", fields.get("sourceParentIdField"));
                    assertEquals("object_datetime", fields.get("objectDateTimeField"));
                }
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            if (body.containsKey("value")) {
                List<?> documents = (List<?>) body.get("value");
                assertEquals(2, documents.size());
                Map<?, ?> first = map(documents.get(0));
                if (path.contains("owned-graph")) {
                    assertEquals("hotels'o", first.get("graph_name"));
                    assertEquals("harbor-operator", first.get("chunk_id"));
                    assertEquals("harbor-brochure", first.get("parent_id"));
                    assertEquals("hotel:harbor", first.get("subject_id"));
                    assertEquals("organization:contoso", first.get("object_id"));
                    return response(request, 200, uploadResponse("assertion-1", "assertion-2"));
                }
                assertEquals("harbor-operator", first.get("uid"));
                assertEquals("harbor-brochure", first.get("snippet_parent_id"));
                return response(request, 200, uploadResponse("harbor-operator", "harbor-location"));
            }
            int query = graphQueries.getAndIncrement();
            assertTrue(((String) body.get("gql")).contains("h.id = 'hotel:harbor'"));
            assertEquals("graph_name eq 'hotels''o'", body.get("filter"));
            if (query == 0) {
                assertEquals("planOnly", body.get("executionMode"));
                assertEquals(Collections.singletonList("physicalPlan"), body.get("explain"));
                assertNull(body.get("provenance"));
                return response(request, 200, "{\"value\":[],\"@search.graphStats\":{\"queryCount\":0},"
                    + "\"@search.graphExplain\":{\"physicalPlan\":{\"operator\":\"scan\"}}}");
            }
            Map<?, ?> provenance = map(body.get("provenance"));
            assertEquals(5, provenance.get("maxEvidencePerRow"));
            if (query == 1) {
                assertEquals("ids", provenance.get("kind"));
                return response(request, 200, graphResult(true));
            }
            assertEquals("hydrate", provenance.get("kind"));
            assertEquals("uid,snippet", map(provenance.get("hydrateDocuments")).get("select"));
            String text = (String) GraphIndexPreviewExample.sourceDocuments().get(0).get("snippet");
            return response(request, 200, "{\"value\":[],\"@search.graphProvenanceDocuments\":"
                + "{\"harbor-operator\":{\"uid\":\"harbor-operator\",\"snippet\":\"" + text + "\"}}}");
        };
        SearchIndexClient indexes = indexes(transport);
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            resources.createIndex(GraphIndexPreviewExample.provenanceIndex("owned-source"));
            resources.createIndex(GraphIndexPreviewExample.graphIndex("owned-graph", "owned-source"));
            PreviewSampleSupport.upload(indexes.getSearchClient("owned-source"),
                GraphIndexPreviewExample.sourceDocuments());
            SearchClient graph = indexes.getSearchClient("owned-graph");
            PreviewSampleSupport.upload(graph, GraphIndexPreviewExample.assertionDocuments("hotels'o"));
            GraphIndexPreviewExample.executeQueries(graph, "hotels'o", true);
        }
        assertEquals(3, graphQueries.get());
        assertEquals(2, deleted.size());
        assertTrue(deleted.get(0).contains("owned-graph"));
        assertTrue(deleted.get(1).contains("owned-source"));
    }

    @Test
    public void directGraphRejectsMissingEvidenceRatherThanPrintingUnverifiedRelationships() {
        AtomicInteger calls = new AtomicInteger();
        SearchClient graph = indexes(request -> response(request, 200,
            calls.getAndIncrement() == 0
                ? "{\"value\":[],\"@search.graphStats\":{\"queryCount\":0},\"@search.graphExplain\":{}}"
                : graphResult(false))).getSearchClient("owned-graph");
        assertThrows(IllegalStateException.class,
            () -> GraphIndexPreviewExample.executeQueries(graph, "hotels", false));
        assertEquals(2, calls.get());
    }

    @Test
    public void topicLinkUpdatesOnlyOwnedCorpusWithEtagAndMatchingStoredVectorDimensions() {
        int dimensions = 1536;
        SearchIndex corpus
            = TopicGraphPreviewExample.corpus("owned-corpus", dimensions, new AzureOpenAIVectorizerParameters())
                .setETag("\"owned-version\"");
        AtomicReference<String> stored = new AtomicReference<>(BinaryData.fromObject(corpus).toString());
        List<HttpMethod> methods = new ArrayList<>();
        SearchIndexClient indexes = indexes(request -> {
            methods.add(request.getHttpMethod());
            if (request.getHttpMethod() == HttpMethod.PUT) {
                assertTrue(request.getUrl().getPath().contains("owned-corpus"));
                assertEquals("\"owned-version\"", request.getHeaders().getValue(HttpHeaderName.IF_MATCH));
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                Map<?, ?> topicProfile = first(map(body.get("vectorSearch")).get("topicGraphProfiles"));
                assertEquals("owned-topicgraph", topicProfile.get("indexName"));
                assertEquals("balanced", topicProfile.get("defaultKind"));
                assertEquals(1000, topicProfile.get("defaultMinDiversityPoolSize"));
                Map<?, ?> vectorField = ((List<?>) body.get("fields")).stream()
                    .map(GraphPreviewSamplesTests::map)
                    .filter(field -> "content_vector".equals(field.get("name")))
                    .findFirst()
                    .get();
                assertEquals("hotel-topics", vectorField.get("topicGraphProfile"));
                assertEquals(dimensions, vectorField.get("dimensions"));
                assertEquals(Boolean.TRUE, vectorField.get("stored"));
                assertEquals(Boolean.FALSE, vectorField.get("retrievable"));
                stored.set(request.getBodyAsBinaryData().toString());
                return response(request, 200, stored.get());
            }
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            if (request.getUrl().getPath().contains("owned-topicgraph")) {
                return response(request, 200,
                    BinaryData.fromObject(TopicGraphPreviewExample.sidecar("owned-topicgraph", dimensions)).toString());
            }
            return response(request, 200, stored.get());
        });
        TopicGraphPreviewExample.linkOwnedIndexes(indexes, "owned-corpus", "owned-topicgraph");
        assertEquals(Arrays.asList(HttpMethod.GET, HttpMethod.GET, HttpMethod.PUT, HttpMethod.GET), methods);
    }

    @Test
    public void topicLinkRefusesUpdateWhenTheNewCorpusHasNoEtag() {
        AtomicInteger calls = new AtomicInteger();
        SearchIndexClient indexes = indexes(request -> {
            calls.incrementAndGet();
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            SearchIndex index = request.getUrl().getPath().contains("owned-topicgraph")
                ? TopicGraphPreviewExample.sidecar("owned-topicgraph", 1536)
                : TopicGraphPreviewExample.corpus("owned-corpus", 1536, new AzureOpenAIVectorizerParameters());
            return response(request, 200, BinaryData.fromObject(index).toString());
        });
        assertThrows(IllegalStateException.class,
            () -> TopicGraphPreviewExample.linkOwnedIndexes(indexes, "owned-corpus", "owned-topicgraph"));
        assertEquals(2, calls.get());
    }

    @Test
    public void topicSearchComparesAllModesAndLeavesTextQueryToInheritTheLinkedProfile() {
        AtomicInteger calls = new AtomicInteger();
        SearchClient search = indexes(request -> {
            int call = calls.getAndIncrement();
            Map<?, ?> query = first(request.getBodyAsBinaryData().toObject(Map.class).get("vectorQueries"));
            assertEquals("content_vector", query.get("fields"));
            assertEquals(6, query.get("k"));
            if (call < 3) {
                assertEquals(Arrays.asList(0.5, 0.75), query.get("vector"));
                Map<?, ?> mode = map(query.get("topicGraph"));
                assertEquals(Arrays.asList("none", "diverse", "balanced").get(call), mode.get("kind"));
                if (call > 0) {
                    assertEquals(500, mode.get("minDiversityPoolSize"));
                }
            } else {
                assertEquals("text", query.get("kind"));
                assertNotNull(query.get("text"));
                assertNull(query.get("topicGraph"));
            }
            List<Map<String, Object>> results = TopicGraphPreviewExample.documents().subList(0, 6);
            return response(request, 200, "{\"value\":" + BinaryData.fromObject(results) + "}");
        }).getSearchClient("owned-corpus");
        TopicGraphPreviewExample.compareSearch(search, Arrays.asList(0.5F, 0.75F));
        assertEquals(4, calls.get());
    }

    @Test
    public void topicSourceDefaultAndRuntimeOverridesAreIndependentIncludingPoolsAbove1000() {
        AtomicInteger retrieveCalls = new AtomicInteger();
        HttpClient transport = request -> {
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            if (body.containsKey("searchIndexParameters")) {
                Map<?, ?> parameters = map(body.get("searchIndexParameters"));
                assertEquals("owned-corpus", parameters.get("searchIndexName"));
                assertEquals("default", parameters.get("semanticConfigurationName"));
                Map<?, ?> mode = map(parameters.get("topicGraph"));
                assertEquals("diverse", mode.get("kind"));
                assertEquals(500, mode.get("minDiversityPoolSize"));
                assertEquals("none", body.get("resultsProcessing"));
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            int call = retrieveCalls.getAndIncrement();
            Map<?, ?> source = first(body.get("knowledgeSourceParams"));
            assertEquals("owned-source", source.get("knowledgeSourceName"));
            assertEquals(Boolean.TRUE, source.get("alwaysQuerySource"));
            assertEquals(Boolean.TRUE, source.get("failOnError"));
            if (call == 0) {
                assertNull(source.get("topicGraph"));
            } else {
                Map<?, ?> mode = map(source.get("topicGraph"));
                assertEquals(Arrays.asList("unused", "diverse", "balanced", "none").get(call), mode.get("kind"));
                if (call == 1) {
                    assertEquals(2000, mode.get("minDiversityPoolSize"));
                }
            }
            return response(request, 200, "{\"response\":[],\"activity\":[],\"references\":[]}");
        };
        indexes(transport)
            .createKnowledgeSource(TopicGraphPreviewExample.knowledgeSource("owned-source", "owned-corpus"));
        KnowledgeBaseRetrievalClient retrieval = new KnowledgeBaseRetrievalClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("key"))
            .knowledgeBaseName("owned-kb")
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(transport)
            .buildClient();
        for (VectorQueryTopicGraph mode : Arrays.asList(null,
            new DiverseVectorQueryTopicGraph().setDiversityFactor(0.25F).setMinDiversityPoolSize(2000),
            new BalancedVectorQueryTopicGraph(), new NoneVectorQueryTopicGraph())) {
            retrieval.retrieve(TopicGraphPreviewExample.retrievalOptions("owned-source", mode));
        }
        assertEquals(4, retrieveCalls.get());
    }

    @Test
    public void embeddingRequestSendsMatchingDimensionsAndParsesTheResponseFromItsBeginning() throws IOException {
        List<String> texts = Arrays.asList("A synthetic hotel.", "Which hotel?");
        HttpRequest request = TopicGraphPreviewExample.embeddingRequest("https://example.openai.azure.com/",
            "hotel-embedding", "synthetic-key", texts, 2);
        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals("/openai/deployments/hotel-embedding/embeddings", request.getUrl().getPath());
        assertEquals("api-version=2024-02-01", request.getUrl().getQuery());
        Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
        assertEquals(texts, body.get("input"));
        assertEquals(2, body.get("dimensions"));
        assertEquals("float", body.get("encoding_format"));
        BinaryData response = BinaryData.fromString(
            " \n{\"data\":[{\"index\":0,\"embedding\":[0.1,0.2]}," + "{\"index\":1,\"embedding\":[0.3,0.4]}]}");
        assertEquals(Arrays.asList(Arrays.asList(0.1F, 0.2F), Arrays.asList(0.3F, 0.4F)),
            TopicGraphPreviewExample.readEmbeddings(response, texts.size(), 2));
    }

    @Test
    public void embeddingResponseIsReorderedAndMismatchedDimensionsOrDuplicateIndicesFailBeforeUpload() {
        BinaryData response = BinaryData.fromString(
            "{\"data\":[{\"index\":1,\"embedding\":[0.3,0.4]}," + "{\"index\":0,\"embedding\":[0.1,0.2]}]}");
        List<List<Float>> vectors = TopicGraphPreviewExample.readEmbeddings(response, 2, 2);
        assertEquals(Arrays.asList(0.1F, 0.2F), vectors.get(0));
        assertEquals(Arrays.asList(0.3F, 0.4F), vectors.get(1));
        assertThrows(IllegalStateException.class, () -> TopicGraphPreviewExample.readEmbeddings(response, 2, 3));
        assertThrows(IllegalStateException.class, () -> TopicGraphPreviewExample.readEmbeddings(response, 3, 2));
        BinaryData duplicate = BinaryData.fromString(
            "{\"data\":[{\"index\":0,\"embedding\":[0.1,0.2]}," + "{\"index\":0,\"embedding\":[0.3,0.4]}]}");
        assertThrows(IllegalStateException.class, () -> TopicGraphPreviewExample.readEmbeddings(duplicate, 2, 2));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "/", "\\", "/shared", "\\shared", "//" })
    public void extractionRejectsContainerRootAndUnapprovedScopesBeforeResourceCreation(String folder) {
        assertThrows(IllegalArgumentException.class,
            () -> GraphExtractionCachePreviewExample.approvedFolder(folder, "true"));
        assertThrows(IllegalArgumentException.class,
            () -> GraphExtractionCachePreviewExample.approvedFolder("approved/hotels", "false"));
    }

    @Test
    public void extractionSourcePreservesApprovedFolderInlineOntologyAndWriteableCacheConfiguration() {
        AtomicInteger calls = new AtomicInteger();
        SearchIndexClient indexes = indexes(request -> {
            calls.incrementAndGet();
            assertEquals(HttpMethod.POST, request.getHttpMethod());
            assertTrue(request.getUrl().getPath().endsWith("/knowledgesources"));
            Map<?, ?> parameters = map(request.getBodyAsBinaryData().toObject(Map.class).get("azureBlobParameters"));
            assertEquals("approved/hotels", parameters.get("folderPath"));
            assertEquals(Boolean.FALSE, parameters.get("isADLSGen2"));
            Map<?, ?> cache = map(map(parameters.get("ingestionParameters")).get("cache"));
            assertEquals("synthetic-cache-connection", cache.get("storageConnectionString"));
            assertEquals(Boolean.TRUE, cache.get("enableReprocessing"));
            Map<?, ?> ontology = map(map(parameters.get("graphExtraction")).get("ontology"));
            assertEquals("inline", ontology.get("kind"));
            Map<?, ?> value = BinaryData.fromString((String) ontology.get("value")).toObject(Map.class);
            assertEquals("https://schema.search.azure.com/graph-extraction#", map(value.get("@context")).get("ekg"));
            List<?> nodes = (List<?>) value.get("@graph");
            Map<?, ?> relationship = nodes.stream()
                .map(GraphPreviewSamplesTests::map)
                .filter(node -> "ekg:operatedBy".equals(node.get("@id")))
                .findFirst()
                .get();
            assertEquals("owl:ObjectProperty", relationship.get("@type"));
            assertEquals("ekg:Hotel", map(relationship.get("rdfs:domain")).get("@id"));
            assertEquals("ekg:Organization", map(relationship.get("rdfs:range")).get("@id"));
            return response(request, 201, request.getBodyAsBinaryData().toString());
        });
        String folder = GraphExtractionCachePreviewExample.approvedFolder(" approved/hotels ", "true");
        indexes.createKnowledgeSource(GraphExtractionCachePreviewExample.knowledgeSource("owned-source", "hotels",
            "synthetic-source-connection", "approved-container", folder, new KnowledgeSourceIngestionParameters()
                .setCache(new KnowledgeSourceCache("synthetic-cache-connection").setEnableReprocessing(true))));
        assertEquals(1, calls.get());
    }

    @Test
    public void extractionRejectsUnrelatedGeneratedResources() {
        Map<String, String> resources = new LinkedHashMap<>();
        for (String kind : Arrays.asList("datasource", "indexer", "skillset", "index", "graphIndex")) {
            resources.put(kind, "owned-source-" + kind.toLowerCase());
        }
        assertDoesNotThrow(
            () -> GraphExtractionCachePreviewExample.validateGeneratedResources("owned-source", resources));
        resources.put("graphIndex", "preexisting-shared-index");
        assertThrows(IllegalStateException.class,
            () -> GraphExtractionCachePreviewExample.validateGeneratedResources("owned-source", resources));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void extractionChecksIndexerFailuresEvenWhenGraphSynchronizationCompleted(int failures) {
        HttpClient transport = request -> {
            if (request.getUrl().getPath().contains("knowledgesources")) {
                return response(request, 200,
                    "{\"synchronizationStatus\":\"active\",\"lastSynchronizationState\":{"
                        + "\"startTime\":\"2026-11-01T00:00:00Z\",\"endTime\":\"2026-11-01T00:01:00Z\","
                        + "\"itemsUpdatesProcessed\":2,\"itemsUpdatesFailed\":0,\"itemsSkipped\":0}}");
            }
            return response(request, 200, "{\"name\":\"owned-indexer\",\"status\":\"running\","
                + "\"lastResult\":{\"status\":\"success\",\"itemsProcessed\":2,\"itemsFailed\":" + failures + "}}");
        };
        SearchIndexerClient indexers = new SearchIndexerClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(transport)
            .buildClient();
        if (failures == 0) {
            assertDoesNotThrow(() -> GraphExtractionCachePreviewExample.waitForSynchronization(indexes(transport),
                indexers, "owned-source", "owned-indexer"));
        } else {
            assertThrows(IllegalStateException.class, () -> GraphExtractionCachePreviewExample
                .waitForSynchronization(indexes(transport), indexers, "owned-source", "owned-indexer"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "transientFailure", "persistentFailure" })
    public void extractionStopsOnFailedOrUnsupportedIndexerExecutionStatus(String status) {
        HttpClient transport = request -> response(request, 200,
            request.getUrl().getPath().contains("knowledgesources")
                ? "{\"synchronizationStatus\":\"active\"}"
                : "{\"name\":\"owned-indexer\",\"status\":\"running\",\"lastResult\":{\"status\":\"" + status + "\"}}");
        SearchIndexerClient indexers = new SearchIndexerClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(transport)
            .buildClient();
        assertThrows(IllegalStateException.class, () -> GraphExtractionCachePreviewExample
            .waitForSynchronization(indexes(transport), indexers, "owned-source", "owned-indexer"));
    }

    @Test
    public void extractedRelationshipsRequireMatchingSyntheticOperatorsAndEvidence() {
        assertDoesNotThrow(() -> GraphExtractionCachePreviewExample
            .validateRelationships(BinaryData.fromString(graphResult(true)).toObject(GraphQueryResult.class)));
        assertThrows(IllegalStateException.class, () -> GraphExtractionCachePreviewExample
            .validateRelationships(BinaryData.fromString(graphResult(false)).toObject(GraphQueryResult.class)));
        assertThrows(IllegalStateException.class,
            () -> GraphExtractionCachePreviewExample.validateRelationships(
                BinaryData.fromString(graphResult(true).replace("Contoso Hospitality", "Wrong operator"))
                    .toObject(GraphQueryResult.class)));
    }

    @Test
    public void extractionRetrievalRequestsGeneratedGqlAndRejectsNonGraphReferences() {
        AtomicInteger calls = new AtomicInteger();
        KnowledgeBaseRetrievalClient retrieval = new KnowledgeBaseRetrievalClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("key"))
            .knowledgeBaseName("owned-kb")
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(request -> {
                calls.incrementAndGet();
                Map<?, ?> parameters
                    = first(request.getBodyAsBinaryData().toObject(Map.class).get("knowledgeSourceParams"));
                Map<?, ?> graph = map(parameters.get("graphParameters"));
                assertFalse(parameters.containsKey("alwaysQuerySource"));
                assertEquals(Boolean.TRUE, graph.get("includeQuery"));
                assertEquals("none", map(graph.get("provenance")).get("kind"));
                return response(request, 200,
                    "{\"response\":[],\"references\":[{\"id\":\"0\","
                        + "\"type\":\"graphSearch\",\"activitySource\":1,\"graphIndexName\":\"owned-graph\"}],"
                        + "\"activity\":[{\"id\":1,\"type\":\"graphSearch\",\"knowledgeSourceName\":\"owned-source\","
                        + "\"graphSearchArguments\":{\"gql\":\"MATCH (h:Hotel) RETURN h\"}}]}");
            })
            .buildClient();
        KnowledgeBaseRetrievalResult result
            = retrieval.retrieve(GraphExtractionCachePreviewExample.retrievalOptions("owned-source"));
        assertDoesNotThrow(() -> GraphExtractionCachePreviewExample.validateGraphRetrieval(result));
        KnowledgeBaseRetrievalResult empty
            = BinaryData.fromString("{\"response\":[],\"references\":[],\"activity\":[]}")
                .toObject(KnowledgeBaseRetrievalResult.class);
        assertThrows(IllegalStateException.class,
            () -> GraphExtractionCachePreviewExample.validateGraphRetrieval(empty));
        KnowledgeBaseRetrievalResult ordinarySearch = BinaryData
            .fromString("{\"references\":[{\"id\":\"0\",\"type\":\"azureBlob\",\"activitySource\":1}],"
                + "\"activity\":[{\"id\":1,\"type\":\"azureBlob\",\"knowledgeSourceName\":\"owned-source\"}]}")
            .toObject(KnowledgeBaseRetrievalResult.class);
        assertThrows(IllegalStateException.class,
            () -> GraphExtractionCachePreviewExample.validateGraphRetrieval(ordinarySearch));
        assertEquals(1, calls.get());
    }

    @Test
    public void extractionPersistsGraphRoutingGuidanceWithTheOwnedSourceAndModel() {
        AtomicInteger calls = new AtomicInteger();
        SearchIndexClient client = indexes(request -> {
            calls.incrementAndGet();
            assertEquals(HttpMethod.POST, request.getHttpMethod());
            assertEquals("/knowledgebases", request.getUrl().getPath());
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            assertEquals("owned-source", first(body.get("knowledgeSources")).get("name"));
            assertTrue(((String) body.get("retrievalInstructions")).contains("graph search"));
            assertTrue(((String) body.get("retrievalInstructions")).contains("operatedBy"));
            assertEquals("azureOpenAI", first(body.get("models")).get("kind"));
            return response(request, 201, request.getBodyAsBinaryData().toString());
        });
        client.createKnowledgeBase(GraphExtractionCachePreviewExample.knowledgeBase("owned-kb", "owned-source",
            new KnowledgeBaseAzureOpenAIModel(
                new AzureOpenAIVectorizerParameters().setResourceUrl("https://models.openai.azure.com")
                    .setDeploymentName("chat"))));
        assertEquals(1, calls.get());
    }

    @Test
    public void extractionRetrievalReportsActivityErrorCodesWithoutServiceErrorDetails() {
        KnowledgeBaseRetrievalResult result = BinaryData
            .fromString("{\"references\":[],\"activity\":[{\"id\":1,\"type\":\"graphSearch\","
                + "\"error\":{\"code\":\"ModelCallFailed\",\"message\":\"private-provider-detail\"}}]}")
            .toObject(KnowledgeBaseRetrievalResult.class);
        IllegalStateException failure = assertThrows(IllegalStateException.class,
            () -> GraphExtractionCachePreviewExample.validateGraphRetrieval(result));
        assertTrue(failure.getMessage().contains("ModelCallFailed"));
        assertFalse(failure.getMessage().contains("private-provider-detail"));
    }

    private static SearchIndexClient indexes(HttpClient transport) {
        return new SearchIndexClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(transport)
            .buildClient();
    }

    private static Mono<HttpResponse> response(HttpRequest request, int status, String body) {
        return Mono.just(new MockHttpResponse(request, status, body.getBytes(StandardCharsets.UTF_8)));
    }

    private static Map<?, ?> map(Object value) {
        return (Map<?, ?>) value;
    }

    private static Map<?, ?> first(Object value) {
        return map(((List<?>) value).get(0));
    }

    private static String uploadResponse(String first, String second) {
        return "{\"value\":[{\"key\":\"" + first + "\",\"status\":true,\"statusCode\":201}," + "{\"key\":\"" + second
            + "\",\"status\":true,\"statusCode\":201}]}";
    }

    private static String graphResult(boolean evidence) {
        return "{\"value\":[{\"hotel\":\"Historic Harbor Hotel\",\"organization\":\"Contoso Hospitality\","
            + "\"@search.graphProvenance\":{\"kind\":\"ids\",\"evidence\":"
            + (evidence
                ? "[{\"edgeId\":\"assertion-1\",\"chunkId\":\"harbor-operator\",\"parentId\":\"harbor-brochure\"}]"
                : "[]")
            + "}}]}";
    }
}
