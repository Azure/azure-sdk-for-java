// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
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
    private static final TypeReference<Map<String, Object>> HOTEL_DOCUMENT_TYPE;
    private static final Function<Map<String, Object>, String> HOTEL_ID_KEY_RETRIEVER;
    private String indexToDelete;
    private SearchClientBuilder clientBuilder;

    static {
        JacksonAdapter adapter = new JacksonAdapter();
        adapter.serializer().setAnnotationIntrospector(new IgnoreJacksonWriteOnlyAccess());

        JACKSON_ADAPTER = adapter;
        HOTEL_DOCUMENT_TYPE = new TypeReference<Map<String, Object>>() { };
        HOTEL_ID_KEY_RETRIEVER = document -> String.valueOf(document.get("HotelId"));
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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .buildSender();

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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofMinutes(5))
            .initialBatchActionCount(10)
            .buildSender();

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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .initialBatchActionCount(10)
            .autoFlushInterval(Duration.ofSeconds(3))
            .buildSender();

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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .buildSender();

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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofMinutes(5))
            .initialBatchActionCount(1000)
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        sleepIfRunningAgainstService(3000);

        assertEquals(0, client.getDocumentCount());
        batchingClient.close();
    }

    @Test
    public void indexManyDocumentsSmallDocumentSets() {
        setupIndex();

        AtomicInteger requestCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();

        SearchClientBuilder builder = clientBuilder.addPolicy((context, next) -> {
            requestCount.incrementAndGet();
            return next.process();
        });

        SearchClient client = builder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofSeconds(5))
            .initialBatchActionCount(10)
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> failedCount.incrementAndGet())
            .buildSender();

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

        batchingClient.close();

        sleepIfRunningAgainstService((long) (15000 * 1.5));

        assertEquals(1000, successCount.get());
        assertEquals(0, failedCount.get());
        assertTrue(requestCount.get() >= 100);
        assertEquals(1000, client.getDocumentCount());
    }

    @Test
    public void indexManyDocumentsOneLargeDocumentSet() {
        setupIndex();

        AtomicInteger requestCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();

        SearchClientBuilder builder = clientBuilder.addPolicy((context, next) -> {
            requestCount.incrementAndGet();
            return next.process();
        });

        SearchClient client = builder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = builder.bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofSeconds(5))
            .initialBatchActionCount(10)
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> failedCount.incrementAndGet())
            .buildSender();

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

        batchingClient.close();

        sleepIfRunningAgainstService((long) (15000 * 1.5));

        assertEquals(1000, successCount.get());
        assertEquals(0, failedCount.get());
        assertTrue(requestCount.get() >= 100);
        assertEquals(1000, client.getDocumentCount());
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
            }).bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .buildSender();

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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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

//    @Test
//    public void resizeFactorDeterminesSubsequentRequestCount() {
//        AtomicInteger callCount = new AtomicInteger();
//        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
//            .httpClient(request -> {
//                int count = callCount.getAndIncrement();
//                if (count == 0) {
//                    return Mono.just(new MockHttpResponse(request, 413));
//                }
//            })
//            .buildBufferedSender(new SearchIndexingBufferedSenderOptions<>(HOTEL_ID_KEY_RETRIEVER)
//                .);
//
//        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));
//
//        assertDoesNotThrow((Executable) batchingClient::flush);
//
//    }

    /**
     * Tests that flushing a batch doesn't include duplicate keys.
     */
    @Test
    public void batchTakesAllNonDuplicateKeys() {
        AtomicInteger callCount = new AtomicInteger();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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
    @Test
    public void batchRetriesUntilLimit() {
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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
        // Document gets sent 10 times for the number of retries that happen.
        assertEquals(11, sentCount.get());
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

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .buildSender();

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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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

        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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

        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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

    @Test
    public void serverBusyResponseRetries() {
        AtomicInteger callCount = new AtomicInteger();
        AtomicInteger addedCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        AtomicInteger sentCount = new AtomicInteger();

        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = getSearchClientBuilder("index")
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
