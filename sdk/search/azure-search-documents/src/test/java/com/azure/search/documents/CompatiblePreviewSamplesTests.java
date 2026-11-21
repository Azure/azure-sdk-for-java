// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeBaseOpenAICompatibleParameters;
import com.azure.search.documents.indexes.models.OpenAICompatibleVectorizerParameters;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompatiblePreviewSamplesTests {
    private static final String ENDPOINT = "https://sample.search.windows.net";
    private static final String RESPONSES_URI
        = "https://gateway.example/openai/responses?api-version=2026-11-01-preview&route=a%2Fb";
    private static final String EMBEDDING_URI
        = "https://gateway.example/deployments/embed/embeddings?api-version=2026-11-01-preview&route=a%2Fb";
    private static final String AUDIENCE = "12345678-1234-1234-1234-123456789abc";
    private static final String HEADERS = "{\"Ocp-Apim-Subscription-Key\":\"sample-secret\"}";

    @ParameterizedTest
    @ValueSource(
        strings = {
            "http://gateway.example/responses",
            "https://gateway.example",
            "https://gateway.example/",
            "https://user:sample-secret@gateway.example/responses",
            "https://gateway.example/responses#sample-secret",
            "https://gateway.example/invalid path?key=sample-secret" })
    public void rejectsIncompleteOrUnsafeProviderUrisWithoutEchoingSecrets(String uri) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> OpenAICompatibleRetrievalPreviewExample.provider(uri, "gpt-6-sol", HEADERS, null));
        assertFalse(error.getMessage().contains("sample-secret"));
        assertNull(error.getCause());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{}",
            "[]",
            "null",
            "{\"Authorization\":1}",
            "{\"Authorization\":\"\"}",
            "{\"Authorization\":\"  \"}",
            "{\"Authorization\":\"Bearer sample-secret\\r\\nInjected: yes\"}",
            "{\"Bad Header\":\"sample-secret\"}",
            "{\"Host\":\"gateway.example\"}",
            "{\"Content-Length\":\"5\"}",
            "{\"Authorization\":\"a\",\"authorization\":\"b\"}",
            "{\"Authorization\":\"sample-secret\"} {}",
            "{\"Authorization\":\"sample-secret" })
    public void rejectsUnsafeProviderHeadersWithoutEchoingSecrets(String headers) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> OpenAICompatibleRetrievalPreviewExample.provider(RESPONSES_URI, "gpt-6-sol", headers, null));
        assertFalse(error.getMessage().contains("sample-secret"));
        assertNull(error.getCause());
    }

    @Test
    public void requiresExactlyOneAuthenticationModeAndBareAudienceGuid() {
        assertThrows(IllegalArgumentException.class,
            () -> OpenAICompatibleRetrievalPreviewExample.provider(RESPONSES_URI, "gpt-6-sol", null, null));
        assertThrows(IllegalArgumentException.class,
            () -> OpenAICompatibleRetrievalPreviewExample.provider(RESPONSES_URI, "gpt-6-sol", HEADERS, AUDIENCE));
        for (String invalid : Arrays.asList("", " ", "api://" + AUDIENCE, AUDIENCE + "/.default")) {
            assertThrows(IllegalArgumentException.class,
                () -> OpenAICompatibleRetrievalPreviewExample.provider(RESPONSES_URI, "gpt-6-sol", null, invalid));
        }
        KnowledgeBaseOpenAICompatibleParameters provider
            = OpenAICompatibleRetrievalPreviewExample.provider(RESPONSES_URI, "gpt-6-sol", null, AUDIENCE);
        assertNull(provider.getHeaders());
        assertEquals(AUDIENCE, provider.getManagedIdentityAudience());
        assertEquals("gpt-6-sol", provider.getModelName().toString());
        assertEquals(RESPONSES_URI, provider.getUri());
    }

    @Test
    public void requiresSpecificGpt6ProfileAndResponsesRoute() {
        assertThrows(IllegalArgumentException.class,
            () -> OpenAICompatibleRetrievalPreviewExample.provider(RESPONSES_URI, "gpt-6", HEADERS, null));
        assertThrows(IllegalArgumentException.class, () -> OpenAICompatibleRetrievalPreviewExample.provider(
            "https://gateway.example/openai/chat/completions?api-version=preview", "gpt-6-sol", HEADERS, null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "success", "bad-reference", "missing-usage", "negative-usage" })
    public void retrievalFlowCreatesResourcesAndChecksScopedEvidenceBeforeReverseCleanup(String outcome) {
        AtomicReference<String> sourceName = new AtomicReference<>();
        List<String> operations = new ArrayList<>();
        HttpClient http = request -> {
            String path = request.getUrl().getPath();
            if (request.getHttpMethod() == HttpMethod.DELETE) {
                operations.add(path.startsWith("/knowledgebases")
                    ? "delete-base"
                    : path.startsWith("/knowledgesources") ? "delete-source" : "delete-index");
                return response(request, 204, "");
            }
            if ("/indexes".equals(path)) {
                operations.add("create-index");
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            if (path.endsWith("/docs/search.index")) {
                operations.add("upload");
                return response(request, 200, uploadResponse());
            }
            if (path.endsWith("/docs/$count")) {
                return response(request, 200, "3");
            }
            if ("/knowledgesources".equals(path)) {
                operations.add("create-source");
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                sourceName.set((String) body.get("name"));
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            if (path.endsWith("/status")) {
                return response(request, 200, "{\"synchronizationStatus\":\"active\"}");
            }
            if ("/knowledgebases".equals(path)) {
                operations.add("create-base");
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                Map<?, ?> model = (Map<?, ?>) ((List<?>) body.get("models")).get(0);
                Map<?, ?> parameters = (Map<?, ?>) model.get("openAICompatibleParameters");
                assertEquals(RESPONSES_URI, parameters.get("uri"));
                assertEquals("gpt-6-sol", parameters.get("modelName"));
                assertEquals("sample-secret", ((Map<?, ?>) parameters.get("headers")).get("Ocp-Apim-Subscription-Key"));
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            if (path.endsWith("/retrieve")) {
                operations.add("retrieve");
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                assertEquals(true, body.get("includeActivity"));
                Map<?, ?> source = (Map<?, ?>) ((List<?>) body.get("knowledgeSourceParams")).get(0);
                assertEquals(sourceName.get(), source.get("knowledgeSourceName"));
                assertEquals(true, source.get("includeReferenceSourceData"));
                String result = retrievalResponse(sourceName.get(),
                    "bad-reference".equals(outcome) ? "other-document" : "hotel-ca-1",
                    "negative-usage".equals(outcome) ? "-1" : "3");
                if ("missing-usage".equals(outcome)) {
                    result = result.replace(",\"reasoningTokens\":3", "");
                }
                return response(request, 200, result);
            }
            throw new AssertionError("Unexpected mock request: " + request.getHttpMethod() + " " + path);
        };
        KnowledgeBaseOpenAICompatibleParameters provider
            = OpenAICompatibleRetrievalPreviewExample.provider(RESPONSES_URI, "gpt-6-sol", HEADERS, null);
        Runnable run
            = () -> OpenAICompatibleRetrievalPreviewExample.run(indexes(http), name -> retrieval(http, name), provider);
        if ("bad-reference".equals(outcome) || "negative-usage".equals(outcome)) {
            assertThrows(IllegalStateException.class, run::run);
        } else {
            run.run();
        }
        assertEquals(Arrays.asList("create-index", "upload", "create-source", "create-base", "retrieve", "delete-base",
            "delete-source", "delete-index"), operations);
    }

    @Test
    public void retrievalVerificationRequiresCorrelatedContentAndGpt6Usage() {
        String valid = retrievalResponse("source", "hotel-ca-1", "3");
        for (String invalid : Arrays.asList(valid.replace("\"activitySource\":1", "\"activitySource\":7"),
            valid.replace("\"content\":\"Hotel guest benefits\"", "\"content\":\"\""),
            valid.replace("\"reasoningTokens\":3", "\"reasoningTokens\":null"),
            valid.replace("\"reasoningTokens\":3", "\"reasoningTokens\":-1"),
            valid.replace("\"text\":\"Hotel guest benefits\"", "\"text\":\"\""))) {
            KnowledgeBaseRetrievalResult result
                = BinaryData.fromString(invalid).toObject(KnowledgeBaseRetrievalResult.class);
            assertThrows(IllegalStateException.class, () -> OpenAICompatibleRetrievalPreviewExample.verifyResult(result,
                "source", Collections.singleton("hotel-ca-1"), true));
        }
    }

    @Test
    public void documentEmbeddingUsesExactProviderUriAndSafeHeadersAndReordersIndexedVectors() {
        OpenAICompatibleVectorizerParameters provider = embeddingProvider(HEADERS, null);
        AtomicInteger calls = new AtomicInteger();
        HttpClient http = request -> {
            calls.incrementAndGet();
            assertEquals(EMBEDDING_URI, request.getUrl().toString());
            assertEquals(HttpMethod.POST, request.getHttpMethod());
            assertEquals("sample-secret", request.getHeaders().getValue("Ocp-Apim-Subscription-Key"));
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            assertEquals(Arrays.asList("spa", "business"), body.get("input"));
            assertEquals(2, body.get("dimensions"));
            assertFalse(body.containsKey("model"));
            return response(request, 200,
                "{\"data\":[{\"index\":1,\"embedding\":[0,1]},{\"index\":0,\"embedding\":[1,0]}]}");
        };
        assertEquals(Arrays.asList(Arrays.asList(1.0f, 0.0f), Arrays.asList(0.0f, 1.0f)),
            OpenAICompatibleEmbeddingsPreviewExample.embed(http, provider, null, Arrays.asList("spa", "business")));
        assertEquals(1, calls.get());
    }

    @Test
    public void documentEmbeddingUsesLocalIdentityWhileSearchConfigurationKeepsAudienceOnly() {
        OpenAICompatibleVectorizerParameters provider = embeddingProvider(null, AUDIENCE);
        TokenCredential identity = context -> {
            assertEquals(Collections.singletonList(AUDIENCE + "/.default"), context.getScopes());
            return Mono.just(new AccessToken("local-sample-token", OffsetDateTime.now().plusMinutes(5)));
        };
        HttpClient http = request -> {
            assertEquals("Bearer local-sample-token", request.getHeaders().getValue("Authorization"));
            return response(request, 200, "{\"data\":[{\"index\":0,\"embedding\":[1,0]}]}");
        };
        OpenAICompatibleEmbeddingsPreviewExample.embed(http, provider, identity, Collections.singletonList("spa"));
        assertNull(provider.getHeaders());
        assertEquals(AUDIENCE, provider.getManagedIdentityAudience());
    }

    @ParameterizedTest
    @ValueSource(ints = { 302, 401, 500 })
    public void providerFailuresAreSanitizedAndNeverFollowed(int status) {
        AtomicInteger calls = new AtomicInteger();
        HttpClient http = request -> {
            calls.incrementAndGet();
            return response(request, status, "{\"error\":\"sample-secret\"}");
        };
        IllegalStateException error
            = assertThrows(IllegalStateException.class, () -> OpenAICompatibleEmbeddingsPreviewExample.embed(http,
                embeddingProvider(HEADERS, null), null, Collections.singletonList("spa")));
        assertEquals(1, calls.get());
        assertFalse(error.getMessage().contains("sample-secret"));
        assertNull(error.getCause());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{\"data\":[]}",
            "{\"data\":[{\"index\":0,\"embedding\":[1]}]}",
            "{\"data\":[{\"index\":1,\"embedding\":[1,0]}]}",
            "{\"data\":[{\"index\":0.5,\"embedding\":[1,0]}]}",
            "{\"data\":[{\"index\":0,\"embedding\":[0,0]}]}",
            "{\"data\":[{\"index\":0,\"embedding\":[1e100,0]}]}",
            "{\"data\":[{\"index\":0,\"embedding\":[\"1\",0]}]}",
            "{\"data\":[{\"index\":0,\"embedding\":[1,0]}]} {}" })
    public void rejectsEmbeddingResponsesThatCannotMatchTheConfiguredIndex(String json) {
        assertThrows(IllegalArgumentException.class,
            () -> OpenAICompatibleEmbeddingsPreviewExample.parseEmbeddings(BinaryData.fromString(json), 1, 2));
    }

    @Test
    public void indexFlowUploadsProviderVectorsAndExecutesBothSearchControls() {
        AtomicInteger searches = new AtomicInteger();
        List<String> operations = new ArrayList<>();
        HttpClient searchHttp = request -> {
            String path = request.getUrl().getPath();
            if (request.getHttpMethod() == HttpMethod.DELETE) {
                operations.add("delete-index");
                return response(request, 204, "");
            }
            if ("/indexes".equals(path)) {
                operations.add("create-index");
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                Map<?, ?> vectorSearch = (Map<?, ?>) body.get("vectorSearch");
                Map<?, ?> vectorizer = (Map<?, ?>) ((List<?>) vectorSearch.get("vectorizers")).get(0);
                Map<?, ?> parameters = (Map<?, ?>) vectorizer.get("openAICompatibleParameters");
                assertEquals(EMBEDDING_URI, parameters.get("uri"));
                assertEquals(2, parameters.get("dimensions"));
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            if (path.endsWith("/docs/search.index")) {
                operations.add("upload");
                List<?> actions = (List<?>) request.getBodyAsBinaryData().toObject(Map.class).get("value");
                assertEquals(3, actions.size());
                assertEquals(Arrays.asList(1.0, 0.0), ((Map<?, ?>) actions.get(0)).get("contentVector"));
                return response(request, 200, uploadResponse());
            }
            if (path.endsWith("/docs/$count")) {
                return response(request, 200, "3");
            }
            if (path.endsWith("/docs/search.post.search")) {
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                Map<?, ?> query = (Map<?, ?>) ((List<?>) body.get("vectorQueries")).get(0);
                if (searches.getAndIncrement() == 0) {
                    assertEquals("vector", query.get("kind"));
                    assertNotNull(query.get("vector"));
                } else {
                    assertEquals("text", query.get("kind"));
                    assertTrue(query.get("text").toString().contains("spa"));
                    assertFalse(query.containsKey("vector"));
                }
                assertEquals("contentVector", query.get("fields"));
                assertEquals(true, query.get("exhaustive"));
                return response(request, 200, "{\"value\":[{\"@search.score\":1,\"id\":\"hotel-ca-1\"}]}");
            }
            throw new AssertionError("Unexpected mock request: " + path);
        };
        HttpClient providerHttp = request -> response(request, 200,
            "{\"data\":[{\"index\":0,\"embedding\":[1,0]},{\"index\":1,\"embedding\":[0,1]},"
                + "{\"index\":2,\"embedding\":[-1,0]}]}");
        OpenAICompatibleEmbeddingsPreviewExample.run(indexes(searchHttp), providerHttp,
            embeddingProvider(HEADERS, null), null);
        assertEquals(2, searches.get());
        assertEquals(Arrays.asList("create-index", "upload", "delete-index"), operations);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void managedBlobFlowCreatesActualSourceAndRequiresSuccessfulNonemptySynchronization(int outcome) {
        List<String> operations = new ArrayList<>();
        HttpClient http = request -> {
            String path = request.getUrl().getPath();
            if (request.getHttpMethod() == HttpMethod.DELETE) {
                operations.add("delete-source");
                return response(request, 204, "");
            }
            if ("/knowledgesources".equals(path)) {
                operations.add("create-source");
                Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
                Map<?, ?> blob = (Map<?, ?>) body.get("azureBlobParameters");
                assertEquals("existing-container", blob.get("containerName"));
                assertEquals("hotels", blob.get("folderPath"));
                Map<?, ?> ingestion = (Map<?, ?>) blob.get("ingestionParameters");
                Map<?, ?> embedding = (Map<?, ?>) ingestion.get("embeddingModel");
                assertEquals("openAICompatible", embedding.get("kind"));
                assertEquals(EMBEDDING_URI, ((Map<?, ?>) embedding.get("openAICompatibleParameters")).get("uri"));
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            if (path.endsWith("/status")) {
                operations.add("status");
                return response(request, 200,
                    "{\"synchronizationStatus\":\"active\",\"lastSynchronizationState\":{"
                        + "\"startTime\":\"2026-11-01T00:00:00Z\",\"endTime\":\"2026-11-01T00:00:01Z\","
                        + "\"itemsUpdatesProcessed\":" + (outcome == 1 ? 0 : 2) + ",\"itemsUpdatesFailed\":"
                        + (outcome == 2 ? 1 : 0) + ",\"itemsSkipped\":0}}");
            }
            throw new AssertionError("Unexpected mock request: " + path);
        };
        Runnable run
            = () -> OpenAICompatibleEmbeddingsPreviewExample
                .runManagedBlob(indexes(http), embeddingProvider(null, AUDIENCE),
                    "ResourceId=/subscriptions/example/resourceGroups/example"
                        + "/providers/Microsoft.Storage/storageAccounts/example",
                    "existing-container", "hotels", "true");
        if (outcome == 0) {
            run.run();
        } else {
            assertThrows(IllegalStateException.class, run::run);
        }
        assertEquals(Arrays.asList("create-source", "status", "delete-source"), operations);
    }

    @Test
    public void managedBlobFlowRejectsUnapprovedAndContainerRootReadsBeforeAnyRequest() {
        AtomicInteger requests = new AtomicInteger();
        SearchIndexClient client = indexes(request -> {
            requests.incrementAndGet();
            throw new AssertionError("Unsafe Blob ingestion must not send requests.");
        });
        for (String folder : Arrays.asList(null, "", " ", "/", "\\", "///", "/hotels", "\\hotels", ".", "a/..")) {
            assertThrows(IllegalArgumentException.class,
                () -> OpenAICompatibleEmbeddingsPreviewExample.runManagedBlob(client, embeddingProvider(null, AUDIENCE),
                    "sample-connection", "existing-container", folder, "true"));
        }
        for (String approved : Arrays.asList(null, "", "false")) {
            assertThrows(IllegalArgumentException.class,
                () -> OpenAICompatibleEmbeddingsPreviewExample.runManagedBlob(client, embeddingProvider(null, AUDIENCE),
                    "sample-connection", "existing-container", "hotels", approved));
        }
        assertEquals(0, requests.get());
    }

    private static OpenAICompatibleVectorizerParameters embeddingProvider(String headers, String audience) {
        return OpenAICompatibleEmbeddingsPreviewExample.provider(EMBEDDING_URI, "text-embedding-3-small", 2, headers,
            audience);
    }

    private static SearchIndexClient indexes(HttpClient http) {
        return new SearchIndexClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("sample-key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(http)
            .buildClient();
    }

    private static KnowledgeBaseRetrievalClient retrieval(HttpClient http, String name) {
        return new KnowledgeBaseRetrievalClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("sample-key"))
            .knowledgeBaseName(name)
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(http)
            .buildClient();
    }

    private static Mono<com.azure.core.http.HttpResponse> response(HttpRequest request, int status, String body) {
        return Mono.just(new MockHttpResponse(request, status, body.getBytes(StandardCharsets.UTF_8)));
    }

    private static String uploadResponse() {
        return "{\"value\":[{\"key\":\"hotel-ca-1\",\"status\":true,\"statusCode\":201},"
            + "{\"key\":\"hotel-ca-2\",\"status\":true,\"statusCode\":201},"
            + "{\"key\":\"hotel-us-1\",\"status\":true,\"statusCode\":201}]}";
    }

    private static String retrievalResponse(String sourceName, String key, String reasoningTokens) {
        return "{\"response\":[{\"role\":\"assistant\",\"content\":[{\"type\":\"text\","
            + "\"text\":\"Hotel guest benefits\"}]}],"
            + "\"activity\":[{\"type\":\"searchIndex\",\"id\":1,\"knowledgeSourceName\":\"" + sourceName + "\"},"
            + "{\"type\":\"agenticReasoning\",\"id\":2,\"reasoningTokens\":" + reasoningTokens + "}],"
            + "\"references\":[{\"type\":\"searchIndex\",\"id\":\"0\",\"activitySource\":1,\"docKey\":\"" + key
            + "\",\"sourceData\":{\"content\":\"Hotel guest benefits\"}}]}";
    }
}
