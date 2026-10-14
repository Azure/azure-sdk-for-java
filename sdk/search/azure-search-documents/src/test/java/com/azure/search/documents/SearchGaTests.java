// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.FluxUtil;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.implementation.SearchUtils;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkill;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkillChunkingProperties;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkillChunkingUnit;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkillExtractionOptions;
import com.azure.search.documents.indexes.models.CorsOptions;
import com.azure.search.documents.indexes.models.FileKnowledgeSource;
import com.azure.search.documents.indexes.models.FileKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.KnowledgeSourceFileCapacity;
import com.azure.search.documents.indexes.models.KnowledgeSourceKind;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalLowReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalMediumReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Execution(ExecutionMode.CONCURRENT)
public class SearchGaTests {
    private static final String ENDPOINT = "https://example.search.windows.net";
    private static final AzureKeyCredential CREDENTIAL = new AzureKeyCredential("key");

    @Test
    public void latestServiceVersionIsGa() {
        assertEquals(SearchServiceVersion.V2026_10_01, SearchServiceVersion.getLatest());
        assertEquals("2026-10-01", SearchServiceVersion.getLatest().getVersion());
        for (SearchServiceVersion version : SearchServiceVersion.values()) {
            assertFalse(version.getVersion().contains("preview"));
        }
    }

