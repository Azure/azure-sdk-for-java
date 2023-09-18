// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.TypeReference;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.search.documents.implementation.models.IndexBatch;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.IndexingResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.search.documents.SearchTestBase.API_KEY;
import static com.azure.search.documents.SearchTestBase.ENDPOINT;
import static com.azure.search.documents.SearchTestBase.HOTELS_DATA_JSON;
import static com.azure.search.documents.TestHelpers.readJsonFileToList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchIndexingBufferedSenderUnitTests {
    private static final TypeReference<Map<String, Object>> HOTEL_DOCUMENT_TYPE;
    private static final Function<Map<String, Object>, String> HOTEL_ID_KEY_RETRIEVER;

    static {
        HOTEL_DOCUMENT_TYPE = new TypeReference<Map<String, Object>>() {
        };
        HOTEL_ID_KEY_RETRIEVER = document -> String.valueOf(document.get("HotelId"));
    }

    private static SearchClientBuilder getSearchClientBuilder() {
        return new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .indexName("index")
            .credential(new AzureKeyCredential(API_KEY));
    }

    /**
     * Tests that a batch can timeout while indexing.
     */
    @Test
    public void flushTimesOut() {
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> Mono.<HttpResponse>just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(0, 200))).delayElement(Duration.ofSeconds(5)))
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 1));

        assertThrows(RuntimeException.class, () -> batchingClient.flush(Duration.ofSeconds(1), Context.NONE));
    }

    /**
     * Tests that a batch will retain in-flight documents if the request is cancelled before the response is handled.
     */
    @Test
    public void inFlightDocumentsAreRetried() {
        AtomicInteger callCount = new AtomicInteger(0);
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> {
                Mono<HttpResponse> response = Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                    createMockResponseData(0, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200)));
                if (callCount.getAndIncrement() == 0) {
                    return response.delayElement(Duration.ofSeconds(5));
                } else {
                    return response;
                }
            })
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .onActionAdded(ignored -> addedCount.incrementAndGet())
            .onActionSent(ignored -> sentCount.incrementAndGet())
            .onActionError(ignored -> errorCount.incrementAndGet())
            .onActionSucceeded(ignored -> successCount.incrementAndGet())
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        // First request is setup to timeout.
        assertThrows(RuntimeException.class, () -> batchingClient.flush(Duration.ofSeconds(3), Context.NONE));

        // Second request shouldn't timeout.
        assertDoesNotThrow(() -> batchingClient.flush(Duration.ofSeconds(3), Context.NONE));

        // Then validate that we have the expected number of requests sent and responded.
        assertEquals(10, addedCount.get());
        assertEquals(20, sentCount.get());
        assertEquals(0, errorCount.get());
        assertEquals(10, successCount.get());
    }

    /**
     * Tests that when a batch has some failures the indexing hook is properly notified.
     */
    @Test
    public void batchHasSomeFailures() {
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(0, 201, 400, 201, 404, 200, 200, 404, 400, 400, 201))))
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .onActionAdded(options -> addedCount.incrementAndGet())
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> errorCount.incrementAndGet())
            .onActionSent(options -> sentCount.incrementAndGet())
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        // Exceptions are propagated into the onActionError.
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(10, addedCount.get());
        assertEquals(5, successCount.get());
        assertEquals(5, errorCount.get());
        assertEquals(10, sentCount.get());

        /*
         * No documents failed with retryable errors, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /**
     * Tests that a batch will retry documents that fail with retryable status code.
     */
    @Test
    public void retryableDocumentsAreAddedBackToTheBatch() {
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(0, 201, 409, 201, 422, 200, 200, 503, 409, 422, 201))))
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .onActionAdded(options -> addedCount.incrementAndGet())
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> errorCount.incrementAndGet())
            .onActionSent(options -> sentCount.incrementAndGet())
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        // Exceptions are propagated into the onActionError.
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(10, addedCount.get());
        assertEquals(10, sentCount.get());
        assertEquals(5, successCount.get());
        assertEquals(0, errorCount.get());

        /*
         * 5 documents failed with retryable errors, so we should expect 5 documents are added back into the batch.
         */
        assertEquals(5, batchingClient.getActions().size());
    }

    /**
     * Tests that a batch splits if the service responds with a 413.
     */
    @Test
    public void batchSplits() {
        AtomicInteger callCount = new AtomicInteger();
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> {
                int count = callCount.getAndIncrement();
                if (count == 0) {
                    return Mono.just(new MockHttpResponse(request, 413));
                } else if (count == 1) {
                    return createMockBatchSplittingResponse(request, 0, 5);
                } else if (count == 2) {
                    return createMockBatchSplittingResponse(request, 5, 5);
                } else {
                    return Mono.error(new IllegalStateException("Unexpected request."));
                }
            }).bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .initialBatchActionCount(10)
            .onActionAdded(options -> addedCount.incrementAndGet())
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> errorCount.incrementAndGet())
            .onActionSent(options -> sentCount.incrementAndGet())
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        // No exception is thrown as the batch splits and retries successfully.
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(10, addedCount.get());
        assertEquals(10, successCount.get());
        assertEquals(0, errorCount.get());
        assertEquals(20, sentCount.get());

        /*
         * No documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /**
     * Tests that flushing a batch doesn't include duplicate keys.
     */
    @Test
    public void batchTakesAllNonDuplicateKeys() {
        AtomicInteger callCount = new AtomicInteger();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> {
                int count = callCount.getAndIncrement();
                if (count == 0) {
                    return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                        createMockResponseData(0, 200, 200, 200, 200, 200, 200, 200, 200, 200)));
                } else {
                    return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                        createMockResponseData(0, 200)));
                }
            }).bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .buildSender();

        List<Map<String, Object>> documents = readJsonFileToList(HOTELS_DATA_JSON);
        documents.get(9).put("HotelId", "1");

        batchingClient.addUploadActions(documents);

        assertDoesNotThrow((Executable) batchingClient::flush);

        /*
         * One document shouldn't have been sent as it contains a duplicate key from an earlier document.
         */
        assertEquals(1, batchingClient.getActions().size());

        assertDoesNotThrow((Executable) batchingClient::flush);

        /*
         * No documents should remain as no duplicate keys exists.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    @Test
    public void batchWithDuplicateKeysBeingRetriedTakesAllNonDuplicateKeys() {
        AtomicInteger callCount = new AtomicInteger();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> {
                int count = callCount.getAndIncrement();
                if (count == 0) {
                    return Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                        createMockResponseData(0, 503, 200, 200, 200, 200, 200, 200, 200, 200)));
                } else {
                    return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                        createMockResponseData(0, 200)));
                }
            }).bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .buildSender();

        List<Map<String, Object>> documents = readJsonFileToList(HOTELS_DATA_JSON);
        documents.get(9).put("HotelId", "1");

        batchingClient.addUploadActions(documents);

        assertDoesNotThrow((Executable) batchingClient::flush);

        /*
         * Two documents should be in the batch as one failed with a retryable status code and another wasn't sent as it
         * used a duplicate key from the batch that was sent.
         */
        assertEquals(2, batchingClient.getActions().size());

        assertDoesNotThrow((Executable) batchingClient::flush);

        /*
         * One document should remain in the batch as it had the same key as another document in the batch.
         */
        assertEquals(1, batchingClient.getActions().size());

        assertDoesNotThrow((Executable) batchingClient::flush);
        assertDoesNotThrow((Executable) batchingClient::close);

        /*
         * No documents should remain as no duplicate keys exists.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /**
     * Tests that an operation will be dumped into a "dead letter" queue if it is retried too many times.
     */
    public void batchRetriesUntilLimit() {
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(0, 409))))
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .maxRetriesPerAction(10)
            .onActionAdded(options -> addedCount.incrementAndGet())
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> errorCount.incrementAndGet())
            .onActionSent(options -> sentCount.incrementAndGet())
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 1));

        // Batch split until it was size of one and failed.
        for (int i = 0; i < 10; i++) {
            assertDoesNotThrow((Executable) batchingClient::flush);

            // Document should be added back into the batch as it is retryable.
            assertEquals(1, batchingClient.getActions().size());
        }

        // Final call which will trigger the retry limit for the document but doesn't throw.
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(1, addedCount.get());
        assertEquals(1, errorCount.get());
        // Document gets sent 10 times for the number of retries that happen.
        assertEquals(11, sentCount.get());
        assertEquals(0, successCount.get());

        /*
         * All documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /**
     * Tests that a batch will split until it is a size of one if the service continues returning 413. When the service
     * returns 413 on a batch size of one it will be deemed a final error state.
     */
    @Test
    public void batchSplitsUntilOneAndFails() {
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 413)))
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .initialBatchActionCount(2)
            .onActionAdded(options -> addedCount.incrementAndGet())
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> errorCount.incrementAndGet())
            .onActionSent(options -> sentCount.incrementAndGet())
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 2));

        // Batch split until it was size of one and fails but doesn't throw.
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(2, addedCount.get());
        assertEquals(2, errorCount.get());
        assertEquals(0, successCount.get());
        assertEquals(4, sentCount.get());

        /*
         * No documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /**
     * Tests that a batch will split until all sub-batches are one document and some of the sub batches fail with 413
     * while others do not.
     */
    @Test
    public void batchSplitsUntilOneAndPartiallyFails() {
        AtomicInteger callCount = new AtomicInteger();
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> (callCount.getAndIncrement() < 2)
                ? Mono.just(new MockHttpResponse(request, 413))
                : createMockBatchSplittingResponse(request, 1, 1))
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .initialBatchActionCount(2)
            .onActionAdded(options -> addedCount.incrementAndGet())
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> errorCount.incrementAndGet())
            .onActionSent(options -> sentCount.incrementAndGet())
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 2));

        // Batch split until it was size of one and fails but doesn't throw.
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(2, addedCount.get());
        assertEquals(1, errorCount.get());
        assertEquals(1, successCount.get());
        assertEquals(4, sentCount.get());

        /*
         * No documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    @ParameterizedTest
    @MethodSource("operationsThrowAfterClientIsClosedSupplier")
    public void operationsThrowAfterClientIsClosed(
        Consumer<SearchIndexingBufferedSender<Map<String, Object>>> operation) {
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .buildSender();

        batchingClient.close();

        assertThrows(IllegalStateException.class, () -> operation.accept(batchingClient));

    }

    static Stream<Consumer<SearchIndexingBufferedSender<Map<String, Object>>>> operationsThrowAfterClientIsClosedSupplier() {
        List<Map<String, Object>> simpleDocuments = Collections.singletonList(Collections.singletonMap("key", "value"));
        List<IndexAction<Map<String, Object>>> actions = simpleDocuments.stream()
            .map(document -> new IndexAction<Map<String, Object>>()
                .setDocument(document)
                .setActionType(IndexActionType.UPLOAD))
            .collect(Collectors.toList());

        return Stream.of(
            client -> client.addActions(actions),
            client -> client.addActions(actions, Duration.ofSeconds(60), Context.NONE),

            client -> client.addUploadActions(simpleDocuments),
            client -> client.addUploadActions(simpleDocuments, Duration.ofSeconds(60), Context.NONE),

            client -> client.addMergeOrUploadActions(simpleDocuments),
            client -> client.addMergeOrUploadActions(simpleDocuments, Duration.ofSeconds(60), Context.NONE),

            client -> client.addMergeActions(simpleDocuments),
            client -> client.addMergeActions(simpleDocuments, Duration.ofSeconds(60), Context.NONE),

            client -> client.addDeleteActions(simpleDocuments),
            client -> client.addDeleteActions(simpleDocuments, Duration.ofSeconds(60), Context.NONE),

            SearchIndexingBufferedSender::flush,
            client -> client.flush(Duration.ofSeconds(60), Context.NONE)
        );
    }

    @Test
    public void closingTwiceDoesNotThrow() {
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .buildSender();

        batchingClient.close();

        assertDoesNotThrow((Executable) batchingClient::close);
    }

    @Test
    public void concurrentFlushesOnlyAllowsOneProcessor() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger();

        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> {
                int count = callCount.getAndIncrement();
                if (count == 0) {
                    return createMockBatchSplittingResponse(request, 0, 5)
                        .delayElement(Duration.ofSeconds(2))
                        .map(Function.identity());
                } else if (count == 1) {
                    return createMockBatchSplittingResponse(request, 5, 5);
                } else {
                    return Mono.error(new IllegalStateException("Unexpected request."));
                }
            }).bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .initialBatchActionCount(5)
            .buildAsyncSender();

        CountDownLatch countDownLatch = new CountDownLatch(2);
        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON)).block();

        AtomicLong firstFlushCompletionTime = new AtomicLong();
        Mono.using(() -> countDownLatch, ignored -> batchingClient.flush(), latch -> {
            firstFlushCompletionTime.set(System.nanoTime());
            latch.countDown();
        }).subscribe();

        AtomicLong secondFlushCompletionTime = new AtomicLong();
        Mono.using(() -> countDownLatch, ignored -> batchingClient.flush(), latch -> {
            secondFlushCompletionTime.set(System.nanoTime());
            latch.countDown();
        }).subscribe();

        countDownLatch.await();
        assertTrue(firstFlushCompletionTime.get() > secondFlushCompletionTime.get());
    }

    @Test
    public void closeWillWaitForAnyCurrentFlushesToCompleteBeforeRunning() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger();

        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .httpClient(request -> {
                int count = callCount.getAndIncrement();
                if (count == 0) {
                    return createMockBatchSplittingResponse(request, 0, 5)
                        .delayElement(Duration.ofSeconds(2))
                        .map(Function.identity());
                } else if (count == 1) {
                    return createMockBatchSplittingResponse(request, 5, 5);
                } else {
                    return Mono.error(new IllegalStateException("Unexpected request."));
                }
            }).bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .initialBatchActionCount(5)
            .buildAsyncSender();

        CountDownLatch countDownLatch = new CountDownLatch(2);
        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON)).block();

        AtomicLong firstFlushCompletionTime = new AtomicLong();
        Mono.using(() -> countDownLatch, ignored -> batchingClient.flush(), latch -> {
            firstFlushCompletionTime.set(System.nanoTime());
            latch.countDown();
        }).subscribe();

        AtomicLong secondFlushCompletionTime = new AtomicLong();
        Mono.using(() -> countDownLatch, ignored -> batchingClient.close(), latch -> {
            secondFlushCompletionTime.set(System.nanoTime());
            latch.countDown();
        }).subscribe();

        countDownLatch.await();
        assertTrue(firstFlushCompletionTime.get() <= secondFlushCompletionTime.get());
    }

    @Test
    public void serverBusyResponseRetries() {
        AtomicInteger callCount = new AtomicInteger();
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)))
            .httpClient(request -> {
                int count = callCount.getAndIncrement();
                if (count < 1) {
                    return Mono.just(new MockHttpResponse(request, 503));
                } else {
                    return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                        createMockResponseData(0, 201, 200, 201, 200, 200, 200, 201, 201, 200, 201)));
                }
            }).bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .onActionAdded(options -> addedCount.incrementAndGet())
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> errorCount.incrementAndGet())
            .onActionSent(options -> sentCount.incrementAndGet())
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        // No exception is thrown as the batch splits and retries successfully.
        assertDoesNotThrow((Executable) batchingClient::flush);
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(10, addedCount.get());
        assertEquals(10, successCount.get());
        assertEquals(0, errorCount.get());
        assertEquals(20, sentCount.get());

        /*
         * No documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    @Test
    public void delayGrowsWith503Response() {
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)))
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 503)))
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        assertDoesNotThrow((Executable) batchingClient::flush);
        Duration retryDuration = batchingClient.client.publisher.getCurrentRetryDelay();
        assertTrue(retryDuration.compareTo(Duration.ZERO) > 0);

        assertDoesNotThrow((Executable) batchingClient::flush);
        assertTrue(batchingClient.client.publisher.getCurrentRetryDelay().compareTo(retryDuration) > 0);
    }

    @Test
    public void delayGrowsWith503BatchOperation() {
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)))
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(0, 503))))
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 1));

        assertDoesNotThrow((Executable) batchingClient::flush);
        Duration retryDuration = batchingClient.client.publisher.getCurrentRetryDelay();
        assertTrue(retryDuration.compareTo(Duration.ZERO) > 0);

        assertDoesNotThrow((Executable) batchingClient::flush);
        assertTrue(batchingClient.client.publisher.getCurrentRetryDelay().compareTo(retryDuration) > 0);
    }

    @Test
    public void delayResetsAfterNo503s() {
        AtomicInteger callCount = new AtomicInteger();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder()
            .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)))
            .httpClient(request -> {
                int count = callCount.getAndIncrement();
                if (count == 0) {
                    return Mono.just(new MockHttpResponse(request, 503));
                } else {
                    return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                        createMockResponseData(0, 200)));
                }
            }).bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 1));

        assertDoesNotThrow((Executable) batchingClient::flush);
        Duration retryDuration = batchingClient.client.publisher.getCurrentRetryDelay();
        assertTrue(retryDuration.compareTo(Duration.ZERO) > 0);

        assertDoesNotThrow((Executable) batchingClient::flush);
        assertEquals(Duration.ZERO, batchingClient.client.publisher.getCurrentRetryDelay());
    }

    /*
     * Helper method that creates mock results with the status codes given. This will create a mock indexing result
     * and turn it into a byte[] so it can be put in a mock response.
     */
    private static byte[] createMockResponseData(int keyIdOffset, int... statusCodes) {
        List<IndexingResult> results = new ArrayList<>();

        for (int i = 0; i < statusCodes.length; i++) {
            int statusCode = statusCodes[i];
            results.add(new IndexingResult(String.valueOf(keyIdOffset + i + 1), statusCode == 200 || statusCode == 201,
                statusCode));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            writer.writeJson(new IndexDocumentsResult(results)).flush();
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Mono<HttpResponse> createMockBatchSplittingResponse(HttpRequest request, int keyIdOffset,
        int expectedBatchSize) {
        return FluxUtil.collectBytesInByteBufferStream(request.getBody())
            .flatMap(bodyBytes -> {
                // Request documents are in a sub-node called value.
                try (JsonReader reader = JsonProviders.createReader(bodyBytes)) {
                    IndexBatch indexBatch = IndexBatch.fromJson(reader);

                    // Given the initial size was 10 and it was split we should expect 5 elements.
                    assertNotNull(indexBatch);
                    assertEquals(expectedBatchSize, indexBatch.getActions().size());

                    int[] statusCodes = new int[expectedBatchSize];
                    Arrays.fill(statusCodes, 200);

                    return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                        createMockResponseData(keyIdOffset, statusCodes)));
                } catch (IOException ex) {
                    return Mono.error(ex);
                }
            });
    }
}
