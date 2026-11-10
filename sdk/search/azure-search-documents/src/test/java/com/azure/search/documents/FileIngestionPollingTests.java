// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.FileUploadMetadata;
import com.azure.search.documents.indexes.models.KnowledgeSourceFile;
import com.azure.search.documents.indexes.models.KnowledgeSourceFileIngestionOperation;
import com.azure.search.documents.indexes.models.UpdateKnowledgeSourceFileRequest;
import com.azure.search.documents.indexes.models.UploadKnowledgeSourceFileMultipartRequest;
import com.azure.search.documents.models.ContentFileDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileIngestionPollingTests {
    private static final String ENDPOINT = "https://example.search.windows.net";
    private static final String OPERATION_PATH = "/knowledgesources('files')/files/operations/op-1";
    private static final String OPERATION_URL = ENDPOINT + OPERATION_PATH + "?api-version=2026-11-01-preview";
    private static final Duration POLL_INTERVAL = Duration.ofMillis(1);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Test
    public void uploadPollerFollowsRelativeOperationLocationAndExtractsFileResult() {
        AtomicInteger polls = new AtomicInteger();
        SyncPoller<KnowledgeSourceFileIngestionOperation, KnowledgeSourceFile> poller
            = builder(ingestionTransport(false, false, polls)).buildClient()
                .beginIngestKnowledgeSourceFileMultipart("files", upload())
                .setPollInterval(POLL_INTERVAL);

        PollResponse<KnowledgeSourceFileIngestionOperation> terminal = poller.waitForCompletion(TIMEOUT);
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, terminal.getStatus());
        assertEquals("op-1", terminal.getValue().getId());
        assertEquals("file-1", terminal.getValue().getResult().getFileId());
        KnowledgeSourceFile file = poller.getFinalResult();
        assertEquals("file-1", file.getFileId());
        assertEquals("notes.txt", file.getFileName());
        assertEquals(2, polls.get(), "Final-result extraction must not issue another resource GET.");
    }

    @Test
    public void asyncReplacementPollerPreservesFileIdAndExtractsFileResult() {
        AtomicInteger polls = new AtomicInteger();
        AsyncPollResponse<KnowledgeSourceFileIngestionOperation, KnowledgeSourceFile> terminal
            = builder(ingestionTransport(true, false, polls)).buildAsyncClient()
                .beginIngestKnowledgeSourceFileReplacement("files", "file-1", replacement())
                .setPollInterval(POLL_INTERVAL)
                .last()
                .block(TIMEOUT);

        assertNotNull(terminal);
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, terminal.getStatus());
        assertEquals("op-1", terminal.getValue().getId());
        KnowledgeSourceFile file = terminal.getFinalResult().block(TIMEOUT);
        assertNotNull(file);
        assertEquals("file-1", file.getFileId());
        assertEquals("notes.txt", file.getFileName());
        assertEquals(2, polls.get(), "Final-result extraction must use the terminal operation's result.");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void failedOperationRetainsServiceErrorAndDoesNotReturnAFile(boolean async) {
        AtomicInteger polls = new AtomicInteger();
        SearchIndexClientBuilder clientBuilder = builder(ingestionTransport(false, true, polls));
        if (async) {
            AsyncPollResponse<KnowledgeSourceFileIngestionOperation, KnowledgeSourceFile> terminal
                = clientBuilder.buildAsyncClient()
                    .beginIngestKnowledgeSourceFileMultipart("files", upload())
                    .setPollInterval(POLL_INTERVAL)
                    .last()
                    .block(TIMEOUT);
            assertNotNull(terminal);
            assertEquals(LongRunningOperationStatus.FAILED, terminal.getStatus());
            assertFailure(terminal.getValue());
            assertThrows(AzureException.class, () -> terminal.getFinalResult().block(TIMEOUT));
        } else {
            SyncPoller<KnowledgeSourceFileIngestionOperation, KnowledgeSourceFile> poller = clientBuilder.buildClient()
                .beginIngestKnowledgeSourceFileMultipart("files", upload())
                .setPollInterval(POLL_INTERVAL);
            PollResponse<KnowledgeSourceFileIngestionOperation> terminal = poller.waitForCompletion(TIMEOUT);
            assertEquals(LongRunningOperationStatus.FAILED, terminal.getStatus());
            assertFailure(terminal.getValue());
            assertThrows(AzureException.class, poller::getFinalResult);
        }
        assertEquals(2, polls.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void rejectedUploadRoutesHttpErrorWithoutPolling(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        SearchIndexClientBuilder clientBuilder = builder(request -> {
            requests.incrementAndGet();
            assertEquals(HttpMethod.POST, request.getHttpMethod());
            return Mono.just(response(request, 400,
                "{\"error\":{\"code\":\"InvalidFile\",\"message\":\"Unsupported file format\"}}"));
        });
        HttpResponseException error = assertThrows(HttpResponseException.class, () -> {
            if (async) {
                clientBuilder.buildAsyncClient()
                    .beginIngestKnowledgeSourceFileMultipart("files", upload())
                    .last()
                    .block(TIMEOUT);
            } else {
                clientBuilder.buildClient()
                    .beginIngestKnowledgeSourceFileMultipart("files", upload())
                    .waitForCompletion(TIMEOUT);
            }
        });
        assertEquals(400, error.getResponse().getStatusCode());
        assertTrue(error.getResponse().getBodyAsString().block().contains("Unsupported file format"));
        assertEquals(1, requests.get());
    }

    @Test
    public void operationGetAndSyncAsyncListsPreserveErrorsResultsAndContinuation() {
        AtomicInteger firstPages = new AtomicInteger();
        AtomicInteger nextPages = new AtomicInteger();
        SearchIndexClientBuilder clientBuilder = builder(request -> {
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            assertTrue(request.getUrl().getQuery().contains("api-version=2026-11-01-preview"));
            String path = request.getUrl().getPath();
            if (path.endsWith("/op-1")) {
                return Mono.just(response(request, 200, operation("op-1", "succeeded", false)));
            }
            assertEquals("/knowledgesources('files')/files/operations", path);
            if (request.getUrl().getQuery().contains("continuation=page2")) {
                nextPages.incrementAndGet();
                return Mono.just(response(request, 200, "{\"value\":[" + operation("op-2", "failed", true) + "]}"));
            }
            firstPages.incrementAndGet();
            String next = ENDPOINT + path + "?api-version=2026-11-01-preview&continuation=page2";
            return Mono.just(response(request, 200,
                "{\"value\":[" + operation("op-1", "succeeded", false) + "],\"@odata.nextLink\":\"" + next + "\"}"));
        });

        KnowledgeSourceFileIngestionOperation syncFetched
            = clientBuilder.buildClient().getKnowledgeSourceFileIngestionOperation("files", "op-1");
        assertEquals("file-1", syncFetched.getResult().getFileId());
        KnowledgeSourceFileIngestionOperation fetched
            = clientBuilder.buildAsyncClient().getKnowledgeSourceFileIngestionOperation("files", "op-1").block(TIMEOUT);
        assertNotNull(fetched);
        assertEquals("file-1", fetched.getResult().getFileId());
        List<KnowledgeSourceFileIngestionOperation> sync = clientBuilder.buildClient()
            .listKnowledgeSourceFileIngestionOperations("files")
            .stream()
            .collect(Collectors.toList());
        List<KnowledgeSourceFileIngestionOperation> async = clientBuilder.buildAsyncClient()
            .listKnowledgeSourceFileIngestionOperations("files")
            .collectList()
            .block(TIMEOUT);
        assertOperationPages(sync);
        assertOperationPages(async);
        assertEquals(2, firstPages.get());
        assertEquals(2, nextPages.get());
    }

    private static HttpClient ingestionTransport(boolean replacement, boolean fail, AtomicInteger polls) {
        return request -> {
            assertTrue(request.getUrl().getQuery().contains("api-version=2026-11-01-preview"));
            if (request.getHttpMethod() == HttpMethod.POST) {
                String path = replacement
                    ? "/knowledgesources('files')/files('file-1'):ingest"
                    : "/knowledgesources('files')/files:ingest";
                assertEquals(path, request.getUrl().getPath());
                assertTrue(
                    request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE).startsWith("multipart/form-data"));
                String body = request.getBodyAsBinaryData().toString();
                assertTrue(body.contains("name=\"metadata\""));
                assertTrue(body.contains("\"fileName\":\"notes.txt\""));
                assertTrue(body.contains("name=\"content\""));
                assertTrue(body.contains("filename=\"notes.txt\""));
                assertTrue(body.contains("offline file contents"));
                HttpHeaders headers = new HttpHeaders()
                    .set(HttpHeaderName.fromString("Operation-Location"),
                        replacement ? OPERATION_URL : OPERATION_PATH + "?api-version=2026-11-01-preview")
                    .set(HttpHeaderName.RETRY_AFTER, "0");
                return Mono.just(new MockHttpResponse(request, 202, headers,
                    operation("op-1", "notStarted", false).getBytes(StandardCharsets.UTF_8)));
            }
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            assertEquals(OPERATION_URL, request.getUrl().toString());
            int poll = polls.incrementAndGet();
            assertTrue(poll <= 2, "Polling must stop when the operation reaches a terminal state.");
            return Mono.just(response(request, 200,
                operation("op-1", poll == 1 ? "running" : fail ? "failed" : "succeeded", fail && poll > 1)));
        };
    }

    private static String operation(String id, String status, boolean error) {
        String details = error
            ? ",\"error\":{\"code\":\"ExtractionFailed\",\"message\":\"Cannot extract document\"}"
            : "succeeded".equals(status) ? ",\"result\":{\"fileId\":\"file-1\",\"fileName\":\"notes.txt\"}" : "";
        return "{\"id\":\"" + id + "\",\"kind\":\"knowledgeSourceFileIngestion\",\"status\":\"" + status + "\""
            + details + "}";
    }

    private static void assertFailure(KnowledgeSourceFileIngestionOperation operation) {
        assertNotNull(operation.getError());
        assertEquals("ExtractionFailed", operation.getError().getCode());
        assertEquals("Cannot extract document", operation.getError().getMessage());
    }

    private static void assertOperationPages(List<KnowledgeSourceFileIngestionOperation> operations) {
        assertNotNull(operations);
        assertEquals(2, operations.size());
        assertEquals("op-1", operations.get(0).getId());
        assertEquals("file-1", operations.get(0).getResult().getFileId());
        assertEquals("op-2", operations.get(1).getId());
        assertFailure(operations.get(1));
    }

    private static UploadKnowledgeSourceFileMultipartRequest upload() {
        return new UploadKnowledgeSourceFileMultipartRequest(metadata(), content());
    }

    private static UpdateKnowledgeSourceFileRequest replacement() {
        return new UpdateKnowledgeSourceFileRequest(metadata(), content());
    }

    private static FileUploadMetadata metadata() {
        return new FileUploadMetadata().setFileName("notes.txt");
    }

    private static ContentFileDetails content() {
        return new ContentFileDetails(BinaryData.fromString("offline file contents")).setFilename("notes.txt");
    }

    private static SearchIndexClientBuilder builder(HttpClient transport) {
        return new SearchIndexClientBuilder().endpoint(ENDPOINT)
            .credential(new AzureKeyCredential("offline-test-key"))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .httpClient(transport);
    }

    private static MockHttpResponse response(HttpRequest request, int status, String body) {
        return new MockHttpResponse(request, status, new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "0"),
            body.getBytes(StandardCharsets.UTF_8));
    }
}