    @Test
    public void searchOptionsForwardEveryGeneratedRequestProperty() throws IOException, ReflectiveOperationException {
        String json = "{\"count\":false,\"facets\":[\"category\"],\"filter\":\"category eq 'Hotel'\","
            + "\"highlight\":\"title,content\",\"highlightPostTag\":\"</b>\",\"highlightPreTag\":\"<b>\","
            + "\"minimumCoverage\":90.0,\"moreLikeThis\":\"hotel-1\",\"orderby\":\"title asc\","
            + "\"queryType\":\"semantic\",\"scoringStatistics\":\"global\",\"sessionId\":\"session\","
            + "\"scoringParameters\":[\"location--122,44\"],\"scoringProfile\":\"profile\",\"debug\":\"all\","
            + "\"search\":\"hotel\",\"searchFields\":\"title,content\",\"searchMode\":\"all\",\"select\":\"id,title\","
            + "\"skip\":0,\"top\":5,\"semanticConfiguration\":\"semantic\",\"semanticErrorHandling\":\"partial\","
            + "\"semanticMaxWaitInMilliseconds\":1000,\"semanticQuery\":\"Find hotels\",\"answers\":\"extractive\","
            + "\"captions\":\"extractive\",\"vectorQueries\":[{\"kind\":\"vector\",\"vector\":[0.1,0.2],"
            + "\"fields\":\"vector\",\"k\":3}],\"vectorFilterMode\":\"preFilter\"}";
        SearchRequest expected;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            expected = SearchRequest.fromJson(reader);
        }
        SearchOptions options = new SearchOptions();
        for (Method getter : SearchRequest.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(getter.getModifiers())
                || Modifier.isStatic(getter.getModifiers())
                || getter.getParameterCount() != 0
                || !(getter.getName().startsWith("get") || getter.getName().startsWith("is"))) {
                continue;
            }
            Object value = getter.invoke(expected);
            assertNotNull(value, "Populate the generated request property in the parity fixture: " + getter.getName());
            String property = getter.getName().substring(getter.getName().startsWith("is") ? 2 : 3);
            SearchOptions.class.getMethod("set" + property, getter.getReturnType()).invoke(options, value);
        }

        assertEquals(expected.toJsonString(), SearchUtils.fromSearchOptions(options).toJsonString());
    }

    @Test
    public void bothSearchClientsSendMoreLikeThis() {
        AtomicInteger requestCount = new AtomicInteger();
        HttpClient httpClient = request -> FluxUtil.collectBytesInByteBufferStream(request.getBody())
            .flatMap(body -> Mono.fromCallable(() -> {
                try (JsonReader reader = JsonProviders.createReader(body)) {
                    assertEquals("hotel-1", reader.readMap(JsonReader::readUntyped).get("moreLikeThis"));
                }
                requestCount.incrementAndGet();
                return new MockHttpResponse(request, 200, new HttpHeaders(),
                    "{\"value\":[]}".getBytes(StandardCharsets.UTF_8));
            }));
        SearchClientBuilder builder = new SearchClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .indexName("index")
            .httpClient(httpClient);
        SearchOptions options = new SearchOptions().setMoreLikeThis("hotel-1");

        assertEquals(0, builder.buildClient().search(options).stream().count());
        assertEquals(0L, builder.buildAsyncClient().search(options).count().block());
        assertEquals(2, requestCount.get());
    }

    @ParameterizedTest
    @MethodSource("searchAuthorizationOptions")
    public void bothSearchClientsForwardAuthorizationOnEveryPage(SearchOptions options, RequestOptions requestOptions,
        String querySourceAuthorization, String enableElevatedRead) {
        AtomicInteger requestCount = new AtomicInteger();
        HttpClient httpClient = request -> {
            assertEquals(querySourceAuthorization,
                request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-query-source-authorization")));
            assertEquals(enableElevatedRead,
                request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-enable-elevated-read")));
            String body = requestCount.incrementAndGet() % 2 != 0
                ? "{\"value\":[{\"@search.score\":1,\"id\":\"1\"}],"
                    + "\"@search.nextPageParameters\":{\"search\":\"hotel\"},"
                    + "\"@odata.nextLink\":\"https://example.search.windows.net/indexes('index')/docs/search"
                    + "?api-version=2026-10-01\"}"
                : "{\"value\":[{\"@search.score\":1,\"id\":\"2\"}]}";
            return Mono
                .just(new MockHttpResponse(request, 200, new HttpHeaders(), body.getBytes(StandardCharsets.UTF_8)));
        };
        SearchClientBuilder builder = new SearchClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .indexName("index")
            .httpClient(httpClient);

        assertEquals(2, builder.buildClient().search(options, requestOptions).stream().count());
        assertEquals(2L, builder.buildAsyncClient().search(options, requestOptions).count().block());
        assertEquals(4, requestCount.get());
    }

    private static Stream<Arguments> searchAuthorizationOptions() {
        return Stream.of(Arguments.of(null, null, null, null), Arguments.of(new SearchOptions(),
            new RequestOptions()
                .setHeader(HttpHeaderName.fromString("x-ms-query-source-authorization"), "request-token")
                .setHeader(HttpHeaderName.fromString("x-ms-enable-elevated-read"), "false"),
            "request-token", "false"),
            Arguments.of(new SearchOptions().setQuerySourceAuthorization("query-token").setEnableElevatedRead(true),
                null, "query-token", "true"),
            Arguments.of(new SearchOptions().setQuerySourceAuthorization("query-token").setEnableElevatedRead(false),
                new RequestOptions(), "query-token", "false"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("defaultClientRequests")
    public void defaultClientsSendGaApiVersion(String clientName, Consumer<HttpClient> sendRequest) {
        AtomicInteger requestCount = new AtomicInteger();
        HttpClient httpClient = request -> {
            assertEquals("api-version=2026-10-01", request.getUrl().getQuery());
            requestCount.incrementAndGet();
            return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                "{\"value\":[],\"response\":[],\"activity\":[],\"references\":[]}".getBytes(StandardCharsets.UTF_8)));
        };

        sendRequest.accept(httpClient);
        assertEquals(1, requestCount.get(), clientName);
    }

    private static Stream<Arguments> defaultClientRequests() {
        return Stream.of(
            Arguments.of("search sync",
                (Consumer<HttpClient>) httpClient -> new SearchClientBuilder().endpoint(ENDPOINT)
                    .credential(CREDENTIAL)
                    .indexName("index")
                    .httpClient(httpClient)
                    .buildClient()
                    .search(new SearchOptions().setSearchText("hotel"))
                    .stream()
                    .count()),
            Arguments.of("search async",
                (Consumer<HttpClient>) httpClient -> new SearchClientBuilder().endpoint(ENDPOINT)
                    .credential(CREDENTIAL)
                    .indexName("index")
                    .httpClient(httpClient)
                    .buildAsyncClient()
                    .search(new SearchOptions().setSearchText("hotel"))
                    .collectList()
                    .block()),
            Arguments.of("index sync",
                (Consumer<HttpClient>) httpClient -> new SearchIndexClientBuilder().endpoint(ENDPOINT)
                    .credential(CREDENTIAL)
                    .httpClient(httpClient)
                    .buildClient()
                    .listIndexes()
                    .stream()
                    .count()),
            Arguments.of("index async",
                (Consumer<HttpClient>) httpClient -> new SearchIndexClientBuilder().endpoint(ENDPOINT)
                    .credential(CREDENTIAL)
                    .httpClient(httpClient)
                    .buildAsyncClient()
                    .listIndexes()
                    .collectList()
                    .block()),
            Arguments.of("indexer sync",
                (Consumer<HttpClient>) httpClient -> new SearchIndexerClientBuilder().endpoint(ENDPOINT)
                    .credential(CREDENTIAL)
                    .httpClient(httpClient)
                    .buildClient()
                    .listIndexers()
                    .stream()
                    .count()),
            Arguments.of("indexer async",
                (Consumer<HttpClient>) httpClient -> new SearchIndexerClientBuilder().endpoint(ENDPOINT)
                    .credential(CREDENTIAL)
                    .httpClient(httpClient)
                    .buildAsyncClient()
                    .listIndexers()
                    .collectList()
                    .block()),
            Arguments.of("retrieval sync",
                (Consumer<HttpClient>) httpClient -> new KnowledgeBaseRetrievalClientBuilder().endpoint(ENDPOINT)
                    .credential(CREDENTIAL)
                    .knowledgeBaseName("kb")
                    .httpClient(httpClient)
                    .buildClient()
                    .retrieve(retrievalOptions())),
            Arguments.of("retrieval async",
                (Consumer<HttpClient>) httpClient -> new KnowledgeBaseRetrievalClientBuilder().endpoint(ENDPOINT)
                    .credential(CREDENTIAL)
                    .knowledgeBaseName("kb")
                    .httpClient(httpClient)
                    .buildAsyncClient()
                    .retrieve(retrievalOptions())
                    .block()));
    }

    @ParameterizedTest
    @EnumSource(SearchServiceVersion.class)
    public void explicitGaVersionsRemainSupported(SearchServiceVersion version) {
        AtomicInteger requestCount = new AtomicInteger();
        HttpClient httpClient = request -> {
            assertEquals("api-version=" + version.getVersion(), request.getUrl().getQuery());
            requestCount.incrementAndGet();
            return Mono
                .just(new MockHttpResponse(request, 200, new HttpHeaders(), "0".getBytes(StandardCharsets.UTF_8)));
        };
        SearchClientBuilder builder = new SearchClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIAL)
            .indexName("index")
            .httpClient(httpClient)
            .serviceVersion(version);

        assertEquals(0L, builder.buildClient().getDocumentCount());
        assertEquals(Long.valueOf(0), builder.buildAsyncClient().getDocumentCount().block());
        assertEquals(2, requestCount.get());
    }

    @Test
    public void retrievalOptionsUseGaTokenLimitWireName() throws IOException {
        KnowledgeBaseRetrievalOptions options = retrievalOptions();
        try (JsonReader reader = JsonProviders.createReader(options.toJsonString())) {
            Map<String, Object> json = reader.readMap(JsonReader::readUntyped);
            assertEquals(5001, ((Number) json.get("maxOutputSizeInTokens")).intValue());
            assertFalse(json.containsKey("maxOutputSize"));
        }
        try (JsonReader reader = JsonProviders.createReader(options.toJsonString())) {
            KnowledgeBaseRetrievalOptions roundTrip = KnowledgeBaseRetrievalOptions.fromJson(reader);
            assertEquals(5001, roundTrip.getMaxOutputSizeInTokens());
            assertEquals(5, roundTrip.getMaxOutputDocuments());
            assertInstanceOf(KnowledgeRetrievalLowReasoningEffort.class, roundTrip.getRetrievalReasoningEffort());
        }
    }

    @Test
    public void knowledgeBaseConfigurationRoundTripsGaProperties() throws IOException {
        KnowledgeBase knowledgeBase = new KnowledgeBase("kb", new KnowledgeSourceReference("source"))
            .setRetrievalReasoningEffort(new KnowledgeRetrievalMediumReasoningEffort())
            .setOutputMode(KnowledgeRetrievalOutputMode.ANSWER_SYNTHESIS)
            .setRetrievalInstructions("Search the configured knowledge sources.")
            .setAnswerInstructions("Include citations.")
            .setCorsOptions(new CorsOptions("https://app.contoso.com"));

        try (JsonReader reader = JsonProviders.createReader(knowledgeBase.toJsonString())) {
            KnowledgeBase roundTrip = KnowledgeBase.fromJson(reader);
            assertEquals("kb", roundTrip.getName());
            assertEquals("source", roundTrip.getKnowledgeSources().get(0).getName());
            assertInstanceOf(KnowledgeRetrievalMediumReasoningEffort.class, roundTrip.getRetrievalReasoningEffort());
            assertEquals(KnowledgeRetrievalOutputMode.ANSWER_SYNTHESIS, roundTrip.getOutputMode());
            assertEquals(knowledgeBase.getRetrievalInstructions(), roundTrip.getRetrievalInstructions());
            assertEquals(knowledgeBase.getAnswerInstructions(), roundTrip.getAnswerInstructions());
            assertEquals(Collections.singletonList("https://app.contoso.com"),
                roundTrip.getCorsOptions().getAllowedOrigins());
        }
    }

    @Test
    public void fileKnowledgeSourceAndCapacityUseTypedGaModels() throws IOException {
        FileKnowledgeSource source
            = new FileKnowledgeSource("files", new FileKnowledgeSourceParameters()).setDescription("GA file source");
        try (JsonReader reader = JsonProviders.createReader(source.toJsonString())) {
            FileKnowledgeSource roundTrip
                = assertInstanceOf(FileKnowledgeSource.class, KnowledgeSource.fromJson(reader));
            assertEquals(KnowledgeSourceKind.FILE, roundTrip.getKind());
            assertEquals("files", roundTrip.getName());
            assertEquals("GA file source", roundTrip.getDescription());
            assertNotNull(roundTrip.getFileParameters());
        }
        try (JsonReader reader = JsonProviders
            .createReader("{\"maxFileCount\":100,\"remainingFileCount\":75,\"maxFileSizeBytes\":1048576}")) {
            KnowledgeSourceFileCapacity capacity = KnowledgeSourceFileCapacity.fromJson(reader);
            assertEquals(100, capacity.getMaxFileCount());
            assertEquals(75, capacity.getRemainingFileCount());
            assertEquals(1048576L, capacity.getMaxFileSizeBytes());
        }
    }

    @Test
    public void contentUnderstandingSkillRoundTripsGaConfiguration() throws IOException {
        ContentUnderstandingSkill skill
            = new ContentUnderstandingSkill(Collections.emptyList(), Collections.emptyList())
                .setExtractionOptions(ContentUnderstandingSkillExtractionOptions.IMAGES,
                    ContentUnderstandingSkillExtractionOptions.LOCATION_METADATA)
                .setChunkingProperties(new ContentUnderstandingSkillChunkingProperties()
                    .setUnit(ContentUnderstandingSkillChunkingUnit.CHARACTERS)
                    .setMaximumLength(2000)
                    .setOverlapLength(100))
                .setModelName("gpt-4.1")
                .setModelDeployment("content-model");

        try (JsonReader reader = JsonProviders.createReader(skill.toJsonString())) {
            ContentUnderstandingSkill roundTrip = ContentUnderstandingSkill.fromJson(reader);
            assertEquals(skill.getExtractionOptions(), roundTrip.getExtractionOptions());
            assertEquals(ContentUnderstandingSkillChunkingUnit.CHARACTERS, roundTrip.getChunkingProperties().getUnit());
            assertEquals(2000, roundTrip.getChunkingProperties().getMaximumLength());
            assertEquals(100, roundTrip.getChunkingProperties().getOverlapLength());
            assertEquals("gpt-4.1", roundTrip.getModelName());
            assertEquals("content-model", roundTrip.getModelDeployment());
        }
    }

    @Test
    public void indexUpdatesDropExcludedPreviewProperties() throws IOException {
        SearchIndex index;
        try (JsonReader reader = JsonProviders.createReader("{\"name\":\"hotels\",\"purviewEnabled\":false,"
            + "\"fields\":[{\"name\":\"id\",\"type\":\"Edm.String\",\"key\":true}]}")) {
            index = SearchIndex.fromJson(reader);
        }
        AtomicInteger requestCount = new AtomicInteger();
        HttpClient httpClient = request -> FluxUtil.collectBytesInByteBufferStream(request.getBody())
            .flatMap(body -> Mono.fromCallable(() -> {
                assertEquals("api-version=2026-10-01", request.getUrl().getQuery());
                try (JsonReader reader = JsonProviders.createReader(body)) {
                    Map<String, Object> json = reader.readMap(JsonReader::readUntyped);
                    assertFalse(json.containsKey("purviewEnabled"));
                    assertEquals("hotels", json.get("name"));
                    assertNotNull(json.get("fields"));
                }
                requestCount.incrementAndGet();
                return new MockHttpResponse(request, 200, new HttpHeaders(), body);
            }));
        SearchIndexClientBuilder builder
            = new SearchIndexClientBuilder().endpoint(ENDPOINT).credential(CREDENTIAL).httpClient(httpClient);

        assertEquals("hotels", builder.buildClient().createOrUpdateIndex(index).getName());
        assertEquals("hotels", builder.buildAsyncClient().createOrUpdateIndex(index).block().getName());
        assertEquals(2, requestCount.get());
    }

    @Test
    public void typedKnowledgeResourceConveniencesRoundTripSyncAndAsync() {
        HttpClient httpClient = request -> FluxUtil.collectBytesInByteBufferStream(request.getBody())
            .map(body -> new MockHttpResponse(request, 200, new HttpHeaders(), body));
        SearchIndexClientBuilder builder
            = new SearchIndexClientBuilder().endpoint(ENDPOINT).credential(CREDENTIAL).httpClient(httpClient);
        KnowledgeBase knowledgeBase = new KnowledgeBase("kb", new KnowledgeSourceReference("files"));
        FileKnowledgeSource source = new FileKnowledgeSource("files", new FileKnowledgeSourceParameters());

        assertEquals("kb", builder.buildClient().createOrUpdateKnowledgeBase(knowledgeBase).getName());
        KnowledgeBase asyncKnowledgeBase
            = builder.buildAsyncClient().createOrUpdateKnowledgeBase(knowledgeBase).block();
        assertNotNull(asyncKnowledgeBase);
        assertEquals("kb", asyncKnowledgeBase.getName());
        Response<KnowledgeBase> knowledgeBaseResponse = builder.buildAsyncClient()
            .createOrUpdateKnowledgeBaseWithResponse(knowledgeBase, new RequestOptions())
            .block();
        assertNotNull(knowledgeBaseResponse);
        assertEquals("kb", knowledgeBaseResponse.getValue().getName());
        assertInstanceOf(FileKnowledgeSource.class, builder.buildClient().createOrUpdateKnowledgeSource(source));
        Response<KnowledgeSource> knowledgeSourceResponse = builder.buildAsyncClient()
            .createOrUpdateKnowledgeSourceWithResponse(source, new RequestOptions())
            .block();
        assertNotNull(knowledgeSourceResponse);
        assertInstanceOf(FileKnowledgeSource.class, knowledgeSourceResponse.getValue());
    }

    private static KnowledgeBaseRetrievalOptions retrievalOptions() {
        return new KnowledgeBaseRetrievalOptions().setIntents(new KnowledgeRetrievalSemanticIntent("Find hotels."))
            .setMaxOutputSizeInTokens(5001)
            .setMaxOutputDocuments(5)
            .setRetrievalReasoningEffort(new KnowledgeRetrievalLowReasoningEffort());
    }
}
