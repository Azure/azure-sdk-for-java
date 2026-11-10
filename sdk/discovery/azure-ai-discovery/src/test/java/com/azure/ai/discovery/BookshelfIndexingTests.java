// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.KnowledgeBaseIndexingOperationResponse;
import com.azure.ai.discovery.models.KnowledgeBaseOperationResponse;
import com.azure.ai.discovery.models.OperationState;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookshelfIndexingTests {
    @ParameterizedTest
    @CsvSource({ "true,true", "true,false", "false,true", "false,false" })
    void indexingPreservesRunningAndTerminalStates(boolean sync, boolean succeeds) {
        AtomicInteger requests = new AtomicInteger();
        String operationLocation = "https://example.com/knowledgeBases/test-kb/operations/test-run";
        HttpClient httpClient = request -> Mono.fromSupplier(() -> {
            int attempt = requests.getAndIncrement();
            assertEquals(attempt == 0 ? HttpMethod.POST : HttpMethod.GET, request.getHttpMethod());
            String status = attempt < 2 ? "Running" : succeeds ? "Succeeded" : "Failed";
            String error = attempt >= 2 && !succeeds
                ? ",\"error\":{\"code\":\"GenericIndexingError\",\"message\":\"Indexing failed\"}"
                : "";
            String body = "{\"id\":\"test-run\",\"operationType\":\"Indexing\",\"status\":\"" + status
                + "\",\"indexingResult\":{\"runId\":\"test-run\"}" + error + "}";
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
            if (attempt == 0) {
                headers.set(HttpHeaderName.fromString("operation-location"), operationLocation);
            } else {
                assertEquals("/knowledgeBases/test-kb/operations/test-run", request.getUrl().getPath());
            }
            return new MockHttpResponse(request, attempt == 0 ? 202 : 200, headers,
                body.getBytes(StandardCharsets.UTF_8));
        });
        BookshelfClientBuilder builder = new BookshelfClientBuilder().endpoint("https://example.com")
            .credential(new MockTokenCredential())
            .httpClient(httpClient);

        LongRunningOperationStatus terminalStatus;
        KnowledgeBaseOperationResponse result;
        if (sync) {
            SyncPoller<KnowledgeBaseOperationResponse, Void> poller = builder.buildClient()
                .beginStartIndexing("test-kb")
                .setPollInterval(Duration.ofMillis(1));
            PollResponse<KnowledgeBaseOperationResponse> running = poller.poll();
            assertEquals(LongRunningOperationStatus.IN_PROGRESS, running.getStatus());
            assertEquals(OperationState.RUNNING, running.getValue().getStatus());
            PollResponse<KnowledgeBaseOperationResponse> completion = poller.waitForCompletion();
            terminalStatus = completion.getStatus();
            result = completion.getValue();
        } else {
            AsyncPollResponse<KnowledgeBaseOperationResponse, Void> completion = builder.buildAsyncClient()
                .beginStartIndexing("test-kb")
                .setPollInterval(Duration.ofMillis(1))
                .doOnNext(response -> {
                    if (!response.getStatus().isComplete()) {
                        assertEquals(LongRunningOperationStatus.IN_PROGRESS, response.getStatus());
                        assertEquals(OperationState.RUNNING, response.getValue().getStatus());
                    }
                })
                .blockLast(Duration.ofSeconds(5));
            assertNotNull(completion);
            terminalStatus = completion.getStatus();
            result = completion.getValue();
        }

        assertEquals(succeeds ? LongRunningOperationStatus.SUCCESSFULLY_COMPLETED : LongRunningOperationStatus.FAILED,
            terminalStatus);
        assertNotNull(result);
        assertEquals(succeeds ? OperationState.SUCCEEDED : OperationState.FAILED, result.getStatus());
        KnowledgeBaseIndexingOperationResponse operation = (KnowledgeBaseIndexingOperationResponse) result;
        assertEquals("test-run", operation.getIndexingResult().getRunId());
        if (succeeds) {
            assertNull(operation.getError());
        } else {
            assertNotNull(operation.getError());
            assertEquals("GenericIndexingError", operation.getError().getCode());
            assertEquals("Indexing failed", operation.getError().getMessage());
        }
        assertEquals(3, requests.get(), "Activation, Running, and the terminal response must all be consumed");
    }
}
