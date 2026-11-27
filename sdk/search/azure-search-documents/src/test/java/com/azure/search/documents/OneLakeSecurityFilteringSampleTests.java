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
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OneLakeSecurityFilteringSampleTests {
    private static final String WORKSPACE = "00000000-0000-0000-0000-000000000001";
    private static final String LAKEHOUSE = "00000000-0000-0000-0000-000000000002";
    private static final String TARGET = "java-onelake-acl-fixture";
    private static final String PREFIX
        = "https://onelake.dfs.fabric.microsoft.com/" + WORKSPACE + "/" + LAKEHOUSE + "/Files/" + TARGET + "/";
    private static final String DOCUMENT_A = PREFIX + "a/document.txt";
    private static final String DOCUMENT_B = PREFIX + "b/document.txt";

    @ParameterizedTest
    @ValueSource(strings = { "java-onelake-acl-fixture", "Files/java-onelake-acl-fixture" })
    public void actualFlowIngestsPermissionsPropagatesTwoUsersAndCleansUpOnlyOwnedResources(String target) {
        Scenario scenario = new Scenario("success");
        scenario.environment.put("SEARCH_ONELAKE_TARGET_PATH", target);
        scenario.execute();
        assertEquals(2, scenario.retrievals);
        assertEquals(2, scenario.statusChecks);
        assertEquals(1, scenario.pauses.get());
        scenario.assertOwnedCleanup(true);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "leak-a",
            "leak-b",
            "wrong-source",
            "unlinked",
            "missing-evidence",
            "activity-error",
            "wrong-type",
            "extra-reference" })
    public void unexpectedEvidenceFailsClosedAndStillCleansUp(String failure) {
        Scenario scenario = new Scenario(failure);
        IllegalStateException thrown = assertThrows(IllegalStateException.class, scenario::execute);
        assertFalse(thrown.getMessage().contains(DOCUMENT_A));
        assertFalse(thrown.getMessage().contains(DOCUMENT_B));
        assertEquals("leak-b".equals(failure) ? 2 : 1, scenario.retrievals);
        scenario.assertOwnedCleanup(true);
    }

    @Test
    public void deniedIdentityHttpFailureDoesNotBecomeAFalsePassingEmptyResult() {
        Scenario scenario = new Scenario("forbidden-b");
        assertThrows(HttpResponseException.class, scenario::execute);
        assertEquals(2, scenario.retrievals);
        scenario.assertOwnedCleanup(true);
    }

    @ParameterizedTest
    @ValueSource(strings = { "active-only", "ingestion-failed", "ingestion-skipped", "current-error" })
    public void incompleteOrFailedIngestionNeverCreatesKnowledgeBaseOrRetrieves(String failure) {
        Scenario scenario = new Scenario(failure);
        assertThrows(IllegalStateException.class, scenario::execute);
        assertEquals(0, scenario.retrievals);
        assertNull(scenario.baseName);
        assertEquals("active-only".equals(failure) ? 3 : 1, scenario.statusChecks);
        scenario.assertOwnedCleanup(false);
    }

    @Test
    public void createConflictDoesNotDeleteAnythingPreexisting() {
        Scenario scenario = new Scenario("source-conflict");
        assertThrows(HttpResponseException.class, scenario::execute);
        assertEquals(0, scenario.statusChecks);
        assertEquals(0, scenario.retrievals);
        assertTrue(scenario.deleted.isEmpty());
    }

    @Test
    public void knowledgeBaseCreateConflictCleansOnlySuccessfullyCreatedSource() {
        Scenario scenario = new Scenario("base-conflict");
        assertThrows(HttpResponseException.class, scenario::execute);
        assertEquals(0, scenario.retrievals);
        scenario.assertOwnedCleanup(false);
    }

    @Test
    public void fixtureApprovalIsCheckedBeforeReadingAnyOtherConfiguration() {
        AtomicInteger reads = new AtomicInteger();
        assertThrows(IllegalArgumentException.class,
            () -> OneLakeSecurityFilteringPreviewExample.Fixture.fromEnvironment(name -> {
                reads.incrementAndGet();
                assertEquals("SEARCH_ONELAKE_FIXTURE_APPROVED", name);
                return null;
            }));
        assertEquals(1, reads.get());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "SEARCH_ONELAKE_WORKSPACE_ID",
            "SEARCH_ONELAKE_LAKEHOUSE_ID",
            "SEARCH_ONELAKE_TARGET_PATH",
            "SEARCH_ONELAKE_DOCUMENT_URL_PREFIX",
            "SEARCH_ONELAKE_USER_A_DOCUMENT_URL",
            "SEARCH_ONELAKE_USER_B_DOCUMENT_URL",
            "SEARCH_ONELAKE_FIXTURE_QUERY",
            "SEARCH_ONELAKE_USER_A_TOKEN",
            "SEARCH_ONELAKE_USER_B_TOKEN" })
    public void missingConfigurationFailsLocally(String missing) {
        Map<String, String> environment = environment();
        environment.remove(missing);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> OneLakeSecurityFilteringPreviewExample.Fixture.fromEnvironment(environment::get));
        assertTrue(error.getMessage().contains(missing));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "Files",
            "/",
            "Files/shared",
            "Files/java-onelake-acl-",
            "Files/java-onelake-acl-fixture/..",
            "Files/java-onelake-acl-fixture/%2e%2e",
            "Files/java-onelake-acl-fixture/" })
    public void refusesRootSharedOrAmbiguousIngestionScopes(String path) {
        Map<String, String> environment = environment();
        environment.put("SEARCH_ONELAKE_TARGET_PATH", path);
        assertThrows(IllegalArgumentException.class,
            () -> OneLakeSecurityFilteringPreviewExample.Fixture.fromEnvironment(environment::get));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "same-token",
            "same-user",
            "other-tenant",
            "expired",
            "long-lived",
            "bearer-prefix",
            "invalid-token",
            "same-document",
            "outside-prefix",
            "ambiguous-prefix" })
    public void rejectsUnusableIdentityPairsAndEvidenceConfigurationWithoutEchoingSecrets(String failure) {
        Map<String, String> environment = environment();
        long expiry = Instant.now().getEpochSecond() + 1800;
        switch (failure) {
            case "same-token":
                environment.put("SEARCH_ONELAKE_USER_B_TOKEN", environment.get("SEARCH_ONELAKE_USER_A_TOKEN"));
                break;

            case "same-user":
                environment.put("SEARCH_ONELAKE_USER_B_TOKEN", token("user-a", "tenant", expiry + 1));
                break;

            case "other-tenant":
                environment.put("SEARCH_ONELAKE_USER_B_TOKEN", token("user-b", "other", expiry));
                break;

            case "expired":
                environment.put("SEARCH_ONELAKE_USER_B_TOKEN", token("user-b", "tenant", expiry - 3600));
                break;

            case "long-lived":
                environment.put("SEARCH_ONELAKE_USER_B_TOKEN", token("user-b", "tenant", expiry + 7200));
                break;

            case "bearer-prefix":
                environment.put("SEARCH_ONELAKE_USER_B_TOKEN",
                    "Bearer " + environment.get("SEARCH_ONELAKE_USER_B_TOKEN"));
                break;

            case "invalid-token":
                environment.put("SEARCH_ONELAKE_USER_B_TOKEN", "invalid.token.input");
                break;

            case "same-document":
                environment.put("SEARCH_ONELAKE_USER_B_DOCUMENT_URL", DOCUMENT_A);
                break;

            case "outside-prefix":
                environment.put("SEARCH_ONELAKE_USER_B_DOCUMENT_URL", "https://example.com/unapproved.txt");
                break;

            case "ambiguous-prefix":
                environment.put("SEARCH_ONELAKE_DOCUMENT_URL_PREFIX", PREFIX + "../");
                break;

            default:
                throw new AssertionError("Unexpected test scenario.");
        }
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> OneLakeSecurityFilteringPreviewExample.Fixture.fromEnvironment(environment::get));
        assertNull(error.getCause());
        assertFalse(error.getMessage().contains(environment.get("SEARCH_ONELAKE_USER_A_TOKEN")));
        assertFalse(error.getMessage().contains(environment.get("SEARCH_ONELAKE_USER_B_TOKEN")));
    }

    private static Map<String, String> environment() {
        Map<String, String> environment = new HashMap<>();
        environment.put("SEARCH_ONELAKE_FIXTURE_APPROVED", "true");
        environment.put("SEARCH_ONELAKE_WORKSPACE_ID", WORKSPACE);
        environment.put("SEARCH_ONELAKE_LAKEHOUSE_ID", LAKEHOUSE);
        environment.put("SEARCH_ONELAKE_TARGET_PATH", TARGET);
        environment.put("SEARCH_ONELAKE_DOCUMENT_URL_PREFIX", PREFIX);
        environment.put("SEARCH_ONELAKE_USER_A_DOCUMENT_URL", DOCUMENT_A);
        environment.put("SEARCH_ONELAKE_USER_B_DOCUMENT_URL", DOCUMENT_B);
        environment.put("SEARCH_ONELAKE_FIXTURE_QUERY", "dedicated ACL fixture");
        long expiry = Instant.now().getEpochSecond() + 1800;
        environment.put("SEARCH_ONELAKE_USER_A_TOKEN", token("user-a", "tenant", expiry));
        environment.put("SEARCH_ONELAKE_USER_B_TOKEN", token("user-b", "tenant", expiry));
        return environment;
    }

    private static String token(String user, String tenant, long expiry) {
        String header = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = "{\"oid\":\"" + user + "\",\"tid\":\"" + tenant + "\",\"exp\":" + expiry + "}";
        return header + "."
            + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
            + ".test-signature";
    }

    private static final class Scenario {
        private final String failure;
        private final Map<String, String> environment = environment();
        private final List<String> deleted = new ArrayList<>();
        private final AtomicInteger pauses = new AtomicInteger();
        private String sourceName;
        private String baseName;
        private int statusChecks;
        private int retrievals;

        private Scenario(String failure) {
            this.failure = failure;
        }

        private void execute() {
            HttpClient transport = this::send;
            SearchIndexClient indexes = new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
                .credential(new AzureKeyCredential("test-search-key"))
                .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
                .httpClient(transport)
                .buildClient();
            OneLakeSecurityFilteringPreviewExample.run(indexes,
                name -> new KnowledgeBaseRetrievalClientBuilder().endpoint("https://example.search.windows.net")
                    .knowledgeBaseName(name)
                    .credential(new AzureKeyCredential("test-search-key"))
                    .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
                    .httpClient(transport)
                    .buildClient(),
                OneLakeSecurityFilteringPreviewExample.Fixture.fromEnvironment(environment::get),
                pauses::incrementAndGet, 3);
        }

        private Mono<HttpResponse> send(HttpRequest request) {
            String path = request.getUrl().getPath();
            assertTrue(request.getUrl().getQuery().contains("api-version=2026-11-01-preview"));
            assertEquals("test-search-key", request.getHeaders().getValue("api-key"));
            assertNull(request.getHeaders().getValue("Authorization"));
            assertNull(request.getHeaders().getValue("x-ms-enable-elevated-read"));
            String sourceToken = request.getHeaders().getValue("x-ms-query-source-authorization");
            if (path.contains("retrieve")) {
                return retrieve(request, sourceToken);
            }
            assertNull(sourceToken);
            if (request.getHttpMethod() == HttpMethod.DELETE) {
                assertTrue(path.contains("knowledgebases") || path.contains("knowledgesources"));
                deleted.add(path);
                return response(request, 204, "");
            }
            if (request.getHttpMethod() == HttpMethod.GET) {
                assertTrue(path.contains(sourceName) && path.endsWith("/status"));
                statusChecks++;
                return response(request, 200, status());
            }
            assertEquals(HttpMethod.POST, request.getHttpMethod());
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            assertFalse(
                request.getBodyAsBinaryData().toString().contains(environment.get("SEARCH_ONELAKE_USER_A_TOKEN")));
            assertFalse(
                request.getBodyAsBinaryData().toString().contains(environment.get("SEARCH_ONELAKE_USER_B_TOKEN")));
            if (path.endsWith("/knowledgesources")) {
                sourceName = (String) body.get("name");
                assertTrue(sourceName.startsWith("java-onelake-acl-ks-"));
                assertEquals("indexedOneLake", body.get("kind"));
                Map<?, ?> parameters = (Map<?, ?>) body.get("indexedOneLakeParameters");
                assertEquals(WORKSPACE, parameters.get("fabricWorkspaceId"));
                assertEquals(LAKEHOUSE, parameters.get("lakehouseId"));
                assertEquals(environment.get("SEARCH_ONELAKE_TARGET_PATH"), parameters.get("targetPath"));
                Map<?, ?> ingestion = (Map<?, ?>) parameters.get("ingestionParameters");
                assertEquals(Arrays.asList("oneLakeParentFolders", "oneLakeLakehouseId"),
                    ingestion.get("ingestionPermissionOptions"));
                assertEquals("minimal", ingestion.get("contentExtractionMode"));
                assertEquals(Boolean.TRUE, ingestion.get("disableImageVerbalization"));
                assertNull(ingestion.get("embeddingModel"));
                assertNull(ingestion.get("chatCompletionModel"));
                assertNull(ingestion.get("aiServices"));
                assertNull(ingestion.get("ingestionSchedule"));
                if ("source-conflict".equals(failure)) {
                    return conflict(request);
                }
            } else {
                assertTrue(path.endsWith("/knowledgebases"));
                baseName = (String) body.get("name");
                assertTrue(baseName.startsWith("java-onelake-acl-kb-"));
                List<?> sources = (List<?>) body.get("knowledgeSources");
                assertEquals(1, sources.size());
                assertEquals(sourceName, ((Map<?, ?>) sources.get(0)).get("name"));
                assertEquals("extractiveData", body.get("outputMode"));
                assertEquals("minimal", ((Map<?, ?>) body.get("retrievalReasoningEffort")).get("kind"));
                assertNull(body.get("models"));
                if ("base-conflict".equals(failure)) {
                    return conflict(request);
                }
            }
            return response(request, 201, request.getBodyAsBinaryData().toString());
        }

        private Mono<HttpResponse> retrieve(HttpRequest request, String sourceToken) {
            assertTrue(request.getUrl().getPath().contains(baseName));
            retrievals++;
            String user = retrievals == 1 ? "A" : "B";
            assertEquals(environment.get("SEARCH_ONELAKE_USER_" + user + "_TOKEN"), sourceToken);
            Map<?, ?> body = request.getBodyAsBinaryData().toObject(Map.class);
            assertNull(body.get("querySourceAuthorization"));
            assertFalse(request.getBodyAsBinaryData().toString().contains(sourceToken));
            assertEquals(Boolean.TRUE, body.get("includeActivity"));
            List<?> sources = (List<?>) body.get("knowledgeSourceParams");
            assertEquals(1, sources.size());
            Map<?, ?> source = (Map<?, ?>) sources.get(0);
            assertEquals("indexedOneLake", source.get("kind"));
            assertEquals(sourceName, source.get("knowledgeSourceName"));
            assertEquals(Boolean.TRUE, source.get("alwaysQuerySource"));
            assertEquals(Boolean.TRUE, source.get("failOnError"));
            assertEquals(Boolean.TRUE, source.get("includeReferences"));
            assertEquals(Boolean.FALSE, source.get("includeReferenceSourceData"));
            assertNull(source.get("filterAddOn"));
            assertNull(body.get("filter"));
            if ("forbidden-b".equals(failure) && retrievals == 2) {
                return response(request, 403, "{\"error\":{\"code\":\"Forbidden\",\"message\":\"User denied\"}}");
            }
            String document = retrievals == 1 ? DOCUMENT_A : DOCUMENT_B;
            if ("leak-a".equals(failure)) {
                document = DOCUMENT_B;
            } else if ("leak-b".equals(failure) && retrievals == 2) {
                document = DOCUMENT_A;
            }
            String reference = "{\"type\":\"" + ("wrong-type".equals(failure) ? "azureBlob" : "indexedOneLake")
                + "\",\"id\":\"citation-1\",\"activitySource\":" + ("unlinked".equals(failure) ? 9 : 1)
                + ",\"docUrl\":\"" + document + "\"}";
            if ("extra-reference".equals(failure)) {
                reference += ",{\"type\":\"indexedOneLake\",\"id\":\"citation-2\",\"activitySource\":1,"
                    + "\"docUrl\":\"" + DOCUMENT_B + "\"}";
            }
            String error
                = "activity-error".equals(failure) ? ",\"error\":{\"code\":\"Error\",\"message\":\"failed\"}" : "";
            String response = "{\"response\":[],\"activity\":[{\"type\":\"indexedOneLake\",\"id\":1,"
                + "\"knowledgeSourceName\":\"" + ("wrong-source".equals(failure) ? "unowned-source" : sourceName) + "\""
                + error + "}],\"references\":[" + ("missing-evidence".equals(failure) ? "" : reference) + "]}";
            return response(request, 200, response);
        }

        private String status() {
            if ("active-only".equals(failure) || ("success".equals(failure) && statusChecks == 1)) {
                return "{\"synchronizationStatus\":\"active\"}";
            }
            if ("current-error".equals(failure)) {
                return "{\"synchronizationStatus\":\"active\",\"currentSynchronizationState\":{"
                    + "\"startTime\":\"2026-11-01T00:00:00Z\",\"itemsUpdatesProcessed\":0,"
                    + "\"itemsUpdatesFailed\":1,\"itemsSkipped\":0}}";
            }
            return "{\"synchronizationStatus\":\"active\",\"lastSynchronizationState\":{"
                + "\"startTime\":\"2026-11-01T00:00:00Z\",\"endTime\":\"2026-11-01T00:00:01Z\","
                + "\"itemsUpdatesProcessed\":2,\"itemsUpdatesFailed\":" + ("ingestion-failed".equals(failure) ? 1 : 0)
                + ",\"itemsSkipped\":" + ("ingestion-skipped".equals(failure) ? 1 : 0) + "}}";
        }

        private void assertOwnedCleanup(boolean baseCreated) {
            assertNotNull(sourceName);
            assertEquals(baseCreated ? 2 : 1, deleted.size());
            if (baseCreated) {
                assertTrue(deleted.get(0).contains("knowledgebases") && deleted.get(0).contains(baseName));
            }
            String last = deleted.get(deleted.size() - 1);
            assertTrue(last.contains("knowledgesources") && last.contains(sourceName));
        }

        private static Mono<HttpResponse> conflict(HttpRequest request) {
            return response(request, 409, "{\"error\":{\"code\":\"AlreadyExists\",\"message\":\"Existing resource\"}}");
        }

        private static Mono<HttpResponse> response(HttpRequest request, int code, String json) {
            return Mono.just(new MockHttpResponse(request, code, BinaryData.fromString(json).toBytes()));
        }
    }
}
