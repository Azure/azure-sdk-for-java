// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSource;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.ChatCompletionCommonModelParameters;
import com.azure.search.documents.indexes.models.ChatCompletionSkill;
import com.azure.search.documents.indexes.models.DataSourceCredentials;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseMicrosoftRetriever1Model;
import com.azure.search.documents.indexes.models.KnowledgeBaseOpenAICompatibleModel;
import com.azure.search.documents.indexes.models.KnowledgeBaseOpenAICompatibleParameters;
import com.azure.search.documents.indexes.models.KnowledgeBaseTool;
import com.azure.search.documents.indexes.models.KnowledgeBaseToolKnowledgeSourceReference;
import com.azure.search.documents.indexes.models.KnowledgeSourceGraphExtractionParameters;
import com.azure.search.documents.indexes.models.KnowledgeSourceGraphOntology;
import com.azure.search.documents.indexes.models.KnowledgeSourceGraphOntologySourceKind;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.OneLakeSecuritySource;
import com.azure.search.documents.indexes.models.OpenAICompatibleVectorizer;
import com.azure.search.documents.indexes.models.OpenAICompatibleVectorizerParameters;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.PermissionFilter;
import com.azure.search.documents.indexes.models.RandomScoringFunction;
import com.azure.search.documents.indexes.models.RandomScoringParameters;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHint;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHintAutoConfiguration;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceQueryHints;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;
import com.azure.search.documents.indexes.models.SearchIndexerDataUserAssignedIdentity;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.AgentRuntimeGraphParams;
import com.azure.search.documents.knowledgebases.models.AzureBlobKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAgenticReasoningActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseGraphSearchActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseGraphSearchReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalMinimalReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceCache;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceOpenAICompatibleVectorizer;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;
import com.azure.search.documents.models.AgenticSearchOptions;
import com.azure.search.documents.models.BalancedVectorQueryTopicGraph;
import com.azure.search.documents.models.DiverseVectorQueryTopicGraph;
import com.azure.search.documents.models.GraphOptions;
import com.azure.search.documents.models.GraphQueryDiagnostic;
import com.azure.search.documents.models.GraphQueryDiagnosticPhase;
import com.azure.search.documents.models.GraphQueryDiagnosticSeverity;
import com.azure.search.documents.models.GraphQueryResult;
import com.azure.search.documents.models.NoneVectorQueryTopicGraph;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchPagedResponse;
import com.azure.search.documents.models.VectorQueryTopicGraph;
import com.azure.search.documents.models.VectorizedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreviewFeatureContractTests {
    private static final String ENDPOINT = "https://example.search.windows.net";
    private static final String IDENTITY
        = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/offline/providers/"
            + "Microsoft.ManagedIdentity/userAssignedIdentities/search";
    private static final String AUDIENCE = "11111111-1111-1111-1111-111111111111";
    private static final String SYNTHETIC_SECRET = "offline-synthetic-secret";
    private static final String ONTOLOGY = "{\"@context\":{\"rdfs\":\"http://www.w3.org/2000/01/rdf-schema#\"},"
        + "\"@graph\":[{\"@id\":\"https://example.com/Person\",\"@type\":\"rdfs:Class\"}]}";

    @ParameterizedTest
    @MethodSource("topicGraphQueries")
    public void searchOptionsForwardTopicGraphModesAndTheirAllowedPropertySubsets(VectorQueryTopicGraph topicGraph,
        String expectedKind, Float diversityFactor, Integer poolSize) {
        AtomicInteger requests = new AtomicInteger();
        SearchClientBuilder builder = searchBuilder(request -> {
            requests.incrementAndGet();
            assertRequest(request, HttpMethod.POST, "/indexes('preview')/docs/search.post.search");
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            assertEquals("connections", body.get("search"));
            Map<?, ?> query = firstMap(body.get("vectorQueries"));
            assertEquals("vector", query.get("kind"));
            assertEquals("embedding", query.get("fields"));
            assertEquals(3, ((Number) query.get("k")).intValue());
            assertEquals(Arrays.asList(0.25, 0.5), query.get("vector"));
            Map<?, ?> graph = map(query.get("topicGraph"));
            assertEquals(expectedKind, graph.get("kind"));
            if (diversityFactor == null) {
                assertFalse(graph.containsKey("diversityFactor"));
            } else {
                assertEquals(diversityFactor.doubleValue(), ((Number) graph.get("diversityFactor")).doubleValue());
            }
            if (poolSize == null) {
                assertFalse(graph.containsKey("minDiversityPoolSize"));
            } else {
                assertEquals(poolSize.intValue(), ((Number) graph.get("minDiversityPoolSize")).intValue());
            }
            return Mono.just(response(request, 200, "{\"value\":[]}"));
        });
        SearchOptions options = new SearchOptions().setSearchText("connections")
            .setVectorQueries(new VectorizedQuery(Arrays.asList(0.25F, 0.5F)).setFields("embedding")
                .setKNearestNeighbors(3)
                .setTopicGraph(topicGraph));
        assertEquals(0, builder.buildClient().search(options).stream().count());
        assertEquals(0, builder.buildAsyncClient().search(options).collectList().block().size());
        assertEquals(2, requests.get());
    }

    private static Stream<Arguments> topicGraphQueries() {
        return Stream.of(Arguments.of(new NoneVectorQueryTopicGraph(), "none", null, null),
            Arguments.of(new DiverseVectorQueryTopicGraph(), "diverse", null, null),
            Arguments.of(new DiverseVectorQueryTopicGraph().setDiversityFactor(0F).setMinDiversityPoolSize(1000),
                "diverse", 0F, 1000),
            Arguments.of(new DiverseVectorQueryTopicGraph().setDiversityFactor(1F).setMinDiversityPoolSize(1000),
                "diverse", 1F, 1000),
            Arguments.of(new BalancedVectorQueryTopicGraph().setMinDiversityPoolSize(1000), "balanced", null, 1000));
    }

    @ParameterizedTest
    @MethodSource("graphDiagnostics")
    public void graphResponsesPreserveDiagnosticSeverityPhaseAndFutureEnumValues(String severity, String phase) {
        SearchClientBuilder builder = searchBuilder(request -> Mono.just(response(request, 200,
            "{\"value\":[],\"@search.graphDiagnostics\":[{\"code\":\"Diagnostic\",\"severity\":\"" + severity
                + "\",\"phase\":\"" + phase + "\"}]}")));
        GraphOptions options = new GraphOptions("MATCH (n) RETURN n").setIncludeDiagnostics(true);
        assertDiagnostic(builder.buildClient().graph(options), severity, phase);
        assertDiagnostic(builder.buildAsyncClient().graph(options).block(), severity, phase);
    }

    private static Stream<Arguments> graphDiagnostics() {
        return Stream.of(Arguments.of("info", "planning"), Arguments.of("warning", "validation"),
            Arguments.of("error", "execution"), Arguments.of("futureSeverity", "futurePhase"));
    }

    private static void assertDiagnostic(GraphQueryResult result, String severity, String phase) {
        assertNotNull(result);
        GraphQueryDiagnostic diagnostic = result.getDiagnostics().get(0);
        assertEquals(GraphQueryDiagnosticSeverity.fromString(severity), diagnostic.getSeverity());
        assertEquals(GraphQueryDiagnosticPhase.fromString(phase), diagnostic.getPhase());
    }

    @Test
    public void randomScoringProfileAndSearchSeedReachTheService() {
        SearchIndex index = new SearchIndex("preview", new SearchField("id", SearchFieldDataType.STRING).setKey(true))
            .setScoringProfiles(new ScoringProfile("shuffle")
                .setFunctions(new RandomScoringFunction().setParameters(new RandomScoringParameters("seed"))));
        SearchIndexClientBuilder indexes = indexBuilder(request -> {
            assertRequest(request, HttpMethod.POST, "/indexes");
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            Map<?, ?> profile = firstMap(body.get("scoringProfiles"));
            assertEquals("shuffle", profile.get("name"));
            Map<?, ?> function = firstMap(profile.get("functions"));
            assertEquals("random", function.get("type"));
            assertEquals("seed", map(function.get("random")).get("seedParameter"));
            assertFalse(function.containsKey("parameters"));
            assertFalse(function.containsKey("fieldName"));
            assertFalse(function.containsKey("boost"));
            return Mono.just(response(request, 201, request.getBodyAsBinaryData().toString()));
        });
        assertInstanceOf(RandomScoringFunction.class,
            indexes.buildClient().createIndex(index).getScoringProfiles().get(0).getFunctions().get(0));

        SearchClientBuilder search = searchBuilder(request -> {
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            assertEquals("shuffle", body.get("scoringProfile"));
            assertEquals(Collections.singletonList("seed:-9223372036854775808"), body.get("scoringParameters"));
            return Mono.just(response(request, 200, "{\"value\":[]}"));
        });
        SearchOptions options = new SearchOptions().setSearchText("*")
            .setScoringProfile("shuffle")
            .setScoringParameters("seed:-9223372036854775808");
        assertEquals(0, search.buildClient().search(options).stream().count());
        assertEquals(0, search.buildAsyncClient().search(options).collectList().block().size());
    }

    @ParameterizedTest
    @MethodSource("compatibleModels")
    public void knowledgeBaseToolsAndCompatibleModelAuthenticationSurviveTypedRequests(AzureOpenAIModelName modelName,
        String uri, boolean managedIdentity) {
        KnowledgeBaseOpenAICompatibleParameters parameters
            = new KnowledgeBaseOpenAICompatibleParameters(uri, modelName);
        if (managedIdentity) {
            parameters.setManagedIdentityAudience(AUDIENCE)
                .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(IDENTITY));
        } else {
            parameters.setHeaders(Collections.singletonMap("X-Offline-Authorization", SYNTHETIC_SECRET));
        }
        KnowledgeBaseTool tool = new KnowledgeBaseTool("lookup", "Find source documents",
            Collections.singletonList(new KnowledgeBaseToolKnowledgeSourceReference("source").setAlwaysQuerySource(true)
                .setFilterAddOn("category eq 'public'")
                .setMaxOutputDocuments(3)
                .setFailOnError(false)));
        KnowledgeBase base = new KnowledgeBase("preview", new KnowledgeSourceReference("source"))
            .setModels(new KnowledgeBaseOpenAICompatibleModel(parameters))
            .setTools(Collections.singletonList(tool));
        AtomicInteger requests = new AtomicInteger();
        SearchIndexClientBuilder builder = indexBuilder(request -> {
            requests.incrementAndGet();
            assertRequest(request, HttpMethod.POST, "/knowledgebases");
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            Map<?, ?> model = firstMap(body.get("models"));
            assertEquals("openAICompatible", model.get("kind"));
            Map<?, ?> wireParameters = map(model.get("openAICompatibleParameters"));
            assertEquals(uri, wireParameters.get("uri"));
            assertEquals(modelName.toString(), wireParameters.get("modelName"));
            if (managedIdentity) {
                assertEquals(AUDIENCE, wireParameters.get("managedIdentityAudience"));
                assertEquals(IDENTITY, map(wireParameters.get("authIdentity")).get("userAssignedIdentity"));
                assertFalse(wireParameters.containsKey("headers"));
            } else {
                assertEquals(SYNTHETIC_SECRET, map(wireParameters.get("headers")).get("X-Offline-Authorization"));
                assertFalse(wireParameters.containsKey("managedIdentityAudience"));
                assertFalse(wireParameters.containsKey("authIdentity"));
            }
            Map<?, ?> wireTool = firstMap(body.get("tools"));
            assertEquals("lookup", wireTool.get("name"));
            Map<?, ?> source = firstMap(wireTool.get("knowledgeSources"));
            assertEquals("source", source.get("name"));
            assertEquals("category eq 'public'", source.get("filterAddOn"));
            assertEquals(Boolean.TRUE, source.get("alwaysQuerySource"));
            assertEquals(Boolean.FALSE, source.get("failOnError"));
            assertEquals(3, ((Number) source.get("maxOutputDocuments")).intValue());
            String returned = "{\"name\":\"preview\",\"knowledgeSources\":[{\"name\":\"source\"}],"
                + "\"models\":[{\"kind\":\"openAICompatible\",\"openAICompatibleParameters\":{\"uri\":\"" + uri
                + "\",\"modelName\":\"" + modelName + "\"}}]}";
            return Mono.just(response(request, 201, returned));
        });
        assertRedactedCompatibleModel(builder.buildClient().createKnowledgeBase(base));
        assertRedactedCompatibleModel(builder.buildAsyncClient().createKnowledgeBase(base).block());
        assertEquals(2, requests.get());
    }

    private static Stream<Arguments> compatibleModels() {
        return Stream.of(
            Arguments.of(AzureOpenAIModelName.GPT4O, "https://example.com/chat/completions?route=offline", false),
            Arguments.of(AzureOpenAIModelName.GPT6SOL, "https://example.com/responses?route=offline", false),
            Arguments.of(AzureOpenAIModelName.GPT6LUNA, "https://example.com/responses?route=offline", true));
    }

    private static void assertRedactedCompatibleModel(KnowledgeBase base) {
        assertNotNull(base);
        KnowledgeBaseOpenAICompatibleModel model
            = assertInstanceOf(KnowledgeBaseOpenAICompatibleModel.class, base.getModels().get(0));
        assertNull(model.getOpenAICompatibleParameters().getHeaders());
    }

    @Test
    public void minimalKnowledgeBaseForwardsMicrosoftRetrieverWithoutAzureOpenAIConfiguration() {
        SearchIndexClientBuilder builder = indexBuilder(request -> {
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            assertEquals("minimal", body.get("name"));
            Map<?, ?> model = firstMap(body.get("models"));
            assertEquals(Collections.singletonMap("kind", "microsoftRetriever1"), model);
            assertEquals("minimal", map(body.get("retrievalReasoningEffort")).get("kind"));
            assertFalse(body.containsKey("tools"));
            return Mono.just(response(request, 201, request.getBodyAsBinaryData().toString()));
        });
        KnowledgeBase base = new KnowledgeBase("minimal", new KnowledgeSourceReference("source"))
            .setModels(new KnowledgeBaseMicrosoftRetriever1Model())
            .setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort());
        assertInstanceOf(KnowledgeBaseMicrosoftRetriever1Model.class,
            builder.buildClient().createKnowledgeBase(base).getModels().get(0));
    }

    @Test
    public void retrievalForwardsSingularToolAndGraphOverridesAndReadsMixedActivityTypes() {
        KnowledgeBaseRetrievalClientBuilder builder = new KnowledgeBaseRetrievalClientBuilder().endpoint(ENDPOINT)
            .knowledgeBaseName("preview")
            .credential(new AzureKeyCredential("offline-test-key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(request -> {
                assertEquals(HttpMethod.POST, request.getHttpMethod());
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                assertEquals("lookup", body.get("toolName"));
                assertFalse(body.containsKey("toolNames"));
                Map<?, ?> source = firstMap(body.get("knowledgeSourceParams"));
                assertEquals("azureBlob", source.get("kind"));
                assertEquals(Boolean.TRUE, map(source.get("graphParameters")).get("includeQuery"));
                assertEquals("none", map(source.get("topicGraph")).get("kind"));
                assertEquals("offline-source-token",
                    request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-query-source-authorization")));
                return Mono.just(response(request, 200,
                    "{\"response\":[],\"activity\":[{\"type\":\"agenticReasoning\",\"id\":1,\"reasoningTokens\":17},"
                        + "{\"type\":\"graphSearch\",\"id\":2,\"knowledgeSourceName\":\"source\","
                        + "\"graphSearchArguments\":{\"gql\":\"MATCH (n) RETURN n\",\"graphIndexName\":\"graph\","
                        + "\"outcome\":\"Success\",\"attempts\":1}}],"
                        + "\"references\":[{\"type\":\"graphSearch\",\"id\":\"ref-1\",\"activitySource\":2,"
                        + "\"graphIndexName\":\"graph\"}]}"));
            });
        KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions().setToolName("lookup")
            .setIncludeActivity(true)
            .setQuerySourceAuthorization("offline-source-token")
            .setKnowledgeSourceParams(Collections.singletonList(new AzureBlobKnowledgeSourceParams("source")
                .setGraphParameters(new AgentRuntimeGraphParams().setIncludeQuery(true))
                .setTopicGraph(new NoneVectorQueryTopicGraph())));
        assertRetrievalActivity(builder.buildClient().retrieve(options));
        assertRetrievalActivity(builder.buildAsyncClient().retrieve(options).block());
    }

    private static void assertRetrievalActivity(KnowledgeBaseRetrievalResult result) {
        assertNotNull(result);
        KnowledgeBaseAgenticReasoningActivityRecord reasoning
            = assertInstanceOf(KnowledgeBaseAgenticReasoningActivityRecord.class, result.getActivity().get(0));
        assertEquals(17, reasoning.getReasoningTokens());
        KnowledgeBaseGraphSearchActivityRecord graph
            = assertInstanceOf(KnowledgeBaseGraphSearchActivityRecord.class, result.getActivity().get(1));
        assertEquals("MATCH (n) RETURN n", graph.getGraphSearchArguments().getGql());
        KnowledgeBaseGraphSearchReference reference
            = assertInstanceOf(KnowledgeBaseGraphSearchReference.class, result.getReferences().get(0));
        assertEquals("graph", reference.getGraphIndexName());
    }

    @Test
    public void directAgenticSearchForwardsOptionsAndReadsRenamedReasoningUsage() {
        SearchClientBuilder builder = searchBuilder(request -> {
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            assertEquals("agentic", body.get("queryType"));
            assertEquals(30, ((Number) map(body.get("agenticOptions")).get("maxRuntimeInSeconds")).intValue());
            return Mono.just(response(request, 200,
                "{\"value\":[],\"@search.debug\":{\"agentic\":{\"terminationReason\":\"completed\","
                    + "\"subQueries\":[{\"search\":\"connections\",\"iteration\":1}],"
                    + "\"usage\":{\"agenticPlanningTokens\":11,\"reasoningTokens\":29}}}}"));
        });
        SearchOptions options = new SearchOptions().setSearchText("connections")
            .setQueryType(QueryType.AGENTIC)
            .setAgenticOptions(new AgenticSearchOptions().setMaxRuntimeInSeconds(30));
        assertAgenticUsage(builder.buildClient().search(options).iterableByPage().iterator().next());
        SearchPagedResponse async = builder.buildAsyncClient().search(options).byPage().blockFirst();
        assertAgenticUsage(async);
    }

    private static void assertAgenticUsage(SearchPagedResponse page) {
        assertNotNull(page);
        assertEquals(11L, page.getDebugInfo().getAgentic().getUsage().getAgenticPlanningTokens());
        assertEquals(29L, page.getDebugInfo().getAgentic().getUsage().getReasoningTokens());
        assertEquals("connections", page.getDebugInfo().getAgentic().getSubQueries().get(0).getSearch());
    }

    @Test
    public void knowledgeSourceForwardsGraphExtractionCacheAndCompatibleIngestionModels() {
        OpenAICompatibleVectorizerParameters embedding
            = new OpenAICompatibleVectorizerParameters("https://example.com/embeddings?route=offline",
                AzureOpenAIModelName.TEXT_EMBEDDING3SMALL).setDimensions(1536)
                    .setHeaders(Collections.singletonMap("X-Offline-Authorization", SYNTHETIC_SECRET));
        KnowledgeSourceIngestionParameters ingestion = new KnowledgeSourceIngestionParameters()
            .setCache(new KnowledgeSourceCache("ResourceId=/subscriptions/offline/storageAccounts/cache")
                .setEnableReprocessing(true)
                .setIdentity(new SearchIndexerDataUserAssignedIdentity(IDENTITY)))
            .setEmbeddingModel(new KnowledgeSourceOpenAICompatibleVectorizer(embedding))
            .setChatCompletionModel(new KnowledgeBaseOpenAICompatibleModel(new KnowledgeBaseOpenAICompatibleParameters(
                "https://example.com/responses", AzureOpenAIModelName.GPT6SOL)
                    .setHeaders(Collections.singletonMap("X-Offline-Authorization", SYNTHETIC_SECRET))));
        AzureBlobKnowledgeSource source = new AzureBlobKnowledgeSource("source",
            new AzureBlobKnowledgeSourceParameters("ResourceId=/subscriptions/offline/storageAccounts/source", "docs")
                .setIngestionParameters(ingestion)
                .setGraphExtraction(new KnowledgeSourceGraphExtractionParameters("graph").setOntology(
                    new KnowledgeSourceGraphOntology(KnowledgeSourceGraphOntologySourceKind.INLINE, ONTOLOGY)
                        .setName("people"))));
        SearchIndexClientBuilder builder = indexBuilder(request -> {
            assertRequest(request, HttpMethod.POST, "/knowledgesources");
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            Map<?, ?> blob = map(body.get("azureBlobParameters"));
            Map<?, ?> graph = map(blob.get("graphExtraction"));
            assertEquals("graph", graph.get("graphName"));
            assertEquals("people", map(graph.get("ontology")).get("name"));
            assertEquals(ONTOLOGY, map(graph.get("ontology")).get("value"));
            Map<?, ?> parameters = map(blob.get("ingestionParameters"));
            Map<?, ?> cache = map(parameters.get("cache"));
            assertEquals("ResourceId=/subscriptions/offline/storageAccounts/cache",
                cache.get("storageConnectionString"));
            assertEquals(Boolean.TRUE, cache.get("enableReprocessing"));
            assertEquals(IDENTITY, map(cache.get("identity")).get("userAssignedIdentity"));
            assertEquals("openAICompatible", map(parameters.get("embeddingModel")).get("kind"));
            assertEquals(SYNTHETIC_SECRET,
                map(map(map(parameters.get("embeddingModel")).get("openAICompatibleParameters")).get("headers"))
                    .get("X-Offline-Authorization"));
            assertEquals("https://example.com/responses",
                map(map(parameters.get("chatCompletionModel")).get("openAICompatibleParameters")).get("uri"));
            return Mono.just(response(request, 201,
                "{\"name\":\"source\",\"kind\":\"azureBlob\",\"azureBlobParameters\":{\"containerName\":\"docs\","
                    + "\"ingestionParameters\":{\"cache\":{\"enableReprocessing\":true}}}}"));
        });
        AzureBlobKnowledgeSource returned
            = assertInstanceOf(AzureBlobKnowledgeSource.class, builder.buildClient().createKnowledgeSource(source));
        assertNull(returned.getAzureBlobParameters().getIngestionParameters().getCache().getStorageConnectionString());
        assertInstanceOf(AzureBlobKnowledgeSource.class,
            builder.buildAsyncClient().createKnowledgeSource(source).block());
    }

    @Test
    public void graphOntologyStatisticsAreRetainedThroughKnowledgeSourceStatusResponses() {
        SearchIndexClientBuilder builder = indexBuilder(request -> {
            assertRequest(request, HttpMethod.GET, "/knowledgesources('source')/status");
            return Mono.just(response(request, 200,
                "{\"kind\":\"azureBlob\",\"synchronizationStatus\":\"active\","
                    + "\"currentSynchronizationState\":{\"startTime\":\"2026-11-01T00:00:00Z\","
                    + "\"itemsUpdatesProcessed\":2,\"itemsUpdatesFailed\":0,\"itemsSkipped\":0,"
                    + "\"graphStatistics\":{\"ontologyName\":\"people\",\"triplesWritten\":42}},"
                    + "\"lastSynchronizationState\":{\"startTime\":\"2026-11-01T00:00:00Z\","
                    + "\"endTime\":\"2026-11-01T00:01:00Z\",\"itemsUpdatesProcessed\":2,"
                    + "\"itemsUpdatesFailed\":0,\"itemsSkipped\":0,"
                    + "\"graphStatistics\":{\"ontologyName\":\"people\",\"triplesWritten\":41}}}"));
        });
        assertGraphStatistics(builder.buildClient().getKnowledgeSourceStatus("source"));
        assertGraphStatistics(builder.buildAsyncClient().getKnowledgeSourceStatus("source").block());
    }

    private static void assertGraphStatistics(KnowledgeSourceStatus status) {
        assertNotNull(status);
        assertEquals("people", status.getCurrentSynchronizationState().getGraphStatistics().getOntologyName());
        assertEquals(42L, status.getCurrentSynchronizationState().getGraphStatistics().getTriplesWritten());
        assertEquals(41L, status.getLastSynchronizationState().getGraphStatistics().getTriplesWritten());
    }

    @Test
    public void facetBackedFilterHintsAndStoredTopicGraphReachKnowledgeSourceRequests() {
        SearchIndexKnowledgeSource source = new SearchIndexKnowledgeSource("source",
            new SearchIndexKnowledgeSourceParameters("preview")
                .setTopicGraph(new BalancedVectorQueryTopicGraph().setMinDiversityPoolSize(1000))
                .setQueryHints(new SearchIndexKnowledgeSourceQueryHints().setFilters(
                    Collections.singletonList(new SearchIndexKnowledgeSourceFilterHint("category").setAutoConfiguration(
                        new SearchIndexKnowledgeSourceFilterHintAutoConfiguration().setCacheTtl(900)
                            .setMaxFacetValueCount(32))))));
        SearchIndexClientBuilder builder = indexBuilder(request -> {
            Map<?, ?> parameters = map(request.getBodyAsBinaryData().toObject(Map.class).get("searchIndexParameters"));
            assertEquals("balanced", map(parameters.get("topicGraph")).get("kind"));
            Map<?, ?> filter = firstMap(map(parameters.get("queryHints")).get("filters"));
            assertEquals("category", filter.get("field"));
            Map<?, ?> configuration = map(filter.get("autoConfiguration"));
            assertEquals(900, ((Number) configuration.get("cacheTtl")).intValue());
            assertEquals(32, ((Number) configuration.get("maxFacetValueCount")).intValue());
            return Mono.just(response(request, 201, request.getBodyAsBinaryData().toString()));
        });
        assertInstanceOf(SearchIndexKnowledgeSource.class, builder.buildClient().createKnowledgeSource(source));
        assertInstanceOf(SearchIndexKnowledgeSource.class,
            builder.buildAsyncClient().createKnowledgeSource(source).block());
    }

    @Test
    public void compatibleIndexVectorizerAndOneLakePermissionsReachIndexRequests() {
        SearchIndex index = new SearchIndex("preview", new SearchField("id", SearchFieldDataType.STRING).setKey(true),
            new SearchField("folders", SearchFieldDataType.collection(SearchFieldDataType.STRING)).setFilterable(true)
                .setPermissionFilter(PermissionFilter.ONE_LAKE_PARENT_FOLDERS))
                    .setOneLakeSecuritySource(new OneLakeSecuritySource("offline-workspace", "offline-lakehouse"))
                    .setVectorSearch(new VectorSearch().setVectorizers(new OpenAICompatibleVectorizer("compatible",
                        new OpenAICompatibleVectorizerParameters("https://example.com/embeddings?route=offline",
                            AzureOpenAIModelName.TEXT_EMBEDDING3SMALL).setDimensions(1536)
                                .setManagedIdentityAudience(AUDIENCE)
                                .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(IDENTITY)))));
        SearchIndexClientBuilder builder = indexBuilder(request -> {
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            Map<?, ?> folders = map(((List<?>) body.get("fields")).get(1));
            assertEquals("Collection(Edm.String)", folders.get("type"));
            assertEquals(Boolean.TRUE, folders.get("filterable"));
            assertEquals("oneLakeParentFolders", folders.get("permissionFilter"));
            assertEquals("offline-workspace", map(body.get("oneLakeSecuritySource")).get("fabricWorkspaceId"));
            Map<?, ?> vectorizer = firstMap(map(body.get("vectorSearch")).get("vectorizers"));
            assertEquals("openAICompatible", vectorizer.get("kind"));
            Map<?, ?> parameters = map(vectorizer.get("openAICompatibleParameters"));
            assertEquals("https://example.com/embeddings?route=offline", parameters.get("uri"));
            assertEquals(1536, ((Number) parameters.get("dimensions")).intValue());
            assertEquals(AUDIENCE, parameters.get("managedIdentityAudience"));
            assertEquals(IDENTITY, map(parameters.get("authIdentity")).get("userAssignedIdentity"));
            assertFalse(parameters.containsKey("headers"));
            return Mono.just(response(request, 201, request.getBodyAsBinaryData().toString()));
        });
        assertInstanceOf(OpenAICompatibleVectorizer.class,
            builder.buildClient().createIndex(index).getVectorSearch().getVectorizers().get(0));
        SearchIndex asyncIndex = builder.buildAsyncClient().createIndex(index).block();
        assertNotNull(asyncIndex);
        assertInstanceOf(OpenAICompatibleVectorizer.class, asyncIndex.getVectorSearch().getVectorizers().get(0));
    }

    @Test
    public void oneLakeDataSourceUsesItsWireDiscriminator() {
        SearchIndexerClientBuilder builder = new SearchIndexerClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("offline-test-key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(request -> {
                assertRequest(request, HttpMethod.POST, "/datasources");
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                assertEquals("onelake", body.get("type"));
                assertEquals("Files/documents", map(body.get("container")).get("name"));
                return Mono.just(response(request, 201, request.getBodyAsBinaryData().toString()));
            });
        SearchIndexerDataSourceConnection source
            = new SearchIndexerDataSourceConnection("onelake", SearchIndexerDataSourceType.ONE_LAKE,
                new DataSourceCredentials().setConnectionString("ResourceId=/subscriptions/offline/workspaces/source"),
                new SearchIndexerDataContainer("Files/documents"));
        assertEquals(SearchIndexerDataSourceType.ONE_LAKE,
            builder.buildClient().createDataSourceConnection(source).getType());
        SearchIndexerDataSourceConnection asyncSource
            = builder.buildAsyncClient().createDataSourceConnection(source).block();
        assertNotNull(asyncSource);
        assertEquals(SearchIndexerDataSourceType.ONE_LAKE, asyncSource.getType());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://example.com/chat/completions?route=offline",
            "https://example.com/responses?route=offline" })
    public void compatibleSkillForwardsExactUriAuthenticationAndNestedModelParameters(String uri) {
        ChatCompletionSkill skill = new ChatCompletionSkill(
            Collections.singletonList(new InputFieldMappingEntry("text").setSource("/document/content")),
            Collections.singletonList(new OutputFieldMappingEntry("response").setTargetName("generated")), uri)
                .setApiKey(SYNTHETIC_SECRET)
                .setCommonModelParameters(
                    new ChatCompletionCommonModelParameters().setModelName(AzureOpenAIModelName.GPT6SOL.toString())
                        .setMaxTokens(256))
                .setExtraParameters(Collections.singletonMap("reasoning", Collections.singletonMap("effort", "low")));
        SearchIndexerClientBuilder builder = new SearchIndexerClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("offline-test-key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(request -> {
                assertRequest(request, HttpMethod.POST, "/skillsets");
                Map<?, ?> wireSkill = firstMap(request.getBodyAsBinaryData().toObject(Map.class).get("skills"));
                assertEquals("#Microsoft.Skills.Custom.ChatCompletionSkill", wireSkill.get("@odata.type"));
                assertEquals(uri, wireSkill.get("uri"));
                assertEquals(SYNTHETIC_SECRET, wireSkill.get("apiKey"));
                Map<?, ?> common = map(wireSkill.get("commonModelParameters"));
                assertEquals(AzureOpenAIModelName.GPT6SOL.toString(), common.get("model"));
                assertEquals(256, ((Number) common.get("maxTokens")).intValue());
                assertEquals("low", map(map(wireSkill.get("extraParameters")).get("reasoning")).get("effort"));
                return Mono.just(response(request, 201, request.getBodyAsBinaryData().toString()));
            });
        SearchIndexerSkillset skillset = new SearchIndexerSkillset("compatible", skill);
        ChatCompletionSkill returned = assertInstanceOf(ChatCompletionSkill.class,
            builder.buildClient().createSkillset(skillset).getSkills().get(0));
        assertEquals(uri, returned.getUrl());
        SearchIndexerSkillset asyncSkillset = builder.buildAsyncClient().createSkillset(skillset).block();
        assertNotNull(asyncSkillset);
        assertEquals(uri, assertInstanceOf(ChatCompletionSkill.class, asyncSkillset.getSkills().get(0)).getUrl());
    }

    private static SearchClientBuilder searchBuilder(HttpClient transport) {
        return new SearchClientBuilder().endpoint(ENDPOINT)
            .indexName("preview")
            .credential(new AzureKeyCredential("offline-test-key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(transport);
    }

    private static SearchIndexClientBuilder indexBuilder(HttpClient transport) {
        return new SearchIndexClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("offline-test-key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(transport);
    }

    private static void assertRequest(HttpRequest request, HttpMethod method, String path) {
        assertEquals(method, request.getHttpMethod());
        assertEquals(path, request.getUrl().getPath());
        assertTrue(request.getUrl().getQuery().contains("api-version=2026-11-01-preview"));
    }

    private static Map<?, ?> map(Object value) {
        return assertInstanceOf(Map.class, value);
    }

    private static Map<?, ?> firstMap(Object value) {
        List<?> list = assertInstanceOf(List.class, value);
        assertFalse(list.isEmpty());
        return map(list.get(0));
    }

    private static MockHttpResponse response(HttpRequest request, int status, String body) {
        return new MockHttpResponse(request, status, body.getBytes(StandardCharsets.UTF_8));
    }
}
