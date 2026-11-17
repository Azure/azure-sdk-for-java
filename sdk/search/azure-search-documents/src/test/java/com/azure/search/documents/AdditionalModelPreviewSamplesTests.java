// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
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
import com.azure.search.documents.indexes.models.ChatCompletionSkill;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdditionalModelPreviewSamplesTests {
    private static final String ENDPOINT = "https://sample.search.windows.net";
    private static final String OPENAI = "https://sample.openai.azure.com";
    private static final String CHAT
        = "https://gateway.example/openai/deployments/chat/chat/completions" + "?api-version=2026-04-01&route=a%2Fb";
    private static final String FACTS
        = "Historic Harbor Hotel, Seattle, founded 1984, Contoso Hospitality, waterfront spa."
            + " Mountain View Inn, Denver, founded 2002, Alpine Hospitality.";

    @ParameterizedTest
    @ValueSource(strings = { "gpt-6", "gpt-6-future", "gpt-5.6-sol", "text-embedding-3-small", " gpt-6-sol" })
    public void nativeConfigurationRejectsUnsupportedProfilesBeforeAnyRequest(String profile) {
        assertThrows(IllegalArgumentException.class,
            () -> NativeGpt6RetrievalPreviewExample.model(OPENAI, "existing-deployment", profile, null));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "http://sample.openai.azure.com",
            "https://sample.openai.azure.com/openai/responses",
            "https://sample.openai.azure.com/?key=not-a-credential",
            "https://user:placeholder@sample.openai.azure.com",
            "https://sample.openai.azure.com/#fragment",
            "https://sample.openai.azure.com/invalid path" })
    public void nativeConfigurationRequiresResourceRootWithoutEchoingInput(String endpoint) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> NativeGpt6RetrievalPreviewExample.model(endpoint, "existing-deployment", "gpt-6-sol", null));
        assertFalse(error.getMessage().contains(endpoint));
        assertNull(error.getCause());
    }

    @Test
    public void nativeConfigurationRequiresExplicitDeploymentAndSupportsIdentityAuthentication() {
        for (String deployment : Arrays.asList(null, "", " ", "name/path")) {
            assertThrows(IllegalArgumentException.class,
                () -> NativeGpt6RetrievalPreviewExample.model(OPENAI, deployment, "gpt-6-sol", null));
        }
        AzureOpenAIVectorizerParameters model
            = NativeGpt6RetrievalPreviewExample.model(OPENAI, "actual-deployment", "gpt-6.1-sol", null);
        assertEquals("actual-deployment", model.getDeploymentName());
        assertEquals("gpt-6.1-sol", model.getModelName().toString());
        assertNull(model.getApiKey());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "success",
            "wrong-document",
            "wrong-content",
            "missing-reasoning",
            "negative-reasoning",
            "base-conflict" })
    public void nativeRetrievalChecksEvidenceAndCleansUpOnlyCreatedResources(String outcome) {
        List<String> operations = new ArrayList<>();
        AtomicReference<String> sourceName = new AtomicReference<>();
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
                assertEquals(HttpMethod.POST, request.getHttpMethod());
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            if (path.endsWith("/docs/search.index")) {
                operations.add("upload");
                List<?> documents = (List<?>) body(request).get("value");
                assertEquals(3, documents.size());
                assertEquals(PreviewSampleSupport.hotelDocuments().get(0).get("content"),
                    ((Map<?, ?>) documents.get(0)).get("content"));
                return response(request, 200,
                    "{\"value\":[" + "{\"key\":\"hotel-ca-1\",\"status\":true,\"statusCode\":201},"
                        + "{\"key\":\"hotel-ca-2\",\"status\":true,\"statusCode\":201},"
                        + "{\"key\":\"hotel-us-1\",\"status\":true,\"statusCode\":201}]}");
            }
            if (path.endsWith("/docs/$count")) {
                return response(request, 200, "3");
            }
            if ("/knowledgesources".equals(path)) {
                operations.add("create-source");
                assertEquals(HttpMethod.POST, request.getHttpMethod());
                sourceName.set((String) body(request).get("name"));
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            if (path.endsWith("/status")) {
                return response(request, 200, "{\"synchronizationStatus\":\"active\"}");
            }
            if ("/knowledgebases".equals(path)) {
                operations.add("create-base");
                assertEquals(HttpMethod.POST, request.getHttpMethod());
                Map<?, ?> model = (Map<?, ?>) ((List<?>) body(request).get("models")).get(0);
                assertEquals("azureOpenAI", model.get("kind"));
                assertFalse(model.containsKey("openAICompatibleParameters"));
                Map<?, ?> parameters = (Map<?, ?>) model.get("azureOpenAIParameters");
                assertEquals(OPENAI, parameters.get("resourceUri"));
                assertEquals("actual-deployment", parameters.get("deploymentId"));
                assertEquals("gpt-6-sol", parameters.get("modelName"));
                assertEquals("mock-key", parameters.get("apiKey"));
                if ("base-conflict".equals(outcome)) {
                    return response(request, 409, "{\"error\":{\"message\":\"Already exists\"}}");
                }
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            if (path.endsWith("/retrieve")) {
                operations.add("retrieve");
                assertEquals(true, body(request).get("includeActivity"));
                Map<?, ?> source = (Map<?, ?>) ((List<?>) body(request).get("knowledgeSourceParams")).get(0);
                assertEquals(sourceName.get(), source.get("knowledgeSourceName"));
                assertEquals(true, source.get("includeReferenceSourceData"));
                return response(request, 200, retrievalResult(sourceName.get(), outcome));
            }
            throw new AssertionError("Unexpected mock request: " + request.getHttpMethod() + " " + path);
        };
        AzureOpenAIVectorizerParameters model
            = NativeGpt6RetrievalPreviewExample.model(OPENAI, "actual-deployment", "gpt-6-sol", "mock-key");
        Runnable run = () -> NativeGpt6RetrievalPreviewExample.run(indexes(http), name -> retrieval(http, name), model);
        if ("success".equals(outcome) || "missing-reasoning".equals(outcome)) {
            run.run();
        } else if ("base-conflict".equals(outcome)) {
            assertThrows(HttpResponseException.class, run::run);
        } else {
            assertThrows(IllegalStateException.class, run::run);
        }
        List<String> expected
            = new ArrayList<>(Arrays.asList("create-index", "upload", "create-source", "create-base"));
        if (!"base-conflict".equals(outcome)) {
            expected.addAll(Arrays.asList("retrieve", "delete-base"));
        }
        expected.addAll(Arrays.asList("delete-source", "delete-index"));
        assertEquals(expected, operations);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://gateway.example/openai/responses",
            "https://gateway.example/embeddings",
            "http://gateway.example/chat/completions" })
    public void skillRejectsResponsesAndEmbeddingRoutes(String uri) {
        assertThrows(IllegalArgumentException.class, () -> GenAiPromptSkillPreviewExample.skill(uri, "mock-key"));
    }

    @Test
    public void skillRequiresItsOwnKeyRatherThanGuessingCompatibleAuthentication() {
        assertThrows(IllegalArgumentException.class, () -> GenAiPromptSkillPreviewExample.skill(CHAT, null));
        assertThrows(IllegalArgumentException.class, () -> GenAiPromptSkillPreviewExample.skill(CHAT, " "));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "/", "\\", ".", "..", "hotels/../other", "hotels\\other", "hotels//other" })
    public void skillRejectsUnsafeSourceScopeBeforeAnyRequest(String folder) {
        AtomicInteger requests = new AtomicInteger();
        HttpClient http = request -> {
            requests.incrementAndGet();
            throw new AssertionError("Validation must precede all service requests.");
        };
        assertThrows(IllegalArgumentException.class, () -> GenAiPromptSkillPreviewExample.run(indexes(http),
            indexers(http), skill(), "mock-connection", "approved-container", folder, "true"));
        assertEquals(0, requests.get());
    }

    @Test
    public void skillRequiresExplicitSourceApproval() {
        AtomicInteger requests = new AtomicInteger();
        HttpClient http = request -> {
            requests.incrementAndGet();
            throw new AssertionError("Unapproved source must not be used.");
        };
        assertThrows(IllegalArgumentException.class, () -> GenAiPromptSkillPreviewExample.run(indexes(http),
            indexers(http), skill(), "mock-connection", "approved-container", "hotels", "false"));
        assertEquals(0, requests.get());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "success",
            "execution-failure",
            "missing-enrichment",
            "wrong-fixture",
            "empty-index",
            "source-conflict",
            "skillset-conflict",
            "indexer-conflict",
            "delete-indexer-failure" })
    public void skillExecutesIndexerVerifiesEnrichmentAndReversesOwnedCleanup(String outcome) {
        List<String> operations = new ArrayList<>();
        HttpClient http = request -> {
            String path = request.getUrl().getPath();
            if (request.getHttpMethod() == HttpMethod.DELETE) {
                operations.add(path.startsWith("/indexers")
                    ? "delete-indexer"
                    : path.startsWith("/skillsets")
                        ? "delete-skillset"
                        : path.startsWith("/datasources") ? "delete-source" : "delete-index");
                if (path.startsWith("/indexers") && "delete-indexer-failure".equals(outcome)) {
                    return response(request, 400, "{\"error\":{\"message\":\"Mock cleanup failure\"}}");
                }
                return response(request, 204, "");
            }
            if ("/indexes".equals(path)) {
                operations.add("create-index");
                assertEquals(HttpMethod.POST, request.getHttpMethod());
                return response(request, 201, request.getBodyAsBinaryData().toString());
            }
            if ("/datasources".equals(path)) {
                operations.add("create-source");
                assertEquals(HttpMethod.POST, request.getHttpMethod());
                assertEquals("azureblob", body(request).get("type"));
                Map<?, ?> container = (Map<?, ?>) body(request).get("container");
                assertEquals("approved-container", container.get("name"));
                assertEquals("hotels/", container.get("query"));
                return createResponse(request, outcome, "source-conflict");
            }
            if ("/skillsets".equals(path)) {
                operations.add("create-skillset");
                assertEquals(HttpMethod.POST, request.getHttpMethod());
                Map<?, ?> skill = (Map<?, ?>) ((List<?>) body(request).get("skills")).get(0);
                assertEquals("#Microsoft.Skills.Custom.ChatCompletionSkill", skill.get("@odata.type"));
                assertEquals(CHAT, skill.get("uri"));
                assertEquals("mock-key", skill.get("apiKey"));
                assertFalse(skill.containsKey("headers"));
                assertFalse(skill.containsKey("managedIdentityAudience"));
                List<?> inputs = (List<?>) skill.get("inputs");
                assertEquals(3, inputs.size());
                assertTrue(
                    inputs.stream().anyMatch(input -> "/document/content".equals(((Map<?, ?>) input).get("source"))));
                assertEquals("hotelFacts", ((Map<?, ?>) ((List<?>) skill.get("outputs")).get(0)).get("targetName"));
                return createResponse(request, outcome, "skillset-conflict");
            }
            if ("/indexers".equals(path)) {
                operations.add("create-indexer");
                assertEquals(HttpMethod.POST, request.getHttpMethod());
                Map<?, ?> definition = body(request);
                assertEquals(definition.get("name"), definition.get("skillsetName"));
                assertEquals(definition.get("name"), definition.get("targetIndexName"));
                assertEquals(definition.get("name"), definition.get("dataSourceName"));
                Map<?, ?> output = (Map<?, ?>) ((List<?>) definition.get("outputFieldMappings")).get(0);
                assertEquals("/document/hotelFacts", output.get("sourceFieldName"));
                assertEquals("hotelFacts", output.get("targetFieldName"));
                assertEquals(0, ((Map<?, ?>) definition.get("parameters")).get("maxFailedItems"));
                return createResponse(request, outcome, "indexer-conflict");
            }
            if (path.endsWith("/search.status")) {
                operations.add("execution-status");
                String status = "execution-failure".equals(outcome) ? "persistentFailure" : "success";
                return response(request, 200, "{\"status\":\"running\",\"lastResult\":{\"status\":\"" + status
                    + "\",\"itemsProcessed\":1,\"itemsFailed\":0,\"errors\":[],\"warnings\":[]}}");
            }
            if (path.endsWith("/docs/$count")) {
                return response(request, 200, "1");
            }
            if (path.endsWith("/docs/search.post.search")) {
                operations.add("read-enrichment");
                assertEquals(Arrays.asList("id", "content", "hotelFacts"),
                    Arrays.asList(((String) body(request).get("select")).split(",")));
                String content = "wrong-fixture".equals(outcome)
                    ? "unrelated text"
                    : GenAiPromptSkillPreviewExample.SYNTHETIC_HOTELS.replace("\n", "\r\n") + "\r\n";
                Map<String, Object> document = new LinkedHashMap<>();
                document.put("@search.score", 1.0);
                document.put("id", "fixture-key");
                document.put("content", content);
                document.put("hotelFacts", "missing-enrichment".equals(outcome) ? null : FACTS);
                return response(request, 200,
                    BinaryData.fromObject(Collections.singletonMap("value",
                        "empty-index".equals(outcome) ? Collections.emptyList() : Collections.singletonList(document)))
                        .toString());
            }
            throw new AssertionError("Unexpected mock request: " + request.getHttpMethod() + " " + path);
        };
        Runnable run = () -> GenAiPromptSkillPreviewExample.run(indexes(http), indexers(http), skill(),
            "mock-connection", "approved-container", "hotels", "true");
        if ("success".equals(outcome)) {
            run.run();
        } else if (outcome.endsWith("-conflict") || "delete-indexer-failure".equals(outcome)) {
            assertThrows(HttpResponseException.class, run::run);
        } else {
            assertThrows(IllegalStateException.class, run::run);
        }
        List<String> expected = new ArrayList<>(Arrays.asList("create-index", "create-source"));
        if (!"source-conflict".equals(outcome)) {
            expected.add("create-skillset");
            if (!"skillset-conflict".equals(outcome)) {
                expected.add("create-indexer");
                if (!"indexer-conflict".equals(outcome)) {
                    expected.add("execution-status");
                    if (!"execution-failure".equals(outcome)) {
                        expected.add("read-enrichment");
                    }
                    expected.add("delete-indexer");
                }
                expected.add("delete-skillset");
            }
            expected.add("delete-source");
        }
        expected.add("delete-index");
        assertEquals(expected, operations);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "\"status\":\"error\"",
            "\"status\":\"running\",\"lastResult\":{\"status\":\"success\",\"itemsProcessed\":0,\"itemsFailed\":0}",
            "\"status\":\"running\",\"lastResult\":{\"status\":\"success\",\"itemsProcessed\":2,\"itemsFailed\":0}",
            "\"status\":\"running\",\"lastResult\":{\"status\":\"success\",\"itemsProcessed\":1,\"itemsFailed\":1}",
            "\"status\":\"running\",\"lastResult\":{\"status\":\"success\",\"itemsProcessed\":1,\"warnings\":[{}]}" })
    public void skillRejectsEmptyPartialOrWarningExecutionWithoutLoggingServiceDetails(String status) {
        HttpClient http = request -> response(request, 200, "{" + status + "}");
        assertThrows(IllegalStateException.class,
            () -> GenAiPromptSkillPreviewExample.waitForExecution(indexers(http), "owned-indexer"));
    }

    private static String retrievalResult(String sourceName, String outcome) {
        String content = "wrong-content".equals(outcome)
            ? "Unrelated content"
            : (String) PreviewSampleSupport.hotelDocuments().get(0).get("content");
        String key = "wrong-document".equals(outcome) ? "not-uploaded" : "hotel-ca-1";
        String tokens = "missing-reasoning".equals(outcome)
            ? ""
            : ",\"reasoningTokens\":" + ("negative-reasoning".equals(outcome) ? "-1" : "7");
        return "{\"response\":[{\"role\":\"assistant\",\"content\":[{\"type\":\"text\","
            + "\"text\":\"Hotel guest benefits\"}]}],"
            + "\"activity\":[{\"type\":\"searchIndex\",\"id\":1,\"knowledgeSourceName\":\"" + sourceName + "\"},"
            + "{\"type\":\"agenticReasoning\",\"id\":2" + tokens + "}],"
            + "\"references\":[{\"type\":\"searchIndex\",\"id\":\"0\",\"activitySource\":1,\"docKey\":\"" + key
            + "\",\"sourceData\":{\"content\":\"" + content + "\"}}]}";
    }

    private static ChatCompletionSkill skill() {
        return GenAiPromptSkillPreviewExample.skill(CHAT, "mock-key");
    }

    private static Map<?, ?> body(HttpRequest request) {
        return request.getBodyAsBinaryData().toObject(Map.class);
    }

    private static Mono<HttpResponse> createResponse(HttpRequest request, String outcome, String conflict) {
        return outcome.equals(conflict)
            ? response(request, 409, "{\"error\":{\"message\":\"Already exists\"}}")
            : response(request, 201, request.getBodyAsBinaryData().toString());
    }

    private static Mono<HttpResponse> response(HttpRequest request, int status, String body) {
        return Mono.just(new MockHttpResponse(request, status, body.getBytes(StandardCharsets.UTF_8)));
    }

    private static SearchIndexClient indexes(HttpClient http) {
        return new SearchIndexClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("mock-key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(http)
            .buildClient();
    }

    private static SearchIndexerClient indexers(HttpClient http) {
        return new SearchIndexerClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("mock-key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(http)
            .buildClient();
    }

    private static KnowledgeBaseRetrievalClient retrieval(HttpClient http, String name) {
        return new KnowledgeBaseRetrievalClientBuilder().endpoint(ENDPOINT)
            .knowledgeBaseName(name)
            .credential(new AzureKeyCredential("mock-key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(http)
            .buildClient();
    }
}
