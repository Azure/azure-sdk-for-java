// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.documents.implementation.models.IndexDocumentsResult;
import com.azure.search.documents.implementation.models.IndexingResult;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.search.documents.TestHelpers.readJsonFileToList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SearchIndexingBufferedSender}.
 */
public class SearchIndexingBufferedSenderTests extends SearchTestBase {
    private static final JacksonAdapter JACKSON_ADAPTER;
    private String indexToDelete;
    private SearchClientBuilder clientBuilder;

    static {
        JacksonAdapter adapter = new JacksonAdapter();
        adapter.serializer().setAnnotationIntrospector(new IgnoreJacksonWriteOnlyAccess());

        JACKSON_ADAPTER = adapter;
    }


    private void setupIndex() {
        String indexName = createHotelIndex();
        this.indexToDelete = indexName;

        this.clientBuilder = getSearchClientBuilder(indexName);
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        if (!CoreUtils.isNullOrEmpty(indexToDelete)) {
            getSearchIndexClientBuilder().buildClient().deleteIndex(indexToDelete);
        }
    }

    /**
     * Tests that flushing the batch sends the documents to the service.
     */
    @Test
    public void flushBatch() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = client.getSearchIndexingBufferedSender(
            new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlush(false)
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));
        batchingClient.flush();

        sleepIfRunningAgainstService(3000);

        assertEquals(10, client.getDocumentCount());

        batchingClient.close();
    }

    /**
     * Tests that the batch will automatically flush when the configured batch size is reached, if configured for auto
     * flushing.
     */
    @Test
    public void autoFlushBatchOnSize() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = client.getSearchIndexingBufferedSender(
            new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlushWindow(Duration.ofMinutes(5))
                .setBatchSize(10)
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        sleepIfRunningAgainstService(3000);

        assertEquals(10, client.getDocumentCount());
        batchingClient.close();
    }

    /**
     * Tests that batching will automatically flush when the flush windows completes, if configured for auto flushing.
     */
    @Test
    public void autoFlushBatchOnDelay() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = client.getSearchIndexingBufferedSender(
            new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setBatchSize(10)
                .setAutoFlushWindow(Duration.ofSeconds(3))
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        sleepIfRunningAgainstService((long) (3000 * 1.5));

        assertEquals(10, client.getDocumentCount());
        batchingClient.close();
    }

    /**
     * Tests that the batch will flush when the client is closed and documents still exist in the batch.
     */
    @Test
    public void batchFlushesOnClose() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = client.getSearchIndexingBufferedSender(
            new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));
        batchingClient.close();

        sleepIfRunningAgainstService(3000);

        assertEquals(10, client.getDocumentCount());
    }

    /**
     * Tests that when a batch has documents added but flush, auto-flush, or close is never called the documents don't
     * get indexed.
     */
    @Test
    public void batchGetsDocumentsButNeverFlushes() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = client.getSearchIndexingBufferedSender(
            new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlushWindow(Duration.ofMinutes(5))
                .setBatchSize(1000)
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        sleepIfRunningAgainstService(3000);

        assertEquals(0, client.getDocumentCount());
        batchingClient.close();
    }

    @Test
    public void indexManyDocumentsSmallDocumentSets() {
        setupIndex();

        AtomicInteger requestCount = new AtomicInteger();
        SearchClient client = clientBuilder
            .addPolicy((context, next) -> {
                requestCount.incrementAndGet();
                return next.process();
            })
            .buildClient();

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = client
            .getSearchIndexingBufferedSender(new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlushWindow(Duration.ofSeconds(5))
                .setBatchSize(10)
                .setOnActionSucceeded(action -> successCount.incrementAndGet())
                .setOnActionError((action, error) -> failedCount.incrementAndGet())
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        List<Map<String, Object>> documents = readJsonFileToList(HOTELS_DATA_JSON);
        for (int i = 0; i < 100; i++) {
            final int offset = i;
            batchingClient.addUploadActions(documents.stream()
                .map(HashMap::new)
                .peek(document -> {
                    int originalId = Integer.parseInt(document.get("HotelId").toString());
                    document.put("HotelId", String.valueOf((offset * 10) + originalId));
                })
                .collect(Collectors.toList()));
        }

        sleepIfRunningAgainstService((long) (15000 * 1.5));

        assertEquals(1000, successCount.get());
        assertEquals(0, failedCount.get());
        assertTrue(requestCount.get() >= 100);
        assertEquals(1000, client.getDocumentCount());
        batchingClient.close();
    }

    @Test
    public void indexManyDocumentsOneLargeDocumentSet() {
        setupIndex();

        AtomicInteger requestCount = new AtomicInteger();
        SearchClient client = clientBuilder
            .addPolicy((context, next) -> {
                requestCount.incrementAndGet();
                return next.process();
            })
            .buildClient();

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = client.
            getSearchIndexingBufferedSender(new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlushWindow(Duration.ofSeconds(5))
                .setBatchSize(10)
                .setOnActionSucceeded(action -> successCount.incrementAndGet())
                .setOnActionError((action, error) -> failedCount.incrementAndGet())
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        List<Map<String, Object>> documents = readJsonFileToList(HOTELS_DATA_JSON);
        List<Map<String, Object>> documentBatch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int offset = i;
            documents.stream().map(HashMap::new)
                .forEach(document -> {
                    int originalId = Integer.parseInt(document.get("HotelId").toString());
                    document.put("HotelId", String.valueOf((offset * 10) + originalId));
                    documentBatch.add(document);
                });
        }

        batchingClient.addUploadActions(documentBatch);

        sleepIfRunningAgainstService((long) (15000 * 1.5));

        assertEquals(1000, successCount.get());
        assertEquals(0, failedCount.get());
        assertTrue(requestCount.get() >= 100);
        assertEquals(1000, client.getDocumentCount());
        batchingClient.close();
    }

    /**
     * Tests that an empty batch doesn't attempt to index.
     */
    @Test
    public void emptyBatchIsNeverSent() {
        AtomicInteger requestCount = new AtomicInteger();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
            .addPolicy((context, next) -> {
                requestCount.incrementAndGet();
                return next.process();
            })
            .buildClient()
            .getSearchIndexingBufferedSender(new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.flush();

        // flushInternal should never be called if the batch doesn't have any documents.
        assertEquals(0, requestCount.get());
        batchingClient.close();
    }

    /**
     * Tests that a batch can timeout while indexing.
     */
    @Test
    public void flushTimesOut() {
        SearchIndexingBufferedSender<Integer> batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> Mono.<HttpResponse>empty().delayElement(Duration.ofSeconds(5)))
            .buildClient()
            .getSearchIndexingBufferedSender(new SearchIndexingBufferedSenderOptions<Integer>()
                .setAutoFlush(false)
                .setDocumentKeyRetriever(String::valueOf));

        batchingClient.addUploadActions(Collections.singletonList(1));

        assertThrows(RuntimeException.class, () -> batchingClient.flush(Duration.ofSeconds(1), Context.NONE));
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

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(0, 201, 400, 201, 404, 200, 200, 404, 400, 400, 201))))
            .buildClient()
            .getSearchIndexingBufferedSender(new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlush(false)
                .setOnActionAdded(action -> addedCount.incrementAndGet())
                .setOnActionSucceeded(action -> successCount.incrementAndGet())
                .setOnActionError((action, throwable) -> errorCount.incrementAndGet())
                .setOnActionSent(action -> sentCount.incrementAndGet())
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

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

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(0, 201, 409, 201, 422, 200, 200, 503, 409, 422, 201))))
            .buildClient()
            .getSearchIndexingBufferedSender(new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlush(false)
                .setOnActionAdded(action -> addedCount.incrementAndGet())
                .setOnActionSucceeded(action -> successCount.incrementAndGet())
                .setOnActionError((action, throwable) -> errorCount.incrementAndGet())
                .setOnActionSent(action -> sentCount.incrementAndGet())
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

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

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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
            })
            .buildClient()
            .getSearchIndexingBufferedSender(new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlush(false)
                .setOnActionAdded(action -> addedCount.incrementAndGet())
                .setOnActionSucceeded(action -> successCount.incrementAndGet())
                .setOnActionError((action, throwable) -> errorCount.incrementAndGet())
                .setOnActionSent(action -> sentCount.incrementAndGet())
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        // No exception is thrown as the batch splits and retries successfully.
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(10, addedCount.get());
        assertEquals(10, successCount.get());
        assertEquals(0, errorCount.get());
        assertEquals(10, sentCount.get());

        /*
         * No documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /**
     * Tests that an operation will be dumped into a "dead letter" queue if it is retried too many times.
     */
    @Test
    public void batchRetriesUntilLimit() {
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(0, 409))))
            .buildClient()
            .getSearchIndexingBufferedSender(new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlush(false)
                .setDocumentTryLimit(10)
                .setOnActionAdded(action -> addedCount.incrementAndGet())
                .setOnActionSucceeded(action -> successCount.incrementAndGet())
                .setOnActionError((action, throwable) -> errorCount.incrementAndGet())
                .setOnActionSent(action -> sentCount.incrementAndGet())
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 1));

        // Batch split until it was size of one and failed.
        for (int i = 0; i < 9; i++) {
            assertDoesNotThrow((Executable) batchingClient::flush);

            // Document should be added back into the batch as it is retryable.
            assertEquals(1, batchingClient.getActions().size());
        }

        // Final call which will trigger the retry limit for the document but doesn't throw.
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(1, addedCount.get());
        // Document gets sent 10 times for the number of retries that happen.
        assertEquals(10, sentCount.get());
        assertEquals(1, errorCount.get());
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

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 413)))
            .buildClient()
            .getSearchIndexingBufferedSender(new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlush(false)
                .setOnActionAdded(action -> addedCount.incrementAndGet())
                .setOnActionSucceeded(action -> successCount.incrementAndGet())
                .setOnActionError((action, throwable) -> errorCount.incrementAndGet())
                .setOnActionSent(action -> sentCount.incrementAndGet())
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 2));

        // Batch split until it was size of one and fails but doesn't throw.
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(2, addedCount.get());
        assertEquals(2, errorCount.get());
        assertEquals(0, successCount.get());
        assertEquals(2, sentCount.get());

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

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> (callCount.getAndIncrement() < 2)
                ? Mono.just(new MockHttpResponse(request, 413))
                : createMockBatchSplittingResponse(request, 1, 1))
            .buildClient()
            .getSearchIndexingBufferedSender(new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlush(false)
                .setOnActionAdded(action -> addedCount.incrementAndGet())
                .setOnActionSucceeded(action -> successCount.incrementAndGet())
                .setOnActionError((action, throwable) -> errorCount.incrementAndGet())
                .setOnActionSent(action -> sentCount.incrementAndGet())
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 2));

        // Batch split until it was size of one and fails but doesn't throw.
        assertDoesNotThrow((Executable) batchingClient::flush);

        assertEquals(2, addedCount.get());
        assertEquals(1, errorCount.get());
        assertEquals(1, successCount.get());
        assertEquals(2, sentCount.get());

        /*
         * No documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    @ParameterizedTest
    @MethodSource("operationsThrowAfterClientIsClosedSupplier")
    public void operationsThrowAfterClientIsClosed(
        Consumer<SearchIndexingBufferedSender<Map<String, Object>>> operation) {
        SearchClient client = getSearchClientBuilder("index").buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = client.getSearchIndexingBufferedSender(
            new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlush(false)
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.close();

        assertThrows(IllegalStateException.class, () -> operation.accept(batchingClient));

    }

    private static Stream<Consumer<SearchIndexingBufferedSender<Map<String, Object>>>> operationsThrowAfterClientIsClosedSupplier() {
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
        SearchClient client = getSearchClientBuilder("index").buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = client.getSearchIndexingBufferedSender(
            new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                .setAutoFlush(false)
                .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        batchingClient.close();

        assertDoesNotThrow((Executable) batchingClient::close);
    }

    @Test
    public void concurrentFlushesOnlyAllowsOneProcessor() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger();

        SearchAsyncClient client = getSearchClientBuilder("index")
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
            }).buildAsyncClient();

        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = client
            .getSearchIndexingBufferedAsyncSender(
                new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                    .setAutoFlush(false)
                    .setBatchSize(5)
                    .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        CountDownLatch countDownLatch = new CountDownLatch(2);
        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON)).block();

        AtomicLong firstFlushCompletionTime = new AtomicLong();
        batchingClient.flush()
            .doFinally(ignored -> {
                firstFlushCompletionTime.set(System.nanoTime());
                countDownLatch.countDown();
            })
            .subscribe();

        AtomicLong secondFlushCompletionTime = new AtomicLong();
        batchingClient.flush()
            .doFinally(ignored -> {
                secondFlushCompletionTime.set(System.nanoTime());
                countDownLatch.countDown();
            })
            .subscribe();

        countDownLatch.await();
        assertTrue(firstFlushCompletionTime.get() > secondFlushCompletionTime.get());
    }

    @Test
    public void closeWillWaitForAnyCurrentFlushesToCompleteBeforeRunning() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger();

        SearchAsyncClient client = getSearchClientBuilder("index")
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
            }).buildAsyncClient();

        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = client
            .getSearchIndexingBufferedAsyncSender(
                new SearchIndexingBufferedSenderOptions<Map<String, Object>>()
                    .setAutoFlush(false)
                    .setBatchSize(5)
                    .setDocumentKeyRetriever(document -> String.valueOf(document.get("HotelId"))));

        CountDownLatch countDownLatch = new CountDownLatch(2);
        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON)).block();

        AtomicLong firstFlushCompletionTime = new AtomicLong();
        batchingClient.flush()
            .doFinally(ignored -> {
                firstFlushCompletionTime.set(System.nanoTime());
                countDownLatch.countDown();
            })
            .subscribe();

        AtomicLong secondFlushCompletionTime = new AtomicLong();
        batchingClient.close()
            .doFinally(ignored -> {
                secondFlushCompletionTime.set(System.nanoTime());
                countDownLatch.countDown();
            })
            .subscribe();

        countDownLatch.await();
        assertTrue(firstFlushCompletionTime.get() <= secondFlushCompletionTime.get());
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

        try {
            return JACKSON_ADAPTER.serialize(new IndexDocumentsResult(results), SerializerEncoding.JSON)
                .getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /*
     * Helper class to ignore write only properties that need to be spoofed.
     */
    private static class IgnoreJacksonWriteOnlyAccess extends JacksonAnnotationIntrospector {
        @Override
        public JsonProperty.Access findPropertyAccess(Annotated m) {
            JsonProperty.Access access = super.findPropertyAccess(m);
            if (access == JsonProperty.Access.WRITE_ONLY) {
                return JsonProperty.Access.AUTO;
            }
            return access;
        }
    }

    private static Mono<HttpResponse> createMockBatchSplittingResponse(HttpRequest request, int keyIdOffset,
        int expectedBatchSize) {
        return FluxUtil.collectBytesInByteBufferStream(request.getBody())
            .flatMap(bodyBytes -> {
                try {
                    // Request documents are in a sub-node called value.
                    JsonNode jsonNode = JACKSON_ADAPTER.serializer().readTree(bodyBytes).get("value");

                    // Given the initial size was 10 and it was split we should expect 5 elements.
                    assertTrue(jsonNode.isArray());
                    assertEquals(expectedBatchSize, jsonNode.size());

                    int[] statusCodes = new int[expectedBatchSize];
                    Arrays.fill(statusCodes, 200);

                    return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                        createMockResponseData(keyIdOffset, statusCodes)));
                } catch (IOException e) {
                    return Mono.error(e);
                }
            });
    }
}
