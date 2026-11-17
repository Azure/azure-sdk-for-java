// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.models.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreviewConfigurationSamplesTests {
    @ParameterizedTest
    @ValueSource(longs = { 0, -42, Long.MAX_VALUE, Long.MIN_VALUE })
    public void seededSearchPreservesSignedSeedPagingAndFilter(long seed) {
        AtomicInteger requests = new AtomicInteger();
        SearchClient search = new SearchClientBuilder().endpoint("https://example.search.windows.net")
            .indexName("owned-index")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                requests.incrementAndGet();
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                assertEquals("randomSeeded", body.get("scoringProfile"));
                assertEquals(Collections.singletonList("randomSeed-" + seed), body.get("scoringParameters"));
                assertEquals("rating ge 4", body.get("filter"));
                assertEquals(5, body.get("top"));
                assertEquals(10, body.get("skip"));
                return response(request, "{\"value\":[{\"id\":\"4\",\"@search.score\":0.75}]}");
            })
            .buildClient();
        List<SearchResult> rows = RandomScoringPreviewExample.seeded(search, seed, 5, 10, "rating ge 4");
        RandomScoringPreviewExample.verifyKeys(rows, Collections.singleton("4"));
        assertEquals(1, requests.get());
    }

    @Test
    public void randomScoringRejectsUnexpectedOrDuplicateDocuments() {
        SearchResult one = BinaryData.fromString("{\"id\":\"1\",\"@search.score\":0.5}").toObject(SearchResult.class);
        SearchResult two = BinaryData.fromString("{\"id\":\"2\",\"@search.score\":0.7}").toObject(SearchResult.class);
        assertThrows(IllegalStateException.class,
            () -> RandomScoringPreviewExample.verifyKeys(Arrays.asList(one, one), Collections.singleton("1")));
        assertThrows(IllegalStateException.class,
            () -> RandomScoringPreviewExample.verifyKeys(Arrays.asList(one, two), Collections.singleton("1")));
    }

    @Test
    public void automaticHintsAreSavedWithoutManualValuesAndRetrievalHasNoForcedFilter() {
        AtomicInteger requests = new AtomicInteger();
        HttpClient transport = request -> {
            requests.incrementAndGet();
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            if (request.getUrl().getPath().contains("knowledgesources")) {
                Map<?, ?> parameters = (Map<?, ?>) body.get("searchIndexParameters");
                Map<?, ?> hints = (Map<?, ?>) parameters.get("queryHints");
                Map<?, ?> hint = (Map<?, ?>) ((List<?>) hints.get("filters")).get(0);
                assertEquals("country", hint.get("field"));
                assertNull(hint.get("fieldValues"));
                assertNotNull(hint.get("autoConfiguration"));
                return Mono.just(new MockHttpResponse(request, 201, request.getBodyAsBinaryData().toBytes()));
            }
            assertNull(body.get("knowledgeSourceParams"));
            assertNull(body.get("toolName"));
            assertEquals(Boolean.TRUE, body.get("includeActivity"));
            Map<?, ?> message = (Map<?, ?>) ((List<?>) body.get("messages")).get(0);
            Map<?, ?> content = (Map<?, ?>) ((List<?>) message.get("content")).get(0);
            assertTrue(((String) content.get("text")).contains("Canada"));
            return response(request, retrievalJson("owned-source", 1, "hotel-ca-1", "country eq 'Canada'"));
        };
        SearchIndexClient indexes = indexes(transport);
        SearchIndexKnowledgeSource stored = (SearchIndexKnowledgeSource) indexes.createKnowledgeSource(
            QueryHintsAutoConfigurationPreviewExample.configuration("owned-source", "owned-index"));
        QueryHintsAutoConfigurationPreviewExample.verifyStoredHint(stored);
        QueryHintsAutoConfigurationPreviewExample.retrieveCountry(retrieval(transport), "Canada", "owned-source",
            Collections.singleton("hotel-ca-1"));
        assertEquals(2, requests.get());
    }

    @Test
    public void automaticHintsRejectMissingGeneratedFilterAndUnexpectedDocuments() {
        HttpClient noFilter = request -> response(request, retrievalJson("source", 1, "hotel-ca-1", null));
        assertThrows(IllegalStateException.class, () -> QueryHintsAutoConfigurationPreviewExample
            .retrieveCountry(retrieval(noFilter), "Canada", "source", Collections.singleton("hotel-ca-1")));
        HttpClient unexpectedDocument
            = request -> response(request, retrievalJson("source", 1, "hotel-us-1", "country eq 'Canada'"));
        assertThrows(IllegalStateException.class, () -> QueryHintsAutoConfigurationPreviewExample
            .retrieveCountry(retrieval(unexpectedDocument), "Canada", "source", Collections.singleton("hotel-ca-1")));
    }

    @Test
    public void toolConfigurationRestrictsSourcesAndRequestSelectsOneNamedTool() {
        AtomicInteger requests = new AtomicInteger();
        HttpClient transport = request -> {
            requests.incrementAndGet();
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            if (request.getUrl().getPath().contains("knowledgebases") && body.containsKey("tools")) {
                List<?> tools = (List<?>) body.get("tools");
                assertEquals(2, tools.size());
                Map<?, ?> hotelTool = (Map<?, ?>) tools.get(0);
                List<?> hotelSources = (List<?>) hotelTool.get("knowledgeSources");
                assertEquals(1, hotelSources.size());
                Map<?, ?> hotelSource = (Map<?, ?>) hotelSources.get(0);
                assertEquals("owned-hotels", hotelSource.get("name"));
                assertEquals("country eq 'Canada'", hotelSource.get("filterAddOn"));
                assertEquals(Boolean.TRUE, hotelSource.get("alwaysQuerySource"));
                assertNotNull(hotelTool.get("retrieveDefaults"));
                Map<?, ?> guideTool = (Map<?, ?>) tools.get(1);
                Map<?, ?> guideSource = (Map<?, ?>) ((List<?>) guideTool.get("knowledgeSources")).get(0);
                assertEquals("owned-guides", guideSource.get("name"));
                return Mono.just(new MockHttpResponse(request, 201, request.getBodyAsBinaryData().toBytes()));
            }
            assertEquals("canada-hotels", body.get("toolName"));
            assertFalse(body.containsKey("toolNames"));
            assertNull(body.get("knowledgeSourceParams"));
            return response(request, retrievalJson("owned-hotels", 1, "hotel-ca-1", null));
        };
        indexes(transport).createKnowledgeBase(
            KnowledgeBaseToolsPreviewExample.configuration("owned-kb", "owned-hotels", "owned-guides"));
        KnowledgeBaseToolsPreviewExample.retrieveScope(retrieval(transport), "canada-hotels",
            Collections.singleton("hotel-ca-1"), Collections.singleton("owned-hotels"));
        assertEquals(2, requests.get());
    }

    @Test
    public void toolScopeRejectsWrongSourceAndUnlinkedReferences() {
        KnowledgeBaseRetrievalResult wrongSource = BinaryData.fromString(retrievalJson("guides", 1, "hotel-ca-1", null))
            .toObject(KnowledgeBaseRetrievalResult.class);
        assertThrows(IllegalStateException.class, () -> KnowledgeBaseToolsPreviewExample.verifyScope(wrongSource,
            Collections.singleton("hotel-ca-1"), Collections.singleton("hotels")));
        KnowledgeBaseRetrievalResult unlinked = BinaryData.fromString(retrievalJson("hotels", 7, "hotel-ca-1", null))
            .toObject(KnowledgeBaseRetrievalResult.class);
        assertThrows(IllegalStateException.class, () -> KnowledgeBaseToolsPreviewExample.verifyScope(unlinked,
            Collections.singleton("hotel-ca-1"), Collections.singleton("hotels")));
    }

    private static SearchIndexClient indexes(HttpClient transport) {
        return new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .httpClient(transport)
            .buildClient();
    }

    private static KnowledgeBaseRetrievalClient retrieval(HttpClient transport) {
        return new KnowledgeBaseRetrievalClientBuilder().endpoint("https://example.search.windows.net")
            .knowledgeBaseName("owned-kb")
            .credential(new AzureKeyCredential("key"))
            .httpClient(transport)
            .buildClient();
    }

    private static Mono<HttpResponse> response(HttpRequest request, String body) {
        return Mono.just(new MockHttpResponse(request, 200, body.getBytes(StandardCharsets.UTF_8)));
    }

    private static String retrievalJson(String sourceName, int activitySource, String key, String filter) {
        String hint = filter == null ? "" : ",\"queryHintProcessing\":{\"generatedFilter\":\"" + filter + "\"}";
        return "{\"response\":[],\"activity\":[{\"type\":\"searchIndex\",\"id\":1,\"knowledgeSourceName\":\""
            + sourceName + "\"" + hint + "}],\"references\":[{\"type\":\"searchIndex\",\"id\":\"ref-1\","
            + "\"activitySource\":" + activitySource + ",\"docKey\":\"" + key + "\"}]}";
    }
}
